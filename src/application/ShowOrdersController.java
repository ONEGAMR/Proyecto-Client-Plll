package application;

import data.LogicSockect;
import data.SocketClient;
import domain.Meal;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class ShowOrdersController {

    @FXML
    private ComboBox<String> cmStatus;

    @FXML
    private TableView<Meal> tableOrders;

    @FXML
    private TableColumn<Meal, String> nameColumn;

    @FXML
    private TableColumn<Meal, Integer> quantityColumn;

    @FXML
    private TableColumn<Meal, Integer> totalColumn;

    @FXML
    private Button btReturn;

    @FXML
    private Button btAll;

    private String beforeList = "";

    @FXML
    private void initialize() {
        // Inicializar columnas de la tabla
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalOrder"));

        // Inicializar el ComboBox con opciones de estado
        cmStatus.getItems().addAll("Pendiente", "Preparando", "Entregado", "Listo");

        // Añadir listener para cambios en la selección del ComboBox
        cmStatus.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            filterOrders(newValue);
        });

        // Manejar clics en los botones
        btReturn.setOnAction(event -> handleReturn());
        btAll.setOnAction(event -> showAllOrders());

        // Cargar todos los pedidos por defecto
        LogicSockect.meals.clear();
        fillListOrder("Todos");
    }

    public void fillListOrder(String status) {
        setStatusOrder(status);
        Logic.sleepTList("meals");

        Platform.runLater(() -> {
            List<Meal> meals = (List<Meal>) LogicSockect.getListMeals();

            if (meals != null && !meals.isEmpty()) {
                tableOrders.setItems(FXCollections.observableArrayList(meals));
            } else {
                tableOrders.setItems(FXCollections.observableArrayList());
            }
        });
    }

    public void setStatusOrder(String status) {
        SocketClient.sendMessage("listOrder," + Logic.user.getCarnet() + "," + status);
        System.out.println("listOrder," + Logic.user.getCarnet() + "," + status + " Envio de orders");
    }

    private void handleReturn() {
        SocketClient.closeWindows(btReturn, "/presentation/MainGUI.fxml");
    }

    private void showAllOrders() {
        LogicSockect.meals.clear();
        fillListOrder("Todos");
    }

    private void filterOrders(String status) {
        // Verificar si el nuevo menú es diferente al anterior
        if (!status.equals(beforeList)) {
            System.out.println("Nuevo menú seleccionado: " + status);
            LogicSockect.meals.clear();
            beforeList = status;
        }

        //se llena la table dependiendo del filtro
        if (status.equals("Pendiente")) {
            fillListOrder("Pendiente");

        } else if (status.equals("Preparando")) {
            fillListOrder("Preparando");

        } else if (status.equals("Entregado")) {
            fillListOrder("Entregado");
        }
    }
}