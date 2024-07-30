package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dto.course.CourseDto;
import ru.astondevs.servletrestservice.dto.course.CourseWithStudentsDto;
import ru.astondevs.servletrestservice.dto.course.NewCourseForm;
import ru.astondevs.servletrestservice.dto.course.UpdateCourseForm;
import ru.astondevs.servletrestservice.service.CourseService;

import java.io.IOException;

@Slf4j
@WebServlet(urlPatterns = "/course/*")
public class CourseServlet extends HttpServlet {
    private transient CourseService courseService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        courseService = (CourseService) context.getAttribute("courseService");
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (!req.getPathInfo().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            NewCourseForm form = objectMapper.readValue(req.getInputStream(), NewCourseForm.class);
            CourseDto course = courseService.createCourse(form);
            objectMapper.writeValue(resp.getOutputStream(), course);

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo.lastIndexOf("/") > 0) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            long courseId = Long.parseLong(pathInfo.substring(1));
            CourseWithStudentsDto course = courseService.getCourse(courseId);
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getOutputStream(), course);

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo.lastIndexOf("/") > 0) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            UpdateCourseForm form = objectMapper.readValue(req.getInputStream(), UpdateCourseForm.class);
            CourseDto course = courseService.updateCourse(form);
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getOutputStream(), course);

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo.lastIndexOf("/") > 0) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            long courseId = Long.parseLong(pathInfo.substring(1));
            courseService.deleteCourse(courseId);

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
