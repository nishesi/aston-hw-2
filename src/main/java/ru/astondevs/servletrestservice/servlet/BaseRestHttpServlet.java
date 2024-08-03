package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.DatabindException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class BaseRestHttpServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setContentType("application/json");
            super.service(req, resp);

        } catch (DatabindException ex) {
            throw new ServletException(ex);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    protected boolean isCorrectRestPath(HttpServletRequest req, HttpServletResponse resp) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return false;
        }
        if (pathInfo.lastIndexOf("/") > 0) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }
        return true;
    }
}
