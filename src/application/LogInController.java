package application;

import data.LogicSockect;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import data.SocketClient;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;

public class LogInController {
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


	//SocketClient.connectToServer("192.168.0.143");
	//"192.168.0.143"


	@FXML
	public void btEnter(ActionEvent event) {
		if(SocketClient.isConnected){

			Logic.notifyAction("conectado", message, Color.RED);
			if (!tfId.getText().isEmpty() && !tfPassword.getText().isEmpty()) {

					SocketClient.reset();
					// Enviar el mensaje al servidor
					SocketClient.sendMessage("user," + tfId.getText() + "," + tfPassword.getText());

					Logic.sleepTrhead();
					// Una vez que se actualice el valor, continúa la validación , se valida si estan bien los datos
					String validate = LogicSockect.validateUser();

					if (validate != null) {
						Logic.notifyAction(LogicSockect.validateUser(), message, Color.RED);
					} else {

						SocketClient.closeWindows(btEnter, "/presentation/MainGUI.fxml");
					}

			}else{
				Logic.notifyAction("No pueden haber campos vacios", message, Color.RED);
			}


		}else if(tfIp.getText().isEmpty()){
			Logic.notifyAction("No puede estar vacio el IP", message, Color.RED);

			//se valida que no esten empty los campos
		} else if (!tfId.getText().isEmpty() && !tfPassword.getText().isEmpty()) {

			boolean cn = SocketClient.connectToServer(tfIp.getText());


			if (cn) {
				tfIp.setDisable(true);
				tfIp.getStyleClass().remove("input-field");
				tfIp.getStyleClass().add("input-field-green");
				SocketClient.reset();
				// Enviar el mensaje al servidor
				SocketClient.sendMessage("user," + tfId.getText() + "," + tfPassword.getText());


				Logic.sleepTrhead();
				// Una vez que se actualice el valor, continúa la validación , se valida si estan bien los datos
				String validate = LogicSockect.validateUser();

				if (validate != null) {
					Logic.notifyAction(LogicSockect.validateUser(), message, Color.RED);
				} else {

					SocketClient.closeWindows(btEnter, "/presentation/MainGUI.fxml");
				}
			} else {
				Logic.notifyAction("No se pudo conectar al servidor", message, Color.RED);
				return;
			}

		}else{
			Logic.notifyAction("No pueden haber campos vacios", message, Color.RED);
		}
	}


}
