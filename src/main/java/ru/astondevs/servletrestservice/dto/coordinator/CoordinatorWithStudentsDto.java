package ru.astondevs.servletrestservice.dto.coordinator;

import lombok.Builder;
import ru.astondevs.servletrestservice.dto.student.StudentDto;

import java.util.Set;

@Builder
public record CoordinatorWithStudentsDto(Long id, String name, Set<StudentDto> students) {
}
