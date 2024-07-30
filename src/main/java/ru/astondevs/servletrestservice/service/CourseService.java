package ru.astondevs.servletrestservice.service;

import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.course.CourseWithStudentsDto;
import ru.astondevs.servletrestservice.dto.course.NewCourseForm;
import ru.astondevs.servletrestservice.dto.course.UpdateCourseForm;

public interface CourseService {

    CourseDto createCourse(NewCourseForm form);

    CourseWithStudentsDto getCourse(long courseId);

    CourseDto updateCourse(UpdateCourseForm form);

    void deleteCourse(long courseId);
}
