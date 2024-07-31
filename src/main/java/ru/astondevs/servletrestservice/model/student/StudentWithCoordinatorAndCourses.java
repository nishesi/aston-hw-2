package ru.astondevs.servletrestservice.model.student;

import lombok.Builder;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.course.Course;

import java.util.Set;

@Builder
public record StudentWithCoordinatorAndCourses(Long id, String name, Coordinator coordinator, Set<Course> courses) {
}
