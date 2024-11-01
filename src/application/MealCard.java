package application;

import domain.Meal;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class MealCard extends VBox {
    private static final ConcurrentHashMap<String, WeakReference<Image>> IMAGE_CACHE = new ConcurrentHashMap<>();

    @FXML private ImageView mealImage;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label quantityLabel;
    @FXML private Button decrementButton;
    @FXML private Button incrementButton;
    @FXML private Button addToCartButton;
    @FXML private HBox quantityContainer;
    @FXML private StackPane imageContainer;

    private Image originalImage;
    private int quantity = 0;
    private final Meal meal;
    private boolean imageLoaded = false;
    private ChangeListener<Number> sizeChangeListener;

    public MealCard(Meal meal) {
        this.meal = meal;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/MealCard.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initializeCard();
    }

    private void initializeCard() {
        nameLabel.setText(meal.getName());
        priceLabel.setText("₡" + meal.getPrice());
        updateQuantityLabel();

        mealImage.setPreserveRatio(false);
        mealImage.fitWidthProperty().bind(imageContainer.widthProperty());
        mealImage.fitHeightProperty().bind(imageContainer.heightProperty());

        setupImageLoading();
        setupButtonHandlers();
        setupSizeListeners();

        quantityContainer.setVisible(false);
        quantityContainer.setManaged(false);
        updateButtonStates();
    }

    private void setupSizeListeners() {
        sizeChangeListener = (obs, oldVal, newVal) -> {
            if (imageContainer.getWidth() > 0 && imageContainer.getHeight() > 0) {
                if (!imageLoaded) {
                    loadImageAsync();
                } else if (originalImage != null) {
                    updateImage();
                }
            }
        };

        imageContainer.widthProperty().addListener(sizeChangeListener);
        imageContainer.heightProperty().addListener(sizeChangeListener);
    }

    private void setupImageLoading() {
        if (imageContainer.getWidth() > 0 && !imageLoaded) {
            loadImageAsync();
        }
    }

    private void loadImageAsync() {
        if (imageLoaded) return;

        Thread imageLoader = new Thread(() -> {
            try {
                Image image = getCachedImage(meal.getImagePath());
                if (image != null && !image.isError()) {
                    originalImage = image;
                    javafx.application.Platform.runLater(this::updateImage);
                    imageLoaded = true;
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                javafx.application.Platform.runLater(() ->
                        mealImage.setStyle("-fx-background-color: #f8f9fa;"));
            }
        });
        imageLoader.setDaemon(true);
        imageLoader.start();
    }

    private Image getCachedImage(String imagePath) {
        WeakReference<Image> imageRef = IMAGE_CACHE.get(imagePath);
        Image image = imageRef != null ? imageRef.get() : null;

        if (image == null) {
            try {
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    System.err.println("Image file not found: " + imagePath);
                    return null;
                }
                String imageUrl = imageFile.toURI().toString();
                image = new Image(imageUrl);

                if (!image.isError()) {
                    IMAGE_CACHE.put(imagePath, new WeakReference<>(image));
                }
            } catch (Exception e) {
                System.err.println("Exception loading image: " + e.getMessage());
                return null;
            }
        }
        return image;
    }

    private void updateImage() {
        if (originalImage == null || imageContainer.getWidth() <= 0 || imageContainer.getHeight() <= 0) {
            return;
        }
        WritableImage croppedImage = cropAndScaleImage(originalImage, imageContainer.getWidth(), imageContainer.getHeight());
        mealImage.setImage(croppedImage);
    }

    private WritableImage cropAndScaleImage(Image sourceImage, double targetWidth, double targetHeight) {
        double sourceRatio = sourceImage.getWidth() / sourceImage.getHeight();
        double targetRatio = targetWidth / targetHeight;

        double srcX = 0;
        double srcY = 0;
        double srcWidth = sourceImage.getWidth();
        double srcHeight = sourceImage.getHeight();

        if (sourceRatio > targetRatio) {
            srcWidth = sourceImage.getHeight() * targetRatio;
            srcX = (sourceImage.getWidth() - srcWidth) / 2;
        } else {
            srcHeight = sourceImage.getWidth() / targetRatio;
            srcY = (sourceImage.getHeight() - srcHeight) / 2;
        }

        Canvas canvas = new Canvas(targetWidth, targetHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(sourceImage, srcX, srcY, srcWidth, srcHeight, 0, 0, targetWidth, targetHeight);

        return canvas.snapshot(null, null);
    }

    private void setupButtonHandlers() {
        decrementButton.setOnAction(e -> decrementQuantity());
        incrementButton.setOnAction(e -> incrementQuantity());
        addToCartButton.setOnAction(e -> addToCart());
    }

    private void decrementQuantity() {
        if (quantity > 0) {
            quantity--;
            updateQuantityLabel();
            updateButtonStates();

            if (quantity == 0) {
                showAddToCartButton();
                ServiceViewGUIController.removeFromCart(meal.getName());
            } else {
                updateMealAndCart();
            }
        }
    }

    private void incrementQuantity() {
        quantity++;
        updateQuantityLabel();
        updateButtonStates();
        updateMealAndCart();
    }

    private void updateMealAndCart() {
        meal.setCantidad(quantity);
        meal.setTotalOrder(quantity * meal.getPrice());
        ServiceViewGUIController.addToCart(meal, this);
    }

    public void restoreState(int quantity) {
        this.quantity = quantity;
        updateQuantityLabel();
        showQuantityControls();
        updateButtonStates();
    }

    public void updateQuantityFromCart(int quantity) {
        this.quantity = quantity;
        updateQuantityLabel();
        if (quantity > 0) {
            showQuantityControls();
        } else {
            showAddToCartButton();
        }
        updateButtonStates();
    }

    private void updateQuantityLabel() {
        quantityLabel.setText(String.valueOf(quantity));
    }

    private void updateButtonStates() {
        decrementButton.setDisable(quantity == 0);
    }

    private void showQuantityControls() {
        // Hacer visible el contenedor de cantidad y ocular el botón de agregar
        quantityContainer.setVisible(true);
        quantityContainer.setManaged(true);
        addToCartButton.setVisible(false);
        addToCartButton.setManaged(false);

        // Actualizar el orden en el StackPane
        addToCartButton.toBack();
        quantityContainer.toFront();
    }

    private void showAddToCartButton() {
        // Hacer visible el botón de agregar y ocultar el contenedor de cantidad
        quantityContainer.setVisible(false);
        quantityContainer.setManaged(false);
        addToCartButton.setVisible(true);
        addToCartButton.setManaged(true);

        // Actualizar el orden en el StackPane
        quantityContainer.toBack();
        addToCartButton.toFront();
    }

    private void addToCart() {
        quantity = 1;
        updateQuantityLabel();
        showQuantityControls();
        updateMealAndCart();
    }

    public void cleanup() {
        if (sizeChangeListener != null) {
            imageContainer.widthProperty().removeListener(sizeChangeListener);
            imageContainer.heightProperty().removeListener(sizeChangeListener);
        }
    }
}