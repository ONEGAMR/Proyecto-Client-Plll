package application;

import data.LogicSockect;
import data.SocketClient;
import domain.Recharge;
import domain.Student;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.List;

public class BalanceController {

    @FXML private TableView<Recharge> tvRecharges;
    @FXML private TableColumn<Recharge, String> tcStudentID;
    @FXML private TableColumn<Recharge, String> tcStudentName;
    @FXML private TableColumn<Recharge, String> tcRechargeDate;
    @FXML private TableColumn<Recharge, Double> tcAmount;
    @FXML private Label lbEmptyTableMessage;
    @FXML private Button btReturn;

    // Inicializa las columnas de la tabla
    @FXML
    private void initialize() {

        setupTableColumns();
        setTvRecharges();
    }

    public void setTvRecharges(){

        //se limpia y se envia mensaje solicitando una lista con elementos
        LogicSockect.recharges.clear();
        SocketClient.sendMessage("listRecharge,"+ Logic.user.getCarnet());

        //Espera a que la lista este cargada
        Logic.sleepTList("recharges");

        //para sincronizar la tabla con el hilo
        Platform.runLater(() -> {

            //llena la tabla si esta tiene elementos
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

    //inicia las columnas
    private void setupTableColumns() {
        tcRechargeDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateTb()));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("monto"));
    }

    // Muestra un mensaje de error en la etiqueta
    private void notifyError(String message) {
        lbEmptyTableMessage.setText(message);
        lbEmptyTableMessage.setTextFill(Color.RED);
    }

    // Maneja la acción del botón de volver
    @FXML
    public void handleReturnAction() {
        SocketClient.closeWindows(btReturn,"/presentation/MainGUI.fxml");
    }
}
