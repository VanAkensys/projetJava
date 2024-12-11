package com.devops.ninjava.manager;

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

public class GameEngine extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Player player;
    private boolean isRunning;
    private GameStatus gameStatus;

    private Pane root;
    private Rectangle ground; // Simpler ground for collision
    private Label scoreLabel; // To display score or game information
    private double backgroundX = 0; // Position horizontale du background
    private Image background;

    private Canvas canvas;

    @Override
    public void start(Stage stage) {
        // Initialisation de la scène et du conteneur principal
        root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);

        // Initialisation du joueur
        player = new Player(100, 300);
        root.getChildren().add(player);

        // Initialisation du terrain
        loadBackground();
        drawInitialBackground();

        // Initialisation de l'état du jeu
        gameStatus = GameStatus.START_SCREEN;

        // Afficher le score
        initializeScoreLabel();

        // Gestion des entrées utilisateur
        scene.setOnKeyPressed(event -> handleKeyPressed(event.getCode().toString()));
        scene.setOnKeyReleased(event -> handleKeyReleased(event.getCode().toString()));

        // Boucle de jeu
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning && gameStatus == GameStatus.RUNNING) {
                    gameLoop();
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


    private void gameLoop() {
        player.update(); // Mise à jour de l'état du joueur
        updateBackground(); // Mettre à jour la position du background
        checkCollisions(); // Vérifier les collisions
        updateScore(); // Mettre à jour l'affichage du score
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
    private void checkCollisions() {
        if (player.getBoundsInParent().intersects(ground.getBoundsInParent())) {
            player.stopFalling(HEIGHT - 50 - player.getHeight());
        }
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
                System.out.println("jump");
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
                // Logique pour tirer ou effectuer une attaque
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
