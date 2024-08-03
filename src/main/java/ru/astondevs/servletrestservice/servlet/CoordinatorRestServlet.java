package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorDto;
import ru.astondevs.servletrestservice.dto.coordinator.CoordinatorWithStudentsDto;
import ru.astondevs.servletrestservice.dto.coordinator.NewCoordinatorForm;
import ru.astondevs.servletrestservice.dto.coordinator.UpdateCoordinatorForm;
import ru.astondevs.servletrestservice.service.CoordinatorService;

import java.io.IOException;

@Slf4j
@WebServlet(urlPatterns = "/coordinator/*")
public class CoordinatorRestServlet extends BaseRestHttpServlet {
    private transient CoordinatorService coordinatorService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        coordinatorService = (CoordinatorService) context.getAttribute("coordinatorService");
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!req.getPathInfo().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var form = objectMapper.readValue(req.getInputStream(), NewCoordinatorForm.class);
        CoordinatorDto coordinatorDto = coordinatorService.create(form);
        objectMapper.writeValue(resp.getOutputStream(), coordinatorDto);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo.lastIndexOf("/") > 0) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long coordinatorId = Long.parseLong(pathInfo.substring(1));
        CoordinatorWithStudentsDto coordinator = coordinatorService.get(coordinatorId);
        objectMapper.writeValue(resp.getOutputStream(), coordinator);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo.lastIndexOf("/") > 0) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var form = objectMapper.readValue(req.getInputStream(), UpdateCoordinatorForm.class);
        CoordinatorDto coordinator = coordinatorService.update(form);
        objectMapper.writeValue(resp.getOutputStream(), coordinator);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo.lastIndexOf("/") > 0) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long studentId = Long.parseLong(pathInfo.substring(1));
        coordinatorService.delete(studentId);
    }
}
