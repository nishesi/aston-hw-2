package ru.astondevs.servletrestservice.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dao.CourseRepository;
import ru.astondevs.servletrestservice.exception.DaoException;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.model.Student;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.course.CourseWithStudents;
import ru.astondevs.servletrestservice.util.TransactionCloseable;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Slf4j
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {
    private final DataSource dataSource;

    @Override
    public Course insertCourse(Course course) {
        try (var connection = dataSource.getConnection();
             var ignored = new TransactionCloseable(connection)) {

            connection.setAutoCommit(false);

            String insertCourseSql = "insert into course (name) values (?) returning course_id";
            try (var ps = connection.prepareStatement(insertCourseSql)) {
                ps.setString(1, course.getName());
                ps.execute();
                ResultSet resultSet = ps.getResultSet();
                resultSet.next();
                long courseId = resultSet.getLong("course_id");
                course.setId(courseId);
            }

            String insertCourseStudentsSql = "insert into student_courses (student_id, course_id) values (?, ?)";
            try (var ps = connection.prepareStatement(insertCourseStudentsSql)) {
                for (long studentId : course.getStudentIds()) {
                    ps.setLong(1, studentId);
                    ps.setLong(2, course.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return course;

        } catch (SQLException e) {
            if (Objects.equals(e.getSQLState(), "23503"))
                throw new DataConsistencyException("invalid student ids passed", e);
            else {
                log.error("unexpected insertion exception", e);
                throw new DaoException("unexpected insertion exception", e);
            }
        }
    }

    @Override
    public Optional<CourseWithStudents> findCourseWithStudentsById(long courseId) {
        var builder = CourseWithStudents.builder();
        try (var connection = dataSource.getConnection()) {
            connection.setReadOnly(true);

            // select course data
            try (var ps = connection.prepareStatement("select course_id, name from course where course_id = ?")) {
                ps.setLong(1, courseId);
                ResultSet resultSet = ps.executeQuery();
                if (!resultSet.next())
                    return Optional.empty();

                builder.id(resultSet.getLong("course_id"))
                        .name(resultSet.getString("name"));
            }

            // select students data
            String sql = """
                    select s.student_id, s.name, s.coordinator_id
                    from student s
                        inner join (select student_id, course_id
                                    from student_courses
                                    where course_id = ?) as sc
                        using (student_id)
                    """;
            try (var ps = connection.prepareStatement(sql)) {
                ps.setLong(1, courseId);
                ResultSet resultSet = ps.executeQuery();
                Set<Student> students = new HashSet<>();
                while (resultSet.next()) {
                    Student student = Student.builder()
                            .id(resultSet.getLong("student_id"))
                            .name(resultSet.getString("name"))
                            .coordinatorId(resultSet.getLong("coordinator_id"))
                            .build();
                    students.add(student);
                }
                builder.students(students);
            }
            connection.setReadOnly(false);
            return Optional.of(builder.build());

        } catch (SQLException e) {
            log.error("unexpected select exception", e);
            throw new DaoException("unexpected select exception", e);
        }
    }

    @Override
    public Course updateCourse(Course course) {
        Set<Long> updateStudentIds = course.getStudentIds();
        try (var connection = dataSource.getConnection();
             var ignored = new TransactionCloseable(connection)) {

            connection.setAutoCommit(false);

            Set<Long> existingStudentIds = new HashSet<>();
            String sql = "select student_id from student_courses where course_id = ?";
            try (var ps = connection.prepareStatement(sql)) {
                ps.setLong(1, course.getId());
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    long studentId = resultSet.getLong("student_id");
                    existingStudentIds.add(studentId);
                }
            }

            List<Long> removalStudentIds = existingStudentIds.stream()
                    .filter(id -> !updateStudentIds.contains(id))
                    .toList();
            try (var ps = connection.prepareStatement("delete from student_courses where student_id = ANY (?)")) {
                Array array = connection.createArrayOf("BIGINT", removalStudentIds.toArray());
                ps.setArray(1, array);
                ps.executeUpdate();
            }

            List<Long> newStudentIds = updateStudentIds.stream()
                    .filter(id -> !existingStudentIds.contains(id))
                    .toList();
            try (var ps = connection.prepareStatement("insert into student_courses (student_id, course_id) values (?, ?)")) {
                for (long studentId : newStudentIds) {
                    ps.setLong(1, studentId);
                    ps.setLong(2, course.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
            return course;

        } catch (SQLException e) {
            if (Objects.equals(e.getSQLState(), "23503"))
                throw new DataConsistencyException("invalid student ids passed", e);
            log.error("unexpected update exception", e);
            throw new DaoException("unexpected update exception", e);
        }
    }

    @Override
    public void deleteCourseById(long courseId) {
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("delete from course where course_id = ?")) {

            ps.setLong(1, courseId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("unexpected delete exception", e);
            throw new DaoException("unexpected delete exception", e);
        }
    }
}
