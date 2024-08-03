package ru.astondevs.servletrestservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.NoArgsConstructor;
import ru.astondevs.servletrestservice.dao.CoordinatorRepository;
import ru.astondevs.servletrestservice.dao.CourseRepository;
import ru.astondevs.servletrestservice.dao.StudentRepository;
import ru.astondevs.servletrestservice.dao.impl.CoordinatorRepositoryJdbcImpl;
import ru.astondevs.servletrestservice.dao.impl.CourseRepositoryJdbcImpl;
import ru.astondevs.servletrestservice.dao.impl.StudentRepositoryJdbcImpl;
import ru.astondevs.servletrestservice.mapper.CoordinatorMapper;
import ru.astondevs.servletrestservice.mapper.CourseMapper;
import ru.astondevs.servletrestservice.mapper.StudentMapper;
import ru.astondevs.servletrestservice.service.CoordinatorService;
import ru.astondevs.servletrestservice.service.CourseService;
import ru.astondevs.servletrestservice.service.StudentService;
import ru.astondevs.servletrestservice.service.impl.CoordinatorServiceImpl;
import ru.astondevs.servletrestservice.service.impl.CourseServiceImpl;
import ru.astondevs.servletrestservice.service.impl.StudentServiceImpl;
import ru.astondevs.servletrestservice.util.PropertiesFilePropertyLoader;
import ru.astondevs.servletrestservice.util.PropertyLoader;

@WebListener
public class AppServletContextListener implements ServletContextListener {

    public AppServletContextListener(PropertyLoader propertyLoader) {
        this.propertyLoader = propertyLoader;
    }

    public AppServletContextListener() {
        propertyLoader = new PropertiesFilePropertyLoader("/application.properties");
    }

    private final PropertyLoader propertyLoader;

    private HikariDataSource hikariDataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        CourseMapper courseMapper = new CourseMapper();
        StudentMapper studentMapper = new StudentMapper();
        CoordinatorMapper coordinatorMapper = new CoordinatorMapper();

        studentMapper.setCourseMapper(courseMapper);
        studentMapper.setCoordinatorMapper(coordinatorMapper);
        coordinatorMapper.setStudentMapper(studentMapper);
        courseMapper.setStudentMapper(studentMapper);

        hikariDataSource = dataSource();

        CoordinatorRepository coordinatorRepository = new CoordinatorRepositoryJdbcImpl(hikariDataSource);
        StudentRepository studentRepository = new StudentRepositoryJdbcImpl(hikariDataSource);
        CourseRepository courseRepository = new CourseRepositoryJdbcImpl(hikariDataSource);

        CoordinatorService coordinatorService = new CoordinatorServiceImpl(coordinatorMapper, coordinatorRepository);
        StudentService studentService = new StudentServiceImpl(studentMapper, studentRepository);
        CourseService courseService = new CourseServiceImpl(courseMapper, courseRepository);

        context.setAttribute("coordinatorService", coordinatorService);
        context.setAttribute("studentService", studentService);
        context.setAttribute("courseService", courseService);

        ObjectMapper objectMapper = objectMapper();
        context.setAttribute("objectMapper", objectMapper);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        hikariDataSource.close();
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    private HikariDataSource dataSource() {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(propertyLoader.getProperty("database.url"));
        config.setUsername(propertyLoader.getProperty("database.username"));
        config.setPassword(propertyLoader.getProperty("database.password"));

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("cant load driver", e);
        }
        return new HikariDataSource(config);
    }
}
