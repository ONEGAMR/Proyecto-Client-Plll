package application;

import data.LogicSockect;
import data.SocketClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class LogInController {
    @FXML public Label message;
    @FXML private TextField tfIp;
    @FXML private TextField tfId;
    @FXML private TextField tfPassword;
    @FXML private Button btEnter;
    @FXML private Label btnText;
    @FXML private ProgressIndicator btnSpinner;

    private String beforeIp;
    private boolean isProcessing = false;

    @FXML
    public void initialize() {
        LogicSockect.imageCount = 0;
        btnSpinner.setMaxSize(30, 30);
        btnText.setTextFill(Color.WHITE);
        LogicSockect.setActiveController(this);
    }

    @FXML
    public void btEnter(ActionEvent event) {
        if (isProcessing) return;
        if (!validateInputs()) return;
        showLoadingState();
        startLoginProcess();
    }

    private boolean validateInputs() {
        if (hasIpChanged()) {
            updateIpFieldStyle(false);
            SocketClient.disconnectFromServer();
        }

        if (tfIp.getText().isEmpty()) {
            Logic.notifyAction("No puede estar vacio el IP", message, Color.RED);
            return false;
        }

        if (tfId.getText().isEmpty() || tfPassword.getText().isEmpty()) {
            Logic.notifyAction("No pueden haber campos vacios", message, Color.RED);
            return false;
        }

        return true;
    }

    private void startLoginProcess() {
        Logic.sleepThread();
        new Thread(() -> {
            try {
                if (SocketClient.isConnected && !hasIpChanged()) {
                    handleLoginAttempt();
                } else if (!SocketClient.isConnected) {
                    handleNewConnection();
                }
            } catch (Exception e) {
                handleError("Error de conexión");
            }
        }).start();
    }

    private void handleNewConnection() {
        if (SocketClient.connectToServer(tfIp.getText())) {
            beforeIp = tfIp.getText();
            updateIpFieldStyle(true);
            handleLoginAttempt();
        } else {
            handleError("No se pudo conectar al servidor");
        }
    }

    private void handleLoginAttempt() {
        try {
            SocketClient.reset();
            SocketClient.sendMessage("user," + tfId.getText() + "," + tfPassword.getText());
            Logic.sleepTrhead();

            String validate = LogicSockect.validateUser();
            if (validate != null) {
                handleError(validate);
            } else {
                notifySuccess();
                processValidUser();
            }
        } catch (Exception e) {
            handleError("Error en la validación");
        }
    }

    private void processValidUser() {
        try {
            LogicSockect.SleepListImages();
            Platform.runLater(() -> {
                hideLoadingState();
                SocketClient.closeWindows(btEnter, "/presentation/MainLayout.fxml");
            });
        } catch (Exception e) {
            handleError("Error al cargar recursos");
        }
    }

    private void showLoadingState() {
        Platform.runLater(() -> {
            btnText.setVisible(false);
            btnSpinner.setVisible(true);
            isProcessing = true;
        });
    }

    private void hideLoadingState() {
        Platform.runLater(() -> {
            btnSpinner.setVisible(false);
            btnText.setVisible(true);
            isProcessing = false;
        });
    }

    private void handleError(String message) {
        Platform.runLater(() -> {
            Logic.notifyAction(message, this.message, Color.RED);
            hideLoadingState();
        });
    }

    private void notifySuccess() {
        Platform.runLater(() ->
                Logic.notifyAction("Recibiendo recursos...", message, Color.GREEN)
        );
    }

    private boolean hasIpChanged() {
        return beforeIp != null && !beforeIp.equals(tfIp.getText());
    }

    private void updateIpFieldStyle(boolean isSuccess) {
        Platform.runLater(() -> {
            tfIp.getStyleClass().remove(isSuccess ? "input-field" : "input-field-green");
            tfIp.getStyleClass().add(isSuccess ? "input-field-green" : "input-field");
        });
    }
}