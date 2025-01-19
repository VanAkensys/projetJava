package com.devops.ninjava.model.item;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public abstract class Item extends Pane {


    protected ImageView itemView;

    public Item(double x, double y, double width, double height) {

        initializeImages();
        initializeView(x, y,width,height); // Initialisation de la vue de la brique
    }

    protected abstract void initializeImages();

    private void initializeView(double x, double y, double width, double height) {
        itemView.setFitWidth(width); // Définir la largeur de l'image
        itemView.setFitHeight(height); // Définir la hauteur de l'image
        this.getChildren().add(itemView); // Ajouter l'image au Pane

        // Positionner la brique sur la scène
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public abstract void applyEffect(com.devops.ninjava.model.hero.Player player);
}