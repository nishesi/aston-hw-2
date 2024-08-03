package ru.astondevs.servletrestservice.dao.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.astondevs.servletrestservice.exception.DaoException;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.course.CourseWithStudents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
public class CourseRepositoryJdbcImplIntegrationTest {

    static HikariDataSource testDataSource;

    static HikariDataSource contextDataSource;

    static HikariDataSource spyDataSource;

    static CourseRepositoryJdbcImpl courseRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2-alpine")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("./init.sql")
//            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
//                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(5433), new ExposedPort(5432)))
//            ))
            ;

    @BeforeAll
    static void setUp() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        testDataSource = new HikariDataSource(hikariConfig);

        contextDataSource = new HikariDataSource(hikariConfig);
        spyDataSource = Mockito.spy(contextDataSource);
        courseRepository = new CourseRepositoryJdbcImpl(spyDataSource);
    }

    @AfterAll
    static void tearDown() {
        testDataSource.close();
        contextDataSource.close();
    }

    @Nested
    class insertCourse_method_test {
        @BeforeAll
        static void setUp() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("insert into student (name) values (?)")) {
                List<String> names = List.of("Student 1", "Student 2");

                for (String name : names) {
                    ps.setString(1, name);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        @AfterAll
        static void tearDown() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course, student restart identity");
            }
        }

        @AfterEach
        void clear_tables() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course");
            }
        }


        @Test
        void should_insert_course_table_data() throws SQLException {
            Course course = Course.builder()
                    .name("course 1")
                    .studentIds(Set.of())
                    .build();

            Course result = courseRepository.insert(course);

            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("select * from course where course_id = ?")) {
                ps.setLong(1, result.getId());
                ResultSet resultSet = ps.executeQuery();

                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is(course.getName()));
            }
        }

        @Test
        void should_insert_course_students() throws SQLException {
            Course course = Course.builder()
                    .name("course 1")
                    .studentIds(Set.of(1L))
                    .build();

            Course result = courseRepository.insert(course);

            try (var connection = testDataSource.getConnection();
                 var ps = connection.prepareStatement("select * from student_courses where course_id = ?")) {
                ps.setLong(1, result.getId());
                ResultSet resultSet = ps.executeQuery();

                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getLong("student_id"), is(1L));
                assertThat(resultSet.next(), is(false));
            }
        }

        @Test
        void should_throw_DataConsistencyException_if_invalid_student_id_passed_and_rollback_transaction() throws SQLException {
            Course course = Course.builder()
                    .name("course 1")
                    .studentIds(Set.of(1L, 100L))
                    .build();

            Assertions.assertThrows(DataConsistencyException.class, () -> courseRepository.insert(course));

            try (var connection = testDataSource.getConnection();
                 var statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select exists(select * from student_courses)");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getBoolean(1), is(false));

                resultSet = statement.executeQuery("select exists(select * from course)");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getBoolean(1), is(false));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Connection spyConnection = Mockito.spy(contextDataSource.getConnection());
            Mockito.when(spyDataSource.getConnection()).thenReturn(spyConnection);
            Mockito.doThrow(new SQLException("test exception")).when(spyConnection).commit();

            Course course = Course.builder()
                    .name("some course")
                    .studentIds(Set.of())
                    .build();

            Assertions.assertThrows(DaoException.class, () -> courseRepository.insert(course));

            Mockito.reset(spyDataSource);
        }
    }

    @Nested
    class findCourseWithStudentsById_method_test {
        @BeforeAll
        static void setUp() throws SQLException {
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

        @AfterAll
        static void clear_tables() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course, student restart identity");
            }
        }

        @Test
        void should_return_empty_optional_if_course_not_found() {
            Optional<CourseWithStudents> result = courseRepository.findCourseWithStudentsById(100L);

            assertThat(result.isEmpty(), is(true));
        }

        @Test
        void should_return_course_with_students() throws SQLException {
            try (var connection = testDataSource.getConnection()) {
                Statement st = connection.createStatement();
                st.executeUpdate("insert into course (name) values ('Course name')");


                String sql = "insert into student_courses (student_id, course_id) values (?, ?)";
                try (var ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, 1L);
                    ps.setLong(2, 1L);
                    ps.addBatch();
                    ps.setLong(1, 2L);
                    ps.setLong(2, 1L);
                    ps.addBatch();

                    ps.executeBatch();
                }

                Optional<CourseWithStudents> result = courseRepository.findCourseWithStudentsById(1L);
                assertThat(result.isPresent(), is(true));

                assertThat(result.get(), allOf(
                        hasProperty("id", is(1L)),
                        hasProperty("name", is("Course name")),
                        hasProperty("students", allOf(
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

            Assertions.assertThrows(DaoException.class, () -> courseRepository.findCourseWithStudentsById(1));

            Mockito.reset(spyDataSource);
        }
    }

    @Nested
    class updateCourse_method_test {

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

                try (var s = connection.createStatement()) {
                    s.executeUpdate("insert into course (name) values ('Course name')");
                    s.executeUpdate("insert into student_courses (student_id, course_id) VALUES (1, 1)");
                    s.executeUpdate("insert into student_courses (student_id, course_id) VALUES (2, 1)");
                }
            }
        }

        @AfterEach
        void clear_tables() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course, student restart identity");
            }
        }

        @Test
        void should_updateCourse() throws SQLException {
            try (var connection = testDataSource.getConnection()) {

                Course course = Course.builder()
                        .id(1L)
                        .name("Course 1")
                        .studentIds(Set.of(1L, 3L))
                        .build();

                courseRepository.update(course);

                try (var s = connection.createStatement()) {
                    ResultSet resultSet = s.executeQuery("select * from student_courses where course_id = 1");
                    Set<Long> studentIds = new HashSet<>();
                    while (resultSet.next()) {
                        studentIds.add(resultSet.getLong("student_id"));
                    }

                    assertThat(studentIds, hasSize(2));
                    assertThat(studentIds, containsInAnyOrder(is(1L), is(3L)));

                    resultSet = s.executeQuery("select * from course where course_id = 1");
                    assertThat(resultSet.next(), is(true));
                    assertThat(resultSet.getString("name"), is("Course 1"));

                    //return data

                    s.executeUpdate("insert into student_courses (student_id, course_id) VALUES (2, 1)");
                    s.executeUpdate("delete from student_courses where student_id = 3 and course_id = 1");
                }
            }
        }


        @Test
        void should_throw_DataConsistencyException_if_invalid_student_id_passed_and_rollback_transaction() throws SQLException {
            Course course = Course.builder()
                    .id(1L)
                    .name("course 1")
                    .studentIds(Set.of(1L, 100L))
                    .build();

            Assertions.assertThrows(DataConsistencyException.class, () -> courseRepository.update(course));

            try (var connection = testDataSource.getConnection();
                 var statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select student_id from student_courses where course_id = 1");
                List<Long> studentIds = new ArrayList<>();
                while (resultSet.next()) {
                    studentIds.add(resultSet.getLong("student_id"));
                }
                assertThat(studentIds, hasSize(2));
                assertThat(studentIds, containsInAnyOrder(is(1L), is(2L)));

                resultSet = statement.executeQuery("select name from course where course_id = 1");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is("Course name"));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Connection spyConnection = Mockito.spy(contextDataSource.getConnection());
            Mockito.when(spyDataSource.getConnection()).thenReturn(spyConnection);
            Mockito.doThrow(new SQLException("test exception")).when(spyConnection).commit();

            Course course = Course.builder()
                    .id(1L)
                    .name("some course")
                    .studentIds(Set.of())
                    .build();

            Assertions.assertThrows(DaoException.class, () -> courseRepository.update(course));

            Mockito.reset(spyDataSource);
        }
    }

    @Nested
    class deleteCourse_method_test {
        @AfterAll
        static void clear_tables() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var st = connection.createStatement()) {
                st.execute("truncate table student_courses, course, student restart identity");
            }
        }

        @Test
        void should_deleteCourse() throws SQLException {
            try (var connection = testDataSource.getConnection();
                 var s = connection.createStatement()) {
                s.executeUpdate("insert into course (name) values ('Course name')");
                ResultSet resultSet = s.executeQuery("select * from course where course_id = 1");
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getString("name"), is("Course name"));

                courseRepository.deleteById(1L);

                resultSet = s.executeQuery("select * from course where course_id = 1");
                assertThat(resultSet.next(), is(false));
            }
        }

        @Test
        void should_throw_DaoException_on_unexpected_behavior() throws SQLException {
            Mockito.when(spyDataSource.getConnection()).thenThrow(new SQLException("test exception"));

            Assertions.assertThrows(DaoException.class, () -> courseRepository.deleteById(1));

            Mockito.reset(spyDataSource);
        }
    }
}
