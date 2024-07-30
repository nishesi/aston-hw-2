package ru.astondevs.servletrestservice.util;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCloseable extends AutoCloseable {
    @Override
    void close() throws SQLException;
}
