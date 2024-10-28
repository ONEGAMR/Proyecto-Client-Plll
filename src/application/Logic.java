package application;

import data.LogicSockect;
import data.SocketClient;
import domain.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class Logic {

	public static User user;
	
	public static void notifyAction(String message, Label noti, Color color) {
		noti.setText(message);
		noti.setTextFill(color);
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> noti.setText("")));
		timeline.setCycleCount(1);
		timeline.play();
	}

	// Muestra una alerta de confirmación con el mensaje y título especificados
	public static boolean showConfirmationAlert(String message, String title) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		ButtonType buttonTypeYes = new ButtonType("Sí");
		ButtonType buttonTypeNo = new ButtonType("No");
		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

		ButtonType result = alert.showAndWait().orElse(buttonTypeNo);
		return result.equals(buttonTypeYes);
	}

	public static void sleepTrhead(){
		// Esperar hasta que 'validate' se actualice
		while(SocketClient.validate == 0) {
			// Puedes agregar un pequeño retraso para evitar el uso excesivo de CPU
			try {
				Thread.sleep(100);  // espera 100ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void sleepTConfirm(){
		int retries = 0;
		try {
			// Esperar hasta 1 segundo para asegurarse de que us_confirm se actualice
			while (!LogicSockect.confirm() && retries < 10) {
				Thread.sleep(100);  // Esperar 100 ms
				retries++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void sleepTList(String typeList){
		int retries = 0;
		ArrayList<?> list = new ArrayList<>();

		if(typeList.equals("meals")){

			list = LogicSockect.meals;
		}
		if(typeList.equals("recharges")){

			list = LogicSockect.recharges;
		}

		try {
			// Esperar hasta 1 segundo para asegurarse de que us_confirm se actualice
			while (list.isEmpty() && retries < 2) {

				Thread.sleep(100);  // Esperar 100 ms
				retries++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Verifica si el saldo está dentro del rango permitido
	public static boolean isValidBalance(double balance, double minBalance, double maxBalance) {
		return balance >= minBalance && balance <= maxBalance;
	}

	// Analiza un texto para convertirlo a un entero, o devuelve un valor predeterminado si no es válido
	public static int parseInteger(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	// Analiza un texto para convertirlo a un número decimal, o devuelve un valor predeterminado si no es válido
	public static double parseDouble(String text) {
		try {
			return Double.parseDouble(text);
		} catch (NumberFormatException e) {
			return -1.0;
		}
	}
	// Muestra una alerta con el tipo, título y mensaje especificados
	public static void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
