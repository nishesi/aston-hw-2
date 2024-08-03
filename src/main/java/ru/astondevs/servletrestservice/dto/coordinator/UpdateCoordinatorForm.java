package ru.astondevs.servletrestservice.dto.coordinator;

import java.util.Set;

public record UpdateCoordinatorForm(Long id, String name, Set<Long> studentIds) {
}
