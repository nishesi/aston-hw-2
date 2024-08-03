package ru.astondevs.servletrestservice.mapper;


import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorDto;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorWithStudentsDto;
import ru.astondevs.servletrestservice.dto.coordinator.NewCoordinatorForm;
import ru.astondevs.servletrestservice.dto.coordinator.UpdateCoordinatorForm;
import ru.astondevs.servletrestservice.model.coordinator.Coordinator;
import ru.astondevs.servletrestservice.model.coordinator.CoordinatorWithStudents;
import ru.astondevs.servletrestservice.model.student.Student;

import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.astondevs.servletrestservice.util.HasRecordComponentWithValue.hasRecordProperty;

class CoordinatorMapperTest {
    static CoordinatorMapper coordinatorMapper;
    Random random = new Random();

    @BeforeAll
    static void setUp() {
        CourseMapper courseMapper = new CourseMapper();
        StudentMapper studentMapper = new StudentMapper();
        coordinatorMapper = new CoordinatorMapper();

        studentMapper.setCourseMapper(courseMapper);
        studentMapper.setCoordinatorMapper(coordinatorMapper);
        coordinatorMapper.setStudentMapper(studentMapper);
        courseMapper.setStudentMapper(studentMapper);
    }

    @Nested
    class toCoordinator_from_NewCoordinatorForm_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            String name = UUID.randomUUID().toString();
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            NewCoordinatorForm form = new NewCoordinatorForm(name, ids);
            Coordinator result = coordinatorMapper.toCoordinator(form);

            assertThat(result, hasProperty("name", is(name)));
            assertThat(result, hasProperty("studentIds", hasSize(ids.size())));
            assertThat(result, hasProperty("studentIds",
                    containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new))));
        }
    }

    @Nested
    class toCoordinatorDto_from_Coordinator_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            Coordinator coordinator = new Coordinator(id, name, ids);

            CoordinatorDto result = coordinatorMapper.toCoordinatorDto(coordinator);

            assertThat(result, allOf(
                    hasRecordProperty("id", is(id)),
                    hasRecordProperty("name", is(name))
            ));

            assertThat(result.studentIds(), allOf(
                    hasSize(ids.size()),
                    containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new))
            ));
        }
    }

    @Nested
    class toCoordinatorWithStudentsDto_from_toCoordinatorWithStudents_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();
            Set<Student> students = Stream.generate(() -> Student.builder()
                            .id(random.nextLong(1_000_000))
                            .name(UUID.randomUUID().toString()).build())
                    .limit(10)
                    .collect(Collectors.toSet());

            CoordinatorWithStudentsDto result = coordinatorMapper
                    .toCoordinatorWithStudentsDto(new CoordinatorWithStudents(id, name, students));


            assertThat(result, allOf(
                    hasRecordProperty("id", is(id)),
                    hasRecordProperty("name", is(name))
            ));

            assertThat(result.students(), hasSize(students.size()));
            assertThat(result.students(), containsInAnyOrder(students.stream().map(s -> allOf(
                            hasRecordProperty("id", is(s.getId())),
                            hasRecordProperty("name", is(s.getName()))))
                    .toList()
            ));
        }
    }

    @Nested
    class toCoordinator_from_UpdateCoordinatorForm_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            UpdateCoordinatorForm form = new UpdateCoordinatorForm(id, name, ids);
            Coordinator result = coordinatorMapper.toCoordinator(form);

            assertThat(result, allOf(
                    hasProperty("id", is(id)),
                    hasProperty("name", is(name)),
                    hasProperty("studentIds", hasSize(ids.size())),
                    hasProperty("studentIds",
                            containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new)))
            ));
        }
    }
}