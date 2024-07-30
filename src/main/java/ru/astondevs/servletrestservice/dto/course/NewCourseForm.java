package ru.astondevs.servletrestservice.dto.course;

import java.util.Set;

public record NewCourseForm(String name, Set<Long> studentIds) {
}
