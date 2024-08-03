package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.exception.ServiceException;

import java.io.IOException;

import static jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION;

@Slf4j
@WebServlet("/exception")
public class ExceptionHandlerServlet extends HttpServlet {
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Exception exception = (Exception) req.getAttribute(ERROR_EXCEPTION);
        resp.setContentType("application/json");
        switch (exception) {
            case ServiceException ex -> {
                resp.setStatus(ex.getCode());
                objectMapper.writeValue(resp.getOutputStream(), new ExceptionDto(ex.getCode(), ex.getMessage()));
            }
            case DatabindException ex -> {
                resp.setStatus(400);
                objectMapper.writeValue(resp.getOutputStream(), new ExceptionDto(400, ex.getMessage()));
            }
            case IllegalArgumentException ex -> {
                resp.setStatus(400);
                objectMapper.writeValue(resp.getOutputStream(), new ExceptionDto(400, ex.getMessage()));
            }
            case null, default -> {
                log.error("Internal error", exception);
                resp.setStatus(500);
                objectMapper.writeValue(resp.getOutputStream(), new ExceptionDto(500, "Internal Server Error"));
            }
        }
    }

    record ExceptionDto(int code, String message) {
    }
}
