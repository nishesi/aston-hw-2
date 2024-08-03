package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.servletrestservice.dto.student.NewStudentForm;
import ru.astondevs.servletrestservice.dto.student.StudentDto;
import ru.astondevs.servletrestservice.dto.student.StudentWithCoordinatorAndCoursesDto;
import ru.astondevs.servletrestservice.dto.student.UpdateStudentForm;
import ru.astondevs.servletrestservice.service.StudentService;

import java.io.IOException;

@Slf4j
@WebServlet(urlPatterns = "/student/*")
public class StudentRestServlet extends BaseRestHttpServlet {
    private transient StudentService studentService;
    private ObjectMapper objectMapper;

    @Override
    public void init(ServletConfig config) {
        ServletContext context = config.getServletContext();
        studentService = (StudentService) context.getAttribute("studentService");
        objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getPathInfo() != null && !req.getPathInfo().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var form = objectMapper.readValue(req.getInputStream(), NewStudentForm.class);
        StudentDto student = studentService.create(form);
        objectMapper.writeValue(resp.getOutputStream(), student);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isCorrectRestPath(req, resp)) {
            long studentId = Long.parseLong(req.getPathInfo().substring(1));
            StudentWithCoordinatorAndCoursesDto student = studentService.get(studentId);
            objectMapper.writeValue(resp.getOutputStream(), student);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isCorrectRestPath(req, resp)) {
            var form = objectMapper.readValue(req.getInputStream(), UpdateStudentForm.class);
            StudentDto student = studentService.update(form);
            objectMapper.writeValue(resp.getOutputStream(), student);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isCorrectRestPath(req, resp)) {
            long studentId = Long.parseLong(req.getPathInfo().substring(1));
            studentService.delete(studentId);
        }
    }
}
