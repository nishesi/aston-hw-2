package ru.astondevs.servletrestservice.model.coordinator;

import lombok.Builder;
import ru.astondevs.servletrestservice.model.student.Student;

import java.util.Set;

@Builder
public record CoordinatorWithStudents(Long id, String name, Set<Student> students) {
}
