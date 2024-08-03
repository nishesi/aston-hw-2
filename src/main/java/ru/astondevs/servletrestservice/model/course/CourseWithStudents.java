package ru.astondevs.servletrestservice.model.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.astondevs.servletrestservice.model.student.Student;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseWithStudents {
    private Long id;
    private String name;
    private Set<Student> students;
}
