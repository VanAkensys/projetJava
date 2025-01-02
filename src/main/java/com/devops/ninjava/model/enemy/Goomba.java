package com.devops.ninjava.model.enemy;

import com.devops.ninjava.model.decor.Brick;
import com.devops.ninjava.model.decor.Pipe;
import com.devops.ninjava.model.hero.FireBall;
import com.devops.ninjava.model.hero.Player;
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

    private static final double GRAVITY = 0.3; // Gravité appliquée au Goomba
    private double velY = 0; // Vitesse verticale
    private boolean isFalling = true;

    public Goomba(double x, double y) {
        this.velX = DEFAULT_SPEED;
        this.movingLeft = true;
        this.floor = (int) (550 - 85);

        initializeImages();
        initializeGoombaView(x, y);
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

        if (isFalling) {
            velY += GRAVITY; // Augmenter la vitesse verticale avec la gravité
            setLayoutY(getLayoutY() + velY);
        }

        // Détection des collisions avec le sol (ou d'autres objets)
        if (getLayoutY() >= floor) { // Collision avec le sol
            isFalling = false;
            velY = 0; // Arrêter le mouvement vertical
            setLayoutY(floor); // Positionner le Goomba sur le sol
        } else {
            isFalling = true; // Le Goomba est en train de tomber
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

    public void onCollision(Pane object) {
        if (object instanceof FireBall) {
            handleFireBallCollision((FireBall) object);
        } else if (object instanceof Brick) {
            handleBrickCollision((Brick) object);
        } else if (object instanceof Player) {
            handlePlayerCollision((Player) object);
        } else if (object instanceof Pipe) {
            handlePipeCollision((Pipe) object);
        }
    }

    private void handleFireBallCollision(FireBall fireBall) {
        if (fireBall.isActive()) {
            die(); // Le Goomba meurt
            fireBall.deactivate(); // La boule de feu disparaît
            System.out.println("Goomba killed by FireBall!");
        }
    }

    private void handleBrickCollision(Brick brick) {
        if (this.getBoundsInParent().intersects(brick.getBoundsInParent())) {
            // Bloquer le Goomba
            if (movingLeft) {
                movingLeft = false;
                setLayoutX(getLayoutX() + 5); // Ajustement pour éviter la collision
            } else {
                movingLeft = true;
                setLayoutX(getLayoutX() - 5);
            }
        }
    }

    private void handlePlayerCollision(Player player) {
        if (this.getBoundsInParent().intersects(player.getBoundsInParent())) {
            double goombaBottom = this.getLayoutY() + HEIGHT;
            double playerTop = player.getLayoutY();

            // Si le joueur saute sur le Goomba
            if (playerTop < goombaBottom && player.getVelY() > 0) {
                die(); // Le Goomba meurt
                player.setVelY(-8); // Le joueur rebondit
                System.out.println("Player jumped on Goomba!");
            } else {
                player.onTouchEnemy(); // Le joueur perd une vie
                System.out.println("Goomba hit the Player!");
            }
        }
    }

    private void handlePipeCollision(Pipe pipe) {
        if (this.getBoundsInParent().intersects(pipe.getBoundsInParent())) {
            // Bloquer le Goomba
            if (movingLeft) {
                movingLeft = false;
                setLayoutX(getLayoutX() + 5); // Ajustement pour éviter la collision
            } else {
                movingLeft = true;
                setLayoutX(getLayoutX() - 5);
            }
        }
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
