package application;

import data.LogicSockect;
import data.SocketClient;
import domain.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;

public class Logic {
    public static User user = new User();

    public static int parseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    public static void notifyAction(String message, Label noti, Color color) {
        noti.setText(message);
        noti.setTextFill(color);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> noti.setText("")));
        timeline.setCycleCount(1);
        timeline.play();
    }

    public static boolean showConfirmationAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType buttonTypeYes = new ButtonType("Sí");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        return alert.showAndWait().orElse(buttonTypeNo).equals(buttonTypeYes);
    }

    public static void sleepTrhead() {
        while (SocketClient.validate == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sleepThread() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepTConfirm() {
        int retries = 0;
        try {
            while (!LogicSockect.confirm() && retries < 10) {
                Thread.sleep(100);
                retries++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}