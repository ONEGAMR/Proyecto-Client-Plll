package application;


import data.LogicSockect;
import data.SocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

import domain.Meal;
import java.util.List;

public class ServiceViewGUIController {

    @FXML private ComboBox<String> cbReservationDay;
    @FXML private RadioButton rbBreakfast;
    @FXML private ToggleGroup mealTimeGroup;
    @FXML private RadioButton rbLunch;
    @FXML private TableView<Meal> tvMeals;
    @FXML private TableColumn<Meal, String> colMealName;
    @FXML private TableColumn<Meal, Integer> colPrice;
    @FXML private TableColumn<Meal, Button> colRequest;
    @FXML private Label lbEmptyTable;
    @FXML private Button btReturn;

    private String menuAnterior = "";
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupRadioButtons();
        setupTableColumns();
        setupListeners();
    }

    // Configura los ComboBox
    private void setupComboBoxes() {
        cbReservationDay.setItems(FXCollections.observableArrayList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"));
    }

    // Configura los RadioButton
    private void setupRadioButtons() {
        rbBreakfast.setUserData("breakfast");
        rbLunch.setUserData("lunch");
    }

    // Configura las columnas de la tabla
    private void setupTableColumns() {
        colMealName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : "₡" + price);
            }
        });
        colRequest.setCellFactory(tc -> new TableCell<>() {
            final Button btn = new Button("Solicitar Comida");
            {
                btn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black; -fx-font-weight: normal; -fx-background-radius: 4;"
                        + " -fx-pref-height: 20; -fx-pref-width: 120;");
                btn.setOnAction(event -> {
                    Meal meal = getTableView().getItems().get(getIndex());
                    handleMealRequest(meal);
                });
            }

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // Configura los escuchadores de eventos
    private void setupListeners() {
        cbReservationDay.setOnAction(e -> updateTable());
        mealTimeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateTable());
    }

    // Actualiza la tabla de comidas según los filtros seleccionados
    private void updateTable() {
        String selectedDay = cbReservationDay.getValue();
        String selectedMealType = mealTimeGroup.getSelectedToggle() != null ?
                (String) mealTimeGroup.getSelectedToggle().getUserData() : null;


        if (selectedDay == null || selectedMealType == null) {
            tvMeals.setItems(FXCollections.observableArrayList());
            lbEmptyTable.setVisible(true);
            return;
        }

        //Enviar el dia y tipo de comida y recibir las comidas y solicitar las comidas
        String nuevoMenu = selectedDay + " - " + selectedMealType;

        // Verificar si el nuevo menú es diferente al anterior
        if (!nuevoMenu.equals(menuAnterior)) {
            System.out.println("Nuevo menú seleccionado: " + nuevoMenu);
            LogicSockect.meals.clear();
            menuAnterior = nuevoMenu; // Actualizar el menú anterior
        }

        SocketClient.sendMessage("listMeals,"+selectedDay+","+selectedMealType);

        Logic.sleepTList("meals");

        Platform.runLater(() -> {

            if (LogicSockect.getListMeals() != null) {
                tvMeals.getItems().clear();
                List<Meal> meals = (List<Meal>) LogicSockect.getListMeals();
                tvMeals.setItems(FXCollections.observableArrayList(meals));
                lbEmptyTable.setVisible(meals.isEmpty());
            } else {
                tvMeals.setItems(FXCollections.observableArrayList());
                lbEmptyTable.setVisible(true);
            }
        });
    }

    // Maneja la solicitud de una comida
    private void handleMealRequest(Meal meal) {

        double availableMoney = Logic.user.getDineroDisponible();
        int mealPrice = meal.getPrice();

        if (availableMoney < mealPrice) {
            Logic.showAlert(AlertType.INFORMATION, "Saldo Insuficiente", "No tienes suficiente dinero para solicitar esta comida.");
            return;
        }

        boolean isConfirmed = Logic.showConfirmationAlert(
                "¿Deseas solicitar esta comida?",
                "Confirmación de Compra"

        );

        if (isConfirmed) {
            Logic.user.setDineroDisponible(availableMoney - mealPrice);


            //enviar datos del pedido y el nuevo saldo del usuario
            meal.setCantidad(1);
            meal.setTotalOrder(1*meal.getPrice());

            SocketClient.sendMessage("foodOrder,"+meal.toStringPedido()+","+Logic.user.getCarnet()+","+Logic.user.getDineroDisponible());
            Logic.showAlert(AlertType.INFORMATION, "Pedido Confirmado", "Pedido realizado con éxito. Nuevo saldo: ₡" + Logic.user.getDineroDisponible());
        }
    }

    @FXML
    public void handleReturnAction(ActionEvent event) {
        Logic.closeWindows(btReturn,"/presentation/MainGUI.fxml");
    }

}
