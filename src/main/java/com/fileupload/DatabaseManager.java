package com.fileupload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
            System.setProperty("javax.net.ssl.trustStore", "src/main/resources/supabase-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "pass_ca_supabase$");
            connection = DriverManager.getConnection("jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?user=postgres.rssumxdyrdqeksxnabao&password=72DHGEKpgrdrATxb&ssl=true&sslmode=verify-full&sslrootcert=src/main/resources/prod-ca-2021.crt");
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
            String select = "select * from \"User\"";
            PreparedStatement preparedStatement = connection.prepareStatement(select);
            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                Role role;

                if (Objects.equals(result.getString("Role"), "admin")) {
                    role = Role.ADMIN;
                } else {
                    role = Role.USER;
                }

                User user = new User(result.getInt("Id"), result.getString("Username"), result.getString("Password"), role);
                users.add(user);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return users;
    }

    public static Contract uploadContract(Contract contract) {
        checkConnection();

        try {
            String insert = "insert into \"Contract\" (\"UserId\", \"FileName\", \"File\", \"ExpDate\") values (?, ?, ?, ?) returning \"Id\"";
            PreparedStatement preparedStatement = connection.prepareStatement(insert);
            FileInputStream fileInputStream = new FileInputStream(contract.getFile());

            preparedStatement.setInt(1, contract.getUserId());
            preparedStatement.setString(2, contract.getFile().getName());
            preparedStatement.setBytes(3, fileInputStream.readAllBytes());
            preparedStatement.setDate(4, Date.valueOf(contract.getDate()));

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                contract.setId(resultSet.getInt("Id"));
                return contract;
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return null;
    }

    public static List<Contract> getContracts(int userId) {
        checkConnection();
        List<Contract> contracts = new ArrayList<>();

        try {
            String select = "select * from \"Contract\" where \"UserId\" = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(select);
            preparedStatement.setInt(1, userId);
            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                byte[] fileData = result.getBytes("File");
                String fileName = result.getString("FileName");
                File file = new File(fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(fileData);

                Contract contract = new Contract(result.getInt("Id"), result.getInt("UserId"), file, result.getDate("ExpDate").toLocalDate());
                contracts.add(contract);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return contracts;
    }

    public static boolean deleteContract(Contract contract) {
        checkConnection();

        try {
            String select = "delete from \"Contract\" where \"Id\" = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(select);
            preparedStatement.setInt(1, contract.getId());
            int rowsDeleted = preparedStatement.executeUpdate();

            if (rowsDeleted > 0) {
                return true;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return false;
    }

    public static boolean deleteUser(User user) {
        checkConnection();

        try {
            String select = "delete from \"User\" where \"Id\" = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(select);
            preparedStatement.setInt(1, user.getId());
            int rowsDeleted = preparedStatement.executeUpdate();

            if (rowsDeleted > 0) {
                return true;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return false;
    }

    public static User addUser(User user) {
        checkConnection();

        try {
            String insert = "insert into \"User\" (\"Username\", \"Password\", \"Role\") values (?, ?, ?) returning \"Id\"";
            PreparedStatement preparedStatement = connection.prepareStatement(insert);

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getRole() == Role.ADMIN ? "admin" : "user");

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user.setId(resultSet.getInt("Id"));
                return user;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return null;
    }

    public static boolean updateUser(int id, String username, String password, Role role) {
        checkConnection();

        try {
            String update = "update \"User\" set \"Username\" = ?, \"Password\" = ?, \"Role\" = ? where \"Id\" = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(update);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, role == Role.ADMIN ? "admin" : "user");
            preparedStatement.setInt(4, id);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                return true;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }

        return false;
    }
}
