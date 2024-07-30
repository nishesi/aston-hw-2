package ru.astondevs.servletrestservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        ObjectMapper objectMapper = objectMapper();
        context.setAttribute("objectMapper", objectMapper);
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
