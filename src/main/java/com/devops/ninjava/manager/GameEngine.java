package com.devops.ninjava.manager;

import com.devops.ninjava.model.Camera;
import com.devops.ninjava.model.decor.Brick;
import com.devops.ninjava.model.decor.Pipe;
import com.devops.ninjava.model.enemy.Enemy;
import com.devops.ninjava.model.enemy.Goomba;
import com.devops.ninjava.model.hero.FireBall;
import com.devops.ninjava.model.hero.Player;
import com.devops.ninjava.model.hero.Projectile;
import com.devops.ninjava.model.hero.Shuriken;
import com.devops.ninjava.utils.MapLoader;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameEngine extends Application  {


    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int WORLD_WIDTH = 20000; // Largeur du monde
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
    private List<Goomba> goombas = new ArrayList<>();
    private List<Goomba> goombasToRemove = new ArrayList<>();
    private List<FireBall> fireBalls = new ArrayList<>();
    private List<FireBall> fireBallsToRemove = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Projectile> projectilesToRemove = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<Brick> toRemove = new ArrayList<>();
    private List<Pipe> pipes = new ArrayList<>();
    private Camera camera;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Enemy> enemiesToRemove = new ArrayList<>();





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
        player = new Player(100, 100);
        gameContainer.getChildren().add(player);



        // Initialisation du terrain
        loadBackground();
        drawInitialBackground();

        // Initialisation de l'état du jeu
        gameStatus = GameStatus.RUNNING;

        // Afficher le score
        initializeScoreLabel();

//        initializeGoombas();
//        initializeBricks();
//        gameContainer.getChildren().addAll(goombas);
//        gameContainer.getChildren().addAll(bricks);

        camera = new Camera(WIDTH, HEIGHT, WORLD_WIDTH, WORLD_HEIGHT);
        root.getChildren().addAll(backgroundContainer, gameContainer);
        try {
            MapLoader.loadMapFromFile("src/main/resources/images/map1.txt", gameContainer, bricks, enemies);
        } catch (IOException e) {
            System.err.println("Error loading map: " + e.getMessage());
        }

        // Gestion des entrées utilisateur
        scene.setOnKeyPressed(event -> handleKeyPressed(event.getCode().toString()));
        scene.setOnKeyReleased(event -> handleKeyReleased(event.getCode().toString()));

        // Boucle de jeu
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning && gameStatus == GameStatus.RUNNING) {
                    player.update(); // Mise à jour de l'état du joueur
//                    updateGoombas();
                    updateScore(); // Mise à jour du score
                    updateBricks();
                    updateFireballs();
                    updateCamera();
                    updatePipes();
                    updateProjectiles();
                    updateEnemies();
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

    private void updateBricks() {
        List<Brick> toRemove = new ArrayList<>(); // Liste temporaire pour supprimer les briques

        for (Brick brick : bricks) {
            player.handleCollision(brick); // Gestion de la collision dans la classe Player

            if (brick.isBroken()) { // Si la brique est cassée
                toRemove.add(brick); // Ajoute la brique à la liste de suppression
            }
        }

        // Supprime les briques cassées
        for (Brick brick : toRemove) {
            gameContainer.getChildren().remove(brick); // Retire la brique de l'affichage
            bricks.remove(brick); // Retire la brique de la liste
        }
    }

    private void updatePipes() {
        for (Pipe pipe : pipes) {
            // Vérifier collision avec le joueur
            if (pipe.collision(player)) {
                player.handleCollision(pipe);
            }

            // Vérifier collision avec les Goombas
            for (Goomba goomba : goombas) {
                if (pipe.collision(goomba)) {
                    goomba.onCollision(pipe);
                }
            }

            // Vérifier collision avec les boules de feu
            for (FireBall fireball : fireBalls) {
                if (pipe.collision(fireball)) {
                    fireball.deactivate();
                }
            }
        }
    }

    private void updateEnemies() {

        for (Enemy enemy : enemies) {
            enemy.update(); // Mise à jour de l'état de l'ennemi

            // Vérifiez les collisions avec le joueur
            enemy.handleCollision(player);

            // Vérifiez les collisions avec les briques et tuyaux
            for (Brick brick : bricks) {
                enemy.handleCollision(brick);
            }
            for (Pipe pipe : pipes) {
                enemy.handleCollision(pipe);
            }

            // Si l'ennemi est mort, ajoutez-le à la liste pour suppression
            if (enemy.isDead()) {
                enemiesToRemove.add(enemy);
            }
        }

        // Supprimez les ennemis morts de la liste principale et de l'affichage
        for (Enemy enemy : enemiesToRemove) {
            enemies.remove(enemy); // Retire de la liste
            gameContainer.getChildren().remove(enemy); // Retire de l'écran
        }
    }


//    private void updateGoombas()
//    {
//        for (Goomba goomba : goombas) {
//            goomba.update(); // Mise à jour des Goombas
//
//            // Collision avec les tuyaux
//            for (Pipe pipe : pipes) {
//                if (pipe.collision(goomba)) {
//                    goomba.onCollision(pipe); // Inverse la direction
//                }
//            }
//            for (Brick brick : bricks) {
//                goomba.onCollision(brick);
//            }
//
//            // Vérifier la collision avec le joueur
//            player.handleEnemyCollision(goomba);
//
//            // Supprimer les Goombas morts
//            if (goomba.isDead()) {
//                goombasToRemove.add(goomba);
//            }
//        }
//
//        // Supprimer les Goombas de la liste et de la scène
//        goombas.removeAll(goombasToRemove);
//        goombasToRemove.forEach(goomba -> gameContainer.getChildren().remove(goomba));
//    }

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
        List<FireBall> fireBallsToRemove = new ArrayList<>();

        for (FireBall fireball : fireBalls) {
            fireball.update(); // Mise à jour de la position

            // Vérifier les collisions avec les briques
            for (Brick brick : bricks) {
                if (fireball.handleCollision(brick)) {
                    fireBallsToRemove.add(fireball); // Marquer pour suppression
                    break; // Pas besoin de vérifier d'autres collisions pour cette boule de feu
                }
            }

            // Vérifier les collisions avec les Goombas
            for (Enemy enemy : enemies) {
                if (fireball.handleCollision(enemy)) {
                    fireBallsToRemove.add(fireball); // Marquer pour suppression
                    break; // Pas besoin de vérifier d'autres collisions pour cette boule de feu
                }
            }

            // Supprimer les boules de feu hors limites
            if (fireball.isOutOfBounds(WORLD_WIDTH)) {
                fireBallsToRemove.add(fireball);
            }
        }

        // Supprimer les boules de feu inutilisées
        for (FireBall fireball : fireBallsToRemove) {
            fireBalls.remove(fireball);
            gameContainer.getChildren().remove(fireball); // Retirer de l'interface graphique
        }
    }

    private void updateProjectiles() {
        List<Projectile> projectilesToRemove = new ArrayList<>();

        for (Projectile projectile : projectiles) {
            projectile.update(); // Mise à jour de la position

            // Vérifier les collisions avec les briques
            for (Brick brick : bricks) {
                if (projectile.handleCollision(brick)) {
                    projectilesToRemove.add(projectile); // Marquer pour suppression
                    break;
                }
            }

            // Vérifier les collisions avec les tuyaux
            for (Pipe pipe : pipes) {
                if (projectile.handleCollision(pipe)) {
                    projectilesToRemove.add(projectile); // Marquer pour suppression
                    break;
                }
            }

            // Vérifier les collisions avec les Goombas
            for (Enemy enemy : enemies) {
                if (projectile.handleCollision(enemy)) {
                    projectilesToRemove.add(projectile); // Marquer pour suppression
                    break;
                }
            }

            // Supprimer les projectiles hors limites
            if (projectile.getLayoutX() < 0 || projectile.getLayoutX() > WORLD_WIDTH) {
                projectilesToRemove.add(projectile);
            }
        }

        // Supprimer les projectiles inutilisés
//        for (Projectile projectile : projectilesToRemove) {
//            projectiles.remove(projectile);
//            gameContainer.getChildren().remove(projectile); // Retirer de l'affichage
//        }
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

    private void launchShuriken() {
        if (!player.isAttacking()) { // Empêcher de lancer plusieurs shurikens en même temps
            player.animateProjectileLaunch(); // Animer le lancement du shuriken

            BufferedImage spriteSheet = null;
            try {
                spriteSheet = ImageIO.read(getClass().getResource("/images/player/Ultimate_Ninja_Spritesheet.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Shuriken shuriken = new Shuriken(
                    player.getX() + (player.getVelX() >= 0 ? 48 : -24), // Position de départ
                    player.getY() + 24, // Position de départ
                    player.getVelX() >= 0 || player.getScaleX() > 0, // Direction
                    spriteSheet
            );

            projectiles.add(shuriken); // Ajouter à la liste des projectiles
            gameContainer.getChildren().add(shuriken); // Ajouter au conteneur
        }
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
                player.attack();
            }
            case "W" -> {
                launchShuriken(); // Lancer un shuriken
                System.out.println("Shuriken launched!");
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
            case ATTACK -> {
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
