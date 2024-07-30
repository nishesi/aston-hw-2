package ru.astondevs.servletrestservice.dao;

import ru.astondevs.servletrestservice.exception.DaoException;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.course.CourseWithStudents;

import java.util.Optional;

public interface CourseRepository {

    /**
     * Create new course and add existing students to this course.
     * If non-existent ids of students passed throw exception.
     *
     * @param course course to add
     * @return same course with entered id
     * @throws DataConsistencyException if non-existed ids of students passed
     * @throws DaoException             if problems with database connection occurred
     */

    Course insertCourse(Course course) throws DataConsistencyException, DaoException;

    /**
     * Return course information by its id, with set of students enrolled to the course.
     * When course with this id not existing returns empty optional.
     *
     * @param courseId course id
     * @return Optional with course and students
     * @throws DaoException if problems with database connection occurred
     */

    Optional<CourseWithStudents> findCourseWithStudentsById(long courseId) throws DaoException;

    /**
     * Update course information, remove students that enrolled to the course in database, and not existing in this set,
     * add students that existing in this set, but not enrolled to the course in database.
     *
     * @param course course that should be updated
     * @return the same course
     * @throws DataConsistencyException if non-existed ids of students passed
     * @throws DaoException             if problems with database connection occurred
     */

    Course updateCourse(Course course) throws DataConsistencyException, DaoException;

    /**
     * Remove course and all related data by course id.
     *
     * @param courseId id of course, that should be deleted.
     * @throws DaoException if problems with database connection occurred
     */

    void deleteCourseById(long courseId) throws DaoException;
}
