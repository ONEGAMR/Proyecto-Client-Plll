package application;

import data.LogicSockect;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import data.SocketClient;
import javafx.event.ActionEvent;

public class GUIController {
	@FXML
	private TextField tfIp;
	@FXML
	private TextField tfId;
	@FXML
	private TextField tfPassword;
	@FXML
	private Button btEnter;
	@FXML
	private Label message;
	

	// Event Listener on Button[#btEnter].onAction
	@FXML
	public void initialize() {
		SocketClient.connectToServer("localhost");
	}
	
	@FXML
	public void btEnter(ActionEvent event) {

		if (!tfId.getText().isEmpty() && !tfPassword.getText().isEmpty()) {

			SocketClient.reset();
			// Enviar el mensaje al servidor
			SocketClient.sendMessage("user,"+ tfId.getText()+ ","+ tfPassword.getText());

			Logic.sleepTrhead();

			// Una vez que se actualice el valor, continúa la validación
			String validate = LogicSockect.validateUser();

			if (validate != null) {

				Logic.notifyAction(LogicSockect.validateUser(), message);
			}
		}else{
			Logic.notifyAction("No pueden haber campos vacios", message);
		}
	}

}
