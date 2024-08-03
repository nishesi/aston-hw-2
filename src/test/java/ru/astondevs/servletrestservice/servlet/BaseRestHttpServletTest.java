package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.astondevs.servletrestservice.exception.ServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        when(req.getMethod()).thenReturn("GET");
    }

    @Nested
    class service_method_test {

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

    @Nested
    class isCorrectRestPath_method_test {
        static List<String> paths() {
            List<String> paths = new ArrayList<>();
            paths.add(null);
            paths.add("");
            paths.add("/");
            return paths;
        }

        @ParameterizedTest
        @MethodSource("paths")
        void should_return_405_in_path_like_that(String path) throws IOException {
            when(req.getPathInfo()).thenReturn(path);
            boolean result = servlet.isCorrectRestPath(req, resp);

            verify(resp).setStatus(405);
            assertThat(result, is(false));
        }

        @Test
        void should_return_404_and_false_if_more_than_one_path_segment() throws IOException {
            when(req.getPathInfo()).thenReturn("/some/path");
            boolean result = servlet.isCorrectRestPath(req, resp);

            verify(resp).setStatus(404);
            assertThat(result, is(false));
        }

        @Test
        void should_return_true_if_correct_path() {
            when(req.getPathInfo()).thenReturn("/1234123");
            boolean result = servlet.isCorrectRestPath(req, resp);

            assertThat(result, is(true));
        }
    }
}