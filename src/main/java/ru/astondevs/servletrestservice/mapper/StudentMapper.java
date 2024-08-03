package ru.astondevs.servletrestservice.mapper;

import lombok.Setter;
import ru.astondevs.servletrestservice.dto.student.NewStudentForm;
import ru.astondevs.servletrestservice.dto.student.StudentDto;
import ru.astondevs.servletrestservice.dto.student.StudentWithCoordinatorAndCoursesDto;
import ru.astondevs.servletrestservice.dto.student.UpdateStudentForm;
import ru.astondevs.servletrestservice.model.student.Student;
import ru.astondevs.servletrestservice.model.student.StudentWithCoordinatorAndCourses;

import java.util.Set;
import java.util.stream.Collectors;

@Setter
public class StudentMapper {

    private CoordinatorMapper coordinatorMapper;
    private CourseMapper courseMapper;

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
        return StudentWithCoordinatorAndCoursesDto.builder()
                .id(student.id())
                .name(student.name())
                .coordinator(coordinatorMapper.toCoordinatorDto(student.coordinator()))
                .courses(courseMapper.toCourseDto(student.courses()))
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

    public Set<StudentDto> toStudentDto(Set<Student> students) {
        return students.stream()
                .map(this::toStudentDto)
                .collect(Collectors.toSet());
    }
}
