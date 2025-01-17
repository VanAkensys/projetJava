package com.devops.ninjava.model.decor;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public abstract class Ground extends Pane {

    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;

    protected ImageView groundView;
    private int floor;
    private boolean isBroken;

    public Ground(double x, double y, double width, double height) {

        initializeImages();
        initializeView(x, y,width,height); // Initialisation de la vue de la brique
    }

    protected abstract void initializeImages();

    private void initializeView(double x, double y, double width, double height) {
        groundView.setFitWidth(width); // Définir la largeur de l'image
        groundView.setFitHeight(height); // Définir la hauteur de l'image
        this.getChildren().add(groundView); // Ajouter l'image au Pane

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