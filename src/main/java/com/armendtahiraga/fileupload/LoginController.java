package com.armendtahiraga.fileupload;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    void login(ActionEvent event) {
        if (usernameTextField.getText().isEmpty() || passwordPasswordField.getText().isEmpty()) {
            errorMessageLabel.setText("Username and Password are required");
        }

    }
}