package com.devops.ninjava.model.projectile;

import com.devops.ninjava.model.environnement.Ground;
import com.devops.ninjava.model.environnement.Wall;
import com.devops.ninjava.model.hero.Player;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class EnemyFireball extends Pane {

    private static final int WIDTH = 24;
    private static final int HEIGHT = 24;
    private static final double SPEED = 6; // Vitesse de la boule de feu
    private final double gravity = 0.2; // Gravité pour simuler la descente progressive
    private final int damage = 30;

    private double velX; // Vitesse horizontale
    private double velY; // Vitesse verticale
    private ImageView fireBallView;
    private boolean isActive;

    public EnemyFireball(double x, double y, double directionX, double directionY, Image fireballImage) {
        this.velX = directionX * SPEED; // Direction basée sur un facteur normalisé
        this.velY = directionY * SPEED;
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

        // Mise à jour de la position en fonction des vitesses
        setLayoutX(getLayoutX() + velX);
        velY += gravity; // Appliquer la gravité
        setLayoutY(getLayoutY() + velY);
    }

    public boolean handleCollision(Pane object) {
        if (!isActive || !this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            return false;
        }

        if (object instanceof Player) {
            handlePlayerCollision((Player) object);
            return true;
        } else if (object instanceof Ground || object instanceof Wall) {
            deactivate(); // Désactiver sur collision avec le sol ou les murs
            return true;
        }
        return false;
    }

    private void handlePlayerCollision(Player player) {
        player.onTouchEnemy(); // Infliger des dégâts au joueur
        deactivate();
    }

    public void deactivate() {
        this.isActive = false;
        this.setVisible(false); // Masquer la boule de feu
    }

    public boolean isActive() {
        return isActive;
    }
}
