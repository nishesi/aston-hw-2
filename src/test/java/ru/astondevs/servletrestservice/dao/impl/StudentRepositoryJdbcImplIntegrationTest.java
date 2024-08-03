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
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.model.student.Student;
import ru.astondevs.servletrestservice.model.student.StudentWithCoordinatorAndCourses;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.astondevs.servletrestservice.util.HasRecordComponentWithValue.hasRecordProperty;

@Testcontainers
public class StudentRepositoryJdbcImplIntegrationTest {

    static HikariDataSource testDataSource;

    static HikariDataSource contextDataSource;

    static HikariDataSource spyDataSource;

    static StudentRepositoryJdbcImpl studentRepository;

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
        studentRepository = new StudentRepositoryJdbcImpl(spyDataSource);
    }

    @AfterAll
    static void tearDown() {
        testDataSource.close();
        contextDataSource.close();
    }

    @Nested
    class insertStudent_method_test {
        @BeforeAll
        static void setUp() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("insert into course (name) values (?)")) {
                List<String> names = List.of("Course 1", "Course 2");

                for (String name : names) {
                    ps.setString(1, name);
                    ps.addBatch();
                }
                ps.executeBatch();

                try (var s = connection.createStatement()) {
                    s.executeUpdate("insert into coordinator (name) VALUES ('Test coordinator')");
                }
            }
        }

        @AfterAll
        static void tearDown() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course, student, coordinator restart identity");
            }
        }

        @AfterEach
        void clear_tables() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, student");
            }
        }


        @Test
        void should_insert_student_table_data() throws SQLException {
            Student student = Student.builder()
                    .name("New Student")
                    .coordinatorId(1L)
                    .courseIds(Set.of(1L, 2L))
                    .build();

            Student result = studentRepository.insert(student);

            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("select * from student where student_id = ?")) {
                ps.setLong(1, result.getId());
                ResultSet resultSet = ps.executeQuery();

                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is(result.getName()));
                assertThat(resultSet.getLong("coordinator_id"), is(1L));
            }
        }

        @Test
        void should_insert_student_courses() throws SQLException {
            Student student = Student.builder()
                    .name("New Student")
                    .coordinatorId(1L)
                    .courseIds(Set.of(1L))
                    .build();

            Student result = studentRepository.insert(student);

            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("select * from student_courses where student_id = ?")) {
                ps.setLong(1, result.getId());
                ResultSet resultSet = ps.executeQuery();

                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getLong("course_id"), is(1L));
                assertThat(resultSet.next(), is(false));
            }
        }

        @Test
        void should_throw_DataConsistencyException_if_invalid_course_id_passed_and_rollback_transaction() throws SQLException {
            Student student = Student.builder()
                    .name("New Student")
                    .coordinatorId(1L)
                    .courseIds(Set.of(1L, 100L))
                    .build();

            Assertions.assertThrows(DataConsistencyException.class, () -> studentRepository.insert(student));

            try (var connection = testDataSource.getConnection();
                 var statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select * from student_courses");
                assertThat(resultSet.next(), is(false));

                resultSet = statement.executeQuery("select * from student");
                assertThat(resultSet.next(), is(false));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Connection spyConnection = Mockito.spy(contextDataSource.getConnection());
            Mockito.when(spyDataSource.getConnection()).thenReturn(spyConnection);
            Mockito.doThrow(new SQLException("test exception")).when(spyConnection).commit();

            Student student = Student.builder()
                    .name("New Student")
                    .coordinatorId(1L)
                    .courseIds(Set.of())
                    .build();

            Assertions.assertThrows(DaoException.class, () -> studentRepository.insert(student));

            Mockito.reset(spyDataSource);
        }

        @Test
        void should_throw_DataConsistencyException_if_coordinator_not_exist() {
            Student student = Student.builder()
                    .name("New Student")
                    .coordinatorId(100L)
                    .courseIds(Set.of())
                    .build();

            Assertions.assertThrows(DataConsistencyException.class, () -> studentRepository.insert(student));
        }
    }

    @Nested
    class findStudentWithCoordinatorAndCoursesById_method_test {
        @BeforeAll
        static void setUp() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("insert into course (name) values (?)")) {
                List<String> names = List.of("Course 1", "Course 2", "Course 3");

                for (String name : names) {
                    ps.setString(1, name);
                    ps.addBatch();
                }
                ps.executeBatch();

                try (var s = connection.createStatement()) {
                    s.executeUpdate("insert into coordinator (name) values ('Coordinator 1'), ('Coordinator 2')");
                }
            }
        }

        @AfterAll
        static void clear_tables() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course, student, coordinator restart identity");
            }
        }

        @Test
        void should_return_empty_optional_if_student_not_found() {
            Optional<StudentWithCoordinatorAndCourses> result = studentRepository.findStudentWithCoordinatorAndCoursesById(100L);

            assertThat(result.isEmpty(), is(true));
        }

        @Test
        void should_return_student_with_coordinator_and_courses() throws SQLException {
            try (var connection = testDataSource.getConnection()) {
                Statement st = connection.createStatement();
                st.executeUpdate("insert into student (name, coordinator_id) values ('Student name', 1)");


                String sql = "insert into student_courses (student_id, course_id) values (?, ?)";
                try (var ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, 1L);
                    ps.setLong(2, 1L);
                    ps.addBatch();
                    ps.setLong(1, 1L);
                    ps.setLong(2, 2L);
                    ps.addBatch();

                    ps.executeBatch();
                }

                Optional<StudentWithCoordinatorAndCourses> result = studentRepository.findStudentWithCoordinatorAndCoursesById(1L);
                assertThat(result.isPresent(), is(true));

                assertThat(result.get(), allOf(
                        hasRecordProperty("id", is(1L)),
                        hasRecordProperty("name", is("Student name")),
                        hasRecordProperty("courses", allOf(
                                hasSize(2),
                                containsInAnyOrder(
                                        hasProperty("id", is(1L)),
                                        hasProperty("id", is(2L)))
                        ))
                ));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Mockito.when(spyDataSource.getConnection()).thenThrow(new SQLException("test exception"));

            Assertions.assertThrows(DaoException.class, () -> studentRepository.findStudentWithCoordinatorAndCoursesById(1L));

            Mockito.reset(spyDataSource);
        }
    }

    @Nested
    class updateStudent_method_test {

        @BeforeEach
        void setUp() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("insert into course (name) values (?)")) {
                List<String> names = List.of("Course 1", "Course 2", "Course 3");

                for (String name : names) {
                    ps.setString(1, name);
                    ps.addBatch();
                }
                ps.executeBatch();

                try (var s = connection.createStatement()) {
                    s.executeUpdate("insert into coordinator (name) values ('Coordinator 1'), ('Coordinator 2')");
                    s.executeUpdate("insert into student (name, coordinator_id) values ('Student name', 1)");
                    s.executeUpdate("insert into student_courses (student_id, course_id) VALUES (1, 1)");
                    s.executeUpdate("insert into student_courses (student_id, course_id) VALUES (1, 2)");
                }
            }
        }

        @AfterEach
        void clear_tables() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course, student, coordinator restart identity");
            }
        }

        @Test
        void should_updateStudent() throws SQLException {
            try (var connection = testDataSource.getConnection()) {

                Student student = Student.builder()
                        .id(1L)
                        .name("Updated Student")
                        .coordinatorId(2L)
                        .courseIds(Set.of(1L, 3L))
                        .build();

                studentRepository.update(student);

                try (var s = connection.createStatement()) {
                    ResultSet resultSet = s.executeQuery("select * from student_courses where student_id = 1");
                    Set<Long> courseIds = new HashSet<>();
                    while (resultSet.next()) {
                        courseIds.add(resultSet.getLong("course_id"));
                    }

                    assertThat(courseIds, hasSize(2));
                    assertThat(courseIds, containsInAnyOrder(is(1L), is(3L)));

                    resultSet = s.executeQuery("select * from student where student_id = 1");
                    assertThat(resultSet.next(), is(true));
                    assertThat(resultSet.getString("name"), is("Updated Student"));
                    assertThat(resultSet.getLong("coordinator_id"), is(2L));

                    //return data

                    s.executeUpdate("insert into student_courses (student_id, course_id) VALUES (1, 2)");
                    s.executeUpdate("delete from student_courses where course_id = 3 and student_id = 1");
                }
            }
        }


        @Test
        void should_throw_DataConsistencyException_if_invalid_course_id_passed_and_rollback_transaction() throws SQLException {
            Student student = Student.builder()
                    .id(1L)
                    .name("Updated Student")
                    .coordinatorId(2L)
                    .courseIds(Set.of(1L, 100L))
                    .build();

            Assertions.assertThrows(DataConsistencyException.class, () -> studentRepository.update(student));

            try (var connection = testDataSource.getConnection();
                 var statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select course_id from student_courses where student_id = 1");
                List<Long> courseIds = new ArrayList<>();
                while (resultSet.next()) {
                    courseIds.add(resultSet.getLong("course_id"));
                }
                assertThat(courseIds, hasSize(2));
                assertThat(courseIds, containsInAnyOrder(is(1L), is(2L)));

                resultSet = statement.executeQuery("select * from student where student_id = 1");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is("Student name"));
                assertThat(resultSet.getLong("coordinator_id"), is(1L));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Connection spyConnection = Mockito.spy(contextDataSource.getConnection());
            Mockito.when(spyDataSource.getConnection()).thenReturn(spyConnection);
            Mockito.doThrow(new SQLException("test exception")).when(spyConnection).commit();

            Student student = Student.builder()
                    .id(1L)
                    .name("Updated Student")
                    .coordinatorId(2L)
                    .courseIds(Set.of(1L, 3L))
                    .build();

            Assertions.assertThrows(DaoException.class, () -> studentRepository.update(student));

            Mockito.reset(spyDataSource);
        }
    }

    @Nested
    class deleteStudent_method_test {
        @BeforeAll
        static void beforeAll() throws SQLException {
            try (var connection = testDataSource.getConnection();
            var statement = connection.createStatement()) {
                statement.executeUpdate("insert into coordinator (name) values ('coordinator 1')");
            }
        }

        @AfterAll
        static void clear_tables() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course, student, coordinator restart identity");
            }
        }

        @Test
        void should_deleteStudent() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {
                s.executeUpdate("insert into student (name, coordinator_id) values ('Student name', 1)");
                ResultSet resultSet = s.executeQuery("select * from student where student_id = 1");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is("Student name"));

                studentRepository.deleteById(1L);

                resultSet = s.executeQuery("select * from student where student_id = 1");
                assertThat(resultSet.next(), is(false));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Mockito.when(spyDataSource.getConnection()).thenThrow(new SQLException("test exception"));

            Assertions.assertThrows(DaoException.class, () -> studentRepository.deleteById(1));

            Mockito.reset(spyDataSource);
        }
    }
}
