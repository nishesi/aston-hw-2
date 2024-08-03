package ru.astondevs.servletrestservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dao.CoordinatorRepository;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorDto;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorWithStudentsDto;
import ru.astondevs.servletrestservice.dto.coordinator.NewCoordinatorForm;
import ru.astondevs.servletrestservice.dto.coordinator.UpdateCoordinatorForm;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.exception.ServiceException;
import ru.astondevs.servletrestservice.mapper.CoordinatorMapper;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.coordinator.CoordinatorWithStudents;
import ru.astondevs.servletrestservice.service.CoordinatorService;

@Slf4j
@RequiredArgsConstructor
public class CoordinatorServiceImpl implements CoordinatorService {

    private final CoordinatorMapper coordinatorMapper;
    private final CoordinatorRepository coordinatorRepository;

    @Override
    public CoordinatorDto create(NewCoordinatorForm form) throws ServiceException {
        try {
            Coordinator coordinator = coordinatorMapper.toCoordinator(form);
            coordinator = coordinatorRepository.insert(coordinator);
            return coordinatorMapper.toCoordinatorDto(coordinator);

        } catch (DataConsistencyException ex) {
            if (log.isDebugEnabled())
                log.debug("coordinator creation failed", ex);
            throw new ServiceException(400, ex.getMessage(), ex);
        }
    }

    @Override
    public CoordinatorWithStudentsDto get(long coordinatorId) throws ServiceException {
        CoordinatorWithStudents student = coordinatorRepository.findCoordinatorWithStudents(coordinatorId)
                .orElseThrow(() -> new ServiceException(404, "coordinator not found", null));
        return coordinatorMapper.toCoordinatorWithStudentsDto(student);
    }

    @Override
    public CoordinatorDto update(UpdateCoordinatorForm form) throws ServiceException {
        try {
            Coordinator student = coordinatorMapper.toCoordinator(form);
            student = coordinatorRepository.update(student);
            return coordinatorMapper.toCoordinatorDto(student);
        } catch (DataConsistencyException ex) {
            if (log.isDebugEnabled())
                log.debug("coordinator update failed", ex);
            throw new ServiceException(400, ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(long coordinatorId) throws ServiceException {
        coordinatorRepository.deleteById(coordinatorId);
    }
}
