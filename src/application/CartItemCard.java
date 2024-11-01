package application;

import domain.Meal;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class CartItemCard extends HBox {
    private final Meal meal;

    public CartItemCard(Meal meal) {
        this.meal = meal;
        this.getStyleClass().add("cart-item");
        this.setSpacing(8.0);

        Label nameLabel = new Label(meal.getName());
        nameLabel.getStyleClass().add("cart-item-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label quantityLabel = new Label(String.valueOf(meal.getCantidad()));
        quantityLabel.getStyleClass().add("cart-item-quantity");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priceLabel = new Label(String.format("%,d", meal.getTotalOrder()));
        priceLabel.getStyleClass().add("cart-item-price");
        priceLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(priceLabel, Priority.ALWAYS);

        this.getChildren().addAll(nameLabel, quantityLabel, spacer, priceLabel);
    }

    public Meal getMeal() {
        return meal;
    }
}