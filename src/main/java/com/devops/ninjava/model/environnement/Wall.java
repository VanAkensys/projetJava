package com.devops.ninjava.model.environnement;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public abstract class Wall extends Pane {


    protected ImageView wallView;

    public Wall(double x, double y, double width, double height) {

        initializeImages();
        initializeView(x, y,width,height); // Initialisation de la vue de la brique
    }

    protected abstract void initializeImages();

    private void initializeView(double x, double y, double width, double height) {
        wallView.setFitWidth(width); // Définir la largeur de l'image
        wallView.setFitHeight(height); // Définir la hauteur de l'image
        this.getChildren().add(wallView); // Ajouter l'image au Pane

        // Positionner la brique sur la scène
        this.setLayoutX(x);
        this.setLayoutY(y);
    }
}