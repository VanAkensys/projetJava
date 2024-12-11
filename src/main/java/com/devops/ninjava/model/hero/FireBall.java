package com.devops.ninjava.model.hero;

import com.devops.ninjava.model.GameObject;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FireBall extends GameObject {

    private static final int WIDTH = 24;
    private static final int HEIGHT = 24;
    private static final double SPEED = 10; // Vitesse de la boule de feu

    public FireBall(double x, double y, Image image, boolean toRight) {
        super(x, y, WIDTH, HEIGHT);
        this.imageView = new ImageView(image);
        this.imageView.setFitWidth(WIDTH);
        this.imageView.setFitHeight(HEIGHT);
        setVelX(toRight ? SPEED : -SPEED);  // Définit la vitesse horizontale
        updateImageViewPosition();  // Met à jour la position initiale de l'ImageView
    }

    @Override
    public void update() {
        x += velX;  // Met à jour la position horizontale
        updateImageViewPosition();  // Met à jour la position de l'ImageView
    }
}
