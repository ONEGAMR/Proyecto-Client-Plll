package application;

import application.Logic;
import domain.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import data.LogicSockect;
import data.SocketClient;
import data.Utils;
import domain.Meal;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ServiceViewGUIController {

    private static ServiceViewGUIController instance;

    @FXML
    private ComboBox<String> cbReservationDay;
    @FXML
    private RadioButton rbBreakfast, rbLunch;
    @FXML
    private ToggleGroup mealTimeGroup;
    @FXML
    private GridPane mealsGrid;
    @FXML
    private Button btReturn, btnPurchase;
    @FXML
    private VBox orderItemsContainer;
    @FXML
    private Label lblTotal, lblSubtotal, lblTax, lblUserName, lblBalance;
    @FXML
    private Circle userAvatar;

    private static class MealContext {
        private final Meal meal;
        private final String day;
        private final String mealTime;
        private int quantity;

        public MealContext(Meal meal, String day, String mealTime) {
            this.meal = meal;
            this.day = day;
            this.mealTime = mealTime;
            // Inicializamos la cantidad en 1 y también actualizamos la cantidad en el objeto Meal
            this.quantity = 1;
            this.meal.setQuantity(1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MealContext that = (MealContext) o;
            return meal.getName().equals(that.meal.getName()) &&
                    day.equals(that.day) &&
                    mealTime.equals(that.mealTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(meal.getName(), day, mealTime);
        }
    }

    private static final double TAX_RATE = 0.00; // Changed from 0.13 to 0.00
    private static final List<String> WEEKDAYS = Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes");
    private static final int LOAD_TIMEOUT = 10000;
    private static final int MAX_RETRIES = 3;
    private static final int MEAL_RECEIVE_TIMEOUT = 1000;
    private static final int INITIAL_WAIT = 500;

    private final Object mealsLock = new Object();
    private volatile boolean isReceivingMeals = false;
    private long lastMealReceived = 0;
    private int previousMealCount = 0;

    private volatile boolean isLoadingMeals = false;
    private final Object loadLock = new Object();
    private Timer loadingTimer;
    private int retryCount = 0;
    private User currentStudent;
    private final Map<MealContext, Integer> selectedMeals = new HashMap<>();
    private final ObservableList<MealContext> orderItems = FXCollections.observableArrayList();
    private final Map<MealContext, VBox> mealCardMap = new HashMap<>();

    @FXML
    public void initialize() {
        instance = this;  // Guardamos la instancia en initialize
        Platform.runLater(() -> {
            initializeStudent();
            setupUserProfile();
            setupComboBoxes();
            setupRadioButtons();
            setupListeners();
            initializeDefaultView();
        });
    }

    // Método para obtener la instancia del controlador
    public static ServiceViewGUIController getInstance() {
        return instance;
    }

    private void initializeStudent() {
        currentStudent = Logic.user != null ? Logic.user : new User("B12345", "Deibis Gutierrez", "deibis.gutierrez@estudiantes.uam.cr", 88776655, true, LocalDate.of(2020, 1, 15), 'M', 50000.0);
    }

    private void setupUserProfile() {
        lblUserName.setText(currentStudent.getNombre());
        updateBalance();
        setupUserAvatar();
    }

    private void setupUserAvatar() {
        userAvatar.setRadius(20);
        String photoPath = currentStudent.getRoutePhoto();

        Image profileImage = (photoPath == null || photoPath.trim().isEmpty()) ? null : loadImageFromPath(photoPath);
        if (profileImage == null || profileImage.isError()) {
            setDefaultAvatarWithInitials();
            return;
        }

        WritableImage croppedImage = cropImage(profileImage);
        userAvatar.setFill(new ImagePattern(croppedImage));
    }

    private WritableImage cropImage(Image profileImage) {
        double size = Math.min(profileImage.getWidth(), profileImage.getHeight());
        double x = (profileImage.getWidth() - size) / 2;
        double y = (profileImage.getHeight() - size) / 2;

        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(profileImage, x, y, size, size, 0, 0, size, size);
        return canvas.snapshot(null, null);
    }

    private Image loadImageFromPath(String photoPath) {
        try {
            File imageFile = new File(photoPath);
            if (imageFile.exists()) return new Image(imageFile.toURI().toString(), false);

            String classPathPath = photoPath.startsWith("/") ? photoPath : "/" + photoPath;
            if (getClass().getResource(classPathPath) != null) return new Image(getClass().getResource(classPathPath).toExternalForm(), false);
            return new Image(photoPath, false);
        } catch (Exception e) {
            System.err.println("Failed to load image from path: " + photoPath);
            return null;
        }
    }

    // Método para actualizar el balance
    public void setBalance(double balance) {
        if (currentStudent != null) {
            currentStudent.setDineroDisponible(balance);
            Platform.runLater(() -> {
                lblBalance.setText(String.format("Saldo disponible actualmente: ₡%.2f", balance));
            });
        }
    }

    private void setDefaultAvatarWithInitials() {
        String initials = getInitials();
        StackPane avatarPane = new StackPane();
        avatarPane.setMinSize(40, 40);
        avatarPane.setMaxSize(40, 40);

        Circle background = new Circle(20, Color.BLACK);
        Text initialsText = new Text(initials != null ? initials : "??");
        initialsText.setFill(Color.WHITE);
        initialsText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        avatarPane.getChildren().addAll(background, initialsText);
        userAvatar.setFill(new ImagePattern(avatarPane.snapshot(null, null)));
    }

    private String getInitials() {
        if (currentStudent == null || currentStudent.getNombre() == null) return null;

        String[] nameParts = currentStudent.getNombre().trim().split("\\s+");
        return Arrays.stream(nameParts)
                .filter(part -> !part.isEmpty())
                .limit(2)
                .map(part -> String.valueOf(part.charAt(0)))
                .reduce("", String::concat)
                .toUpperCase();
    }

    private void updateBalance() {
        lblBalance.setText(String.format("Saldo disponible: ₡%.2f", currentStudent.getDineroDisponible()));
    }

    private void setupComboBoxes() {
        cbReservationDay.setItems(FXCollections.observableArrayList(WEEKDAYS));
    }

    private void setupRadioButtons() {
        mealTimeGroup = new ToggleGroup();
        rbBreakfast.setToggleGroup(mealTimeGroup);
        rbLunch.setToggleGroup(mealTimeGroup);
        rbBreakfast.setUserData("breakfast");
        rbLunch.setUserData("lunch");
    }

    private void setupListeners() {
        cbReservationDay.setOnAction(e -> updateGrid());
        mealTimeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) updateGrid();
        });
    }

    private void initializeDefaultView() {
        cbReservationDay.setValue(WEEKDAYS.get(0));
        rbBreakfast.setSelected(true);
        updateGrid();
    }

    private void updateGrid() {
        String selectedDay = cbReservationDay.getValue();
        String selectedMealType = mealTimeGroup.getSelectedToggle() != null ? (String) mealTimeGroup.getSelectedToggle().getUserData() : null;

        if (selectedDay == null || selectedMealType == null) {
            Platform.runLater(mealsGrid.getChildren()::clear);
            return;
        }

        if (loadingTimer != null) loadingTimer.cancel();
        synchronized (loadLock) {
            if (isLoadingMeals) return;
            isLoadingMeals = true;
        }

        showLoadingIndicator();
        CompletableFuture.supplyAsync(() -> fetchMeals(selectedDay, selectedMealType))
                .thenAcceptAsync(this::processReceivedMeals, Platform::runLater);

        startLoadingTimeout();
    }

    private List<Meal> fetchMeals(String selectedDay, String selectedMealType) {
        try {
            synchronized (LogicSockect.meals) {
                LogicSockect.meals.clear();
            }
            isReceivingMeals = true;
            lastMealReceived = System.currentTimeMillis();
            previousMealCount = 0;

            SocketClient.sendMessage("listMeals," + selectedDay + "," + selectedMealType);
            Thread.sleep(INITIAL_WAIT);
            return waitForCompleteMealSet();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Meal> waitForCompleteMealSet() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        List<Meal> receivedMeals = null;
        boolean complete = false;

        while (System.currentTimeMillis() - startTime < LOAD_TIMEOUT && retryCount < MAX_RETRIES && !complete) {
            synchronized (LogicSockect.meals) {
                int currentMealCount = LogicSockect.meals.size();
                if (currentMealCount > previousMealCount) {
                    lastMealReceived = System.currentTimeMillis();
                    previousMealCount = currentMealCount;
                }

                if (!LogicSockect.meals.isEmpty() &&
                        System.currentTimeMillis() - lastMealReceived > MEAL_RECEIVE_TIMEOUT) {
                    receivedMeals = new ArrayList<>(LogicSockect.getListMeals());
                    complete = true;
                } else if (System.currentTimeMillis() - lastMealReceived > MEAL_RECEIVE_TIMEOUT * 2) {
                    retryCount++;
                    if (retryCount < MAX_RETRIES) {
                        LogicSockect.meals.clear();
                        previousMealCount = 0;
                        SocketClient.sendMessage("listMeals," + cbReservationDay.getValue() + "," + mealTimeGroup.getSelectedToggle().getUserData());
                        lastMealReceived = System.currentTimeMillis();
                    }
                }
            }
            Thread.sleep(100);
        }

        isReceivingMeals = false;
        return receivedMeals;
    }

    private void processReceivedMeals(List<Meal> finalMeals) {
        try {
            mealsGrid.getChildren().clear();
            mealCardMap.clear();

            if (finalMeals != null && !finalMeals.isEmpty()) {
                System.out.println("Received " + finalMeals.size() + " meals total");
                displayMealsInGrid(finalMeals);
                syncSelectionWithCart(finalMeals);
            } else {
                if (retryCount >= MAX_RETRIES) {
                    showErrorMessage("Failed to load meals after " + MAX_RETRIES + " attempts. Please try again.");
                } else {
                    showNoMealsMessage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error displaying meals: " + e.getMessage());
        } finally {
            synchronized (loadLock) {
                isLoadingMeals = false;
                retryCount = 0;
            }
        }
    }

    private void showLoadingIndicator() {
        Platform.runLater(() -> {
            mealsGrid.getChildren().clear();
            VBox loadingBox = new VBox(10);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.getChildren().add(new ProgressIndicator());
            mealsGrid.add(loadingBox, 1, 1);
        });
    }

    public void onMealReceived() {
        lastMealReceived = System.currentTimeMillis();
    }

    private void syncSelectionWithCart(List<Meal> currentMeals) {
        String currentDay = cbReservationDay.getValue();
        String currentMealTime = (String) mealTimeGroup.getSelectedToggle().getUserData();

        for (Meal currentMeal : currentMeals) {
            orderItems.stream()
                    .filter(orderItem ->
                            orderItem.meal.getName().equals(currentMeal.getName()) &&
                                    orderItem.day.equals(currentDay) &&
                                    orderItem.mealTime.equals(currentMealTime))
                    .findFirst()
                    .ifPresent(orderItem -> {
                        MealContext context = new MealContext(currentMeal, currentDay, currentMealTime);
                        VBox mealCard = mealCardMap.get(context);
                        if (mealCard != null) {
                            mealCard.getStyleClass().add("selected");
                            selectedMeals.put(context, currentMeal.getQuantity());
                        }
                    });
        }
    }

    private void startLoadingTimeout() {
        loadingTimer = new Timer();
        loadingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (isLoadingMeals) {
                        synchronized (loadLock) {
                            isLoadingMeals = false;
                            retryCount = 0;
                        }
                        showErrorMessage("Loading timeout. Please try again.");
                    }
                });
            }
        }, LOAD_TIMEOUT);
    }

    private void showNoMealsMessage() {
        mealsGrid.add(new Label("No meals available for this selection"), 1, 1);
    }

    private void showErrorMessage(String message) {
        mealsGrid.getChildren().clear();
        mealsGrid.add(new Label(message), 1, 1);
    }

    private void displayMealsInGrid(List<Meal> meals) {
        if (meals == null || meals.isEmpty()) {
            showNoMealsMessage();
            return;
        }

        mealsGrid.getRowConstraints().clear();
        mealsGrid.getColumnConstraints().clear();
        for (int i = 0; i < 3; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / 3);
            mealsGrid.getColumnConstraints().add(colConstraints);
        }

        int row = 0, column = 0;
        String currentDay = cbReservationDay.getValue();
        String currentMealTime = (String) mealTimeGroup.getSelectedToggle().getUserData();

        for (Meal meal : meals) {
            if (meal == null) continue;
            MealContext context = new MealContext(meal, currentDay, currentMealTime);
            VBox mealCard = createMealCard(context);
            mealCardMap.put(context, mealCard);
            ensureRowConstraints(row);
            mealsGrid.add(mealCard, column, row);
            column = (column + 1) % 3;
            if (column == 0) row++;
        }
    }


    private void ensureRowConstraints(int row) {
        while (mealsGrid.getRowConstraints().size() <= row) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(120);
            rowConstraints.setVgrow(Priority.ALWAYS);
            mealsGrid.getRowConstraints().add(rowConstraints);
        }
    }

    private VBox createMealCard(MealContext mealContext) {
        VBox card = new VBox(8);
        card.getStyleClass().add("meal-card");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(100);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));



        Label nameLabel = new Label(mealContext.meal.getName());
        nameLabel.getStyleClass().add("meal-name");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label priceLabel = new Label(String.format("₡%.2f", (double) mealContext.meal.getPrice()));
        priceLabel.getStyleClass().add("meal-price");
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox priceContainer = new HBox(priceLabel);
        priceContainer.setAlignment(Pos.CENTER);
        priceContainer.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(nameLabel, priceContainer);
        card.setOnMouseClicked(event -> handleMealSelection(mealContext, card));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f0f0f0;"));
        card.setOnMouseExited(e -> card.setStyle(""));

        return card;
    }

    private void handleMealSelection(MealContext mealContext, VBox card) {
        if (orderItems.contains(mealContext)) return;

        // La cantidad inicial ya está establecida en el constructor de MealContext
        selectedMeals.put(mealContext, mealContext.quantity);
        card.getStyleClass().add("selected");
        addToOrderList(mealContext);
        updateOrderSummary();
    }

    private Node createOrderItemView(MealContext mealContext) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(8));
        container.getStyleClass().add("order-item");

        VBox mealInfo = createMealInfoView(mealContext);
        HBox quantityControl = createQuantityControlView(mealContext);

        container.getChildren().addAll(mealInfo, quantityControl);
        HBox.setHgrow(mealInfo, Priority.ALWAYS);

        return container;
    }

    private VBox createMealInfoView(MealContext mealContext) {
        VBox mealInfo = new VBox(4);

        // Create HBox for name and price in the same line
        HBox nameAndPrice = new HBox(8);
        nameAndPrice.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(mealContext.meal.getName());
        Label priceLabel = new Label(String.format("₡%d", mealContext.meal.getPrice()));
        nameAndPrice.getChildren().addAll(nameLabel, priceLabel);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Context info in second line
        Label contextLabel = new Label(String.format("%s - %s",
                mealContext.day,
                mealContext.mealTime.equals("breakfast") ? "Desayuno" : "Almuerzo"
        ));
        contextLabel.getStyleClass().add("meal-context");

        mealInfo.getChildren().addAll(nameAndPrice, contextLabel);
        return mealInfo;
    }

    private HBox createQuantityControlView(MealContext mealContext) {
        HBox quantityControl = new HBox(8);
        quantityControl.setAlignment(Pos.CENTER);

        Button decreaseBtn = new Button("-");
        // Initialize with quantity 1 instead of 0
        Label quantityLabel = new Label("1");
        Button increaseBtn = new Button("+");

        decreaseBtn.setOnAction(e -> updateMealQuantity(mealContext, quantityLabel, -1));
        increaseBtn.setOnAction(e -> updateMealQuantity(mealContext, quantityLabel, 1));

        quantityControl.getChildren().addAll(decreaseBtn, quantityLabel, increaseBtn);
        return quantityControl;
    }

    private void updateMealQuantity(MealContext mealContext, Label quantityLabel, int delta) {
        int newQuantity = mealContext.quantity + delta;

        // If quantity would become less than 1, remove the item
        if (newQuantity < 1) {
            removeMealFromOrder(mealContext);
            return;
        }

        // Update quantity
        mealContext.quantity = newQuantity;
        mealContext.meal.setQuantity(newQuantity);
        selectedMeals.put(mealContext, newQuantity);
        quantityLabel.setText(String.valueOf(newQuantity));
        updateOrderSummary();
    }

    private void removeMealFromOrder(MealContext mealContext) {
        selectedMeals.remove(mealContext);
        removeFromOrderList(mealContext);
        VBox mealCard = mealCardMap.get(mealContext);
        if (mealCard != null) {
            mealCard.getStyleClass().remove("selected");
        }
    }

    private void addToOrderList(MealContext mealContext) {
        orderItems.add(mealContext);
        updateOrderItemsView(); // Use the updated view method instead of directly adding
    }

    private void removeFromOrderList(MealContext mealContext) {
        orderItems.remove(mealContext);
        updateOrderItemsView();
    }

    private void updateOrderItemsView() {
        orderItemsContainer.getChildren().clear();
        int lastIndex = orderItems.size() - 1;

        for (int i = 0; i < orderItems.size(); i++) {
            MealContext mealContext = orderItems.get(i);
            orderItemsContainer.getChildren().add(createOrderItemView(mealContext));

            // Add separator after each item except the last one
            if (i < lastIndex) {
                Pane separator = new Pane();
                separator.getStyleClass().add("separator"); // Aplica la clase de estilo
                orderItemsContainer.getChildren().add(separator);
            }
        }
        updateOrderSummary();
    }

    private void updateOrderSummary() {
        double subtotal = calculateSubtotal();
        double tax = subtotal * TAX_RATE; // Will always be 0 now
        double total = subtotal + tax; // Will be equal to subtotal

        lblSubtotal.setText(String.format("Subtotal: ₡%.2f", subtotal));
        lblTax.setText(String.format("IVA (0%%): ₡%.2f", tax)); // Changed from 13% to 0%
        lblTotal.setText(String.format("Total: ₡%.2f", total));
    }

    private double calculateSubtotal() {
        return selectedMeals.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().meal.getPrice() * entry.getValue())
                .sum();
    }

    @FXML
    public void handlePurchaseAction(ActionEvent event) {
        if (selectedMeals.isEmpty()) {
            Utils.showAlert(Alert.AlertType.ERROR, "Error", "Please select at least one meal.");
            return;
        }

        double total = calculateSubtotal(); // Removed tax multiplication since it's 0%
        if (currentStudent.getDineroDisponible() < total) {
            Utils.showAlert(Alert.AlertType.INFORMATION, "Insufficient Balance", "You don't have enough balance for this order.");
            return;
        }

        if (confirmPurchase()) {
            processPurchase(total);
        }
    }

    private boolean confirmPurchase() {
        return Utils.showConfirmationAlert("Do you want to complete this order?", "Purchase Confirmation");
    }

    private void processPurchase(double total) {
        currentStudent.setDineroDisponible(currentStudent.getDineroDisponible() - total);

        selectedMeals.forEach((mealContext, quantity) -> {
            // Aseguramos que la cantidad sea al menos 1
            int finalQuantity = Math.max(quantity, 1);
            mealContext.meal.setQuantity(finalQuantity);
            mealContext.meal.setTotalOrder(finalQuantity * mealContext.meal.getPrice());

            // Debug log para verificar la cantidad antes de enviar
            System.out.println("Sending meal order: " + mealContext.meal.getName() +
                    " with quantity: " + mealContext.meal.getQuantity());

            SocketClient.sendMessage("foodOrder," + mealContext.meal.toStringPedido() + "," +
                    currentStudent.getCarnet() + "," + currentStudent.getDineroDisponible());
        });

        Utils.showAlert(Alert.AlertType.INFORMATION, "Order Confirmed",
                "Order completed successfully. New balance: ₡" + currentStudent.getDineroDisponible());
        resetOrder();
    }

    private void resetOrder() {
        selectedMeals.clear();
        orderItems.clear();
        orderItemsContainer.getChildren().clear();
        updateOrderSummary();
        updateBalance();
        updateGrid();
    }

    public void handleReturnAction(ActionEvent actionEvent) {
        Logic.closeWindows(btReturn,"/presentation/MainGUI.fxml");
    }
}