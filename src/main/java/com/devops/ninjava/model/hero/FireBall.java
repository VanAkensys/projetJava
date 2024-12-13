package com.devops.ninjava.model.hero;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class FireBall extends Pane {

    private static final int WIDTH = 24;
    private static final int HEIGHT = 24;
    private static final double SPEED = 8; // Vitesse de la boule de feu
    private double velX; // Vitesse horizontale
    private double velY; // Vitesse verticale
    private final double gravity = 0.3; // Gravité pour faire descendre la boule de feu

    private ImageView fireBallView;

    public FireBall(double x, double y, boolean toRight, Image fireballImage) {
        this.velX = toRight ? SPEED : -SPEED; // Direction basée sur `toRight`
        this.velY = 0; // Initialement, pas de mouvement vertical

        // Initialisation de l'image de la boule de feu
        fireBallView = new ImageView(fireballImage);
        fireBallView.setFitWidth(WIDTH);
        fireBallView.setFitHeight(HEIGHT);

        this.getChildren().add(fireBallView);

        // Positionner la boule de feu à sa position de départ
        setLayoutX(x);
        setLayoutY(y);
    }

    public void update() {
        // Mettre à jour la position en fonction des vitesses
        setLayoutX(getLayoutX() + velX);
        velY += gravity; // Appliquer la gravité
        setLayoutY(getLayoutY() + velY);

        // Vérifier si la boule de feu touche le sol
        if (getLayoutY() >= 500) { // 500 est un exemple de hauteur de sol
            velY = -Math.abs(velY) * 0.6; // Rebondir avec une vitesse réduite
        }
    }
}
