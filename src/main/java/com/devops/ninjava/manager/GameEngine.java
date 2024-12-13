package com.devops.ninjava.manager;

import com.devops.ninjava.model.Camera;
import com.devops.ninjava.model.brick.Brick;
import com.devops.ninjava.model.enemy.Goomba;
import com.devops.ninjava.model.hero.FireBall;
import com.devops.ninjava.model.hero.Player;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GameEngine extends Application  {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int WORLD_WIDTH = 2000; // Largeur du monde
    private static final int WORLD_HEIGHT = 600; // Hauteur du monde



    private boolean isRunning;
    private GameStatus gameStatus;



    //gestion de l'affichage java fx
    private Pane backgroundContainer;
    private Pane gameContainer; // Conteneur pour tout le monde du jeu
    private Pane root;
    private Rectangle ground;
    private Label scoreLabel;
    private double backgroundX = 0; // Position horizontale du background
    private Image background;

    private Canvas canvas;

    //éléments du jeu
    private Player player;
    private List<Goomba> goombas;
    private List<Goomba> goombasToRemove = new ArrayList<>();
    private List<FireBall> fireBalls = new ArrayList<>();
    private List<FireBall> fireBallsToRemove = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<Brick> toRemove = new ArrayList<>();
    private Camera camera;





    @Override
    public void start(Stage stage) {


        // Initialisation de la scène et du conteneur principal
        root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        backgroundContainer = new Pane();
        gameContainer = new Pane();


        canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);

        // Initialisation du joueur
        player = new Player(100, 300);
        gameContainer.getChildren().add(player);

        // Initialisation du terrain
        loadBackground();
        drawInitialBackground();

        // Initialisation de l'état du jeu
        gameStatus = GameStatus.RUNNING;

        // Afficher le score
        initializeScoreLabel();
        initializeGoombas();
        initializeBricks();
        gameContainer.getChildren().addAll(goombas);
        gameContainer.getChildren().addAll(bricks);
        camera = new Camera(WIDTH, HEIGHT, WORLD_WIDTH, WORLD_HEIGHT);

        root.getChildren().addAll(backgroundContainer, gameContainer);


        // Gestion des entrées utilisateur
        scene.setOnKeyPressed(event -> handleKeyPressed(event.getCode().toString()));
        scene.setOnKeyReleased(event -> handleKeyReleased(event.getCode().toString()));

        // Boucle de jeu
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning && gameStatus == GameStatus.RUNNING) {
                    player.update(); // Mise à jour de l'état du joueur
                    updateGoombas();
                    updateScore(); // Mise à jour du score
                    updateBricks();
                    updateFireballs();
                    updateCamera();


                }
            }
        };
        // Démarrage du jeu
        gameLoop.start();
        isRunning = true;

        stage.setScene(scene);
        stage.setTitle("Advanced Game Engine");
        stage.show();
    }

    private void initializeGoombas() {
        goombas = new ArrayList<>();
        for (int i = 0; i < 15; i++) { // Ajouter 3 Goombas pour l'exemple
            Goomba goomba = new Goomba(200 + i * 150, 500); // Position initiale
            goombas.add(goomba);
            root.getChildren().add(goomba); // Ajouter le Goomba à l'interface
        }
    }

    private void initializeBricks() {
        bricks = new ArrayList<>();
        for (int i = 0; i < 15; i++) { // Ajouter 5 briques pour l'exemple
            Brick brick = new Brick(200 + i * 150, 500); // Position initiale
            bricks.add(brick);
            root.getChildren().add(brick); // Ajouter la brique à l'interface
        }
    }

    private void updateBricks() {
        for (Brick brick : bricks) {
            if (checkCollision(player, brick) && player.getVelY() < 0) { // Collision par le bas
                brick.breakBrick(); // Appelle la méthode pour casser la brique
                toRemove.add(brick);
                player.setVelY(2); // Rebondir après avoir cassé la brique
            }
        }

        // Supprimer les briques cassées
        for (Brick brick : toRemove) {
            root.getChildren().remove(brick); // Supprime la brique de l'affichage
            bricks.remove(brick); // Supprime la brique de la liste
        }
    }

    private void updateGoombas() {
        // Liste des Goombas à supprimer après la collision
        List<Goomba> goombasToRemove = new ArrayList<>();

        for (Goomba goomba : goombas) {
            goomba.update(); // Mise à jour des Goombas

            if (!goomba.isDead()) {
                // Liste des boules de feu à supprimer après la collision
                List<FireBall> fireBallsToRemove = new ArrayList<>();

                // Collision avec une boule de feu
                for (FireBall fireball : fireBalls) {
                    if (checkCollision(goomba, fireball)) {
                        goomba.die(); // Le Goomba meurt
                        fireball.setVisible(false); // Marquer la boule de feu comme invisible
                        fireBallsToRemove.add(fireball); // Ajouter à la liste des boules de feu à supprimer
                    }
                }

                // Supprimer les boules de feu qui ont touché ce Goomba
                fireBalls.removeAll(fireBallsToRemove);
                fireBallsToRemove.forEach(fireball -> gameContainer.getChildren().remove(fireball));
            }

            // Si le Goomba est mort, l'ajouter à la liste des Goombas à supprimer
            if (goomba.isDead()) {
                goombasToRemove.add(goomba);
            }

            // Collision avec le joueur (saut)
            if (checkCollision(player, goomba) && player.getVelY() > 0) {
                goomba.die();
                player.setVelY(-8); // Rebondir après avoir tué un Goomba
                goombasToRemove.add(goomba); // Ajouter à la liste des Goombas à supprimer

            }
        }

        goombas.removeAll(goombasToRemove);

    }

    private void updateCamera() {
        // Calculer la position cible pour centrer la caméra sur le joueur
        double offsetX = player.getLayoutX() - WIDTH / 2.0;
        double offsetY = player.getLayoutY() - HEIGHT / 2.0;

        // Limiter le décalage pour que le joueur ne sorte pas des limites du monde
        offsetX = Math.max(0, Math.min(offsetX, WORLD_WIDTH - WIDTH));
        offsetY = Math.max(0, Math.min(offsetY, WORLD_HEIGHT - HEIGHT));

        // Déplacer le conteneur du jeu
        gameContainer.setLayoutX(-offsetX);
        gameContainer.setLayoutY(-offsetY);
    }

    private void drawInitialBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        if (background != null) {
            gc.drawImage(background, 0, 0, WIDTH, HEIGHT); // Dessine le fond à la taille de la scène
        } else {
            System.out.println("Background image is null!");
        }
    }

    private void loadBackground() {
        try {
            background = new Image(getClass().getResource("/images/background.png").toExternalForm());
            System.out.println("Background loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeScoreLabel() {
        scoreLabel = new Label("Score: 0");
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);
        scoreLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        root.getChildren().add(scoreLabel);
    }


    private void updateFireballs() {
        for (FireBall fireball : fireBalls) {
            fireball.update(); // Mise à jour
            // Supprimer les boules de feu hors limites du monde
            if (fireball.getLayoutX() < 0 || fireball.getLayoutX() > WORLD_WIDTH) {
                fireBallsToRemove.add(fireball);
            }
        }
        // Suppression des boules de feu inutilisées
        fireBalls.removeAll(fireBallsToRemove);
        fireBallsToRemove.forEach(fireball -> gameContainer.getChildren().remove(fireball));
        fireBallsToRemove.clear();
    }

    private void fireBallAction() {
        Image fireballImage = new Image(getClass().getResource("/images/powerUp/fireball.png").toExternalForm());
        FireBall fireball = new FireBall(
                player.getX() + (player.getVelX() >= 0 ? 48 : -24),
                player.getY() + 24,
                player.getVelX() >= 0,
                fireballImage
        );
        fireBalls.add(fireball);
        gameContainer.getChildren().add(fireball);
    }



    private void updateBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Effacer l'ancien frame
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Dessiner le fond à la position actuelle
        gc.drawImage(background, backgroundX, 0);

        // Si le background sort de l'écran, le dessiner à nouveau pour un effet de boucle
        if (backgroundX + background.getWidth() < WIDTH) {
            gc.drawImage(background, backgroundX + background.getWidth(), 0);
        }

        // Mise à jour de la position du background
        if (player.getX() >= 700) {
            player.setX(700 - player.getVelX()); // Empêcher le joueur de dépasser cette limite
            backgroundX -= player.getVelX(); // Déplacer le background

            // Réinitialiser si le fond dépasse ses limites
            if (backgroundX <= -background.getWidth()) {
                backgroundX = 0;
            }
        }
    }

    // Exemple de logique simple pour gérer les collisions
    private boolean checkCollision(Pane a, Pane b) {
        return a.getBoundsInParent().intersects(b.getBoundsInParent());
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + player.getPoints());
    }

    // Réception des actions utilisateur
    private void handleKeyPressed(String keyCode) {
        switch (keyCode) {
            case "RIGHT" -> player.moveRight(); // Déplacement à droite
            case "LEFT" -> player.moveLeft(); // Déplacement à gauche
            case "UP" -> {
                player.jump(); // Saut
                player.update();
                System.out.println("jump");
            }
            case "SPACE" -> {
                fireBallAction();
            }
            case "P" -> togglePauseResume(); // Pause/Resume
            default -> System.out.println("Unhandled key: " + keyCode);
        }
    }

    private void handleKeyReleased(String keyCode) {
        switch (keyCode) {
            case "RIGHT", "LEFT" -> player.stop(); // Arrêt des déplacements horizontaux
            default -> {
                // Aucune action spécifique
            }
        }
    }

    private void togglePauseResume() {
        if (gameStatus == GameStatus.RUNNING) {
            gameStatus = GameStatus.PAUSED;
            isRunning = false;
        } else if (gameStatus == GameStatus.PAUSED) {
            gameStatus = GameStatus.RUNNING;
            isRunning = true;
        }
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void receiveInput(ButtonAction input) {
        switch (input) {
            case M_RIGHT -> {
                player.moveRight(); // Déplacement à droite
            }
            case M_LEFT -> {
                player.moveLeft(); // Déplacement à gauche
            }
            case JUMP -> {
                player.jump(); // Saut
            }
            case ACTION_COMPLETED -> {
                player.stop(); // Arrêt du mouvement
            }
            case PAUSE_RESUME -> {
                togglePauseResume(); // Pause ou reprise du jeu
            }
            case GO_UP -> {
                // Logique pour monter dans un menu ou une carte
            }
            case GO_DOWN -> {
                // Logique pour descendre dans un menu ou une carte
            }
            case SELECT -> {
                // Logique pour valider une action, ex. démarrer une partie
            }
            case GO_TO_START_SCREEN -> {
                gameStatus = GameStatus.START_SCREEN;
                isRunning = false; // Retour à l'écran de démarrage
            }
            case FIRE -> {
            }
            default -> {
                // Aucune action à gérer
            }
        }
    }


    public static void main(String[] args) {
        launch(args); // Démarrage de l'application JavaFX
    }
}
