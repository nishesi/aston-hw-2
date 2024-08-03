package ru.astondevs.servletrestservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dao.CourseRepository;
import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.course.CourseWithStudentsDto;
import ru.astondevs.servletrestservice.dto.course.NewCourseForm;
import ru.astondevs.servletrestservice.dto.course.UpdateCourseForm;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.exception.ServiceException;
import ru.astondevs.servletrestservice.mapper.CourseMapper;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.course.CourseWithStudents;
import ru.astondevs.servletrestservice.service.CourseService;

@Slf4j
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseMapper courseMapper;
    private final CourseRepository courseRepository;

    @Override
    public CourseDto create(NewCourseForm form) {
        try {
            Course course = courseMapper.toCourse(form);
            course = courseRepository.insert(course);
            return courseMapper.toCourseDto(course);

        } catch (DataConsistencyException ex) {
            if (log.isDebugEnabled())
                log.debug("course creation failed", ex);
            throw new ServiceException(400, ex.getMessage(), ex);
        }
    }

    @Override
    public CourseWithStudentsDto get(long courseId) {
        CourseWithStudents course = courseRepository.findCourseWithStudentsById(courseId)
                .orElseThrow(() -> new ServiceException(404, "course not found", null));
        return courseMapper.toCourseWithStudentsDto(course);
    }

    @Override
    public CourseDto update(UpdateCourseForm form) {
        try {
            Course course = courseMapper.toCourse(form);
            course = courseRepository.update(course);
            return courseMapper.toCourseDto(course);
        } catch (DataConsistencyException ex) {
            if (log.isDebugEnabled())
                log.debug("course creation failed", ex);
            throw new ServiceException(400, ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(long courseId) {
        courseRepository.deleteById(courseId);
    }
}
