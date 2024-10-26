package application;

import application.Logic;
import domain.User;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import data.LogicSockect;
import data.SocketClient;
import data.Utils;
import domain.Meal;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ServiceViewGUIController {
    // FXML Injected Controls
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

    // Constants
    private static final double TAX_RATE = 0.13;
    private static final List<String> WEEKDAYS = Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes");
    private static final int LOAD_TIMEOUT = 10000;
    private static final int MAX_RETRIES = 3;

    private static final int MEAL_RECEIVE_TIMEOUT = 1000; // 1 segundo de espera entre comidas
    private static final int INITIAL_WAIT = 500; // Espera inicial para comenzar a recibir comidas
    private final Object mealsLock = new Object();
    private volatile boolean isReceivingMeals = false;
    private long lastMealReceived = 0;
    private int previousMealCount = 0;

    private volatile boolean isLoadingMeals = false;
    private final Object loadLock = new Object();
    private Timer loadingTimer;
    private int retryCount = 0;
    private User currentStudent;
    private final Map<Meal, Integer> selectedMeals = new HashMap<>();
    private final ObservableList<Meal> orderItems = FXCollections.observableArrayList();
    private final Map<Meal, VBox> mealCardMap = new HashMap<>();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            initializeStudent();
            setupUserProfile();
            setupComboBoxes();
            setupRadioButtons();
            setupListeners();
            initializeDefaultView();
        });
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

        try {
            String photoPath = currentStudent.getRoutePhoto();
            if (photoPath == null || photoPath.trim().isEmpty()) {
                setDefaultAvatarWithInitials();
                return;
            }

            Image profileImage = loadImageFromPath(photoPath);
            if (profileImage == null || profileImage.isError()) {
                System.err.println("Error loading image from path: " + photoPath);
                setDefaultAvatarWithInitials();
                return;
            }

            // Crear un recorte cuadrado de la imagen
            double size = Math.min(profileImage.getWidth(), profileImage.getHeight());
            double x = (profileImage.getWidth() - size) / 2;
            double y = (profileImage.getHeight() - size) / 2;

            // Crear un canvas para el recorte
            Canvas canvas = new Canvas(size, size);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Dibujar la porción central de la imagen
            gc.drawImage(profileImage,
                    x, y,                    // Coordenadas de origen en la imagen fuente
                    size, size,             // Tamaño del área a recortar
                    0, 0,                   // Coordenadas de destino en el canvas
                    size, size              // Tamaño del área de destino
            );

            // Convertir el canvas a una imagen
            WritableImage croppedImage = new WritableImage((int)size, (int)size);
            canvas.snapshot(null, croppedImage);

            // Aplicar la imagen recortada al avatar
            userAvatar.setFill(new ImagePattern(croppedImage));

        } catch (Exception e) {
            System.err.println("Error setting user avatar: " + e.getMessage());
            e.printStackTrace();
            setDefaultAvatarWithInitials();
        }
    }

    private Image loadImageFromPath(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return null;
        }

        try {
            // Try loading as local file
            File imageFile = new File(photoPath);
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString(), false); // false for non-background loading
            }

            // Try loading as classpath resource
            String classPathPath = photoPath.startsWith("/") ? photoPath : "/" + photoPath;
            if (getClass().getResource(classPathPath) != null) {
                return new Image(getClass().getResource(classPathPath).toExternalForm(), false);
            }

            // Try loading as direct URL/path
            return new Image(photoPath, false);

        } catch (Exception e) {
            System.err.println("Failed to load image from path: " + photoPath);
            e.printStackTrace();
            return null;
        }
    }

    private void setDefaultAvatarWithInitials() {
        String initials = getInitials();

        // Create a StackPane for the avatar background and initials
        StackPane avatarPane = new StackPane();
        avatarPane.setMinSize(40, 40);
        avatarPane.setMaxSize(40, 40);

        // Create circular background
        Circle background = new Circle(20);
        background.setFill(Color.BLACK);

        // Create text for initials
        Text initialsText = new Text(initials != null ? initials : "??");
        initialsText.setFill(javafx.scene.paint.Color.WHITE);
        initialsText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");


        // Add both to the StackPane
        avatarPane.getChildren().addAll(background, initialsText);

        // Take a snapshot of the StackPane and use it as the avatar fill
        userAvatar.setFill(new ImagePattern(avatarPane.snapshot(null, null)));
    }

    private String getInitials() {
        if (currentStudent == null || currentStudent.getNombre() == null) {
            return null;
        }

        StringBuilder initials = new StringBuilder();
        String[] nameParts = currentStudent.getNombre().trim().split("\\s+");

        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
                if (initials.length() >= 2) break;
            }
        }

        return initials.length() > 0 ? initials.toString().toUpperCase() : null;
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
        String selectedMealType = mealTimeGroup.getSelectedToggle() != null ?
                (String) mealTimeGroup.getSelectedToggle().getUserData() : null;

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

        CompletableFuture.supplyAsync(() -> {
            try {
                synchronized (LogicSockect.meals) {
                    LogicSockect.meals.clear();
                }
                isReceivingMeals = true;
                lastMealReceived = System.currentTimeMillis();
                previousMealCount = 0;

                // Solicitar las comidas
                SocketClient.sendMessage("listMeals," + selectedDay + "," + selectedMealType);

                // Espera inicial para que comiencen a llegar las comidas
                Thread.sleep(INITIAL_WAIT);

                return waitForCompleteMealSet();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAcceptAsync(this::processReceivedMeals, Platform::runLater);

        startLoadingTimeout();
    }

    private List<Meal> waitForCompleteMealSet() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        List<Meal> receivedMeals = null;
        boolean complete = false;

        while (System.currentTimeMillis() - startTime < LOAD_TIMEOUT && retryCount < MAX_RETRIES && !complete) {
            synchronized (LogicSockect.meals) {
                int currentMealCount = LogicSockect.meals.size();

                // Si hay nuevas comidas, actualizar el tiempo de última recepción
                if (currentMealCount > previousMealCount) {
                    lastMealReceived = System.currentTimeMillis();
                    previousMealCount = currentMealCount;
                }

                // Verificar si el conjunto está completo
                if (!LogicSockect.meals.isEmpty() &&
                        System.currentTimeMillis() - lastMealReceived > MEAL_RECEIVE_TIMEOUT) {
                    receivedMeals = (List<Meal>) new ArrayList<>(LogicSockect.getListMeals());
                    complete = true;
                } else if (System.currentTimeMillis() - lastMealReceived > MEAL_RECEIVE_TIMEOUT * 2) {
                    // Si ha pasado mucho tiempo sin recibir nuevas comidas, reintentar
                    retryCount++;
                    if (retryCount < MAX_RETRIES) {
                        LogicSockect.meals.clear();
                        previousMealCount = 0;
                        SocketClient.sendMessage("listMeals," + cbReservationDay.getValue() + ","
                                + mealTimeGroup.getSelectedToggle().getUserData());
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
            ProgressIndicator progress = new ProgressIndicator();
            Label loadingLabel = new Label("Loading meals...");
            loadingBox.getChildren().addAll(progress, loadingLabel);
            mealsGrid.add(loadingBox, 1, 1);
        });
    }

    // Método para ser llamado desde LogicSocket cuando se recibe una nueva comida
    public void onMealReceived() {
        lastMealReceived = System.currentTimeMillis();
    }

    private List<Meal> waitForMeals() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        List<Meal> receivedMeals = null;

        while (System.currentTimeMillis() - startTime < LOAD_TIMEOUT && retryCount < MAX_RETRIES) {
            synchronized (LogicSockect.meals) {
                if (!LogicSockect.meals.isEmpty()) {
                    receivedMeals = (List<Meal>) new ArrayList<>(LogicSockect.getListMeals());
                    break;
                }
            }

            if (System.currentTimeMillis() - startTime > 2000) {
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    SocketClient.sendMessage("listMeals," + cbReservationDay.getValue() + "," + mealTimeGroup.getSelectedToggle().getUserData());
                    startTime = System.currentTimeMillis();
                }
            }
            Thread.sleep(100);
        }
        return receivedMeals;
    }

    private void syncSelectionWithCart(List<Meal> currentMeals) {
        for (Meal currentMeal : currentMeals) {
            orderItems.stream()
                    .filter(orderItem -> orderItem.getName().equals(currentMeal.getName()))
                    .findFirst()
                    .ifPresent(orderItem -> {
                        VBox mealCard = mealCardMap.get(currentMeal);
                        if (mealCard != null) {
                            mealCard.getStyleClass().add("selected");
                            selectedMeals.put(currentMeal, currentMeal.getQuantity());
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
        for (Meal meal : meals) {
            if (meal == null) continue;
            VBox mealCard = createMealCard(meal);
            mealCardMap.put(meal, mealCard);
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

    private VBox createMealCard(Meal meal) {
        VBox card = new VBox(8);
        card.getStyleClass().add("meal-card");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(100);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));

        Label nameLabel = new Label(meal.getName());
        nameLabel.getStyleClass().add("meal-name");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label priceLabel = new Label(String.format("₡%.2f", (double) meal.getPrice()));
        priceLabel.getStyleClass().add("meal-price");
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox priceContainer = new HBox();
        priceContainer.setAlignment(Pos.CENTER);
        priceContainer.getChildren().add(priceLabel);
        priceContainer.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(nameLabel, priceContainer);

        card.setOnMouseClicked(event -> handleMealSelection(meal, card));

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f0f0f0;"));
        card.setOnMouseExited(e -> card.setStyle(""));

        return card;
    }

    private void handleMealSelection(Meal meal, VBox card) {
        if (orderItems.stream().anyMatch(item -> item.getName().equals(meal.getName()))) return;

        meal.setQuantity(1);
        selectedMeals.put(meal, meal.getQuantity());
        card.getStyleClass().add("selected");
        addToOrderList(meal);
        updateOrderSummary();
    }

    private Node createOrderItemView(Meal meal) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(8));
        container.getStyleClass().add("order-item");

        VBox mealInfo = createMealInfoView(meal);
        HBox quantityControl = createQuantityControlView(meal);

        container.getChildren().addAll(mealInfo, quantityControl);
        HBox.setHgrow(mealInfo, Priority.ALWAYS);

        return container;
    }

    private VBox createMealInfoView(Meal meal) {
        VBox mealInfo = new VBox(4);
        mealInfo.getChildren().addAll(new Label(meal.getName()), new Label(String.format("₡%d", meal.getPrice())));
        return mealInfo;
    }

    private HBox createQuantityControlView(Meal meal) {
        HBox quantityControl = new HBox(8);
        quantityControl.setAlignment(Pos.CENTER);

        Button decreaseBtn = new Button("-");
        Label quantityLabel = new Label(String.valueOf(meal.getQuantity()));
        Button increaseBtn = new Button("+");

        decreaseBtn.setOnAction(e -> updateMealQuantity(meal, quantityLabel, -1));
        increaseBtn.setOnAction(e -> updateMealQuantity(meal, quantityLabel, 1));

        quantityControl.getChildren().addAll(decreaseBtn, quantityLabel, increaseBtn);
        return quantityControl;
    }

    private void updateMealQuantity(Meal meal, Label quantityLabel, int delta) {
        meal.setQuantity(meal.getQuantity() + delta);
        if (meal.getQuantity() < 1) {
            removeMealFromOrder(meal);
        } else {
            selectedMeals.put(meal, meal.getQuantity());
            quantityLabel.setText(String.valueOf(meal.getQuantity()));
        }
        updateOrderSummary();
    }

    private void removeMealFromOrder(Meal meal) {
        selectedMeals.remove(meal);
        removeFromOrderList(meal);
        VBox mealCard = mealCardMap.get(meal);
        if (mealCard != null) {
            mealCard.getStyleClass().remove("selected");
        }
    }

    private void addToOrderList(Meal meal) {
        orderItems.add(meal);
        orderItemsContainer.getChildren().add(createOrderItemView(meal));
    }

    private void removeFromOrderList(Meal meal) {
        orderItems.removeIf(item -> item.getName().equals(meal.getName()));
        updateOrderItemsView();
    }

    private void updateOrderItemsView() {
        orderItemsContainer.getChildren().clear();
        orderItems.forEach(meal -> orderItemsContainer.getChildren().add(createOrderItemView(meal)));
        updateOrderSummary();
    }

    private void updateOrderSummary() {
        double subtotal = calculateSubtotal();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        lblSubtotal.setText(String.format("Subtotal: ₡%.2f", subtotal));
        lblTax.setText(String.format("IVA (13%%): ₡%.2f", tax));
        lblTotal.setText(String.format("Total: ₡%.2f", total));
    }

    private double calculateSubtotal() {
        return selectedMeals.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
    }

    @FXML
    public void handlePurchaseAction(ActionEvent event) {
        if (selectedMeals.isEmpty()) {
            Utils.showAlert(AlertType.ERROR, "Error", "Please select at least one meal.");
            return;
        }

        double total = calculateSubtotal() * (1 + TAX_RATE);
        if (currentStudent.getDineroDisponible() < total) {
            Utils.showAlert(AlertType.INFORMATION, "Insufficient Balance", "You don't have enough balance for this order.");
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
        selectedMeals.forEach((meal, quantity) -> {
            meal.setQuantity(quantity);
            meal.setTotalOrder(meal.getQuantity() * meal.getPrice());
            SocketClient.sendMessage("foodOrder," + meal.toStringPedido() + "," + currentStudent.getCarnet() + "," + currentStudent.getDineroDisponible());
        });

        Utils.showAlert(AlertType.INFORMATION, "Order Confirmed", "Order completed successfully. New balance: ₡" + currentStudent.getDineroDisponible());
        selectedMeals.clear();
        orderItems.clear();
        orderItemsContainer.getChildren().clear();
        updateOrderSummary();
        updateBalance();
        updateGrid(); // Refresh the grid to reset selection states
    }

    public void handleReturnAction(ActionEvent actionEvent) {
        Logic.closeWindows(btReturn,"/presentation/MainGUI.fxml");
    }
}