package application;

import data.ImageUtils;
import data.LogicSockect;
import data.SocketClient;
import domain.Meal;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceViewGUIController {
    private static final Map<String, Meal> cartItems = new HashMap<>();
    private static final Map<String, MealCard> activeMealCards = new HashMap<>();
    private static List<Meal> currentMeals = new ArrayList<>();
    private static ServiceViewGUIController instance;

    @FXML private ComboBox<String> dayComboBox;
    @FXML private ToggleButton btnBreakfast;
    @FXML private ToggleButton btnLunch;
    @FXML private GridPane mealsGrid;
    @FXML private VBox cartItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label userNameLabel;
    @FXML private Label balanceLabel;
    @FXML private Button checkoutButton;
    @FXML private Button clearCartButton;
    @FXML private Button btReturn;
    @FXML private ImageView profileImageView;
    @FXML private Circle avatarClip;
    @FXML private StackPane avatarContainer;

    @FXML
    public void initialize() {
        instance = this;
        setupInitialData();
        setupEventHandlers();
        setupUserAvatar();
        updateUserInfo();
        // Initialize the ComboBox
        dayComboBox.getItems().addAll("Lunes", "Martes", "Miércoles", "Jueves", "Viernes");
        dayComboBox.setValue("Lunes");
        btnBreakfast.setSelected(true);
        updateMeals();
    }

    public static void addToCart(Meal meal) {
        addToCart(meal, null);
    }

    public static void addToCart(Meal meal, MealCard mealCard) {
        String mealId = meal.getName();
        if (cartItems.containsKey(mealId)) {
            Meal existingMeal = cartItems.get(mealId);
            existingMeal.setCantidad(meal.getCantidad());
        } else {
            cartItems.put(mealId, meal);
        }
        if (mealCard != null) {
            activeMealCards.put(mealId, mealCard);
        }
        instance.updateCartDisplay();
    }

    public void cleanup() {
        cartItems.clear();
        for (MealCard card : activeMealCards.values()) {
            card.cleanup();
        }
        activeMealCards.clear();
        currentMeals.clear();
    }

    public static void removeFromCart(String mealId) {
        cartItems.remove(mealId);
        instance.updateCartDisplay();
    }

    private void setupUserAvatar() {
        Circle clip = new Circle(18);
        clip.setCenterX(18);
        clip.setCenterY(18);
        profileImageView.setClip(clip);

        String photoPath = Logic.user.getRoutePhoto();
        Image profileImage = ImageUtils.loadImage(photoPath);

        if (profileImage != null && !profileImage.isError()) {
            WritableImage croppedImage = ImageUtils.cropToSquare(profileImage);
            profileImageView.setImage(croppedImage);
        } else {
            avatarContainer.setStyle("-fx-background-color: #cccccc;");
        }
    }

    private void setupInitialData() {
        clearCartButton.setOnAction(e -> clearCart());
    }

    private void setupEventHandlers() {
        ToggleGroup mealTypeGroup = new ToggleGroup();
        btnBreakfast.setToggleGroup(mealTypeGroup);
        btnLunch.setToggleGroup(mealTypeGroup);

        // Prevent deselection of all toggles
        mealTypeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null) {
                // If nothing is selected, reselect the old toggle
                mealTypeGroup.selectToggle(oldToggle);
            }
        });

        // Add ComboBox listener
        dayComboBox.setOnAction(e -> updateMeals());
        btnBreakfast.setOnAction(e -> updateMeals());
        btnLunch.setOnAction(e -> updateMeals());
    }

    private String getSelectedDay() {
        return dayComboBox.getValue();
    }

    public static void resetState() {
        if (instance != null) {
            instance.cleanup();
        }
        cartItems.clear();
        activeMealCards.clear();
        currentMeals.clear();
    }

    private void updateUserInfo() {
        if (Logic.user != null) {
            userNameLabel.setText(Logic.user.getNombre());
            balanceLabel.setText(String.format("₡%.2f", Logic.user.getDineroDisponible()));
        }
    }

    private void updateMeals() {
        String selectedDay = getSelectedDay();
        String selectedMealType = getSelectedMealType();

        if (selectedDay != null && selectedMealType != null) {
            currentMeals.clear();
            LogicSockect.clearMeals();

            String request = String.format("listMeals,%s,%s", selectedDay, selectedMealType);
            SocketClient.sendMessage(request);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if(LogicSockect.listSize > 0){
                int maxAttempts = 10;
                int currentAttempt = 0;
                while (LogicSockect.getListMeals().isEmpty() && currentAttempt < maxAttempts) {
                    try {
                        Thread.sleep(10);
                        currentAttempt++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

            currentMeals = new ArrayList<>(LogicSockect.getListMeals());
            System.out.println("UpdateMeals");

            LogicSockect.SleepList();
            Platform.runLater(this::updateMealDisplay);

        }
    }

    private String getSelectedMealType() {
        ToggleButton selected = (ToggleButton) btnBreakfast.getToggleGroup().getSelectedToggle();
        return selected != null ? selected.getText() : null;
    }

    private void updateMealDisplay() {
        mealsGrid.getChildren().clear();
        activeMealCards.clear();

        if (currentMeals.isEmpty()) {
            Label noMealsLabel = new Label("No meals available for this selection");
            noMealsLabel.getStyleClass().add("no-meals-label");
            mealsGrid.add(noMealsLabel, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;

        for (Meal meal : currentMeals) {
            MealCard mealCard = new MealCard(meal);

            if (cartItems.containsKey(meal.getName())) {
                Meal cartMeal = cartItems.get(meal.getName());
                mealCard.restoreState(cartMeal.getCantidad());
            }

            mealCard.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(mealCard, true);
            mealsGrid.add(mealCard, column, row);
            activeMealCards.put(meal.getName(), mealCard);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
            System.out.println("UpdateMealDisplay");
        }
    }

    private void updateCartDisplay() {
        cartItemsContainer.getChildren().clear();
        double subtotal = 0;

        for (Meal meal : cartItems.values()) {
            CartItemCard cartItemCard = new CartItemCard(meal);
            cartItemsContainer.getChildren().add(cartItemCard);
            subtotal += meal.getTotalOrder();

            MealCard mealCard = activeMealCards.get(meal.getName());
            if (mealCard != null) {
                mealCard.updateQuantityFromCart(meal.getCantidad());
            }
        }

        double tax = subtotal * 0.175;
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("%,.0f", subtotal));
        taxLabel.setText(String.format("%,.0f", tax));
        totalLabel.setText(String.format("%,.0f", total));

        checkoutButton.setDisable(cartItems.isEmpty());
    }

    private void clearCart() {
        // Clear the cart items map
        cartItems.clear();

        // Reset all meal cards to initial state
        for (MealCard card : activeMealCards.values()) {
            card.updateQuantityFromCart(0);
        }

        // Update the cart display
        updateCartDisplay();
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert("Carrito vacío", "Agregue items al carrito antes de realizar el pedido.");
            return;
        }

        double total = calculateTotal();
        double balance = Logic.user.getDineroDisponible();
        String userCarnet = Logic.user.getCarnet();

        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar compra");
        confirmDialog.setHeaderText("¿Está seguro que desea realizar esta compra?");
        confirmDialog.setContentText(String.format("Total a pagar: ₡%,.0f", total));

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (!validateTransaction(total, balance)) {
                    return;
                }

                processOrders(userCarnet, total);
                updateUserBalance(balance, total);

                // Reset all UI elements
                resetUI();

                showAlert("Pedido realizado", "Su pedido ha sido procesado exitosamente.");
            }
        });
    }

    private void resetUI() {
        // Clear the cart
        clearCart();

        // Reset all meal cards to initial state
        for (MealCard card : activeMealCards.values()) {
            card.updateQuantityFromCart(0);
        }

        // Clear collections
        cartItems.clear();
        activeMealCards.clear();

        // Update the display
        updateCartDisplay();
        updateMealDisplay();
    }

    private double calculateTotal() {
        String totalText = totalLabel.getText().replace(",", "");
        return Double.parseDouble(totalText);
    }

    private boolean validateTransaction(double total, double balance) {
        if (total > balance) {
            showAlert("Saldo insuficiente", "No tiene suficiente saldo para realizar este pedido.");
            return false;
        }
        return true;
    }

    private void processOrders(String userCarnet, double total) {
        // Process individual orders
        cartItems.values().forEach(meal -> {
            String orderMessage = String.format("foodOrder,%s,%d,%.2f,%s",
                    meal.getName(),
                    meal.getCantidad(),
                    meal.getTotalOrder(),
                    userCarnet
            );
            SocketClient.sendMessage(orderMessage);
        });

        String deductionMessage = String.format("totalToDeduce,%s,%.2f", userCarnet, total);
        SocketClient.sendMessage(deductionMessage);
        System.out.println(deductionMessage);
    }

    private void updateUserBalance(double balance, double total) {
        double newBalance = balance - total;
        balanceLabel.setText(String.valueOf(newBalance));
        Logic.user.setDineroDisponible(newBalance);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}