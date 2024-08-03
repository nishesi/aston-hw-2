package ru.astondevs.servletrestservice.dto.course;

import lombok.Builder;
import ru.astondevs.servletrestservice.dto.student.StudentDto;

import java.util.Set;

@Builder
public record CourseWithStudentsDto(Long id,
                                    String name,
                                    Set<StudentDto> students) {
}
