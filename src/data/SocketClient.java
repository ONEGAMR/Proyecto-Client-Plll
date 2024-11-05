package data;

import application.Logic;
import application.NotificationManager;
import application.ServiceViewGUIController;
import application.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    private static final int PORT = 12345;
    private static String host;
    private static Socket socket;
    private static PrintWriter output;
    private static BufferedReader input;
    public static boolean isConnected = false;
    public static int validate;

    public static void reset() {
        validate = 0;
    }

    public static void setValidate(int validate) {
        SocketClient.validate = validate;
    }

    public static boolean connectToServer(String serverHost) {
        host = serverHost;
        new Thread(() -> {
            initConnection();
            receiveMessages();
        }).start();
        Logic.sleepThread();
        return isConnected;
    }

    private static void initConnection() {
        try {
            socket = new Socket(host, PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            isConnected = true;
        } catch (IOException e) {
            System.out.println("Error al conectar al servidor: " + e.getMessage());
        }
    }

    public static void disconnectFromServer() {
        try {
            isConnected = false;
            Logic.sleepThread();
            closeResources();
        } catch (IOException e) {
            System.out.println("Error al desconectar del servidor: " + e.getMessage());
        }
    }

    private static void closeResources() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
        if (input != null) {
            input.close();
            input = null;
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
            socket = null;
        }
    }

    public static void sendMessage(String message) {
        if (output != null) {
            output.println(message);
            output.flush();
        }
    }

    private synchronized static void receiveMessages() {
        while (isConnected) {
            try {
                if (!isConnectionValid()) break;

                String message = input.readLine();
                if (message == null) {
                    isConnected = false;
                    break;
                }

                processMessage(message);
            } catch (IOException e) {
                if (!isConnected) break;
                System.out.println("Servidor desconectado...");
                isConnected = false;
                break;
            }
        }

        handleDisconnection();
    }

    private static boolean isConnectionValid() {
        return input != null && socket != null && !socket.isClosed();
    }

    private static void processMessage(String message) {
        String firstWord = LogicSockect.separarPalabras(message).get(0);

        switch (firstWord) {
            case "user":
                handleUserMessage(message);
                break;
            case "us_confirm":
                LogicSockect.setUs_confirm(Boolean.parseBoolean(LogicSockect.separarPalabras(message).get(1)));
                break;
            case "listSize":
                LogicSockect.listSize = Integer.parseInt(LogicSockect.separarPalabras(message).get(1));
                break;
            case "listMeals":
                LogicSockect.setListMeals(message);
                break;
            case "listOrder":
                LogicSockect.setListMealsOrder(message);
                break;
            case "listRecharge":
                LogicSockect.setListRecharge(message);
                break;
            case "image":
            case "singleImage":
            case "imageCount":
                LogicSockect.handleImageTransfer(message);
                break;
            case "newBalance":
                updateBalance(message);
                break;
            case "notifyStatus":
                NotificationManager.showNotificationInAllWindows(message.split(",")[1]);
                break;
        }
    }

    private static void handleUserMessage(String message) {
        validate = Integer.parseInt(LogicSockect.separarPalabras(message).get(1));
        if (LogicSockect.separarPalabras(message).size() > 2) {
            LogicSockect.fullUser(message);
        }
    }

    private static void updateBalance(String message) {
        try {
            double newBalance = Double.parseDouble(message.split(",")[1]);
            Logic.user.setDineroDisponible(newBalance);
            ServiceViewGUIController.updateBalanceFromSocket(newBalance);
        } catch (Exception e) {
            System.out.println("Error al actualizar el balance: " + e.getMessage());
        }
    }

    private static void handleDisconnection() {
        if (!isConnected) {
            disconnectFromServer();
            Platform.runLater(() -> showDisconnectionDialog());
        }
    }

    public static void closeWindows(Node node, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(Logic.class.getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            WindowManager.registerStage(stage);

            if (path.equals("/presentation/MainLayout.fxml")) {
                WindowManager.enableNotificationsForStage(stage);
            }

            stage.show();
            ((Stage) node.getScene().getWindow()).close();
        } catch (Exception e) {
            System.out.println("Error al cerrar la ventana: " + e.getMessage());
        }
    }

    private static void showDisconnectionDialog() {
        WindowManager.getStages().forEach(stage -> {
            if (stage.getScene().getRoot() instanceof AnchorPane) {
                try {
                    AnchorPane overlay = createOverlay(stage);
                    AnchorPane dialogPane = loadDisconnectDialog();
                    centerDialog(stage, overlay, dialogPane);
                    addOverlayToStage(stage, overlay);
                } catch (IOException e) {
                    System.out.println("Error al mostrar el diálogo de desconexión: " + e.getMessage());
                }
            }
        });
    }

    private static AnchorPane createOverlay(Stage stage) {
        AnchorPane overlay = new AnchorPane();
        overlay.setPrefSize(stage.getScene().getWidth(), stage.getScene().getHeight());
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.prefWidthProperty().bind(stage.getScene().widthProperty());
        overlay.prefHeightProperty().bind(stage.getScene().heightProperty());
        return overlay;
    }

    private static AnchorPane loadDisconnectDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(Logic.class.getResource("/presentation/Disconnected.fxml"));
        return loader.load();
    }

    private static void centerDialog(Stage stage, AnchorPane overlay, AnchorPane dialogPane) {
        AnchorPane.setTopAnchor(dialogPane, (stage.getScene().getHeight() - dialogPane.getPrefHeight()) / 2);
        AnchorPane.setLeftAnchor(dialogPane, (stage.getScene().getWidth() - dialogPane.getPrefWidth()) / 2);
        overlay.getChildren().add(dialogPane);
    }

    private static void addOverlayToStage(Stage stage, AnchorPane overlay) {
        ((AnchorPane) stage.getScene().getRoot()).getChildren().add(overlay);
    }
}