package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.astondevs.servletrestservice.exception.ServiceException;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.sameInstance;

class BaseRestHttpServletTest {

    static BaseRestHttpServlet servlet;
    static HttpServletRequest req;
    static HttpServletResponse resp;

    @BeforeAll
    static void setUp() {
        servlet = new BaseRestHttpServlet();

        req = Mockito.mock(HttpServletRequest.class);
        resp = Mockito.mock(HttpServletResponse.class);
    }

    @BeforeEach
    void init() {
        Mockito.reset(req, resp);

        Mockito.when(req.getMethod()).thenReturn("GET");
    }

    @Test
    void should_throw_Servlet_exception_if_any_runtime_exception_thrown() {
        List<RuntimeException> exceptions = List.of(new IllegalArgumentException(),
                new RuntimeException(),
                new NoSuchElementException(),
                new ServiceException(0, null, null));

        for (RuntimeException exception : exceptions) {
            var servletChild = new BaseRestHttpServlet() {
                @Override
                protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
                    throw exception;
                }
            };


            Assertions.assertThrows(ServletException.class, () -> servletChild.service(req, resp));
            try {
                servletChild.service(req, resp);
            } catch (Exception e) {
                assertThat(e.getCause(), allOf(
                        sameInstance(exception)
                ));
            }
        }
    }

    @Test
    void should_rethrow_IOException() {
        IOException exception = new IOException();

        var servletChild = new BaseRestHttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                throw exception;
            }
        };

        Assertions.assertThrows(IOException.class, () -> servletChild.service(req, resp));
    }


    @Test
    void should_throw_ServletException_if_DatabindException_thrown() {
        DatabindException exception = new JsonMappingException(null);

        var servletChild = new BaseRestHttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                throw exception;
            }
        };

        Assertions.assertThrows(ServletException.class, () -> servletChild.service(req, resp));

        try {
            servletChild.service(req, resp);
        } catch (Exception e) {
            assertThat(e.getCause(), allOf(
                    sameInstance(exception)
            ));
        }
    }
}