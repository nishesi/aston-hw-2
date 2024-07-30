package ru.astondevs.servletrestservice.dto.student;

import lombok.Builder;

@Builder
public record StudentDto(Long id, String name, Long coordinatorId) {
}
