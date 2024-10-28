package application;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class NotificationManager {

    public static void showNotificationInAllWindows(String title) {
        Platform.runLater(() -> {
            for (Stage stage : WindowManager.getNotificationEnabledStages()) {
                showNotificationInWindow(title, stage);
            }
        });
    }

    private static void showNotificationInWindow(String title, Stage stage) {
        try {
            Pane root = (Pane) stage.getScene().getRoot();
            FXMLLoader loader = new FXMLLoader(NotificationManager.class.getResource("/presentation/notificationCard.fxml"));
            AnchorPane notification = loader.load();
            NotificationCardController controller = loader.getController();

            AnchorPane.setTopAnchor(notification, 15.0);
            AnchorPane.setRightAnchor(notification, 15.0);

            root.getChildren().add(notification);
            controller.setNotification(title);

            controller.getNotificationContainer().translateYProperty().addListener((obs, old, newVal) -> {
                if (newVal.doubleValue() <= -100) {
                    root.getChildren().remove(notification);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}