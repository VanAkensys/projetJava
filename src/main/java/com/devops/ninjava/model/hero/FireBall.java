package com.devops.ninjava.model.hero;

import com.devops.ninjava.model.decor.Brick;
import com.devops.ninjava.model.decor.Pipe;
import com.devops.ninjava.model.enemy.Goomba;
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
    private boolean isActive;

    public FireBall(double x, double y, boolean toRight, Image fireballImage) {
        this.velX = toRight ? SPEED : -SPEED; // Direction basée sur `toRight`
        this.velY = 0; // Initialement, pas de mouvement vertical
        this.isActive = true;

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
        if (!isActive) return;
        // Mettre à jour la position en fonction des vitesses
        setLayoutX(getLayoutX() + velX);
        velY += gravity; // Appliquer la gravité
        setLayoutY(getLayoutY() + velY);

        // Vérifier si la boule de feu touche le sol
        if (getLayoutY() >= 500) { // 500 est un exemple de hauteur de sol
            velY = -Math.abs(velY) * 0.6; // Rebondir avec une vitesse réduite
        }
    }

    public boolean handleCollision(Pane object) {
        if (!isActive || !this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            return false;
        }

        if (object instanceof Brick) {
            return handleBrickCollision((Brick) object);
        } else if (object instanceof Goomba) {
            handleGoombaCollision((Goomba) object);
            return true;
        } else if (object instanceof Pipe) {
            return handlePipeCollision((Pipe) object);
        }

        return false;
    }

    private boolean handleBrickCollision(Brick brick) {
        double fireBallBottom = this.getLayoutY() + HEIGHT;
        double fireBallTop = this.getLayoutY();
        double fireBallRight = this.getLayoutX() + WIDTH;
        double fireBallLeft = this.getLayoutX();

        double brickBottom = brick.getLayoutY() + brick.getHeight();
        double brickTop = brick.getLayoutY();
        double brickRight = brick.getLayoutX() + brick.getWidth();
        double brickLeft = brick.getLayoutX();

        // Collision par le dessus
        if (fireBallBottom > brickTop && fireBallTop < brickTop &&
                fireBallRight > brickLeft && fireBallLeft < brickRight) {
            velY = -Math.abs(velY) * 0.8; // Rebondir avec une vitesse réduite
            return false; // Ne désactive pas la boule de feu
        }

        // Autres collisions : Désactiver la boule de feu
        deactivate();
        return true;
    }

    private boolean handlePipeCollision(Pipe pipe) {
        double fireBallBottom = this.getLayoutY() + HEIGHT;
        double fireBallTop = this.getLayoutY();
        double fireBallRight = this.getLayoutX() + WIDTH;
        double fireBallLeft = this.getLayoutX();

        double pipeBottom = pipe.getLayoutY() + pipe.getHeight();
        double pipeTop = pipe.getLayoutY();
        double pipeRight = pipe.getLayoutX() + pipe.getWidth();
        double pipeLeft = pipe.getLayoutX();

        // Collision par le dessus
        if (fireBallBottom > pipeTop && fireBallTop < pipeTop &&
                fireBallRight > pipeLeft && fireBallLeft < pipeRight) {
            velY = -Math.abs(velY) * 0.8; // Rebondir avec une vitesse réduite
            return false; // Ne désactive pas la boule de feu
        }

        // Autres collisions : Désactiver la boule de feu
        deactivate();
        return true;
    }

    private void handleGoombaCollision(Goomba goomba) {
        goomba.die(); // Tuer le Goomba
        deactivate(); // Désactiver la boule de feu
    }

    public void deactivate() {
        this.isActive = false;
        this.setVisible(false); // Masquer la boule de feu
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isOutOfBounds(int worldWidth) {
        return getLayoutX() < 0 || getLayoutX() > worldWidth;
    }

}
