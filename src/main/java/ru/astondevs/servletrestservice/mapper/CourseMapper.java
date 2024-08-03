package ru.astondevs.servletrestservice.mapper;

import lombok.Setter;
import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.course.CourseWithStudentsDto;
import ru.astondevs.servletrestservice.dto.course.NewCourseForm;
import ru.astondevs.servletrestservice.dto.course.UpdateCourseForm;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.course.CourseWithStudents;

import java.util.Set;
import java.util.stream.Collectors;

@Setter
public class CourseMapper {

    private StudentMapper studentMapper;

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
        return CourseWithStudentsDto.builder()
                .id(course.getId())
                .name(course.getName())
                .students(studentMapper.toStudentDto(course.getStudents()))
                .build();
    }

    public Course toCourse(UpdateCourseForm form) {
        return Course.builder()
                .id(form.id())
                .name(form.name())
                .studentIds(form.studentIds())
                .build();
    }

    public Set<CourseDto> toCourseDto(Set<Course> courses) {
        return courses.stream()
                .map(this::toCourseDto)
                .collect(Collectors.toSet());
    }
}
