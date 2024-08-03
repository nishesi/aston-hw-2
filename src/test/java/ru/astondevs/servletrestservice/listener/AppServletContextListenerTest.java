package ru.astondevs.servletrestservice.listener;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.astondevs.servletrestservice.util.PropertyLoader;

import static org.mockito.Mockito.when;

@Testcontainers
class AppServletContextListenerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2-alpine")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("./init.sql");

    static AppServletContextListener appServletContextListener;
    static PropertyLoader propertyLoader;

    @BeforeAll
    static void setUp() {
        propertyLoader = Mockito.mock(PropertyLoader.class);
        appServletContextListener = new AppServletContextListener(propertyLoader);
    }

    @Test
    void try_initialize_context() {
        when(propertyLoader.getProperty("database.url")).thenReturn(postgres.getJdbcUrl());
        when(propertyLoader.getProperty("database.username")).thenReturn(postgres.getUsername());
        when(propertyLoader.getProperty("database.password")).thenReturn(postgres.getPassword());

        ServletContext context = Mockito.mock(ServletContext.class);
        ServletContextEvent event = new ServletContextEvent(context);
        appServletContextListener.contextInitialized(event);
    }
}