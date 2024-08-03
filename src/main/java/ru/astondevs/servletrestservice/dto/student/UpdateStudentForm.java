package ru.astondevs.servletrestservice.dto.student;

import java.util.Set;

public record UpdateStudentForm(Long id, String name, Long coordinatorId, Set<Long> courseIds) {
}
