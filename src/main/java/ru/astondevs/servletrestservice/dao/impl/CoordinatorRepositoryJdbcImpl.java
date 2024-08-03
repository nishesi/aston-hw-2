package ru.astondevs.servletrestservice.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dao.CoordinatorRepository;
import ru.astondevs.servletrestservice.exception.DaoException;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.coordinator.CoordinatorWithStudents;
import ru.astondevs.servletrestservice.model.student.Student;
import ru.astondevs.servletrestservice.util.TransactionCloseable;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class CoordinatorRepositoryJdbcImpl implements CoordinatorRepository {

    private final DataSource dataSource;

    @Override
    public Coordinator insert(Coordinator coordinator) throws DaoException {
        try (var connection = dataSource.getConnection();
             var ignored = new TransactionCloseable(connection)) {

            String sql = "insert into postgres.public.coordinator (name) values (?) returning coordinator_id";
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, coordinator.getName());
                ps.execute();
                ResultSet resultSet = ps.getResultSet();
                resultSet.next();
                coordinator.setId(resultSet.getLong(1));
            }

            String sql1 = "update student set coordinator_id = ? where student_id = ANY (?)";
            try (var ps = connection.prepareStatement(sql1)) {
                ps.setLong(1, coordinator.getId());
                Array array = connection.createArrayOf("bigint", coordinator.getStudentIds().toArray());
                ps.setArray(2, array);
                ps.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return coordinator;

        } catch (SQLException e) {
            log.error("unexpected insertion exception", e);
            throw new DaoException("unexpected insertion exception", e);
        }
    }

    @Override
    public Optional<CoordinatorWithStudents> findCoordinatorWithStudents(long coordinatorId) throws DaoException {
        var builder = CoordinatorWithStudents.builder();
        try (var connection = dataSource.getConnection()) {

            connection.setReadOnly(true);

            String sql = "select coordinator_id, name from coordinator where coordinator_id = ?";
            try (var ps = connection.prepareStatement(sql)) {
                ps.setLong(1, coordinatorId);

                ResultSet resultSet = ps.executeQuery();
                if (!resultSet.next()) return Optional.empty();

                builder.id(resultSet.getLong(1))
                        .name(resultSet.getString(2));
            }

            Set<Student> students = new HashSet<>();
            String sql1 = "select student_id, name, coordinator_id from student where coordinator_id = ?";
            try (var ps = connection.prepareStatement(sql1)) {
                ps.setLong(1, coordinatorId);
                ResultSet resultSet = ps.executeQuery();

                while (resultSet.next()) {
                    students.add(Student.builder()
                            .id(resultSet.getLong(1))
                            .name(resultSet.getString(2))
                            .coordinatorId(resultSet.getLong(3))
                            .build());
                }
            }
            builder.students(students);

            connection.setReadOnly(false);
            return Optional.of(builder.build());

        } catch (SQLException e) {
            log.error("unexpected select exception", e);
            throw new DaoException("unexpected select exception", e);
        }
    }

    @Override
    public Coordinator update(Coordinator coordinator) throws DataConsistencyException, DaoException {
        Set<Long> updateStudentIds = coordinator.getStudentIds();
        try (var connection = dataSource.getConnection();
             var ignored = new TransactionCloseable(connection)) {

            String sql = "update coordinator set name = ? where coordinator_id = ?";
            try (var ps = connection.prepareStatement(sql)) {
                ps.setString(1, coordinator.getName());
                ps.setLong(2, coordinator.getId());
                ps.executeUpdate();
            }

            Set<Long> relatedStudentIds = new HashSet<>();
            String sql1 = "select student_id from student where coordinator_id = ?";
            try (var ps = connection.prepareStatement(sql1)) {
                ps.setLong(1, coordinator.getId());
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    long studentId = resultSet.getLong(1);
                    relatedStudentIds.add(studentId);
                }
            }

            List<Long> removalStudentIds = relatedStudentIds.stream()
                    .filter(id -> !updateStudentIds.contains(id))
                    .toList();
            String sql2 = "update student set coordinator_id = null where student_id = ANY (?) and coordinator_id = ?";
            try (var ps = connection.prepareStatement(sql2)) {
                Array array = connection.createArrayOf("BIGINT", removalStudentIds.toArray());
                ps.setArray(1, array);
                ps.setLong(2, coordinator.getId());
                ps.executeUpdate();
            }

            List<Long> newCourseIds = updateStudentIds.stream()
                    .filter(id -> !relatedStudentIds.contains(id))
                    .toList();
            String sql3 = "update student set coordinator_id = ? where student_id = ANY (?)";
            try (var ps = connection.prepareStatement(sql3)) {
                ps.setLong(1, coordinator.getId());
                Array array = connection.createArrayOf("BIGINT", newCourseIds.toArray());
                ps.setArray(2, array);
                ps.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return coordinator;

        } catch (SQLException e) {
            if (Objects.equals(e.getSQLState(), "23503"))
                throw new DataConsistencyException("invalid course ids passed", e);
            log.error("unexpected update exception", e);
            throw new DaoException("unexpected update exception", e);
        }
    }

    @Override
    public void deleteById(long coordinatorId) throws DaoException {
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("delete from coordinator where coordinator_id = ?")) {

            ps.setLong(1, coordinatorId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("unexpected delete exception", e);
            throw new DaoException("unexpected delete exception", e);
        }
    }
}
