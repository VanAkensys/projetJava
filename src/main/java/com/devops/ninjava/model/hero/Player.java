package com.devops.ninjava.model.hero;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Player extends Pane {

    private int remainingLives;
    private int coins;
    private int points;
    private boolean isJumping;
    private boolean isFalling;
    private double velX;
    private double velY;
    private int floor; // Hauteur minimale atteignable par le joueur
    private int jumpHeight; // Hauteur du saut
    private int jumpCounter;

    private Image[] walkRightFrames; // Images pour l'animation droite
    private Image[] walkLeftFrames;  // Images pour l'animation gauche
    private Image[] jumpRightFrames;
    private Image[] jumpLeftFrames;
    private ImageView playerView;

    public Player(double x, double y) {
        this.remainingLives = 3;
        this.coins = 0;
        this.points = 0;
        this.floor = (int) (550 - 85);
        this.isJumping = false;
        this.isFalling = false;
        this.jumpHeight = 5;
        this.jumpCounter = 0;

        initializeImages();
        initializePlayerView(x, floor - 50);
    }

    private void initializeImages() {
        try {
            walkRightFrames = new Image[]{
                    new Image(getClass().getResource("/images/player/marioRight0Lvl1.png").toExternalForm()),
                    new Image(getClass().getResource("/images/player/marioRight1Lvl1.png").toExternalForm()),
                    new Image(getClass().getResource("/images/player/marioRight2Lvl1.png").toExternalForm()),
                    new Image(getClass().getResource("/images/player/marioRight3Lvl1.png").toExternalForm())
            };

            walkLeftFrames = new Image[]{
                    new Image(getClass().getResource("/images/player/marioLeft0Lvl1.png").toExternalForm()),
                    new Image(getClass().getResource("/images/player/marioLeft1Lvl1.png").toExternalForm()),
                    new Image(getClass().getResource("/images/player/marioLeft2Lvl1.png").toExternalForm()),
                    new Image(getClass().getResource("/images/player/marioLeft3Lvl1.png").toExternalForm())
            };
            jumpRightFrames = new Image[]{
                    new Image(getClass().getResource("/images/player/marioRight4Lvl1.png").toExternalForm()),
            };

            jumpLeftFrames = new Image[]{
                    new Image(getClass().getResource("/images/player/marioLeft4Lvl1.png").toExternalForm()),
            };
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private void initializePlayerView(double x, double y) {
        playerView = new ImageView(walkRightFrames[0]); // Image par défaut
        playerView.setFitWidth(48);
        playerView.setFitHeight(48);
        this.getChildren().add(playerView); // Ajoute l'ImageView au Pane

        // Position initiale
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void moveRight() {
        velX = 3;
        animateMovement(true);
    }

    public void moveLeft() {
        velX = -3;
        animateMovement(false);
    }

    private void animateMovement(boolean toRight) {
        Image[] frames = toRight ? walkRightFrames : walkLeftFrames;
        int frameIndex = (int) (Math.abs(getLayoutX()) / 10) % frames.length; // Change d'image toutes les 10 unités
        playerView.setImage(frames[frameIndex]);

        // Mise à jour de la position
        setLayoutX(getLayoutX() + velX);
    }

    public void jump() {
        if (!isJumping && !isFalling) {
            System.out.println("Jump triggered!");
            isJumping = true;
            velY = -10; // Vitesse initiale pour le saut
        } else {
            System.out.println("Already jumping or falling");
        }
    }

    public void stopFalling(double groundY) {
        System.out.println("Stopping falling");
        isFalling = false;
        isJumping = false;
        velY = 0;
        setLayoutY(groundY); // Positionner sur le sol

        if (velX > 0) {
            animateMovement(true); // Animation vers la droite
        } else if (velX < 0) {
            animateMovement(false); // Animation vers la gauche
        } else {
            playerView.setImage(walkRightFrames[0]); // Image par défaut
        }

    }


    public void update() {

        // Appliquer la gravité
        velY += 0.5; // Gravité qui augmente la vitesse verticale
        setLayoutY(getLayoutY() + velY);

        // Gestion des sauts
        if (isJumping) {
            System.out.println("Jumping...");
            animateJump(velX >= 0);
            if (velY >= 0) { // Lorsque la vitesse devient positive, le joueur commence à tomber
                isJumping = false;
                isFalling = true;
            }
        }

        if (isFalling) {
            System.out.println("Falling...");
            animateJump(velX >= 0);
        }

        // Vérification des limites avec le sol
        if (getLayoutY() >= floor) {
            stopFalling(floor); // Arrêter la chute et repositionner le joueur au sol
        }
        // Mise à jour de la position horizontale
        setLayoutX(getLayoutX() + velX);

    }
    private void animateJump(boolean toRight) {
        Image[] frames = toRight ? jumpRightFrames : jumpLeftFrames;
        playerView.setImage(frames[0]); // Pour simplifier, on prend la première image de saut
    }


    public void stop() {
        velX = 0; // Arrêter le mouvement horizontal
    }

    // Gestion des interactions avec des ennemis ou des objets
    public void onTouchEnemy() {
        if (remainingLives > 0) {
            remainingLives--;
        }
    }


    public void acquireCoin() {
        coins++;
    }

    public void acquirePoints(int points) {
        this.points += points;
    }

    // Getters pour les propriétés spécifiques
    public int getRemainingLives() {
        return remainingLives;
    }

    public int getCoins() {
        return coins;
    }

    public int getPoints() {
        return points;
    }

    public double getX() {
        return getLayoutX();
    }

    public void setX(double x) {
        setLayoutX(x);
    }


    public double getY() {
        return getLayoutY();
    }

    public void setY(double y) {
        setLayoutY(y);
    }


    // Getter et Setter pour velX
    public double getVelX() {
        return velX;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    // Getter et Setter pour velY
    public double getVelY() {
        return velY;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

}
