package com.armendtahiraga.fileupload;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UserSession {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        UserSession.currentUser = currentUser;
    }

    public static void logout(Stage stage) {
        currentUser = null;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1440, 1024);
            stage.setTitle("File Upload");

            stage.setScene(scene);
            stage.show();
        } catch (IOException exception){
            throw new RuntimeException(exception);
        }
    }
}
