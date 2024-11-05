package application;

import data.LogicSockect;
import data.SocketClient;
import domain.Recharge;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    @FXML
    private void initialize() {
        setupTableColumns();
        setTvRecharges();
    }

    private void setupTableColumns() {
        tcRechargeDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateTb()));
        tcAmount.setCellValueFactory(new PropertyValueFactory<>("monto"));
    }

    public void setTvRecharges() {
        LogicSockect.recharges.clear();
        LogicSockect.resetSizeList();
        SocketClient.sendMessage("listRecharge," + Logic.user.getCarnet());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (LogicSockect.listSize > 0) {
            int maxAttempts = 10;
            int currentAttempt = 0;
            while (LogicSockect.getListRecharges().isEmpty() && currentAttempt < maxAttempts) {
                try {
                    Thread.sleep(10);
                    currentAttempt++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        LogicSockect.SleepListRecharges();
        Platform.runLater(() -> {
            if (LogicSockect.getListRecharges() != null && !LogicSockect.getListRecharges().isEmpty()) {
                tvRecharges.getItems().clear();
                List<Recharge> recharges = (List<Recharge>) LogicSockect.getListRecharges();
                tvRecharges.setItems(FXCollections.observableArrayList(recharges));
            } else {
                notifyError("No tiene recargas registradas");
                tvRecharges.setItems(FXCollections.observableArrayList());
                lbEmptyTableMessage.setVisible(true);
            }
        });
    }

    private void notifyError(String message) {
        lbEmptyTableMessage.setText(message);
        lbEmptyTableMessage.setTextFill(Color.RED);
    }
}