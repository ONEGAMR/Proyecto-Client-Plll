package data;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Utils {

    private static final int DEFAULT_INTEGER_VALUE = -1;
    private static final double DEFAULT_DOUBLE_VALUE = -1.0;

    // Muestra una alerta de confirmación con el mensaje y título especificados
    public static boolean showConfirmationAlert(String message, String title) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType buttonTypeYes = new ButtonType("Sí");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        ButtonType result = alert.showAndWait().orElse(buttonTypeNo);
        return result.equals(buttonTypeYes);
    }

    // Verifica si alguno de los campos de texto está vacío
    public static boolean validateEmptyFields(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // Muestra un mensaje en una etiqueta y luego lo oculta después de un intervalo
    public static void notifyAction(Label label, String message, Color color) {
        label.setText(message);
        label.setTextFill(color);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> label.setText("")));
        timeline.setCycleCount(1);
        timeline.play();
    }

    // Analiza un texto para convertirlo a un entero, o devuelve un valor predeterminado si no es válido
    public static int parseInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return DEFAULT_INTEGER_VALUE;
        }
    }

    // Analiza un texto para convertirlo a un número decimal, o devuelve un valor predeterminado si no es válido
    public static double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return DEFAULT_DOUBLE_VALUE;
        }
    }

    // Muestra una alerta con el tipo, título y mensaje especificados
    public static void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
