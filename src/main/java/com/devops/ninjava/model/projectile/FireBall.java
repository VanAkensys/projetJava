package com.devops.ninjava.model.projectile;

import com.devops.ninjava.model.environnement.Ground;
import com.devops.ninjava.model.environnement.Wall;
import com.devops.ninjava.model.enemy.Enemy;
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
    private final int damage = 50;

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

    }

    public boolean handleCollision(Pane object) {
        if (!isActive || !this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            return false;
        }

        if (object instanceof Ground) {
            return handleBrickCollision((Ground) object);
        } else if (object instanceof Wall) {
            return handleWallColision((Wall) object);
        } else if (object instanceof Enemy) {
            handleEnemyCollision((Enemy) object);
            return true;
        }

        return false;
    }

    private boolean handleBrickCollision(Ground ground) {
        double fireBallBottom = this.getLayoutY() + HEIGHT;
        double fireBallTop = this.getLayoutY();
        double fireBallRight = this.getLayoutX() + WIDTH;
        double fireBallLeft = this.getLayoutX();

        double brickBottom = ground.getLayoutY() + ground.getHeight();
        double brickTop = ground.getLayoutY();
        double brickRight = ground.getLayoutX() + ground.getWidth();
        double brickLeft = ground.getLayoutX();

        // Collision par le dessus
        if (fireBallBottom > brickTop && fireBallTop < brickTop &&
                fireBallRight > brickLeft && fireBallLeft < brickRight) {
            velY = -Math.abs(velY) * 0.9; // Rebondir avec une vitesse réduite
            return false; // Ne désactive pas la boule de feu
        }

        // Autres collisions : Désactiver la boule de feu
        deactivate();
        return true;
    }

    private boolean handleWallColision(Wall wall) {
        double fireBallBottom = this.getLayoutY() + HEIGHT;
        double fireBallTop = this.getLayoutY();
        double fireBallRight = this.getLayoutX() + WIDTH;
        double fireBallLeft = this.getLayoutX();

        double brickBottom = wall.getLayoutY() + wall.getHeight();
        double brickTop = wall.getLayoutY();
        double brickRight = wall.getLayoutX() + wall.getWidth();
        double brickLeft = wall.getLayoutX();

        // Collision par le dessus
        if (fireBallBottom > brickTop && fireBallTop < brickTop &&
                fireBallRight > brickLeft && fireBallLeft < brickRight) {
            velY = -Math.abs(velY) * 0.9; // Rebondir avec une vitesse réduite
            return false; // Ne désactive pas la boule de feu
        }

        // Autres collisions : Désactiver la boule de feu
        deactivate();
        return true;
    }



    private void handleEnemyCollision(Enemy enemy) {
        enemy.takeDamage(damage); // Tuer le Goomba
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
