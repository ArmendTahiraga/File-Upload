package com.fileupload;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ContractController implements Initializable {
    @FXML
    private Label contractNameLabel;

    @FXML
    private DatePicker expirationDatePicker;

    @FXML
    private Label helloLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private VBox uploadContractModal;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private TableView<Contract> contractsTable;

    @FXML
    private TableColumn<Contract, String> fileNameTableColumn;

    @FXML
    private TableColumn<Contract, LocalDate> expirationDateTableColumn;

    @FXML
    private TableColumn<Contract, Void> openButtonTableColumn;

    @FXML
    private TableColumn<Contract, Void> deleteButtonTableColumn;

    private File selectedContract;
    private ObservableList<Contract> contractsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        helloLabel.setText("Hello, " + UserSession.getCurrentUser().getUsername() + "!");

        contractsTable.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        List<Contract> contracts = DatabaseManager.getContracts(UserSession.getCurrentUser().getId());
        contractsList.setAll(contracts);

        fileNameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFile().getName().split(".pdf")[0]));
        expirationDateTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));

        openButtonTableColumn.setCellFactory(column -> new TableCell<Contract, Void>() {
            private Button openButton = new Button();

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (openButton.getGraphic() == null) {
                        ImageView openIcon = new ImageView(getClass().getResource("/icons/open.png").toExternalForm());
                        openIcon.setFitWidth(20);
                        openIcon.setFitHeight(20);

                        openButton.setGraphic(openIcon);
                        openButton.setStyle("-fx-background-color: white; -fx-border-width: 0;");
                        openButton.setOnAction(event -> {
                            Contract contract = getTableView().getItems().get(getIndex());
                            openContract(contract);
                        });
                    }

                    setGraphic(openButton);
                }
            }
        });

        deleteButtonTableColumn.setCellFactory(column -> new TableCell<Contract, Void>() {
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
                            Contract contract = getTableView().getItems().get(getIndex());
                            deleteContract(contract);
                        });
                    }

                    setGraphic(deleteButton);
                }
            }
        });

        contractsTable.setItems(contractsList);
    }

    private void openContract(Contract contract) {
        try {
            Desktop.getDesktop().open(contract.getFile());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void deleteContract(Contract contract) {
        boolean hasBeenDeleted = DatabaseManager.deleteContract(contract);

        if (hasBeenDeleted) {
            contractsList.remove(contract);
        }
    }

    @FXML
    void logout() {
        UserSession.logout((Stage) logoutButton.getScene().getWindow());
    }

    @FXML
    void openUploadContractModal() {
        uploadContractModal.setVisible(true);
    }

    @FXML
    void chooseContract() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Contract");

        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files", "*.pdf");
        fileChooser.getExtensionFilters().add(pdfFilter);
        selectedContract = fileChooser.showOpenDialog(null);

        if (selectedContract != null) {
            contractNameLabel.setText(selectedContract.getName());
        } else {
            errorMessageLabel.setText("No Contract Selected");
        }
    }

    @FXML
    void uploadContract() {
        if (selectedContract == null && expirationDatePicker.getValue() == null) {
            errorMessageLabel.setText("Contract and expiration date are required.");
            return;
        }

        Contract contract = new Contract(UserSession.getCurrentUser().getId(), selectedContract, expirationDatePicker.getValue());
        contract = DatabaseManager.uploadContract(contract);
        if (contract != null) {
            uploadContractModal.setVisible(false);
            selectedContract = null;
            contractNameLabel.setText("");
            expirationDatePicker.setValue(null);

            contractsList.add(contract);

            String contractName = contract.getFile().getName().split(".pdf")[0];
            scheduleDesktopNotification(contract.getDate().minusMonths(1), "Your " + contractName + " contract is expiring in one month!");
            scheduleDesktopNotification(contract.getDate().minusWeeks(1), "Your " + contractName + " contract is expiring in one week!");
        } else {
            errorMessageLabel.setText("Failed to upload contract.");
        }
    }

    private void scheduleDesktopNotification(LocalDate date, String message) {
        long delay = Duration.between(LocalDate.now().atStartOfDay(), date.atStartOfDay()).toMillis();

        if (delay > 0) {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.schedule(() -> {
                try {
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        showNotificationWindows(message);
                    } else if (os.contains("mac")) {
                        showNotificationMac(message);
                    }
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void showNotificationWindows(String message) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon trayIcon = new TrayIcon(null, "Contract Reminder");
            trayIcon.setToolTip("Contract Notification");
            tray.add(trayIcon);
            trayIcon.displayMessage("Contract Reminder", message, TrayIcon.MessageType.INFO);

            Thread.sleep(10000);
            tray.remove(trayIcon);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void showNotificationMac(String message) {
        try {
            String[] script = {"osascript", "-e", "display notification \"" + message + "\" with title \"Contract Reminder\""};

            Runtime.getRuntime().exec(script);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    void closeModal() {
        uploadContractModal.setVisible(false);
        selectedContract = null;
        contractNameLabel.setText("");
        expirationDatePicker.setValue(null);
    }
}
