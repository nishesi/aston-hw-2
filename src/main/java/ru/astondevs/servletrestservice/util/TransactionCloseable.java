package ru.astondevs.servletrestservice.util;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public class TransactionCloseable implements AutoCloseable {

    private final Connection connection;

    @Override
    public void close() throws SQLException {
        if (!connection.getAutoCommit()) {
            connection.rollback();
            connection.setAutoCommit(true);
        }
    }
}
