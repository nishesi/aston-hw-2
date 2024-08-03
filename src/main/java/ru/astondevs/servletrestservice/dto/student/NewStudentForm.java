package ru.astondevs.servletrestservice.dto.student;

import java.util.Set;

public record NewStudentForm(String name, Long coordinatorId, Set<Long> courseIds) {
}
