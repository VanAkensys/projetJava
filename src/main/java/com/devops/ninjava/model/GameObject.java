package com.devops.ninjava.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;

public abstract class GameObject {
    protected double x, y;
    protected double velX, velY;
    protected int width, height;
    protected ImageView imageView;  // ImageView pour afficher l'image en JavaFX
    private boolean falling, jumping;

    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.velX = 0;
        this.velY = 0;


    }

    public abstract void update();  // Méthode pour mettre à jour la logique du jeu

    protected void updateImageViewPosition() {
        if (imageView != null) {
            imageView.setLayoutX(x);
            imageView.setLayoutY(y);
        }
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public ImageView getImageView() {
        return imageView;
    }
}


