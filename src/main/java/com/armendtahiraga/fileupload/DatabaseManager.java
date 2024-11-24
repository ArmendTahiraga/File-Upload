package com.armendtahiraga.fileupload;

import java.io.ByteArrayInputStream;
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
            String select = "select * from \"User\"";
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
        } catch (Exception exception){
            throw new RuntimeException(exception);
        }

        return null;
    }

    public static List<Contract> getContracts() {
        checkConnection();
        List<Contract> contracts = new ArrayList<>();

        try {
            String select = "select * from \"Contract\"";
            PreparedStatement preparedStatement = connection.prepareStatement(select);
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
        } catch (Exception exception){
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
        } catch (SQLException exception){
            throw new RuntimeException(exception);
        }

        return false;
    }
}
