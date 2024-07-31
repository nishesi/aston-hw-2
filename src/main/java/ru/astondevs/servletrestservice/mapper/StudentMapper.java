package ru.astondevs.servletrestservice.mapper;

import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorDto;
import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.student.NewStudentForm;
import ru.astondevs.servletrestservice.dto.student.StudentDto;
import ru.astondevs.servletrestservice.dto.student.StudentWithCoordinatorAndCoursesDto;
import ru.astondevs.servletrestservice.dto.student.UpdateStudentForm;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.student.Student;
import ru.astondevs.servletrestservice.model.student.StudentWithCoordinatorAndCourses;

import java.util.Set;
import java.util.stream.Collectors;

public class StudentMapper {
    public Student toStudent(NewStudentForm form) {
        return Student.builder()
                .name(form.name())
                .coordinatorId(form.coordinatorId())
                .courseIds(form.courseIds())
                .build();
    }

    public StudentDto toStudentDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .name(student.getName())
                .coordinatorId(student.getCoordinatorId())
                .courseIds(student.getCourseIds())
                .build();
    }

    public StudentWithCoordinatorAndCoursesDto toStudentWithCoordinatorAndCoursesDto(StudentWithCoordinatorAndCourses student) {
        Coordinator coordinator = student.coordinator();
        CoordinatorDto coordinatorDto = CoordinatorDto.builder()
                .id(coordinator.getId())
                .name(coordinator.getName())
                .build();

        Set<Course> courses = student.courses();
        Set<CourseDto> courseDtos = courses.stream()
                .map(c -> CourseDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .collect(Collectors.toSet());

        return StudentWithCoordinatorAndCoursesDto.builder()
                .id(student.id())
                .name(student.name())
                .coordinator(coordinatorDto)
                .courses(courseDtos)
                .build();
    }

    public Student toStudent(UpdateStudentForm form) {
        return Student.builder()
                .id(form.id())
                .name(form.name())
                .coordinatorId(form.coordinatorId())
                .courseIds(form.courseIds())
                .build();
    }
}
