package ru.astondevs.servletrestservice.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final int code;
    public ServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
