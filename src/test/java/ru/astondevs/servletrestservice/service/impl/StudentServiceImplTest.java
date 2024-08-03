package ru.astondevs.servletrestservice.service.impl;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import ru.astondevs.servletrestservice.dao.StudentRepository;
import ru.astondevs.servletrestservice.dto.student.NewStudentForm;
import ru.astondevs.servletrestservice.dto.student.StudentDto;
import ru.astondevs.servletrestservice.dto.student.StudentWithCoordinatorAndCoursesDto;
import ru.astondevs.servletrestservice.dto.student.UpdateStudentForm;
import ru.astondevs.servletrestservice.exception.DataConsistencyException;
import ru.astondevs.servletrestservice.exception.ServiceException;
import ru.astondevs.servletrestservice.mapper.StudentMapper;
import ru.astondevs.servletrestservice.model.student.Student;
import ru.astondevs.servletrestservice.model.student.StudentWithCoordinatorAndCourses;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class StudentServiceImplTest {
    static StudentServiceImpl studentService;
    static StudentMapper studentMapper;
    static StudentRepository studentRepository;

    @BeforeAll
    static void init() {
        studentRepository = Mockito.mock(StudentRepository.class);
        studentMapper = Mockito.mock(StudentMapper.class);
        studentService = new StudentServiceImpl(studentMapper, studentRepository);
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(studentMapper, studentRepository);
    }

    @Nested
    class createStudent_method_test {
        @Test
        void should_create_new_student() {
            NewStudentForm form = new NewStudentForm(null, null, null);
            Student student = new Student();
            Student student1 = new Student();
            StudentDto studentDto = new StudentDto(null, null, null, null);
            when(studentMapper.toStudent(form)).thenReturn(student);
            when(studentRepository.insert(student)).thenReturn(student1);
            when(studentMapper.toStudentDto(student1)).thenReturn(studentDto);

            StudentDto result = studentService.create(form);

            assertThat(result, sameInstance(studentDto));

            verify(studentMapper).toStudent(form);
            verify(studentRepository).insert(student);
            verify(studentMapper).toStudentDto(student1);
        }

        @Test
        void should_throw_service_exception_when_data_consistency_exception() {
            when(studentRepository.insert(any()))
                    .thenThrow(new DataConsistencyException("test exception", new RuntimeException()));

            NewStudentForm form = new NewStudentForm(null, null, null);

            Assertions.assertThrows(ServiceException.class, () -> studentService.create(form));
        }
    }

    @Nested
    class getStudent_method_test {
        @Test
        void should_return_student() {
            StudentWithCoordinatorAndCourses student = StudentWithCoordinatorAndCourses.builder().build();
            StudentWithCoordinatorAndCoursesDto studentDto = StudentWithCoordinatorAndCoursesDto.builder().build();

            when(studentRepository.findStudentWithCoordinatorAndCoursesById(anyLong())).thenReturn(Optional.of(student));
            when(studentMapper.toStudentWithCoordinatorAndCoursesDto(student)).thenReturn(studentDto);

            StudentWithCoordinatorAndCoursesDto result = studentService.get(1L);

            assertThat(result, sameInstance(studentDto));

            verify(studentRepository).findStudentWithCoordinatorAndCoursesById(1L);
            verify(studentMapper).toStudentWithCoordinatorAndCoursesDto(student);
        }

        @Test
        void should_throw_service_exception_if_student_not_found() {
            when(studentRepository.findStudentWithCoordinatorAndCoursesById(anyLong())).thenReturn(Optional.empty());

            Assertions.assertThrows(ServiceException.class, () -> studentService.get(1L));

            verify(studentRepository).findStudentWithCoordinatorAndCoursesById(1L);
        }
    }

    @Nested
    class updateStudent_method_test {
        @Test
        void should_update_course() {
            UpdateStudentForm form = new UpdateStudentForm(null, null, null, null);
            Student student = new Student();
            Student student1 = new Student();
            StudentDto studentDto = new StudentDto(null, null, null, null);
            when(studentMapper.toStudent(form)).thenReturn(student);
            when(studentRepository.update(student)).thenReturn(student1);
            when(studentMapper.toStudentDto(student1)).thenReturn(studentDto);

            StudentDto result = studentService.update(form);

            assertThat(result, sameInstance(studentDto));

            verify(studentMapper).toStudent(form);
            verify(studentRepository).update(student);
            verify(studentMapper).toStudentDto(student1);
        }

        @Test
        void should_throw_service_exception_when_data_consistency_exception() {
            when(studentRepository.update(any()))
                    .thenThrow(new DataConsistencyException("test exception", new RuntimeException()));

            UpdateStudentForm form = new UpdateStudentForm(null, null, null, null);

            Assertions.assertThrows(ServiceException.class, () -> studentService.update(form));
        }
    }

    @Nested
    class deleteStudent_method_test {
        @Test
        void should_delete_student() {
            studentService.delete(1L);
            verify(studentRepository).deleteById(1L);
        }
    }
}