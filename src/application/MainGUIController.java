package application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class MainGUIController {
	@FXML private Label lbProyect;
	@FXML private Label lbTitle;
	@FXML private Label lbSubtitle;
	@FXML private Button btSolicitarServicio;
	@FXML private Button btVerSaldo;
	@FXML private Button btActualizarEstudent;
	@FXML private Button btVerPedidos;


//	@FXML
//	private void initialize() {
//		//ServerSocketOrder.runServer();
//	}
	// Event Listener on Button[#btSolicitarServicio].onAction
	@FXML
	public void solicitarServicio(ActionEvent event) {
		Logic.closeWindows(btSolicitarServicio,"/presentation/ServiceViewGUI.fxml");
	}

	// Event Listener on Button[#btVerSaldo].onAction
	@FXML
	public void verSaldo(ActionEvent event) {

	}
	// Event Listener on Button[#btAgregarEstudiante].onAction
	@FXML
	public void updateStudent(ActionEvent event) {
		Logic.closeWindows(btActualizarEstudent,"/presentation/UpdateStudentGUI.fxml");
	}
	// Event Listener on Button[#btVerEstudiante].onAction
	@FXML
	public void verPerdidos(ActionEvent event) {

	}
}
