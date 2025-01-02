package com.devops.ninjava.model.enemy;

import com.devops.ninjava.model.decor.Brick;
import com.devops.ninjava.model.decor.Pipe;
import com.devops.ninjava.model.hero.Player;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public abstract class Enemy extends Pane {

    protected double velX;  // Vitesse horizontale
    protected double velY;  // Vitesse verticale
    protected boolean isDead = false;

    protected int width;  // Largeur de l'ennemi
    protected int height; // Hauteur de l'ennemi
    protected ImageView imageView; // Gestion de l'affichage des sprites

    public Enemy(double x, double y, int width, int height) {
        this.width = width;
        this.height = height;

        initializeEnemyView(x, y);
    }

    private void initializeEnemyView(double x, double y) {
        imageView = new ImageView();
        imageView.setFitWidth(width * 2);
        imageView.setFitHeight(height * 2);
        this.getChildren().add(imageView);

        // Position initiale
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void setImageViewFrame(javafx.scene.image.Image frame) {
        imageView.setImage(frame); // Change l'image affichée
    }
    // Méthode abstraite pour que chaque ennemi définisse son propre comportement
    public abstract void update();

    // Gestion des collisions avec un objet générique
    public void handleCollision(Pane object) {
        if (this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            if (object instanceof Player) {
                handleCollisionWithPlayer((Player) object);
            } else if (object instanceof Brick || object instanceof Pipe) {
                handleCollisionWithDecor(object);
            }
        }
    }

    // Gestion des collisions avec le joueur
    public void handleCollisionWithPlayer(Player player) {
        if (this.getBoundsInParent().intersects(player.getBoundsInParent())) {
            double enemyBottom = this.getLayoutY() + height;
            double playerTop = player.getLayoutY();

            // Si le joueur saute sur l'ennemi
            if (playerTop < enemyBottom && player.getVelY() > 0) {
                die();
                player.setVelY(-8); // Rebond du joueur
            } else {
                player.onTouchEnemy(); // Le joueur perd une vie
            }
        }
    }

    // Gestion des collisions avec les décors (briques, tuyaux, etc.)
    public void handleCollisionWithDecor(Pane decor) {
        // Inverser la direction horizontale lors d'une collision
        velX = -velX;
    }

    public void die() {
        isDead = true;
        this.setVisible(false); // Masquer l'ennemi après la mort
    }

    public boolean isDead() {
        return isDead;
    }
}
