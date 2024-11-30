package com.fileupload;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminController implements Initializable {
    @FXML
    private VBox addUserModal;

    @FXML
    private TableColumn<User, Void> deleteButtonTableColumn;

    @FXML
    private VBox editUserModal;

    @FXML
    private Label errorMessageAddLabel;

    @FXML
    private Label errorMessageEditLabel;

    @FXML
    private Label helloLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private TableColumn<User, Void> editButtonTableColumn;

    @FXML
    private PasswordField passwordAddPasswordField;

    @FXML
    private PasswordField passwordEditPasswordField;

    @FXML
    private TableColumn<User, String> passwordTableColumn;

    @FXML
    private TextField usernameAddTextField;

    @FXML
    private TextField usernameEditTextField;

    @FXML
    private TableColumn<User, String> usernameTableColumn;

    @FXML
    private TableColumn<User, Role> roleTableColumn;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private ComboBox<Role> roleAddComboBox;

    @FXML
    private ComboBox<Role> roleEditComboBox;

    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private int editingUserIndex;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        helloLabel.setText("Hello, " + UserSession.getCurrentUser().getUsername() + "!");

        roleAddComboBox.getItems().addAll(Role.ADMIN);
        roleAddComboBox.getItems().addAll(Role.USER);

        roleEditComboBox.getItems().addAll(Role.ADMIN);
        roleEditComboBox.getItems().addAll(Role.USER);

        usersTable.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        List<User> users = DatabaseManager.getUsers();
        usersList.setAll(users);

        usernameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>("*".repeat(cellData.getValue().getPassword().length())));
        roleTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getRole()));

        editButtonTableColumn.setCellFactory(column -> new TableCell<User, Void>() {
            private Button openButton = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (openButton.getGraphic() == null) {
                        ImageView openIcon = new ImageView(getClass().getResource("/icons/editing.png").toExternalForm());
                        openIcon.setFitWidth(20);
                        openIcon.setFitHeight(20);

                        openButton.setGraphic(openIcon);
                        openButton.setStyle("-fx-background-color: white; -fx-border-width: 0;");
                        openButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            editUser(user);
                        });
                    }

                    setGraphic(openButton);
                }
            }
        });

        deleteButtonTableColumn.setCellFactory(column -> new TableCell<User, Void>() {
            private Button deleteButton = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (deleteButton.getGraphic() == null) {
                        ImageView deleteIcon = new ImageView(getClass().getResource("/icons/delete.png").toExternalForm());
                        deleteIcon.setFitWidth(20);
                        deleteIcon.setFitHeight(20);

                        deleteButton.setGraphic(deleteIcon);
                        deleteButton.setStyle("-fx-background-color: white; -fx-border-width: 0;");
                        deleteButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            deleteUser(user);
                        });
                    }

                    setGraphic(deleteButton);
                }
            }
        });

        usersTable.setItems(usersList);
    }

    private void deleteUser(User user) {
        boolean hasBeenDeleted = DatabaseManager.deleteUser(user);

        if (hasBeenDeleted) {
            usersList.remove(user);
        }
    }

    private void editUser(User user) {
        editUserModal.setVisible(true);
        usernameEditTextField.setText(user.getUsername());
        passwordEditPasswordField.setText("");
        roleEditComboBox.setValue(user.getRole());
        editingUserIndex = usersList.indexOf(user);
    }

    @FXML
    void logout() {
        UserSession.logout((Stage) logoutButton.getScene().getWindow());
    }

    @FXML
    void openAddUserModal() {
        addUserModal.setVisible(true);
    }

    @FXML
    void closeAddUserModal() {
        addUserModal.setVisible(false);
        usernameAddTextField.setText("");
        passwordAddPasswordField.setText("");
        roleAddComboBox.setValue(null);
    }

    @FXML
    void closeEditUserModal() {
        editUserModal.setVisible(false);
        usernameEditTextField.setText("");
        passwordEditPasswordField.setText("");
        roleEditComboBox.setValue(null);
        editingUserIndex = -1;
    }

    @FXML
    void addUser() {
        if (usernameAddTextField.getText().isEmpty() && passwordAddPasswordField.getText().isEmpty() && roleAddComboBox.getValue() == null) {
            errorMessageAddLabel.setText("Username, password and role are required.");
            return;
        }

        User user = new User(usernameAddTextField.getText(), BCrypt.hashpw(passwordAddPasswordField.getText(), BCrypt.gensalt()), roleAddComboBox.getValue());
        user = DatabaseManager.addUser(user);

        if (user != null) {
            addUserModal.setVisible(false);
            usernameAddTextField.setText("");
            passwordAddPasswordField.setText("");
            roleAddComboBox.setValue(null);

            usersList.add(user);
        } else {
            errorMessageAddLabel.setText("Failed to add user.");
        }
    }

    @FXML
    void updateUser() {
        if (usernameEditTextField.getText().isEmpty() && roleEditComboBox.getValue() == null) {
            errorMessageEditLabel.setText("Username and role are required.");
            return;
        }

        User currentEditingUser = usersList.get(editingUserIndex);

        String updatedPassword = currentEditingUser.getPassword();
        if (!passwordEditPasswordField.getText().isEmpty()) {
            updatedPassword = BCrypt.hashpw(passwordEditPasswordField.getText(), BCrypt.gensalt());
        }

        boolean success = DatabaseManager.updateUser(currentEditingUser.getId(), usernameEditTextField.getText(), updatedPassword, roleEditComboBox.getValue());

        if (success) {
            currentEditingUser.setUsername(usernameEditTextField.getText());
            currentEditingUser.setPassword(updatedPassword);
            currentEditingUser.setRole(roleEditComboBox.getValue());

            editingUserIndex = -1;
            editUserModal.setVisible(false);
            usernameEditTextField.setText("");
            passwordEditPasswordField.setText("");
            roleEditComboBox.setValue(null);

            usersTable.refresh();
        } else {
            errorMessageEditLabel.setText("Failed to update user.");
        }
    }
}
