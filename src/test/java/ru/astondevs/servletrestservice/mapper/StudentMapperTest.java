package ru.astondevs.servletrestservice.mapper;


import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import ru.astondevs.servletrestservice.dto.student.NewStudentForm;
import ru.astondevs.servletrestservice.dto.student.StudentDto;
import ru.astondevs.servletrestservice.dto.student.StudentWithCoordinatorAndCoursesDto;
import ru.astondevs.servletrestservice.dto.student.UpdateStudentForm;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.student.Student;
import ru.astondevs.servletrestservice.model.student.StudentWithCoordinatorAndCourses;

import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.astondevs.servletrestservice.util.HasRecordComponentWithValue.hasRecordProperty;

class StudentMapperTest {
    static StudentMapper studentMapper;
    Random random = new Random();

    @BeforeAll
    static void setUp() {
        studentMapper = new StudentMapper();
    }

    @Nested
    class toStudent_from_NewStudentForm_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            String name = UUID.randomUUID().toString();
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            Long coordinatorId = random.nextLong(1_000_000);
            NewStudentForm form = new NewStudentForm(name, coordinatorId, ids);
            Student result = studentMapper.toStudent(form);

            assertThat(result, hasProperty("name", is(name)));
            assertThat(result, hasProperty("coordinatorId", is(coordinatorId)));
            assertThat(result, hasProperty("courseIds", hasSize(ids.size())));
            assertThat(result, hasProperty("courseIds",
                    containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new))));
        }
    }

    @Nested
    class toStudentDto_from_Student_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            Long coordinatorId = random.nextLong(1_000_000);
            Student student = new Student(id, name, coordinatorId, ids);

            StudentDto result = studentMapper.toStudentDto(student);

            assertThat(result, allOf(
                    hasRecordProperty("id", is(id)),
                    hasRecordProperty("name", is(name)),
                    hasRecordProperty("coordinatorId", is(coordinatorId))
            ));

            assertThat(result.courseIds(), allOf(
                    hasSize(ids.size()),
                    containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new))
            ));
        }
    }

    @Nested
    class toStudentWithCoordinatorAndCoursesDto_from_StudentWithCoordinatorAndCourses_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();
            Coordinator coordinator = Coordinator.builder()
                    .id(random.nextLong(1_000_000))
                    .name(UUID.randomUUID().toString())
                    .build();
            Set<Course> courses = Stream.generate(() -> Course.builder()
                            .id(random.nextLong(1_000_000))
                            .name(UUID.randomUUID().toString()).build())
                    .limit(10)
                    .collect(Collectors.toSet());

            StudentWithCoordinatorAndCoursesDto result = studentMapper
                    .toStudentWithCoordinatorAndCoursesDto(new StudentWithCoordinatorAndCourses(id, name, coordinator, courses));


            assertThat(result, allOf(
                    hasRecordProperty("id", is(id)),
                    hasRecordProperty("name", is(name)),
                    hasRecordProperty("coordinator", allOf(
                            hasRecordProperty("id", is(coordinator.getId())),
                            hasRecordProperty("name", is(coordinator.getName()))
                    ))
            ));

            assertThat(result.courses(), hasSize(courses.size()));
            assertThat(result.courses(), containsInAnyOrder(courses.stream().map(s -> allOf(
                            hasRecordProperty("id", is(s.getId())),
                            hasRecordProperty("name", is(s.getName()))))
                    .toList()
            ));
        }
    }

    @Nested
    class toStudent_from_UpdateStudentForm_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();
            Long coordinatorId = random.nextLong(1_000_000);
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            UpdateStudentForm form = new UpdateStudentForm(id, name, coordinatorId, ids);
            Student student = studentMapper.toStudent(form);

            assertThat(student, allOf(
                    hasProperty("id", is(id)),
                    hasProperty("name", is(name)),
                    hasProperty("coordinatorId", is(coordinatorId)),
                    hasProperty("courseIds", hasSize(ids.size())),
                    hasProperty("courseIds",
                            containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new)))
            ));
        }
    }
}