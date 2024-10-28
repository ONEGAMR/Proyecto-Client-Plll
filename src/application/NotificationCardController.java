package application;

import data.SocketClient;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class NotificationCardController {
    @FXML public Label descriptionLabel;
    @FXML private AnchorPane notificationContainer;
    @FXML private Button closeButton;
    @FXML private Button contentButton;

    public void initialize() {
        // Configurar el botón de cierre
        closeButton.setOnAction(event -> {
            closeNotification();
            event.consume();
        });

        // Configurar el botón de contenido
        contentButton.setOnAction(event -> {
            handleNotificationClick();
            event.consume();
        });
    }

    public void setNotification(String title) {
        descriptionLabel.setText(title);

        showNotification();
    }

    private void closeNotification() {
        hideNotification();
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

    private void handleNotificationClick() {
        SocketClient.closeWindows(closeButton,"/presentation/ShowOrders.fxml");
    }

    public AnchorPane getNotificationContainer() {
        return notificationContainer;
    }
}