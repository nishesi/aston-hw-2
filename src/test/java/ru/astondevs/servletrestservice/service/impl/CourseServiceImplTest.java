package ru.astondevs.servletrestservice.service.impl;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import ru.astondevs.servletrestservice.dao.CourseRepository;
import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.course.CourseWithStudentsDto;
import ru.astondevs.servletrestservice.dto.course.NewCourseForm;
import ru.astondevs.servletrestservice.dto.course.UpdateCourseForm;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.exception.ServiceException;
import ru.astondevs.servletrestservice.mapper.CourseMapper;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.course.CourseWithStudents;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CourseServiceImplTest {
    static CourseServiceImpl courseService;
    static CourseMapper courseMapper;
    static CourseRepository courseRepository;

    @BeforeAll
    static void init() {
        courseRepository = Mockito.mock(CourseRepository.class);
        courseMapper = Mockito.mock(CourseMapper.class);
        courseService = new CourseServiceImpl(courseMapper, courseRepository);
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(courseMapper, courseRepository);
    }

    @Nested
    class createCourse_method_test {
        @Test
        void should_create_new_course() {
            NewCourseForm form = new NewCourseForm(null, null);
            Course course = new Course();
            Course course1 = new Course();
            CourseDto courseDto = new CourseDto(null, null, null);
            when(courseMapper.toCourse(form)).thenReturn(course);
            when(courseRepository.insert(course)).thenReturn(course1);
            when(courseMapper.toCourseDto(course1)).thenReturn(courseDto);

            CourseDto result = courseService.create(form);

            assertThat(result, sameInstance(courseDto));

            verify(courseMapper).toCourse(form);
            verify(courseRepository).insert(course);
            verify(courseMapper).toCourseDto(course1);
        }

        @Test
        void should_throw_service_exception_when_data_consistency_exception() {
            when(courseRepository.insert(any()))
                    .thenThrow(new DataConsistencyException("test exception", new RuntimeException()));

            NewCourseForm form = new NewCourseForm(null, null);

            Assertions.assertThrows(ServiceException.class, () -> courseService.create(form));
        }
    }

    @Nested
    class getCourse_method_test {
        @Test
        void should_return_course() {
            CourseWithStudents courseWithStudents = new CourseWithStudents();
            CourseWithStudentsDto courseWithStudentsDto = new CourseWithStudentsDto(null, null, null);

            when(courseRepository.findCourseWithStudentsById(anyLong())).thenReturn(Optional.of(courseWithStudents));
            when(courseMapper.toCourseWithStudentsDto(courseWithStudents)).thenReturn(courseWithStudentsDto);

            CourseWithStudentsDto result = courseService.get(1L);

            assertThat(result, sameInstance(courseWithStudentsDto));

            verify(courseRepository).findCourseWithStudentsById(1L);
            verify(courseMapper).toCourseWithStudentsDto(courseWithStudents);
        }

        @Test
        void should_throw_service_exception_if_course_not_found() {
            when(courseRepository.findCourseWithStudentsById(anyLong())).thenReturn(Optional.empty());

            Assertions.assertThrows(ServiceException.class, () -> courseService.get(1L));

            verify(courseRepository).findCourseWithStudentsById(1L);
        }
    }

    @Nested
    class updateCourse_method_test {
        @Test
        void should_update_course() {
            UpdateCourseForm form = new UpdateCourseForm(null, null, null);
            Course course = new Course();
            Course course1 = new Course();
            CourseDto courseDto = new CourseDto(null, null, null);
            when(courseMapper.toCourse(form)).thenReturn(course);
            when(courseRepository.update(course)).thenReturn(course1);
            when(courseMapper.toCourseDto(course1)).thenReturn(courseDto);

            CourseDto result = courseService.update(form);

            assertThat(result, sameInstance(courseDto));

            verify(courseMapper).toCourse(form);
            verify(courseRepository).update(course);
            verify(courseMapper).toCourseDto(course1);
        }

        @Test
        void should_throw_service_exception_when_data_consistency_exception() {
            when(courseRepository.update(any()))
                    .thenThrow(new DataConsistencyException("test exception", new RuntimeException()));

            UpdateCourseForm form = new UpdateCourseForm(null, null, null);

            Assertions.assertThrows(ServiceException.class, () -> courseService.update(form));
        }
    }

    @Nested
    class deleteCourse_method_test {
        @Test
        void should_delete_course() {
            courseService.delete(1L);
            verify(courseRepository).deleteById(1L);
        }
    }
}