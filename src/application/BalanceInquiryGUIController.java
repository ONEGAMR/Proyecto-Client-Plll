package application;

import data.LogicSockect;
import data.SocketClient;
import domain.Meal;
import domain.Recharge;
import domain.Student;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BalanceInquiryGUIController {

    @FXML private TableView<Recharge> tvRecharges;
    @FXML private TableColumn<Recharge, String> tcStudentID;
    @FXML private TableColumn<Recharge, String> tcStudentName;
    @FXML private TableColumn<Recharge, String> tcRechargeDate;
    @FXML private TableColumn<Recharge, Double> tcAmount;
    @FXML private Label lbEmptyTableMessage;
    @FXML private Button btConsult;
    @FXML private Button btReturn;
    @FXML private Button btAddBalance;

    private Student currentStudent;

    // Inicializa las columnas de la tabla
    @FXML
    private void initialize() {

        setupTableColumns();
        setTvRecharges();
    }

    public void setTvRecharges(){

        LogicSockect.recharges.clear();
        SocketClient.sendMessage("listRecharge,"+ Logic.user.getCarnet());

        Logic.sleepTList("recharges");

        Platform.runLater(() -> {

            if (LogicSockect.getListRecharges() != null && !LogicSockect.getListRecharges().isEmpty()) {

                tvRecharges.getItems().clear();
                List<Recharge> recharges = (List<Recharge>) LogicSockect.getListRecharges();

                tvRecharges.setItems(FXCollections.observableArrayList(recharges));
                //lbEmptyTableMessage.setVisible(recharges.isEmpty());
            } else {
                notifyError("No tiene recargas registradas");
                tvRecharges.setItems(FXCollections.observableArrayList());
                lbEmptyTableMessage.setVisible(true);
            }
        });
    }

    private void setupTableColumns() {
        tcRechargeDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateTb()));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("monto"));
    }

    // Maneja la acción del botón de consultar saldo
    @FXML
    public void handleConsultBalance() {

    }

    // Muestra un mensaje de error en la etiqueta
    private void notifyError(String message) {
        lbEmptyTableMessage.setText(message);
        lbEmptyTableMessage.setTextFill(Color.RED);
    }

    // Maneja la acción del botón de volver
    @FXML
    public void handleReturnAction() {
        Logic.closeWindows(btReturn,"/presentation/MainGUI.fxml");
    }

    // Maneja la acción del botón de agregar saldo
    @FXML
    public void handleAddBalanceAction() {
       // Logic.closeCurrentWindowAndOpen("/presentation/AddBalanceGUI.fxml", (Stage) btAddBalance.getScene().getWindow());
    }
}
