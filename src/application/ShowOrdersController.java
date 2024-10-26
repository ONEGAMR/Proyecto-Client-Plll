package application;

import data.LogicSockect;
import data.SocketClient;
import domain.Meal;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.stream.Collectors;

public class ShowOrdersController {

    @FXML
    private ComboBox<String> cmStatus;

    @FXML
    private ListView<String> listOrders;

    @FXML
    private Button btReturn;

    @FXML
    private Button btAll;

    private String beforeList = "";

    @FXML
    private void initialize() {
        // Inicializar el ComboBox con algunas opciones de estado
        cmStatus.getItems().addAll("Pendiente", "Preparando", "Entregado");

        // Añadir un listener para manejar cambios en la selección del ComboBox
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

    public void fillListOrder(String status){

        setStatusOrder(status);

        Logic.sleepTList("meals");

        Platform.runLater(() -> {
            listOrders.getItems().clear();
            List<Meal> meals = (List<Meal>) LogicSockect.getListMeals();

            if (meals != null && !meals.isEmpty()) {
                // Convertir Meal a String solo si hay elementos en la lista
                List<String> mealDescriptions = meals.stream()
                        .map(meal -> meal.toStringPedidoForList())
                        .collect(Collectors.toList());

                listOrders.setItems(FXCollections.observableArrayList(mealDescriptions));
            } else {
                // Mostrar el mensaje "No hay pedidos" si la lista es nula o está vacía
                listOrders.setItems(FXCollections.observableArrayList("No hay pedidos"));
            }
            listOrders.setVisible(true); // Asegurar que la lista sea visible
        });

    }

    public void setStatusOrder(String status){

        SocketClient.sendMessage("listOrder,"+Logic.user.getCarnet()+","+ status);
        System.out.println("listOrder,"+Logic.user.getCarnet()+","+ status+ " Envio de orders");
    }

    private void handleReturn() {

         Logic.closeWindows(btReturn,"/presentation/MainGUI.fxml");

    }

    private void showAllOrders() {

        fillListOrder("Todos");
    }

    private void filterOrders(String status) {
        // Implementar la lógica para filtrar pedidos según el estado
        listOrders.getItems().clear();

        // Verificar si el nuevo menú es diferente al anterior
        if (!status.equals(beforeList)) {
            System.out.println("Nuevo menú seleccionado: " + status);
            LogicSockect.meals.clear();
            beforeList = status; // Actualizar el menú anterior
        }

        if(status.equals("Pendiente")){

            fillListOrder("Pendiente");
        }else if(status.equals("Preparando")){

            fillListOrder("Preparando");
        }else if(status.equals("Entregado")){

            fillListOrder("Entregado");
        }
    }
}