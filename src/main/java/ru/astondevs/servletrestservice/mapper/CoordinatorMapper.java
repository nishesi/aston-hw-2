package ru.astondevs.servletrestservice.mapper;

import lombok.Setter;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorDto;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorWithStudentsDto;
import ru.astondevs.servletrestservice.dto.coordinator.NewCoordinatorForm;
import ru.astondevs.servletrestservice.dto.coordinator.UpdateCoordinatorForm;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.coordinator.CoordinatorWithStudents;

@Setter
public class CoordinatorMapper {

    private StudentMapper studentMapper;

    public Coordinator toCoordinator(NewCoordinatorForm form) {
        return Coordinator.builder()
                .name(form.name())
                .studentIds(form.studentIds())
                .build();
    }

    public CoordinatorDto toCoordinatorDto(Coordinator coordinator) {
        return CoordinatorDto.builder()
                .id(coordinator.getId())
                .name(coordinator.getName())
                .studentIds(coordinator.getStudentIds())
                .build();
    }

    public CoordinatorWithStudentsDto toCoordinatorWithStudentsDto(CoordinatorWithStudents coordinator) {
        return CoordinatorWithStudentsDto.builder()
                .id(coordinator.id())
                .name(coordinator.name())
                .students(studentMapper.toStudentDto(coordinator.students()))
                .build();
    }

    public Coordinator toCoordinator(UpdateCoordinatorForm form) {
        return Coordinator.builder()
                .id(form.id())
                .name(form.name())
                .studentIds(form.studentIds())
                .build();
    }
}
