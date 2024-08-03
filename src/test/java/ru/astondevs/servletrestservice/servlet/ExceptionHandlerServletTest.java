package ru.astondevs.servletrestservice.servlet;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.astondevs.servletrestservice.exception.ServiceException;
import ru.astondevs.servletrestservice.servlet.ExceptionHandlerServlet.ExceptionDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

import static jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExceptionHandlerServletTest {
    static ExceptionHandlerServlet servlet;
    static ObjectMapper objectMapper;
    static HttpServletRequest req;
    static HttpServletResponse resp;
    static ByteArrayOutputStream out;
    Random random = new Random();

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        ServletConfig config = Mockito.mock(ServletConfig.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);
        when(context.getAttribute("objectMapper")).thenReturn(objectMapper);

        servlet = new ExceptionHandlerServlet();
        servlet.init(config);

        req = Mockito.mock(HttpServletRequest.class);
        resp = Mockito.mock(HttpServletResponse.class);
    }

    @BeforeEach
    void setUp() throws IOException {
        Mockito.reset(req, resp);
        out = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new TestServletOutputStream(out));
    }

    @Test
    void should_return_data_from_ServiceException() throws IOException {

        int code = random.nextInt(100, 600);
        String mess = UUID.randomUUID().toString();
        when(req.getAttribute(ERROR_EXCEPTION)).thenReturn(new ServiceException(code, mess, null));
        ExceptionDto dto = new ExceptionDto(code, mess);
        servlet.service(req, resp);

        var in = new ByteArrayInputStream(out.toByteArray());
        var result = objectMapper.readValue(in, ExceptionDto.class);

        assertThat(result, is(dto));
    }

    @Test
    void should_set_application_json_format() throws IOException {
        servlet.service(req, resp);

        verify(resp).setContentType("application/json");
    }

    @Test
    void should_return_400_and_message_on_DatabindException() throws IOException {
        when(req.getAttribute(ERROR_EXCEPTION)).thenReturn(new JsonMappingException("some message"));
        ExceptionDto dto = new ExceptionDto(400, "some message");

        servlet.service(req, resp);

        var in = new ByteArrayInputStream(out.toByteArray());
        var result = objectMapper.readValue(in, ExceptionDto.class);

        verify(resp).setStatus(400);
        assertThat(result, is(dto));
    }

    @Test
    void should_return_400_and_message_on_IllegalArgumentException() throws IOException {
        when(req.getAttribute(ERROR_EXCEPTION)).thenReturn(new IllegalArgumentException("some message"));
        ExceptionDto dto = new ExceptionDto(400, "some message");

        servlet.service(req, resp);

        var in = new ByteArrayInputStream(out.toByteArray());
        var result = objectMapper.readValue(in, ExceptionDto.class);

        verify(resp).setStatus(400);
        assertThat(result, is(dto));
    }

    @Test
    void should_return_500_by_default_on_any_unexpectedException() throws IOException {
        when(req.getAttribute(ERROR_EXCEPTION)).thenReturn(new Exception("some message"));
        ExceptionDto dto = new ExceptionDto(500, "Internal Server Error");

        servlet.service(req, resp);

        var in = new ByteArrayInputStream(out.toByteArray());
        var result = objectMapper.readValue(in, ExceptionDto.class);

        verify(resp).setStatus(500);
        assertThat(result, is(dto));
    }

    static class TestServletOutputStream extends ServletOutputStream {

        private final OutputStream out;

        public TestServletOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }
    }
}