package application;

import data.LogicSockect;
import data.SocketClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.io.File;

public class UpdateProfileController {

    @FXML private TextField tfStudentID;
    @FXML private TextField tfName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfPassword;
    @FXML private TextField tfRoutePhoto;
    @FXML private Button btnSelectPhoto;

    @FXML private CheckBox cbIsActive;
    @FXML private TextField tfAvailableMoney;
    @FXML private Label lb_ErrorMessage;
    @FXML private Button btReturn;
    @FXML private Button btEdit;
    @FXML private Button getBtnSelectPhoto;

    @FXML
    public void handleSelectPhoto(ActionEvent event) {
        if (btnSelectPhoto == null) {
            lb_ErrorMessage.setText("Button for selecting photo is not initialized.");
            lb_ErrorMessage.setTextFill(Color.WHITE);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(btnSelectPhoto.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String filePath = selectedFile.getAbsolutePath();
                tfRoutePhoto.setText(filePath);
                Logic.notifyAction("Profile picture path updated successfully!", lb_ErrorMessage, Color.GREEN);
            } catch (Exception e) {
                Logic.notifyAction("Could not set the selected image path. Please try again.", lb_ErrorMessage, Color.WHITE);
            }
        }
    }

    @FXML
    public void handleReturnAction(ActionEvent event) {
        SocketClient.closeWindows(btReturn,"/presentation/MainGUI.fxml");
    }

    @FXML
    public void handleEditAction(ActionEvent event) {
        String errorMessage = validateForm();
        if (errorMessage != null) {
            Logic.notifyAction(errorMessage, lb_ErrorMessage, Color.WHITE);
            return;
        }

        int phone = Logic.parseInteger(tfPhone.getText());
        double availableMoney = Logic.parseDouble(tfAvailableMoney.getText());

        if (phone == -1) {
            Logic.notifyAction("Número de teléfono inválido", lb_ErrorMessage, Color.WHITE);
            return;
        }
        if (availableMoney == -1.0) {
            Logic.notifyAction("Cantidad de dinero disponible inválida", lb_ErrorMessage,Color.WHITE);
            return;
        }

        // Actualiza el objeto estudiante existente
        Logic.user.setNombre(tfName.getText().isEmpty() ? null : tfName.getText());
        Logic.user.setCorreoElectronico(tfEmail.getText() == null || tfEmail.getText().isEmpty() ? null : tfEmail.getText());
        Logic.user.setTelefono(phone);
        Logic.user.setEstaActivo(cbIsActive.isSelected());
        Logic.user.setDineroDisponible(availableMoney);
        Logic.user.setPassword(tfPassword.getText());
        Logic.user.setRoutePhoto(tfRoutePhoto.getText());

        // Confirma la acción antes de guardar
        if (Logic.showConfirmationAlert("¿Estás seguro de que deseas guardar los cambios?", "Confirmación")) {
            SocketClient.sendMessage("updateUser,"+Logic.user.toStringUserData());

            Logic.sleepTConfirm();

            if (LogicSockect.confirm()) {
                Logic.notifyAction("Estudiante actualizado con éxito", lb_ErrorMessage, Color.GREEN);
                SocketClient.closeWindows(btReturn,"/presentation/MainGUI.fxml");
            } else {
                Logic.notifyAction("Error al actualizar el estudiante", lb_ErrorMessage, Color.WHITE);
            }
        } else {
            Logic.notifyAction("Operación cancelada", lb_ErrorMessage, Color.ORANGE);
        }
    }

    // Valida los campos del formulario
    private String validateForm() {
        if (tfStudentID.getText().trim().isEmpty()) return "El ID no puede estar vacío";
        if (tfPhone.getText().trim().isEmpty()) return "El número de teléfono no puede estar vacío";
        if (tfAvailableMoney.getText().trim().isEmpty()) return "La cantidad de dinero disponible no puede estar vacía";
        if (!tfPhone.getText().matches("\\d{8,10}")) return "El número de teléfono debe tener entre 8 y 10 dígitos";
        if(tfPassword.getText().trim().isEmpty()) return "La contraseña no puede estar vacía";
        if(tfRoutePhoto.getText().trim().isEmpty()) return "Sin ruta de la foto";
        if(tfPassword.getText().length() > 45) return "La contraseña no puede ser mayor a 45 caracteres";
        try {
            double money = Double.parseDouble(tfAvailableMoney.getText());

            if (money < 1000) {
                return "La cantidad de dinero disponible debe ser mayor a 1000";
            }
        } catch (NumberFormatException e) {
            return "La cantidad de dinero disponible debe ser un número válido";
        }

        return null;
    }

    @FXML
    private void initialize() {
        if (Logic.user != null) {
            tfStudentID.setText(Logic.user.getCarnet());
            tfName.setText(Logic.user.getNombre());
            tfEmail.setText(Logic.user.getCorreoElectronico());
            tfPhone.setText(String.valueOf(Logic.user.getTelefono()));
            cbIsActive.setSelected(Logic.user.isEstaActivo());
            tfAvailableMoney.setText(String.valueOf(Logic.user.getDineroDisponible()));
            tfPassword.setText(Logic.user.getPassword());
            tfRoutePhoto.setText(Logic.user.getRoutePhoto());
        } else {
            lb_ErrorMessage.setText("No hay datos de estudiante disponibles.");
        }
    }
}