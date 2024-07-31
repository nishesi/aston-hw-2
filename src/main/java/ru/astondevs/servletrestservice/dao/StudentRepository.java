package ru.astondevs.servletrestservice.dao;

import ru.astondevs.servletrestservice.model.student.StudentWithCoordinatorAndCourses;
import ru.astondevs.servletrestservice.exception.DaoException;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.model.student.Student;

import java.util.Optional;

public interface StudentRepository {
    /**
     * Create new student and add him to existing courseIds.
     * If non-existent ids of courseIds passed throw exception.
     *
     * @param student student to add
     * @return same student with entered id
     * @throws DataConsistencyException if non-existed ids of courseIds passed
     * @throws DaoException             if problems with database connection occurred
     */

    Student insert(Student student) throws DataConsistencyException, DaoException;

    /**
     * Return student information by its id, with set of courseIds where he enrolled.
     * When student with this id not existing returns empty optional.
     *
     * @param studentId student id
     * @return Optional with student
     * @throws DaoException if problems with database connection occurred
     */

    Optional<StudentWithCoordinatorAndCourses> findStudentWithCoordinatorAndCoursesById(long studentId) throws DaoException;

    /**
     * Update student information, remove relations between student and courseIds that exist in database,
     * and not existing in this set, add relations that existing in this set, but not existing in database.
     *
     * @param student student that should be updated
     * @return the same student
     * @throws DataConsistencyException if non-existed ids of courseIds passed
     * @throws DaoException             if problems with database connection occurred
     */

    Student update(Student student) throws DataConsistencyException, DaoException;

    /**
     * Remove student and all related data by his id.
     *
     * @param studentId id of student, that should be deleted.
     * @throws DaoException if problems with database connection occurred
     */

    void deleteById(long studentId) throws DaoException;
}
