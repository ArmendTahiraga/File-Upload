package com.fileupload;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.List;

public class LoginController {
    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private Button loginButton;

    @FXML
    void login() {
        if (usernameTextField.getText().isEmpty() || passwordPasswordField.getText().isEmpty()) {
            errorMessageLabel.setText("Username and Password are required.");
            return;
        }

        List<User> users = DatabaseManager.getUsers();

        for (User user : users) {
            if (BCrypt.checkpw(passwordPasswordField.getText(), user.getPassword()) && usernameTextField.getText().equals(user.getUsername())) {
                UserSession.setCurrentUser(user);

                try {
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene scene;

                    if (UserSession.getCurrentUser().getRole() == Role.ADMIN) {
                        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("admin.fxml"));
                        scene = new Scene(fxmlLoader.load(), 1440, 1024);
                        stage.setTitle("File Upload - Admin");
                    } else {
                        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("contract.fxml"));
                        scene = new Scene(fxmlLoader.load(), 1440, 1024);
                        stage.setTitle("File Upload - Contracts");
                    }

                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return;
            }
        }

        errorMessageLabel.setText("Invalid username or password.");
    }
}