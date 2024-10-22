package application;

import domain.Meal;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;

public class ServiceViewGUIController {

    @FXML
    private ComboBox<String> cbReservationDay;

    @FXML
    private ToggleGroup mealTimeGroup;

    @FXML
    private RadioButton rbBreakfast;

    @FXML
    private RadioButton rbLunch;

    @FXML
    private Label lbName;

    @FXML
    private TableView<Meal> tvMeals;

    @FXML
    private TableColumn<Meal, String> colMealName;

    @FXML
    private TableColumn<Meal, Double> colPrice;

    @FXML
    private TableColumn<Meal, Boolean> colRequest;

    @FXML
    private Label lbEmptyTable;

    @FXML
    private Button btReturn;



    private ObservableList<Meal> mealList;


    @FXML
    public void initialize() {
        // Initialize the comboBox with some example days
        cbReservationDay.setItems(FXCollections.observableArrayList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes"));
        setupRadioButtons();
        //setupTableColumns();

        // Add placeholder text
        tvMeals.setPlaceholder(lbEmptyTable);
        
        // Initialize meal list
        mealList = FXCollections.observableArrayList();
        tvMeals.setItems(mealList);
    }

    @FXML
    private void handleReturnAction() {
        // Handle the return button action
        showAlert("Botón Regresar", "Se presionó el botón Regresar.");
    }

    // Configura los RadioButton
    private void setupRadioButtons() {
        rbBreakfast.setUserData("breakfast");
        rbLunch.setUserData("lunch");
    }

//    private void setupTableColumns() {
//        colMealName.setCellValueFactory(new PropertyValueFactory<>("name"));
//        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
//        colPrice.setCellFactory(column -> new TableCell<>() {
//            @Override
//            protected void updateItem(Integer price, boolean empty) {
//                super.updateItem(price, empty);
//                setText(empty || price == null ? null : "₡" + price);
//            }
//        });
//        colRequest.setCellFactory(tc -> new TableCell<>() {
//            final Button btn = new Button("Solicitar Comida");
//            {
//                btn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black; -fx-font-weight: normal; -fx-background-radius: 4;"
//                        + " -fx-pref-height: 20; -fx-pref-width: 120;");
//                btn.setOnAction(event -> {
//                    Meal meal = getTableView().getItems().get(getIndex());
//                    handleMealRequest(meal);
//                });
//            }
//
//            @Override
//            protected void updateItem(Button item, boolean empty) {
//                super.updateItem(item, empty);
//                setGraphic(empty ? null : btn);
//            }
//        });
//    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}