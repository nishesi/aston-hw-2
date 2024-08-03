package ru.astondevs.servletrestservice.dao;

import ru.astondevs.servletrestservice.exception.DaoException;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.coordinator.CoordinatorWithStudents;

import java.util.Optional;

public interface CoordinatorRepository {
    /**
     * Create new coordinator and add him to existing courseIds.
     * If non-existent ids of courseIds passed they will be ignored.
     *
     * @param coordinator coordinator to add
     * @return same coordinator with entered id
     * @throws DaoException if problems with database connection occurred
     */

    Coordinator insert(Coordinator coordinator) throws DaoException;

    /**
     * Return coordinator information by its id, with set of his students.
     * When coordinator with this id not existing returns empty optional.
     *
     * @param coordinatorId coordinator id
     * @return Optional with coordinator
     * @throws DaoException if problems with database connection occurred
     */

    Optional<CoordinatorWithStudents> findCoordinatorWithStudents(long coordinatorId) throws DaoException;

    /**
     * Update coordinator information, remove relations between coordinator and students that exist in database,
     * and not existing in this set, add relations that existing in this set, but not existing in database.
     *
     * @param coordinator student that should be updated
     * @return the same coordinator
     * @throws DaoException if problems with database connection occurred
     */

    Coordinator update(Coordinator coordinator) throws DaoException;

    /**
     * Remove coordinator and set related coordinatorIds of students to null.
     *
     * @param coordinatorId id of coordinator, that should be deleted.
     * @throws DaoException if problems with database connection occurred
     */

    void deleteById(long coordinatorId) throws DaoException;
}
