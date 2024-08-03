package ru.astondevs.servletrestservice.exception;

public class DataConsistencyException extends DaoException {
    public DataConsistencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
