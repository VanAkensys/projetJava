package com.devops.ninjava.model.enemy;

import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class Goomba extends Pane {

    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;
    private static final double DEFAULT_SPEED = 2.0;

    private boolean isDead = false;

    private double velX;
    private boolean movingLeft;
    private Image[] walkFrames;
    private ImageView goombaView;
    private int floor;

    public Goomba(double x, double y) {
        this.velX = DEFAULT_SPEED;
        this.movingLeft = true;
        this.floor = (int) (550 - 85);

        initializeImages();
        initializeGoombaView(x, floor + 15);
    }

    private void initializeImages() {
        try {
            walkFrames = new Image[]{
                    new Image(getClass().getResource("/images/enemy/enemy1.png").toExternalForm()),
                    new Image(getClass().getResource("/images/enemy/enemy2.png").toExternalForm())
            };
        } catch (Exception e) {
            System.err.println("Error loading Goomba images: " + e.getMessage());
        }
    }

    private void initializeGoombaView(double x, double y) {
        goombaView = new ImageView(walkFrames[0]);
        goombaView.setFitWidth(WIDTH);
        goombaView.setFitHeight(HEIGHT);
        this.getChildren().add(goombaView);

        // Position initiale
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void update() {
        // Mise à jour de la position horizontale
        if (isDead) return; // Ne pas mettre à jour si le Goomba est mort

        if (movingLeft) {
            setLayoutX(getLayoutX() - velX);
        } else {
            setLayoutX(getLayoutX() + velX);
        }

        // Logique pour inverser la direction si le Goomba atteint un bord
        if (getLayoutX() <= 0 || getLayoutX() + WIDTH >= 10000) { // Exemple de limites
            movingLeft = !movingLeft;
        }

        // Mise à jour de l'animation
        animateMovement();
    }

    private void animateMovement() {
        int frameIndex = (int) (Math.abs(getLayoutX() / 10) % walkFrames.length);
        goombaView.setImage(walkFrames[frameIndex]);
    }

    public void onCollision() {
        // Gestion de la collision, par exemple avec un joueur ou un objet
        System.out.println("Goomba collided!");
    }

    public void setSpeed(double speed) {
        this.velX = speed;
    }

    public void die() {
        isDead = true;
        goombaView.setImage(new Image(getClass().getResource("/images/enemy/enemyDead.png").toExternalForm()));

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> this.setVisible(false));
        delay.play();
    }

    public boolean isDead() {
        return isDead;
    }



}
