package application;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class NotificationCardController {
    @FXML public Label descriptionLabel;
    @FXML private AnchorPane notificationContainer;
    @FXML private Button closeButton;
    @FXML private Button contentButton;

    private MainLayoutController mainLayoutController;

    public void initialize() {
        closeButton.setOnAction(event -> {
            closeNotification();
            event.consume();
        });

        contentButton.setOnAction(event -> {
            handleNotificationClick();
            event.consume();
        });
    }

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    public void setNotification(String title) {
        descriptionLabel.setText(title);
        showNotification();
    }

    private void closeNotification() {
        hideNotification();
    }

    private void handleNotificationClick() {
        // Cerrar la notificación
        closeNotification();

        // Obtener el MainLayoutController de la ventana principal
        if (mainLayoutController == null) {
            // Si no tenemos referencia directa, podemos buscarla
            StackPane contentArea = (StackPane) contentButton.getScene().lookup("#contentArea");
            if (contentArea != null) {
                mainLayoutController = (MainLayoutController) contentArea.getParent().getUserData();
            }
        }

        // Cargar la vista de Orders
        if (mainLayoutController != null) {
            mainLayoutController.loadView("/presentation/ShowOrders.fxml");
        }
    }

    private void showNotification() {
        // Reset position
        notificationContainer.setTranslateY(-100);
        notificationContainer.setOpacity(0);

        // Slide in and fade in animation
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), notificationContainer);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), notificationContainer);
        fadeIn.setToValue(1);

        slideIn.play();
        fadeIn.play();
    }

    private void hideNotification() {
        TranslateTransition slideOut = new TranslateTransition(Duration.seconds(0.5), notificationContainer);
        slideOut.setToY(-100);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), notificationContainer);
        fadeOut.setToValue(0);

        slideOut.play();
        fadeOut.play();
    }

    public AnchorPane getNotificationContainer() {
        return notificationContainer;
    }
}