package ru.astondevs.servletrestservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dao.StudentRepository;
import ru.astondevs.servletrestservice.dto.student.NewStudentForm;
import ru.astondevs.servletrestservice.dto.student.StudentDto;
import ru.astondevs.servletrestservice.dto.student.StudentWithCoordinatorAndCoursesDto;
import ru.astondevs.servletrestservice.dto.student.UpdateStudentForm;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.exception.ServiceException;
import ru.astondevs.servletrestservice.mapper.StudentMapper;
import ru.astondevs.servletrestservice.model.student.Student;
import ru.astondevs.servletrestservice.model.student.StudentWithCoordinatorAndCourses;
import ru.astondevs.servletrestservice.service.StudentService;

@Slf4j
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final StudentRepository studentRepository;

    @Override
    public StudentDto create(NewStudentForm form) throws ServiceException {
        try {
            Student student = studentMapper.toStudent(form);
            student = studentRepository.insert(student);
            return studentMapper.toStudentDto(student);

        } catch (DataConsistencyException ex) {
            if (log.isDebugEnabled())
                log.debug("student creation failed", ex);
            throw new ServiceException(400, ex.getMessage(), ex);
        }
    }

    @Override
    public StudentWithCoordinatorAndCoursesDto get(long studentId) throws ServiceException {
        StudentWithCoordinatorAndCourses student = studentRepository.findStudentWithCoordinatorAndCoursesById(studentId)
                .orElseThrow(() -> new ServiceException(404, "student not found", null));
        return studentMapper.toStudentWithCoordinatorAndCoursesDto(student);
    }

    @Override
    public StudentDto update(UpdateStudentForm form) throws ServiceException {
        try {
            Student student = studentMapper.toStudent(form);
            student = studentRepository.update(student);
            return studentMapper.toStudentDto(student);
        } catch (DataConsistencyException ex) {
            if (log.isDebugEnabled())
                log.debug("student update failed", ex);
            throw new ServiceException(400, ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(long studentId) throws ServiceException {
        studentRepository.deleteById(studentId);
    }
}
