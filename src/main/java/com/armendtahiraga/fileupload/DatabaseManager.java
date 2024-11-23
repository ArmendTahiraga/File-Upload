package com.armendtahiraga.fileupload;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseManager {
    private static Connection connection;

    public static void createConnection() {
        if (connection != null) {
            return;
        }

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?user=postgres.rssumxdyrdqeksxnabao&password=72DHGEKpgrdrATxb");
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void checkConnection() {
        if (connection == null) {
            createConnection();
        }
    }

    public static List<User> getUsers() {
        checkConnection();
        List<User> users = new ArrayList<>();

        try {
            String select = "SELECT * FROM \"User\"";
            PreparedStatement preparedStatement = connection.prepareStatement(select);
            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                Role role;

                if(Objects.equals(result.getString("Role"), "admin")) {
                    role = Role.ADMIN;
                } else {
                    role = Role.USER;
                }

                User user = new User(result.getInt("Id"), result.getString("Username"), result.getString("Password"), role);
                users.add(user);
            }
        } catch (SQLException exception){
            throw new RuntimeException(exception);
        }

        return users;
    }
}
