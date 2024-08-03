package ru.astondevs.servletrestservice.dao.impl;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.astondevs.servletrestservice.exception.DaoException;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.coordinator.CoordinatorWithStudents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.astondevs.servletrestservice.util.HasRecordComponentWithValue.hasRecordProperty;

@Testcontainers
public class CoordinatorRepositoryJdbcImplIntegrationTest {

    static HikariDataSource testDataSource;

    static HikariDataSource contextDataSource;

    static HikariDataSource spyDataSource;

    static CoordinatorRepositoryJdbcImpl coordinatorRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2-alpine")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("./init.sql")
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(5433), new ExposedPort(5432)))
            ));

    @BeforeAll
    static void setUp() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        testDataSource = new HikariDataSource(hikariConfig);

        contextDataSource = new HikariDataSource(hikariConfig);
        spyDataSource = Mockito.spy(contextDataSource);
        coordinatorRepository = new CoordinatorRepositoryJdbcImpl(spyDataSource);
    }

    @AfterAll
    static void tearDown() {
        testDataSource.close();
        contextDataSource.close();
    }

    static class MethodTestBase {
        @BeforeEach
        void setUp() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("insert into student (name) values (?)")) {
                List<String> names = List.of("Student 1", "Student 2", "Student 3");

                for (String name : names) {
                    ps.setString(1, name);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        @AfterEach
        void tearDown() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table course, student, coordinator, student_courses restart identity");
            }
        }
    }

    @Nested
    class insert_method_test extends MethodTestBase {
        @Test
        void should_insert_coordinator_table_data() throws SQLException {
            Coordinator coordinator = Coordinator.builder()
                    .name("Coordinator 1")
                    .studentIds(Set.of())
                    .build();

            Coordinator result = coordinatorRepository.insert(coordinator);

            assertThat(result, hasProperty("id", is(1L)));

            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {
                ResultSet resultSet = s.executeQuery("select * from coordinator where coordinator_id = 1");

                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is(coordinator.getName()));
            }
        }

        @Test
        void should_add_students_to_coordinator() throws SQLException {
            Coordinator coordinator = Coordinator.builder()
                    .name("Coordinator 1")
                    .studentIds(Set.of(1L, 2L))
                    .build();

            coordinatorRepository.insert(coordinator);

            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {
                ResultSet resultSet = s.executeQuery("select * from student where coordinator_id = 1");

                Set<Long> studentIds = new HashSet<>();
                while (resultSet.next()) {
                    studentIds.add(resultSet.getLong("student_id"));
                }

                assertThat(studentIds, hasSize(2));
                assertThat(studentIds, containsInAnyOrder(is(1L), is(2L)));
            }
        }

        @Test
        void should_ignore_unexisting_student_id() throws SQLException {
            Coordinator coordinator = Coordinator.builder()
                    .name("Coordinator 1")
                    .studentIds(Set.of(1L, 2L, 100L))
                    .build();

            Assertions.assertDoesNotThrow(() -> coordinatorRepository.insert(coordinator));

            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {
                ResultSet resultSet = s.executeQuery("select * from student where coordinator_id = 1");

                Set<Long> studentIds = new HashSet<>();
                while (resultSet.next()) {
                    studentIds.add(resultSet.getLong("student_id"));
                }

                assertThat(studentIds, hasSize(2));
                assertThat(studentIds, containsInAnyOrder(is(1L), is(2L)));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Connection spyConnection = Mockito.spy(contextDataSource.getConnection());
            Mockito.when(spyDataSource.getConnection()).thenReturn(spyConnection);
            Mockito.doThrow(new SQLException("test exception")).when(spyConnection).commit();

            Coordinator coordinator = Coordinator.builder()
                    .name("Coordinator 1")
                    .studentIds(Set.of(1L, 2L, 100L))
                    .build();

            Assertions.assertThrows(DaoException.class, () -> coordinatorRepository.insert(coordinator));

            Mockito.reset(spyDataSource);
        }
    }

    @Nested
    class findCoordinatorWithStudentsById_method_test extends MethodTestBase {

        @Test
        void should_return_empty_optional_if_coordinator_not_found() {
            var result = coordinatorRepository.findCoordinatorWithStudents(100L);

            assertThat(result.isEmpty(), is(true));
        }

        @Test
        void should_return_coordinator_with_students() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.executeUpdate("insert into coordinator (name) values ('Coordinator 1')");
                st.executeUpdate("update student set coordinator_id = 1 where student_id in (1, 2)");

                Optional<CoordinatorWithStudents> result = coordinatorRepository.findCoordinatorWithStudents(1L);
                assertThat(result.isPresent(), is(true));

                assertThat(result.get(), allOf(
                        hasRecordProperty("id", is(1L)),
                        hasRecordProperty("name", is("Coordinator 1"))
                ));
                assertThat(result.get().students(), allOf(
                        hasSize(2),
                        containsInAnyOrder(
                                hasProperty("id", is(1L)),
                                hasProperty("id", is(2L)))
                ));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Mockito.when(spyDataSource.getConnection()).thenThrow(new SQLException("test exception"));

            Assertions.assertThrows(DaoException.class, () -> coordinatorRepository.findCoordinatorWithStudents(1L));

            Mockito.reset(spyDataSource);
        }
    }

    @Nested
    class update_method_test extends MethodTestBase {

        @BeforeEach
        void init() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {
                s.executeUpdate("insert into coordinator (name) values ('Coordinator 1')");
                s.executeUpdate("insert into coordinator (name) values ('Coordinator 2')");
                s.executeUpdate("update student set coordinator_id = 1 where student_id in (1, 2)");
                s.executeUpdate("update student set coordinator_id = 2 where student_id = 3");
            }
        }

        @Test
        void should_update_coordinator_and_related_students() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {

                Coordinator coordinator = Coordinator.builder()
                        .id(1L)
                        .name("New Coordinator")
                        .studentIds(Set.of(1L, 3L))
                        .build();

                coordinatorRepository.update(coordinator);

                ResultSet resultSet = s.executeQuery("select * from student where coordinator_id = 1");
                Set<Long> studentIds = new HashSet<>();
                while (resultSet.next()) {
                    studentIds.add(resultSet.getLong("student_id"));
                }

                assertThat(studentIds, hasSize(2));
                assertThat(studentIds, containsInAnyOrder(is(1L), is(3L)));

                resultSet = s.executeQuery("select * from coordinator where coordinator_id = 1");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is("New Coordinator"));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Connection spyConnection = Mockito.spy(contextDataSource.getConnection());
            Mockito.when(spyDataSource.getConnection()).thenReturn(spyConnection);
            Mockito.doThrow(new SQLException("test exception")).when(spyConnection).commit();

            Coordinator coordinator = Coordinator.builder()
                    .id(1L)
                    .name("New Coordinator")
                    .studentIds(Set.of(1L, 3L))
                    .build();

            Assertions.assertThrows(DaoException.class, () -> coordinatorRepository.update(coordinator));

            Mockito.reset(spyDataSource);
        }
    }

    @Nested
    class delete_method_test extends MethodTestBase {

        @Test
        void should_deleteStudent() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {
                s.executeUpdate("insert into coordinator (name) values ('Coordinator name')");
                ResultSet resultSet = s.executeQuery("select * from coordinator where coordinator_id = 1");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is("Coordinator name"));

                coordinatorRepository.deleteById(1L);

                resultSet = s.executeQuery("select * from coordinator where coordinator_id = 1");
                assertThat(resultSet.next(), is(false));
            }
        }

        @Test
        void should_set_related_student_coordinator_ids_to_null() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {
                s.executeUpdate("insert into coordinator (name) values ('Coordinator name')");
                s.executeUpdate("update student set coordinator_id = 1 where student_id in (1, 2)");

                coordinatorRepository.deleteById(1L);

                ResultSet resultSet = s.executeQuery("select * from student where coordinator_id = 1");
                assertThat(resultSet.next(), is(false));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Mockito.when(spyDataSource.getConnection()).thenThrow(new SQLException("test exception"));

            Assertions.assertThrows(DaoException.class, () -> coordinatorRepository.deleteById(1));

            Mockito.reset(spyDataSource);
        }
    }
}
