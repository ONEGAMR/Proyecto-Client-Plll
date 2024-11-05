package application;

import data.LogicSockect;
import data.SocketClient;
import domain.Meal;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class ShowOrdersController {
    private static final int SOCKET_WAIT_TIME = 500;
    private static final int MAX_ATTEMPTS = 10;
    private static final int RETRY_DELAY = 10;

    private String previousStatus = "";

    @FXML private ComboBox<String> cmStatus;
    @FXML private TableView<Meal> tableOrders;
    @FXML private TableColumn<Meal, String> nameColumn;
    @FXML private TableColumn<Meal, Integer> quantityColumn;
    @FXML private TableColumn<Meal, Integer> totalColumn;
    @FXML private Button btAll;

    @FXML
    private void initialize() {
        setupTableColumns();
        setupStatusComboBox();
        setupEventHandlers();
        loadInitialOrders();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalOrder"));
    }

    private void setupStatusComboBox() {
        cmStatus.getItems().addAll("Pendiente", "Preparando", "Entregado", "Listo");
        cmStatus.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue != null) {
                filterOrders(newValue);
            }
        });
    }

    private void setupEventHandlers() {
        btAll.setOnAction(event -> loadInitialOrders());
    }

    private void loadInitialOrders() {
        LogicSockect.meals.clear();
        fillListOrder("Todos");
    }

    public void fillListOrder(String status) {
        LogicSockect.resetSizeList();
        setStatusOrder(status);

        if (LogicSockect.listSize > 0) {
            waitForMeals();
        }

        LogicSockect.SleepList();
        updateTableView();
    }

    private void waitForMeals() {
        int currentAttempt = 0;
        while (LogicSockect.getListMeals().isEmpty() && currentAttempt < MAX_ATTEMPTS) {
            try {
                Thread.sleep(RETRY_DELAY);
                currentAttempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateTableView() {
        Platform.runLater(() -> {
            List<Meal> meals = LogicSockect.getListMeals();
            tableOrders.setItems(FXCollections.observableArrayList(
                    meals != null ? meals : FXCollections.emptyObservableList()
            ));
        });
    }

    private void setStatusOrder(String status) {
        String message = String.format("listOrder,%s,%s", Logic.user.getCarnet(), status);
        SocketClient.sendMessage(message);
        try {
            Thread.sleep(SOCKET_WAIT_TIME);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error esperando por la respuesta del socket", e);
        }
    }

    private void filterOrders(String status) {
        if (!status.equals(previousStatus)) {
            LogicSockect.meals.clear();
            previousStatus = status;
            fillListOrder(status);
        }
    }
}