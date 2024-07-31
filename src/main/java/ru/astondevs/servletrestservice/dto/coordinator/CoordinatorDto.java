package ru.astondevs.servletrestservice.dto.coordinator;

import lombok.Builder;

import java.util.Set;

@Builder
public record CoordinatorDto(Long id, String name, Set<Long> students) {
}
