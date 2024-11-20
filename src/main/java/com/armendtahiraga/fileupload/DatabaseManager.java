package com.armendtahiraga.fileupload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private String url;
    private String username;
    private String password;
    private Connection connection;

    public void createConnection() {
        if (connection == null) {
            return;
        }

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkConnection() {
        if (connection == null) {
            createConnection();
        }
    }
}
