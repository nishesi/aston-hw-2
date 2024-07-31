package ru.astondevs.servletrestservice.dto.student;

import lombok.Builder;

import java.util.Set;

@Builder
public record StudentDto(Long id, String name, Long coordinatorId, Set<Long> courseIds) {
}
