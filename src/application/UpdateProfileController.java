package application;

import data.LogicSockect;
import data.SocketClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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

    private static final double MIN_MONEY_AMOUNT = 1000.0;
    private static final int MAX_PASSWORD_LENGTH = 45;
    private static final String PHONE_PATTERN = "\\d{8,10}";
    private static final String DECIMAL_PATTERN = "^\\d*\\.?\\d+$";

    @FXML
    private void initialize() {
        loadUserData();
    }

    @FXML
    public void handleEditAction(ActionEvent event) {
        String validationError = validateForm();
        if (validationError != null) {
            Logic.notifyAction(validationError, lb_ErrorMessage, Color.RED);
            return;
        }

        updateUserData();

        if (Logic.showConfirmationAlert("¿Estás seguro de que deseas guardar los cambios?", "Confirmación")) {
            saveUserData();
        } else {
            Logic.notifyAction("Operación cancelada", lb_ErrorMessage, Color.ORANGE);
        }
    }

    @FXML
    public void handleSelectPhoto(ActionEvent event) {
        selectPhoto();
    }

    @FXML
    public void handleSelectPhotoTF(MouseEvent actionEvent) {
        selectPhoto();
    }

    private void selectPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(btnSelectPhoto.getScene().getWindow());
        if (selectedFile != null) {
            tfRoutePhoto.setText(selectedFile.getAbsolutePath());
        }
    }

    private void loadUserData() {
        if (Logic.user == null) {
            lb_ErrorMessage.setText("No hay datos de estudiante disponibles.");
            return;
        }

        tfStudentID.setText(Logic.user.getCarnet());
        tfName.setText(Logic.user.getNombre());
        tfEmail.setText(Logic.user.getCorreoElectronico());
        tfPhone.setText(String.valueOf(Logic.user.getTelefono()));
        cbIsActive.setSelected(Logic.user.isEstaActivo());
        tfAvailableMoney.setText(String.valueOf(Logic.user.getDineroDisponible()));
        tfPassword.setText(Logic.user.getPassword());
        tfRoutePhoto.setText(Logic.user.getRoutePhoto());
    }

    private String validateForm() {
        // Validar campos requeridos
        if (isFieldEmpty(tfStudentID)) return "El ID no puede estar vacío";
        if (isFieldEmpty(tfPhone)) return "El número de teléfono no puede estar vacío";
        if (isFieldEmpty(tfAvailableMoney)) return "La cantidad de dinero disponible no puede estar vacía";
        if (isFieldEmpty(tfPassword)) return "La contraseña no puede estar vacía";
        if (isFieldEmpty(tfRoutePhoto)) return "Sin ruta de la foto";

        // Validar longitud de contraseña
        if (tfPassword.getText().length() > MAX_PASSWORD_LENGTH) {
            return "La contraseña no puede ser mayor a " + MAX_PASSWORD_LENGTH + " caracteres";
        }

        // Validar formato del teléfono
        String phone = tfPhone.getText().trim();
        if (!phone.matches(PHONE_PATTERN)) {
            return "El número de teléfono debe tener entre 8 y 10 dígitos";
        }

        // Validar monto de dinero
        String moneyStr = tfAvailableMoney.getText().trim();
        if (!moneyStr.matches(DECIMAL_PATTERN)) {
            return "La cantidad de dinero disponible debe ser un número válido";
        }

        try {
            double money = Double.parseDouble(moneyStr);
            if (money < MIN_MONEY_AMOUNT) {
                return "La cantidad de dinero disponible debe ser mayor a " + MIN_MONEY_AMOUNT;
            }
        } catch (NumberFormatException e) {
            return "La cantidad de dinero disponible debe ser un número válido";
        }

        return null;
    }

    private boolean isFieldEmpty(TextField field) {
        return field.getText().trim().isEmpty();
    }

    private void updateUserData() {
        Logic.user.setNombre(tfName.getText().isEmpty() ? null : tfName.getText());
        Logic.user.setCorreoElectronico(tfEmail.getText().isEmpty() ? null : tfEmail.getText());
        Logic.user.setTelefono(Logic.parseInteger(tfPhone.getText()));
        Logic.user.setEstaActivo(cbIsActive.isSelected());
        Logic.user.setDineroDisponible(Logic.parseDouble(tfAvailableMoney.getText()));
        Logic.user.setPassword(tfPassword.getText());
        Logic.user.setRoutePhoto(tfRoutePhoto.getText());
    }

    private void saveUserData() {
        SocketClient.sendMessage("updateUser," + Logic.user.toStringUserData());
        Logic.sleepTConfirm();

        if (LogicSockect.confirm()) {
            Logic.notifyAction("Estudiante actualizado con éxito", lb_ErrorMessage, Color.GREEN);
        } else {
            Logic.notifyAction("Error al actualizar el estudiante", lb_ErrorMessage, Color.RED);
        }
    }
}