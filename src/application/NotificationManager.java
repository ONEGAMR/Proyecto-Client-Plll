package application;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class NotificationManager {
    private static final double NOTIFICATION_TOP_ANCHOR = 15.0;
    private static final double NOTIFICATION_RIGHT_ANCHOR = 15.0;
    private static final double NOTIFICATION_REMOVAL_THRESHOLD = -100.0;

    public static void showNotificationInAllWindows(String title) {
        Platform.runLater(() ->
                WindowManager.getNotificationEnabledStages()
                        .forEach(stage -> showNotificationInWindow(title, stage))
        );
    }

    private static void showNotificationInWindow(String title, Stage stage) {
        try {
            Pane root = (Pane) stage.getScene().getRoot();
            FXMLLoader loader = new FXMLLoader(NotificationManager.class.getResource("/presentation/notificationCard.fxml"));
            AnchorPane notification = loader.load();
            NotificationCardController controller = loader.getController();

            setupController(controller, root);
            setupNotificationAnchors(notification);

            root.getChildren().add(notification);
            controller.setNotification(title);

            setupRemovalListener(notification, root, controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupController(NotificationCardController controller, Pane root) {
        if (root.getUserData() instanceof MainLayoutController) {
            controller.setMainLayoutController((MainLayoutController) root.getUserData());
        }
    }

    private static void setupNotificationAnchors(AnchorPane notification) {
        AnchorPane.setTopAnchor(notification, NOTIFICATION_TOP_ANCHOR);
        AnchorPane.setRightAnchor(notification, NOTIFICATION_RIGHT_ANCHOR);
    }

    private static void setupRemovalListener(AnchorPane notification, Pane root, NotificationCardController controller) {
        controller.getNotificationContainer().translateYProperty().addListener((obs, old, newVal) -> {
            if (newVal.doubleValue() <= NOTIFICATION_REMOVAL_THRESHOLD) {
                root.getChildren().remove(notification);
            }
        });
    }
}