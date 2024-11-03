package application;

import data.LogicSockect;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ProgressIndicator;
import data.SocketClient;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;

public class LogInController {
	@FXML private TextField tfIp;
	@FXML private TextField tfId;
	@FXML private TextField tfPassword;
	@FXML private Button btEnter;
	@FXML private Label message;
	@FXML private StackPane rootPane;
	@FXML private Label btnText;
	@FXML private ProgressIndicator btnSpinner;

	private String beforeIp;
	private ProgressIndicator progressIndicator;
	private String originalButtonText;

	@FXML
	public void initialize() {
		LogicSockect.imageCount = 0;
		btnSpinner.setMaxSize(30, 30); // Increased size
		btnText.setTextFill(Color.WHITE); // Set text color to white
	}

	private void showLoadingState() {
		Platform.runLater(() -> {
			btnText.setVisible(false);
			btnSpinner.setVisible(true);
			// Removed btEnter.setDisable(true);
		});
	}

	private void hideLoadingState() {
		Platform.runLater(() -> {
			btnSpinner.setVisible(false);
			btnText.setVisible(true);
			btEnter.setDisable(false);
		});
	}

	private void createLoadingIndicator() {
		progressIndicator = new ProgressIndicator();
		progressIndicator.setMaxSize(20, 20);
		progressIndicator.setStyle("-fx-progress-color: white;");
	}

	@FXML
	public void btEnter(ActionEvent event) {
		if (beforeIp != null && !beforeIp.equals(tfIp.getText())) {
			tfIp.getStyleClass().remove("input-field-green");
			tfIp.getStyleClass().add("input-field");
			SocketClient.disconnectFromServer();
		}

		if (tfIp.getText().isEmpty()) {
			Logic.notifyAction("No puede estar vacio el IP", message, Color.RED);
			return;
		}

		if (tfId.getText().isEmpty() || tfPassword.getText().isEmpty()) {
			Logic.notifyAction("No pueden haber campos vacios", message, Color.RED);
			return;
		}

		showLoadingState();

		new Thread(() -> {
			try {
				if (SocketClient.isConnected && beforeIp != null && beforeIp.equals(tfIp.getText())) {
					handleLoginAttempt();
				} else if (!SocketClient.isConnected) {
					if (SocketClient.connectToServer(tfIp.getText())) {
						beforeIp = tfIp.getText();
						Platform.runLater(() -> {
							tfIp.getStyleClass().remove("input-field");
							tfIp.getStyleClass().add("input-field-green");
						});
						handleLoginAttempt();
					} else {
						Platform.runLater(() -> {
							Logic.notifyAction("No se pudo conectar al servidor", message, Color.RED);
							hideLoadingState();
						});
					}
				}
			} catch (Exception e) {
				Platform.runLater(() -> {
					Logic.notifyAction("Error de conexión", message, Color.RED);
					hideLoadingState();
				});
			}
		}).start();
	}

	private void handleLoginAttempt() {
		try {
			SocketClient.reset();
			SocketClient.sendMessage("user," + tfId.getText() + "," + tfPassword.getText());
			Logic.sleepTrhead();

			String validate = LogicSockect.validateUser();
			if (validate != null) {
				Platform.runLater(() -> {
					Logic.notifyAction(validate, message, Color.RED);
					hideLoadingState();
				});
			} else {
				processValidUser();
			}
		} catch (Exception e) {
			Platform.runLater(() -> {
				Logic.notifyAction("Error en la validación", message, Color.RED);
				hideLoadingState();
			});
		}
	}

	private void processValidUser() {
		try {
			LogicSockect.SleepListImages();
			Platform.runLater(() -> {
				message.setTextFill(Color.GREEN);
				message.setText("Recibiendo imágenes del servidor...");
				hideLoadingState();
				SocketClient.closeWindows(btEnter, "/presentation/MainLayout.fxml");
			});
		} catch (Exception e) {
			Platform.runLater(() -> {
				hideLoadingState();
				Logic.notifyAction("Error al cargar recursos", message, Color.RED);
			});
		}
	}
}