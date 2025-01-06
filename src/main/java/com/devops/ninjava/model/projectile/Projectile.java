package com.devops.ninjava.model.projectile;

import com.devops.ninjava.model.decor.Ground;
import com.devops.ninjava.model.enemy.Enemy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public abstract class Projectile extends Pane {

    protected double velX; // Vitesse horizontale
    protected double velY; // Vitesse verticale
    protected boolean isActive;
    protected final double gravity = 0.3; // Gravité pour simuler la chute

    protected ImageView projectileView;

    public Projectile(double x, double y, boolean toRight, Image initialImage) {
        this.velX = toRight ? 8 : -8; // Direction et vitesse initiales
        this.velY = 0; // Initialement pas de mouvement vertical
        this.isActive = true;

        projectileView = new ImageView(initialImage);

        projectileView.setFitWidth(initialImage.getWidth() * 2);
        projectileView.setFitHeight(initialImage.getHeight() * 2);

        this.getChildren().add(projectileView);

        // Positionner le projectile au point de départ
        setLayoutX(x);
        setLayoutY(y);
    }

    public void update() {
        if (!isActive) return;

        setLayoutX(getLayoutX() + velX);
    }

    public boolean isActive() {
        return isActive;
    }

    public void deactivate() {
        this.isActive = false;
        this.setVisible(false);
    }

    public boolean handleCollision(Pane object) {
        if (!isActive || !this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            return false;
        }

        if (object instanceof Ground) {
            handleBrickCollision((Ground) object);
        }else if (object instanceof Enemy) {
            handleEnemyCollision((Enemy) object);
        }
        return false;
    }

    // Gestion des collisions avec une brique
    protected boolean handleBrickCollision(Ground ground) {
        deactivate(); // Par défaut, désactive le projectile après une collision
        return true;
    }


    // Gestion des collisions avec un ennemi
    protected boolean handleEnemyCollision(Enemy enemy) {
        enemy.die(); // Tuer le Goomba
        deactivate(); // Désactiver le projectile après l'impact
        return true;
    }
}
