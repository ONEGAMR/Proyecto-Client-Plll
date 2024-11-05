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

    private static final double TAX_RATE = 0.13;
    private static final String[] DAYS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};

    @FXML
    public void initialize() {
        instance = this;
        initializeUI();
        setupUserData();
        updateMeals();
    }

    private void initializeUI() {
        setupToggleGroup();
        setupDayComboBox();
        setupCartButton();
        setupUserAvatar();
    }

    private void setupToggleGroup() {
        ToggleGroup mealTypeGroup = new ToggleGroup();
        btnBreakfast.setToggleGroup(mealTypeGroup);
        btnLunch.setToggleGroup(mealTypeGroup);
        btnBreakfast.setSelected(true);

        mealTypeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null) {
                mealTypeGroup.selectToggle(oldToggle);
            }
            updateMeals();
        });
    }

    private void setupDayComboBox() {
        dayComboBox.getItems().addAll(DAYS);
        dayComboBox.setValue(DAYS[0]);
        dayComboBox.setOnAction(e -> updateMeals());
    }

    private void setupCartButton() {
        clearCartButton.setOnAction(e -> clearCart());
    }

    private void setupUserAvatar() {
        Circle clip = new Circle(18);
        clip.setCenterX(18);
        clip.setCenterY(18);
        profileImageView.setClip(clip);

        Image profileImage = ImageUtils.loadImage(Logic.user.getRoutePhoto());
        if (profileImage != null && !profileImage.isError()) {
            profileImageView.setImage(ImageUtils.cropToSquare(profileImage));
        } else {
            avatarContainer.setStyle("-fx-background-color: #cccccc;");
        }
    }

    private void setupUserData() {
        if (Logic.user != null) {
            userNameLabel.setText(Logic.user.getNombre());
            updateBalanceDisplay(Logic.user.getDineroDisponible());
        }
    }

    public static void addToCart(Meal meal, MealCard mealCard) {
        String mealId = meal.getName();
        if (cartItems.containsKey(mealId)) {
            cartItems.get(mealId).setCantidad(meal.getCantidad());
        } else {
            cartItems.put(mealId, meal);
        }
        if (mealCard != null) {
            activeMealCards.put(mealId, mealCard);
        }
        instance.updateCartDisplay();
    }

    public static void removeFromCart(String mealId) {
        cartItems.remove(mealId);
        instance.updateCartDisplay();
    }

    private void updateMeals() {
        String selectedDay = dayComboBox.getValue();
        String selectedMealType = getSelectedMealType();

        if (selectedDay != null && selectedMealType != null) {
            fetchMeals(selectedDay, selectedMealType);
        }
    }

    private void fetchMeals(String selectedDay, String selectedMealType) {
        currentMeals.clear();
        LogicSockect.clearMeals();

        SocketClient.sendMessage(String.format("listMeals,%s,%s", selectedDay, selectedMealType));
        waitForMealsData();

        currentMeals = new ArrayList<>(LogicSockect.getListMeals());
        LogicSockect.SleepList();
        Platform.runLater(this::updateMealDisplay);
    }

    private void waitForMealsData() {
        try {
            Thread.sleep(500);
            if (LogicSockect.listSize > 0) {
                int attempts = 0;
                while (LogicSockect.getListMeals().isEmpty() && attempts < 10) {
                    Thread.sleep(10);
                    attempts++;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateMealDisplay() {
        mealsGrid.getChildren().clear();
        activeMealCards.clear();

        if (currentMeals.isEmpty()) {
            showNoMealsMessage();
            return;
        }

        displayMealCards();
    }

    private void showNoMealsMessage() {
        Label noMealsLabel = new Label("No meals available for this selection");
        noMealsLabel.getStyleClass().add("no-meals-label");
        mealsGrid.add(noMealsLabel, 0, 0);
    }

    private void displayMealCards() {
        int column = 0, row = 0;
        for (Meal meal : currentMeals) {
            MealCard mealCard = createMealCard(meal);
            mealsGrid.add(mealCard, column, row);

            column = (column + 1) % 3;
            if (column == 0) row++;
        }
    }

    private MealCard createMealCard(Meal meal) {
        MealCard mealCard = new MealCard(meal);
        if (cartItems.containsKey(meal.getName())) {
            mealCard.restoreState(cartItems.get(meal.getName()).getCantidad());
        }
        mealCard.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(mealCard, true);
        activeMealCards.put(meal.getName(), mealCard);
        return mealCard;
    }

    private void updateCartDisplay() {
        cartItemsContainer.getChildren().clear();
        double subtotal = updateCartItems();
        updatePriceLabels(subtotal);
        checkoutButton.setDisable(cartItems.isEmpty());
    }

    private double updateCartItems() {
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
        return subtotal;
    }

    private void updatePriceLabels(double subtotal) {
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("%,.0f", subtotal));
        taxLabel.setText(String.format("%,.0f", tax));
        totalLabel.setText(String.format("%,.0f", total));
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert("Carrito vacío", "Agregue items al carrito antes de realizar el pedido.");
            return;
        }

        processCheckout();
    }

    private void processCheckout() {
        double total = calculateTotal();
        double balance = Logic.user.getDineroDisponible();

        showCheckoutConfirmation(total, balance);
    }

    private void showCheckoutConfirmation(double total, double balance) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar compra");
        confirmDialog.setHeaderText("¿Está seguro que desea realizar esta compra?");
        confirmDialog.setContentText(String.format("Total a pagar: ₡%,.0f", total));

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && validateTransaction(total, balance)) {
                completeTransaction(total, balance);
            }
        });
    }

    private void completeTransaction(double total, double balance) {
        String userCarnet = Logic.user.getCarnet();
        processOrders(userCarnet, total);
        updateUserBalance(balance, total);
        resetUI();
        showAlert("Pedido realizado", "Su pedido ha sido procesado exitosamente.");
    }

    private void processOrders(String userCarnet, double total) {
        cartItems.values().forEach(meal ->
                SocketClient.sendMessage(String.format("foodOrder,%s,%d,%.2f,%s",
                        meal.getName(), meal.getCantidad(), meal.getTotalOrder(), userCarnet))
        );

        SocketClient.sendMessage(String.format("totalToDeduce,%s,%.2f", userCarnet, total));
    }

    private void updateUserBalance(double balance, double total) {
        double newBalance = balance - total;
        updateBalanceDisplay(newBalance);
        Logic.user.setDineroDisponible(newBalance);
    }

    public static void updateBalanceFromSocket(double newBalance) {
        if (instance != null) {
            Platform.runLater(() -> instance.updateBalanceDisplay(newBalance));
        }
    }

    private void updateBalanceDisplay(double balance) {
        balanceLabel.setText(String.format("₡%.2f", balance));
    }

    private double calculateTotal() {
        return Double.parseDouble(totalLabel.getText().replace(",", ""));
    }

    private boolean validateTransaction(double total, double balance) {
        if (total > balance) {
            showAlert("Saldo insuficiente", "No tiene suficiente saldo para realizar este pedido.");
            return false;
        }
        return true;
    }

    private void clearCart() {
        cartItems.clear();
        activeMealCards.values().forEach(card -> card.updateQuantityFromCart(0));
        updateCartDisplay();
    }

    private void resetUI() {
        clearCart();
        activeMealCards.clear();
        updateCartDisplay();
        updateMealDisplay();
    }

    private String getSelectedMealType() {
        ToggleButton selected = (ToggleButton) btnBreakfast.getToggleGroup().getSelectedToggle();
        return selected != null ? selected.getText() : null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void resetState() {
        if (instance != null) {
            instance.cleanup();
        }
        cartItems.clear();
        activeMealCards.clear();
        currentMeals.clear();
    }

    public void cleanup() {
        cartItems.clear();
        activeMealCards.values().forEach(MealCard::cleanup);
        activeMealCards.clear();
        currentMeals.clear();
    }
}