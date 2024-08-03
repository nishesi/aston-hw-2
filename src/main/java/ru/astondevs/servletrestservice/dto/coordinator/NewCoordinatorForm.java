package ru.astondevs.servletrestservice.dto.coordinator;

import java.util.Set;

public record NewCoordinatorForm(String name, Set<Long> studentIds) {
}
