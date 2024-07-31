package ru.astondevs.servletrestservice.service;

import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.course.CourseWithStudentsDto;
import ru.astondevs.servletrestservice.dto.course.NewCourseForm;
import ru.astondevs.servletrestservice.dto.course.UpdateCourseForm;
import ru.astondevs.servletrestservice.exception.ServiceException;

public interface CourseService {

    /**
     * Creates course from this form.
     *
     * @param form new course data
     * @return course with data from form and enrolled id
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    CourseDto create(NewCourseForm form) throws ServiceException;

    /**
     * Returns information about the course related to this ID
     *
     * @param courseId id of course
     * @return course data, and related students data
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    CourseWithStudentsDto get(long courseId) throws ServiceException;

    /**
     * Update course data and related list of students
     *
     * @param form new course data
     * @return dto with new data
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    CourseDto update(UpdateCourseForm form) throws ServiceException;

    /**
     * Deletes course and related data by ID
     *
     * @param courseId course id
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    void delete(long courseId) throws ServiceException;
}
