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
    private double velocityY;  // Vitesse verticale
    private final double gravity = 0.5; // Gravité constante
    private final double jumpPower = 10;
    private final int maxJumpHeight = 100;

    private Image[] walkRightFrames; // Images pour l'animation droite
    private Image[] walkLeftFrames;  // Images pour l'animation gauche
    private ImageView playerView;

    public Player(double x, double y) {
        this.remainingLives = 3;
        this.coins = 0;
        this.points = 0;
        this.floor = 500;
        this.isJumping = false;
        this.isFalling = false;
        this.jumpHeight = 5;
        this.jumpCounter = 0;
        this.velocityY = 0;

        initializeImages();
        initializePlayerView(x, y);
        setLayoutX(x);
        setLayoutY(y);
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
        velX = 5;
        animateMovement(true);
    }

    public void moveLeft() {
        velX = -5;
        animateMovement(false);
    }

    public void moveUp() {
        setLayoutY(getLayoutY() - 10); // Déplacement vertical vers le haut
    }

    private void animateMovement(boolean toRight) {
        Image[] frames = toRight ? walkRightFrames : walkLeftFrames;
        int frameIndex = (int) (Math.abs(getLayoutX()) / 10) % frames.length; // Change d'image toutes les 10 unités
        playerView.setImage(frames[frameIndex]);

        // Mise à jour de la position
        setLayoutX(getLayoutX() + velX);
    }

    public void startJump() {
        if (!isJumping && !isFalling) { // Saut uniquement si le joueur est au sol
            isJumping = true;
            velocityY = -jumpPower; // Applique une vitesse initiale vers le haut
        }
    }

    public void updateJump() {
        if (isJumping || isFalling) {
            velocityY += gravity;
            setLayoutY(getLayoutY() + velocityY);

            if (getLayoutY() >= floor) {
                setLayoutY(floor);
                isJumping = false;
                isFalling = false;
                velocityY = 0;
            }

            if (velocityY > 0) {
                isJumping = false;
                isFalling = true;
            }
        }
    }

    public void stopFalling(double groundY) {
        isFalling = false;
        isJumping = false;
        velY = 0;
        setLayoutY(groundY); // Positionner sur le sol
    }


    public void jump() {
            velocityY = -jumpPower;
    }

    public void update() {
        // Appliquer la gravité
        velocityY += gravity;
        setLayoutY(getLayoutY() + velocityY);

        // Vérifier si le joueur touche le sol
        if (getLayoutY() >= floor) {
            setLayoutY(floor);
            velocityY = 0; // Réinitialiser la vitesse verticale au sol
        }
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
