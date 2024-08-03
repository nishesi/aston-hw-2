package ru.astondevs.servletrestservice.service.impl;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import ru.astondevs.servletrestservice.dao.CoordinatorRepository;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorDto;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorWithStudentsDto;
import ru.astondevs.servletrestservice.dto.coordinator.NewCoordinatorForm;
import ru.astondevs.servletrestservice.dto.coordinator.UpdateCoordinatorForm;
import ru.astondevs.servletrestservice.exception.ServiceException;
import ru.astondevs.servletrestservice.mapper.CoordinatorMapper;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.coordinator.CoordinatorWithStudents;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CoordinatorServiceImplTest {
    static CoordinatorServiceImpl coordinatorService;
    static CoordinatorMapper coordinatorMapper;
    static CoordinatorRepository coordinatorRepository;

    @BeforeAll
    static void init() {
        coordinatorRepository = Mockito.mock(CoordinatorRepository.class);
        coordinatorMapper = Mockito.mock(CoordinatorMapper.class);
        coordinatorService = new CoordinatorServiceImpl(coordinatorMapper, coordinatorRepository);
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(coordinatorMapper, coordinatorRepository);
    }

    @Nested
    class create_method_test {
        @Test
        void should_create_new_coordinator() {
            NewCoordinatorForm form = new NewCoordinatorForm(null, null);
            Coordinator coordinator = new Coordinator();
            Coordinator coordinator1 = new Coordinator();
            CoordinatorDto coordinatorDto = new CoordinatorDto(null, null, null);
            when(coordinatorMapper.toCoordinator(form)).thenReturn(coordinator);
            when(coordinatorRepository.insert(coordinator)).thenReturn(coordinator1);
            when(coordinatorMapper.toCoordinatorDto(coordinator1)).thenReturn(coordinatorDto);

            CoordinatorDto result = coordinatorService.create(form);

            assertThat(result, sameInstance(coordinatorDto));

            verify(coordinatorMapper).toCoordinator(form);
            verify(coordinatorRepository).insert(coordinator);
            verify(coordinatorMapper).toCoordinatorDto(coordinator1);
        }
    }

    @Nested
    class get_method_test {
        @Test
        void should_return_coordinator() {
            CoordinatorWithStudents coordinator = CoordinatorWithStudents.builder().build();
            CoordinatorWithStudentsDto coordinatorDto = CoordinatorWithStudentsDto.builder().build();

            when(coordinatorRepository.findCoordinatorWithStudents(anyLong())).thenReturn(Optional.of(coordinator));
            when(coordinatorMapper.toCoordinatorWithStudentsDto(coordinator)).thenReturn(coordinatorDto);

            CoordinatorWithStudentsDto result = coordinatorService.get(1L);

            assertThat(result, sameInstance(coordinatorDto));

            verify(coordinatorRepository).findCoordinatorWithStudents(1L);
            verify(coordinatorMapper).toCoordinatorWithStudentsDto(coordinator);
        }

        @Test
        void should_throw_service_exception_if_student_not_found() {
            when(coordinatorRepository.findCoordinatorWithStudents(anyLong())).thenReturn(Optional.empty());

            Assertions.assertThrows(ServiceException.class, () -> coordinatorService.get(1L));

            verify(coordinatorRepository).findCoordinatorWithStudents(1L);
        }
    }

    @Nested
    class update_method_test {
        @Test
        void should_update_course() {
            UpdateCoordinatorForm form = new UpdateCoordinatorForm(null, null, null);
            Coordinator coordinator = new Coordinator();
            Coordinator coordinator1 = new Coordinator();
            CoordinatorDto coordinatorDto = new CoordinatorDto(null, null, null);
            when(coordinatorMapper.toCoordinator(form)).thenReturn(coordinator);
            when(coordinatorRepository.update(coordinator)).thenReturn(coordinator1);
            when(coordinatorMapper.toCoordinatorDto(coordinator1)).thenReturn(coordinatorDto);

            CoordinatorDto result = coordinatorService.update(form);

            assertThat(result, sameInstance(coordinatorDto));

            verify(coordinatorMapper).toCoordinator(form);
            verify(coordinatorRepository).update(coordinator);
            verify(coordinatorMapper).toCoordinatorDto(coordinator1);
        }
    }

    @Nested
    class delete_method_test {
        @Test
        void should_delete_coordinator() {
            coordinatorService.delete(1L);
            verify(coordinatorRepository).deleteById(1L);
        }
    }
}