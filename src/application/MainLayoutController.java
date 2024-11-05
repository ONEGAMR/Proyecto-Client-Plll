// MainLayoutController.java
package application;

import data.LogicSockect;
import data.SocketClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainLayoutController extends Application {
    @FXML private Button btOpenOrders;
    @FXML private Button btUpdateUser;
    @FXML private Button btOpenBalance;
    @FXML private Button btServiceRequest;
    @FXML private StackPane contentArea;

    @Override
    public void start(Stage primaryStage) throws Exception {
        WindowManager.registerStage(primaryStage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/LogInGUI.fxml"));
        Parent root = loader.load();
        root.setUserData(this);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @FXML
    public void initialize() {
        contentArea.getParent().setUserData(this);
        setActiveButton(btServiceRequest);
        loadView("/presentation/ServiceRequestGUI.fxml");
    }

    @FXML
    void handleServiceRequest() {
        setActiveButton(btServiceRequest);
        loadView("/presentation/ServiceRequestGUI.fxml");
    }

    @FXML
    private void handleOpenBalance() {
        setActiveButton(btOpenBalance);
        loadView("/presentation/BalanceGUI.fxml");
    }

    @FXML
    private void handleUpdateUser() {
        setActiveButton(btUpdateUser);
        loadView("/presentation/UpdateProfileGUI.fxml");
    }

    @FXML
    private void handleOpenOrders() {
        setActiveButton(btOpenOrders);
        loadView("/presentation/ShowOrders.fxml");
    }

    @FXML
    private void handleLogOut() {
        SocketClient.disconnectFromServer();
        Logic.user = null;
        LogicSockect.clearMeals();
        LogicSockect.resetSizeList();
        SocketClient.reset();
        SocketClient.closeWindows(contentArea, "/presentation/LogInGUI.fxml");
    }

    private void setActiveButton(Button activeButton) {
        btServiceRequest.getStyleClass().remove("active");
        btOpenBalance.getStyleClass().remove("active");
        btUpdateUser.getStyleClass().remove("active");
        btOpenOrders.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }

    void loadView(String fxml) {
        try {
            if (fxml.equals("/presentation/ServiceRequestGUI.fxml")) {
                ServiceViewGUIController.resetState();
            }
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}