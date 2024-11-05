package application;

import data.SocketClient;
import data.LogicSockect;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DisconnectedDialogController {
    @FXML private Label statusLabel;

    @FXML
    private void handleLogout() {
        SocketClient.disconnectFromServer();
        Logic.user = null;
        LogicSockect.clearMeals();
        LogicSockect.resetSizeList();
        SocketClient.reset();
        SocketClient.closeWindows(statusLabel, "/presentation/LogInGUI.fxml");
    }
}