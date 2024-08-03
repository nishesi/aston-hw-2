package ru.astondevs.servletrestservice.dto.student;

import lombok.Builder;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorDto;
import ru.astondevs.servletrestservice.dto.course.CourseDto;

import java.util.Set;

@Builder
public record StudentWithCoordinatorAndCoursesDto(Long id,
                                                  String name,
                                                  CoordinatorDto coordinator,
                                                  Set<CourseDto> courses) {
}
