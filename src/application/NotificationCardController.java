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
    @FXML private AnchorPane notificationContainer;
    @FXML private Label descriptionLabel;
    @FXML private Button closeButton;
    @FXML private Button contentButton;

    private MainLayoutController mainLayoutController;
    private static final Duration ANIMATION_DURATION = Duration.seconds(0.5);
    private static final double HIDDEN_POSITION = -100;
    private static final double VISIBLE_POSITION = 0;

    public void initialize() {
        closeButton.setOnAction(event -> {
            hideNotification();
            event.consume();
        });

        contentButton.setOnAction(event -> {
            handleContentButtonClick();
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

    public AnchorPane getNotificationContainer() {
        return notificationContainer;
    }

    private void handleContentButtonClick() {
        hideNotification();
        loadOrdersView();
    }

    private void loadOrdersView() {
        if (mainLayoutController == null) {
            StackPane contentArea = (StackPane) contentButton.getScene().lookup("#contentArea");
            if (contentArea != null) {
                mainLayoutController = (MainLayoutController) contentArea.getParent().getUserData();
            }
        }

        if (mainLayoutController != null) {
            mainLayoutController.loadView("/presentation/ShowOrders.fxml");
        }
    }

    private void showNotification() {
        notificationContainer.setTranslateY(HIDDEN_POSITION);
        notificationContainer.setOpacity(0);

        TranslateTransition slideIn = createTranslateTransition(VISIBLE_POSITION);
        FadeTransition fadeIn = createFadeTransition(1);

        slideIn.play();
        fadeIn.play();
    }

    private void hideNotification() {
        TranslateTransition slideOut = createTranslateTransition(HIDDEN_POSITION);
        FadeTransition fadeOut = createFadeTransition(0);

        slideOut.play();
        fadeOut.play();
    }

    private TranslateTransition createTranslateTransition(double toY) {
        TranslateTransition transition = new TranslateTransition(ANIMATION_DURATION, notificationContainer);
        transition.setToY(toY);
        return transition;
    }

    private FadeTransition createFadeTransition(double toValue) {
        FadeTransition transition = new FadeTransition(ANIMATION_DURATION, notificationContainer);
        transition.setToValue(toValue);
        return transition;
    }
}