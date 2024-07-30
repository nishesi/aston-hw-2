package ru.astondevs.servletrestservice.dto.course;

import lombok.Builder;

import java.util.Set;

@Builder
public record CourseWithStudentsDto(Long id,
                                    String name,
                                    Set<StudentDto> students) {

    @Builder
    public record StudentDto(Long id,
                             String name,
                             Long coordinatorId) {
    }
}
