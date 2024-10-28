package application;
	
import data.SocketClient;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

//40252

public class Main extends Application {

	@FXML private Label lbProyect;
	@FXML private Label lbTitle;
	@FXML private Label lbSubtitle;
	@FXML private Button btSolicitarServicio;
	@FXML private Button btVerSaldo;
	@FXML private Button btActualizarEstudent;
	@FXML private Button btVerPedidos;
//pro
	@Override
	public void start(Stage primaryStage) {
		try {

			WindowManager.registerStage(primaryStage);

			Parent root = FXMLLoader.load(getClass().getResource("/presentation/LogInGUI.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	// Event Listener on Button[#btSolicitarServicio].onAction
	@FXML
	public void solicitarServicio(ActionEvent event) {
		SocketClient.closeWindows(btSolicitarServicio, "/presentation/ServiceRequestGUI.fxml");
	}

	// Event Listener on Button[#btVerSaldo].onAction
	@FXML
	public void verSaldo(ActionEvent event) {
		SocketClient.closeWindows(btVerSaldo, "/presentation/BalanceGUI.fxml");
	}
	// Event Listener on Button[#btAgregarEstudiante].onAction
	@FXML
	public void updateStudent(ActionEvent event) {
		SocketClient.closeWindows(btActualizarEstudent, "/presentation/UpdateProfileGUI.fxml");
	}
	// Event Listener on Button[#btVerEstudiante].onAction
	@FXML
	public void verPerdidos(ActionEvent event) {

		SocketClient.closeWindows(btVerPedidos,"/presentation/ShowOrders.fxml");
	}

}
