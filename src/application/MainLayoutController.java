package application;

import data.LogicSockect;
import data.SocketClient;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainLayoutController extends Application {
    @FXML private StackPane contentArea;

    @Override
    public void start(Stage primaryStage) {
        try {

            WindowManager.registerStage(primaryStage);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/LogInGUI.fxml"));
            Parent root = loader.load();
            root.setUserData(this);
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        contentArea.getParent().setUserData(this);
        loadView("/presentation/ServiceRequestGUI.fxml");
    }

    @FXML
    private void handleServiceRequest() {
        loadView("/presentation/ServiceRequestGUI.fxml");
    }

    @FXML
    private void handleOpenBalance() {
        loadView("/presentation/BalanceGUI.fxml");
    }

    @FXML
    private void handleUpdateUser() {
        loadView("/presentation/UpdateProfileGUI.fxml");
    }

    @FXML
    private void handleOpenOrders() {
        loadView("/presentation/ShowOrders.fxml");
    }

    @FXML
    private void handleLogOut() {
        // Desconectar del servidor
        SocketClient.disconnectFromServer();

        // Limpiar los datos del usuario actual
        Logic.user = null;

        // Resetear variables estáticas relevantes
        LogicSockect.clearMeals();
        LogicSockect.resetSizeList();
        SocketClient.reset();

        // Cerrar la ventana actual y abrir la pantalla de login
        SocketClient.closeWindows(contentArea, "/presentation/LogInGUI.fxml");
    }

    public void loadView(String fxml) {
        try {
            if (fxml.equals("/presentation/ServiceRequestGUI.fxml")) {
                ServiceViewGUIController.resetState();
            }

            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}