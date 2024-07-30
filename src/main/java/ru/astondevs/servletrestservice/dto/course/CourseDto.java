package ru.astondevs.servletrestservice.dto.course;

import lombok.Builder;

import java.util.Set;

@Builder
public record CourseDto(Long id, String name, Set<Long> studentIds) {
}
