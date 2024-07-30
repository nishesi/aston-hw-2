package ru.astondevs.servletrestservice.mapper;


import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.course.CourseWithStudentsDto;
import ru.astondevs.servletrestservice.dto.course.NewCourseForm;
import ru.astondevs.servletrestservice.dto.course.UpdateCourseForm;
import ru.astondevs.servletrestservice.model.Student;
import ru.astondevs.servletrestservice.model.course.Course;
import ru.astondevs.servletrestservice.model.course.CourseWithStudents;

import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.astondevs.servletrestservice.util.HasRecordComponentWithValue.hasRecordComponent;

class CourseMapperTest {
    static CourseMapper courseMapper;
    Random random = new Random();

    @BeforeAll
    static void setUp() {
        courseMapper = new CourseMapper();
    }

    @Nested
    class toCourse_from_NewCourseForm_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            String name = UUID.randomUUID().toString();
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            NewCourseForm newCourseForm = new NewCourseForm(name, ids);
            Course course = courseMapper.toCourse(newCourseForm);

            assertThat(course, hasProperty("name", is(name)));
            assertThat(course, hasProperty("studentIds", hasSize(ids.size())));
            assertThat(course, hasProperty("studentIds",
                    containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new))));
        }
    }

    @Nested
    class toCourseDto_from_Course_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            Course course = new Course(id, name, ids);

            CourseDto result = courseMapper.toCourseDto(course);

            assertThat(result.id(), is(id));
            assertThat(result.name(), is(name));

            assertThat(result.studentIds(), hasSize(ids.size()));
            assertThat(result.studentIds(),
                    containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new)));
        }
    }

    @Nested
    class toCourseWithStudentsDto_from_CourseWithStudents_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();

            Set<Student> students = Stream.generate(() -> Student.builder()
                            .id(random.nextLong(1_000_000))
                            .coordinatorId(random.nextLong(1_000_000))
                            .name(UUID.randomUUID().toString()).build())
                    .limit(10)
                    .collect(Collectors.toSet());

            CourseWithStudentsDto result = courseMapper.toCourseWithStudentsDto(new CourseWithStudents(id, name, students));

            assertThat(result.id(), is(id));
            assertThat(result.name(), is(name));

            assertThat(result.students(), hasSize(students.size()));
            assertThat(result.students(), containsInAnyOrder(students.stream().map(s -> allOf(
                            hasRecordComponent("id", is(s.getId())),
                            hasRecordComponent("name", is(s.getName())),
                            hasRecordComponent("coordinatorId", is(s.getCoordinatorId()))))
                    .toList()));
        }
    }

    @Nested
    class toCourse_from_UpdateCourseForm_method_test {
        @RepeatedTest(10)
        void should_transfer_all_data() {
            Long id = random.nextLong(1_000_000);
            String name = UUID.randomUUID().toString();
            Set<Long> ids = Stream.generate(() -> random.nextLong(1_000_000))
                    .limit(random.nextInt(10))
                    .collect(Collectors.toSet());
            UpdateCourseForm form = new UpdateCourseForm(id, name, ids);
            Course course = courseMapper.toCourse(form);

            assertThat(course, hasProperty("id", is(id)));
            assertThat(course, hasProperty("name", is(name)));
            assertThat(course, hasProperty("studentIds", hasSize(ids.size())));
            assertThat(course, hasProperty("studentIds",
                    containsInAnyOrder(ids.stream().map(Matchers::is).toArray(Matcher[]::new))));
        }
    }
}