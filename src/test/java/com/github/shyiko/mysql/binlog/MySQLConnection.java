package com.github.shyiko.mysql.binlog;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Representation of a MySQL connection.
 */
public final class MySQLConnection implements Closeable {

    public final String hostname;
    public final int port;
    public final String username;
    public final String password;
    public Connection connection;

    public MySQLConnection(String hostname, int port, String username, String password)
        throws ClassNotFoundException, SQLException {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        Class.forName("com.mysql.jdbc.Driver");
        connect();
    }

    private void connect() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port +
            "?serverTimezone=UTC", username, password);
        execute(new BinaryLogClientIntegrationTest.Callback<Statement>() {

            @Override
            public void execute(Statement statement) throws SQLException {
                statement.execute("SET time_zone = '+00:00'");
            }
        });
    }

    public String hostname() {
        return hostname;
    }

    public int port() {
        return port;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public void execute(BinaryLogClientIntegrationTest.Callback<Statement> callback, boolean autocommit) throws SQLException {
        connection.setAutoCommit(autocommit);
        Statement statement = connection.createStatement();
        try {
            callback.execute(statement);
            if ( !autocommit ) {
                connection.commit();
            }
        } finally {
            statement.close();
        }
    }

    public void execute(BinaryLogClientIntegrationTest.Callback<Statement> callback) throws SQLException {
        execute(callback, false);
    }

    public void execute(final String... statements) throws SQLException {
        execute(new BinaryLogClientIntegrationTest.Callback<Statement>() {
            @Override
            public void execute(Statement statement) throws SQLException {
                for ( String command : statements ) {
                    statement.execute(command);
                }
            }
        });
    }

    public void query(String sql, BinaryLogClientIntegrationTest.Callback<ResultSet> callback) throws SQLException {
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        try {
            ResultSet rs = statement.executeQuery(sql);
            try {
                callback.execute(rs);
                connection.commit();
            } finally {
                rs.close();
            }
        } finally {
            statement.close();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch ( SQLException e ) {
            throw new IOException(e);
        }
    }

    public void reconnect() throws IOException, SQLException {
        close();
        connect();
    }
}
