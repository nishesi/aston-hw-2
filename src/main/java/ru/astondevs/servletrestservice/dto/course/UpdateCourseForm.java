package ru.astondevs.servletrestservice.dto.course;

import java.util.Set;

public record UpdateCourseForm(Long id, String name, Set<Long> studentIds) {
}
