package ru.astondevs.servletrestservice.service;

import ru.astondevs.servletrestservice.dto.student.NewStudentForm;
import ru.astondevs.servletrestservice.dto.student.StudentDto;
import ru.astondevs.servletrestservice.dto.student.StudentWithCoordinatorAndCoursesDto;
import ru.astondevs.servletrestservice.dto.student.UpdateStudentForm;
import ru.astondevs.servletrestservice.exception.ServiceException;

public interface StudentService {

    /**
     * Creates student from this form.
     *
     * @param form new student data
     * @return student with data from form and enrolled id
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    StudentDto create(NewStudentForm form) throws ServiceException;

    /**
     * Returns information about the student related to this ID
     *
     * @param studentId id of student
     * @return student data, related courses and coordinator data
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    StudentWithCoordinatorAndCoursesDto get(long studentId) throws ServiceException;

    /**
     * Update student data and related list of courses
     *
     * @param form new student data
     * @return dto with new data
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    StudentDto update(UpdateStudentForm form) throws ServiceException;

    /**
     * Deletes student and related data by ID
     *
     * @param studentId student id
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    void delete(long studentId) throws ServiceException;
}
