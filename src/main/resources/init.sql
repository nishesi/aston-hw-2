create table coordinator
(
    coordinator_id bigserial primary key,
    name           varchar
);

create table student
(
    student_id     bigserial primary key,
    name           varchar,
    coordinator_id bigint references coordinator (coordinator_id) on delete set null
);

create table course
(
    course_id bigserial primary key,
    name      varchar
);

create table student_courses
(
    student_id bigint references student (student_id) on delete cascade,
    course_id  bigint references course (course_id) on delete cascade,
    constraint unique_student_courses unique (student_id, course_id)
);
