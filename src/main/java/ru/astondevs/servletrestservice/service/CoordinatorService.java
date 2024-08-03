package ru.astondevs.servletrestservice.service;

import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorDto;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorWithStudentsDto;
import ru.astondevs.servletrestservice.dto.coordinator.NewCoordinatorForm;
import ru.astondevs.servletrestservice.dto.coordinator.UpdateCoordinatorForm;
import ru.astondevs.servletrestservice.exception.ServiceException;

public interface CoordinatorService {

    /**
     * Creates coordinator from this form.
     *
     * @param form new coordinator data
     * @return coordinator with data from form and enrolled id
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    CoordinatorDto create(NewCoordinatorForm form) throws ServiceException;

    /**
     * Returns information about the coordinator related to this ID
     *
     * @param coordinatorId id of coordinator
     * @return coordinator data, related students
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    CoordinatorWithStudentsDto get(long coordinatorId) throws ServiceException;

    /**
     * Update coordinator data and related list of students
     *
     * @param form new coordinator data
     * @return dto with new data
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    CoordinatorDto update(UpdateCoordinatorForm form) throws ServiceException;

    /**
     * Deletes coordinator and related data by ID
     *
     * @param coordinatorId coordinator id
     * @throws ServiceException if an event occurred that does not fit into the normal operation of the method
     */

    void delete(long coordinatorId) throws ServiceException;
}
