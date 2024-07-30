package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.astondevs.servletrestservice.exception.ServiceException;

import java.io.IOException;

import static jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION;

@WebServlet("/exception")
public class ExceptionHandler extends HttpServlet {
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Exception exception = (Exception) req.getAttribute(ERROR_EXCEPTION);
        resp.setContentType("application/json");

        if (exception instanceof ServiceException ex) {
            resp.setStatus(ex.getCode());
            objectMapper.writeValue(resp.getOutputStream(), new ExceptionDto(ex.getCode(), ex.getMessage()));
        }
    }

    record ExceptionDto(int code, String message) {
    }
}
