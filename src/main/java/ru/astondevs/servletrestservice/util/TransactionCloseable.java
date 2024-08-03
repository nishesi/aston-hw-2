package ru.astondevs.servletrestservice.util;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionCloseable implements AutoCloseable {

    private final Connection connection;

    public TransactionCloseable(Connection connection) throws SQLException {
        this.connection = connection;
        connection.setAutoCommit(false);
    }

    @Override
    public void close() throws SQLException {
        if (!connection.getAutoCommit()) {
            connection.rollback();
            connection.setAutoCommit(true);
        }
    }
}
