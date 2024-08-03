package ru.astondevs.servletrestservice.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dao.StudentRepository;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.student.StudentWithCoordinatorAndCourses;
import ru.astondevs.servletrestservice.exception.DaoException;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.model.student.Student;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.util.TransactionCloseable;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class StudentRepositoryJdbcImpl implements StudentRepository {

    private final DataSource dataSource;

    @Override
    public Student insert(Student student) throws DaoException {
        try (var connection = dataSource.getConnection();
             var ignored = new TransactionCloseable(connection)) {

            String sql = "insert into student (name, coordinator_id) values (?, ?) returning student_id";
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, student.getName());
                ps.setLong(2, student.getCoordinatorId());
                ps.execute();
                ResultSet resultSet = ps.getResultSet();
                resultSet.next();
                student.setId(resultSet.getLong(1));
            }

            String sql1 = "insert into student_courses (student_id, course_id) values (?, ?)";
            try (var ps = connection.prepareStatement(sql1)) {
                for (Long courseId : student.getCourseIds()) {
                    ps.setLong(1, student.getId());
                    ps.setLong(2, courseId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return student;

        } catch (SQLException e) {
            if (Objects.equals(e.getSQLState(), "23503"))
                throw new DataConsistencyException("invalid course ids passed", e);
            else {
                log.error("unexpected insertion exception", e);
                throw new DaoException("unexpected insertion exception", e);
            }
        }
    }

    @Override
    public Optional<StudentWithCoordinatorAndCourses> findStudentWithCoordinatorAndCoursesById(long studentId) throws DaoException {
        var builder = StudentWithCoordinatorAndCourses.builder();
        try (var connection = dataSource.getConnection()) {

            connection.setReadOnly(true);

            String sql = """
                    select s.student_id, s.name, c.coordinator_id, c.name
                    from student s left join coordinator c using (coordinator_id)
                    where student_id = ?;
                    """;
            try (var ps = connection.prepareStatement(sql)) {
                ps.setLong(1, studentId);

                ResultSet resultSet = ps.executeQuery();
                if (!resultSet.next()) return Optional.empty();

                builder.id(resultSet.getLong(1))
                        .name(resultSet.getString(2));

                resultSet.getLong(3);
                if (!resultSet.wasNull())
                    builder.coordinator(Coordinator.builder()
                                    .id(resultSet.getLong(3))
                                    .name(resultSet.getString(4))
                                    .build());
            }

            Set<Course> courses = new HashSet<>();
            String sql1 = """
                    select course_id, name
                    from course inner join student_courses using (course_id)
                    where student_id = ?
                    """;
            try (var ps = connection.prepareStatement(sql1)) {
                ps.setLong(1, studentId);
                ResultSet resultSet = ps.executeQuery();

                while (resultSet.next()) {
                    courses.add(Course.builder()
                            .id(resultSet.getLong(1))
                            .name(resultSet.getString(2))
                            .build());
                }
            }
            builder.courses(courses);

            connection.setReadOnly(false);
            return Optional.of(builder.build());

        } catch (SQLException e) {
            log.error("unexpected select exception", e);
            throw new DaoException("unexpected select exception", e);
        }
    }

    @Override
    public Student update(Student student) throws DaoException {
        Set<Long> updateCourseIds = student.getCourseIds();
        try (var connection = dataSource.getConnection();
             var ignored = new TransactionCloseable(connection)) {

            String sql = "update student set name = ?, coordinator_id = ? where student_id = ?";
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, student.getName());
                ps.setLong(2, student.getCoordinatorId());
                ps.setLong(3, student.getId());
                ps.executeUpdate();
            }

            Set<Long> existingCourseIds = new HashSet<>();
            String sql1 = "select course_id from student_courses where student_id = ?";
            try (var ps = connection.prepareStatement(sql1)) {
                ps.setLong(1, student.getId());
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    long courseId = resultSet.getLong(1);
                    existingCourseIds.add(courseId);
                }
            }

            List<Long> removalCourseIds = existingCourseIds.stream()
                    .filter(id -> !updateCourseIds.contains(id))
                    .toList();
            String sql2 = "delete from student_courses where course_id = ANY (?)";
            try (var ps = connection.prepareStatement(sql2)) {
                Array array = connection.createArrayOf("BIGINT", removalCourseIds.toArray());
                ps.setArray(1, array);
                ps.executeUpdate();
            }

            List<Long> newCourseIds = updateCourseIds.stream()
                    .filter(id -> !existingCourseIds.contains(id))
                    .toList();
            String sql3 = "insert into student_courses (student_id, course_id) values (?, ?)";
            try (var ps = connection.prepareStatement(sql3)) {
                for (long courseId : newCourseIds) {
                    ps.setLong(1, student.getId());
                    ps.setLong(2, courseId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return student;

        } catch (SQLException e) {
            if (Objects.equals(e.getSQLState(), "23503"))
                throw new DataConsistencyException("invalid course ids passed", e);
            log.error("unexpected update exception", e);
            throw new DaoException("unexpected update exception", e);
        }
    }

    @Override
    public void deleteById(long studentId) throws DaoException {
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("delete from student where student_id = ?")) {

            ps.setLong(1, studentId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("unexpected delete exception", e);
            throw new DaoException("unexpected delete exception", e);
        }
    }
}
