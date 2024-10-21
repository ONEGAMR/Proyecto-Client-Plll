package application;

import data.SocketClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Logic {

	public static void closeWindows(Button button, String window) {
	    FXMLLoader loader = new FXMLLoader(Logic.class.getResource(window));
	    try {
	        Parent root = loader.load();
	        Scene scene = new Scene(root);
	        Stage stage = new Stage();
	        stage.setScene(scene);
	        stage.show();
	        Stage temp = (Stage) button.getScene().getWindow();
	        temp.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void notifyAction(String message,Label noti) {
		noti.setText(message);
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> noti.setText("")));
		timeline.setCycleCount(1);
		timeline.play();
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

}
