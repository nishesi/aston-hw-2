package ru.astondevs.servletrestservice.mapper;

import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.course.CourseWithStudentsDto;
import ru.astondevs.servletrestservice.dto.course.NewCourseForm;
import ru.astondevs.servletrestservice.dto.course.UpdateCourseForm;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.course.CourseWithStudents;

import java.util.stream.Collectors;

public class CourseMapper {
    public Course toCourse(NewCourseForm form) {
        return Course.builder()
                .name(form.name())
                .studentIds(form.studentIds())
                .build();
    }

    public CourseDto toCourseDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .name(course.getName())
                .studentIds(course.getStudentIds())
                .build();
    }

    public CourseWithStudentsDto toCourseWithStudentsDto(CourseWithStudents course) {
        var students = course.getStudents().stream()
                .map(s -> new CourseWithStudentsDto.StudentDto(s.getId(), s.getName(), s.getCoordinatorId()))
                .collect(Collectors.toSet());

        return CourseWithStudentsDto.builder()
                .id(course.getId())
                .name(course.getName())
                .students(students)
                .build();
    }

    public Course toCourse(UpdateCourseForm form) {
        return Course.builder()
                .id(form.id())
                .name(form.name())
                .studentIds(form.studentIds())
                .build();
    }
}
