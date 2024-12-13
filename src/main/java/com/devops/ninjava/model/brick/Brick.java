package com.devops.ninjava.model.brick;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Brick extends Pane {

    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;

    private ImageView brickView;
    private int floor;
    private boolean isBroken;

    public Brick(double x, double y) {
        this.floor = (int) (575 - 225); // Position de base sur le sol

        initializeImages();
        initializeBrickView(x, floor + 15); // Initialisation de la vue de la brique
    }

    private void initializeImages() {
        try {
            // Initialiser l'Image et assigner à ImageView
            Image brickImage = new Image(getClass().getResource("/images/wall/wall.png").toExternalForm());
            brickView = new ImageView(brickImage);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
        }
    }

    private void initializeBrickView(double x, double y) {
        brickView.setFitWidth(WIDTH); // Définir la largeur de l'image
        brickView.setFitHeight(HEIGHT); // Définir la hauteur de l'image
        this.getChildren().add(brickView); // Ajouter l'image au Pane

        // Positionner la brique sur la scène
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void breakBrick() {
        if (!isBroken) {
            isBroken = true;
            this.setVisible(false); // Cache la brique cassée
        }
    }

    public boolean isBroken() {
        return isBroken;
    }
}