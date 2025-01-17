package com.devops.ninjava.manager;

import com.devops.ninjava.model.Camera;
import com.devops.ninjava.model.decor.Ground;
import com.devops.ninjava.model.enemy.Enemy;
import com.devops.ninjava.model.projectile.FireBall;
import com.devops.ninjava.model.hero.Player;
import com.devops.ninjava.model.projectile.Projectile;
import com.devops.ninjava.model.projectile.Shuriken;
import com.devops.ninjava.utils.MapLoader;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GameEngine extends Application  {


    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int WORLD_WIDTH = 20000; // Largeur du monde
    private static final int WORLD_HEIGHT = 5000; // Hauteur du monde


    private boolean isRunning;
    private GameStatus gameStatus;



    //gestion de l'affichage java fx
    private Pane backgroundContainer;
    private Pane gameContainer; // Conteneur pour tout le monde du jeu
    private Pane root;
    private Rectangle ground;
    private Label scoreLabel;
    private Label livesLabel;
    private Label shurikenLabel;
    private Label energyLabel;
    private Image shurikenIcon;
    private double backgroundX = 0; // Position horizontale du background
    private Image background;
    private Font menuFont;
    private Stage primaryStage;

    private Pane pauseMenu;
    private Label continueLabel;
    private Label exitLabel;
    private int selectedOption = 0;


    private Canvas canvas;

    //éléments du jeu
    private Player player;
    private Player player2;
    private List<FireBall> fireBalls = new ArrayList<>();
    private List<FireBall> fireBallsToRemove = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Projectile> projectilesToRemove = new ArrayList<>();
    private List<Ground> grounds = new ArrayList<>();
    private List<Ground> toRemove = new ArrayList<>();
    private Camera camera;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Enemy> enemiesToRemove = new ArrayList<>();

    private PrintWriter serverOut;
    private boolean isPlayer1 = true;
    private boolean isPlayer2Connected = false;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 3303;
    private boolean isMultiplayer = true; // Détermine si le jeu est en mode multijoueur
    private boolean bothPlayersConnected = false;



    @Override
    public void start(Stage stage) {


        // Initialisation de la scène et du conteneur principal
        root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        gameStatus = GameStatus.WAITING_FOR_PLAYER;

        stage.setFullScreen(false);
        stage.setFullScreenExitHint(""); // Optionnel : désactive le message de sortie du plein écran
        stage.setFullScreenExitKeyCombination(null);
        stage.setResizable(false); // Empêche la redimensionnement de la fenêtre

        backgroundContainer = new Pane();
        gameContainer = new Pane();


        canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);

        // Initialisation du joueur
        player = new Player(100, 100);
        gameContainer.getChildren().add(player);

        if (isMultiplayer) {
            connectToServer();
            initializePlayer2();
            if (!bothPlayersConnected) {
                System.out.println("Waiting for both players to connect...");
            }
        } else
        {
            bothPlayersConnected = true;
        }

        // Initialisation du terrain
        loadBackground();
        drawInitialBackground();

        // Initialisation de l'état du jeu

        // Afficher le score

        initializeScoreLabel();
        initializePauseMenu();

        this.primaryStage = stage;

        camera = new Camera(WIDTH, HEIGHT, WORLD_WIDTH, WORLD_HEIGHT);
        root.getChildren().addAll(backgroundContainer, gameContainer);
        try {
            MapLoader.loadMapFromFile("src/main/resources/images/map1.txt", gameContainer, grounds, enemies);
        } catch (IOException e) {
            System.err.println("Error loading map: " + e.getMessage());
        }

        // Gestion des entrées utilisateur
        scene.setOnKeyPressed(event -> handleKeyPressed(event.getCode().toString()));
        scene.setOnKeyReleased(event -> handleKeyReleased(event.getCode().toString()));

        // Liez la taille du Canvas à celle de la scène
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        // Listener pour détecter les changements de taille de la fenêtre
        stage.widthProperty().addListener((observable, oldValue, newValue) -> drawBackground());
        stage.heightProperty().addListener((observable, oldValue, newValue) -> drawBackground());

        // Boucle de jeu
        gameStatus = GameStatus.RUNNING;
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning && gameStatus == GameStatus.RUNNING && bothPlayersConnected) {
                    if (!player.isDead()) {
                        player.update();
                        updateEnergy(getActivePlayer());
                        updateLives(getActivePlayer());
                        updateScore(getActivePlayer());
                        updateCamera(getActivePlayer());
                        updateShurikens(getActivePlayer());
                    }
                    if (isMultiplayer && bothPlayersConnected && !player2.isDead()) {
                        player2.update();
                        updateEnergy(getActivePlayer());
                        updateLives(getActivePlayer());
                        updateScore(getActivePlayer());
                        updateCamera(getActivePlayer());
                        updateShurikens(getActivePlayer());
                    }
                    drawBackground();

                    updateBricks();
                    updateFireballs();
                    updateProjectiles();
                    updateEnemies();
                }else {
                    System.out.println("Waiting for players");
                }
            }
        };
        // Démarrage du jeu
        gameLoop.start();
        isRunning =true;


        stage.setScene(scene);
        stage.setTitle("Advanced Game Engine");
        stage.show();
    }


    private void initializePlayer2() {
        if (!isPlayer2Connected) {
            player2 = new Player(200, 100); // Position initiale
            gameContainer.getChildren().add(player2);
            isPlayer2Connected = true;
            System.out.println("Player 2 initialized and added to the game.");
        }
    }

    private void initializePauseMenu() {
        // Chargement de la police utilisée dans le score
        URL fontResource = getClass().getResource("/font/gameFont.ttf");
        if (fontResource == null) {
            throw new RuntimeException("Font file not found! Check the path: /font/gameFont.ttf");
        }

        try {
            String decodedFontPath = URLDecoder.decode(fontResource.toExternalForm(), StandardCharsets.UTF_8);
            menuFont = Font.loadFont(decodedFontPath, 30); // Taille adaptée au menu
            if (menuFont == null) {
                throw new RuntimeException("Failed to load font for the pause menu.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading font for the pause menu: " + e.getMessage());
        }

        // Création du menu
        pauseMenu = new Pane();
        pauseMenu.setPrefSize(WIDTH, HEIGHT);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        continueLabel = new Label("Continue");
        exitLabel = new Label("Exit");

        continueLabel.setLayoutX(WIDTH / 2.0 - 80);
        continueLabel.setLayoutY(HEIGHT / 2.0 - 50);

        exitLabel.setLayoutX(WIDTH / 2.0 - 80);
        exitLabel.setLayoutY(HEIGHT / 2.0 + 10);

        continueLabel.setFont(menuFont);
        exitLabel.setFont(menuFont);

        continueLabel.setStyle("-fx-text-fill: white;");
        exitLabel.setStyle("-fx-text-fill: white;");

        pauseMenu.getChildren().addAll(continueLabel, exitLabel);
        pauseMenu.setVisible(false); // Masquer par défaut

        root.getChildren().add(pauseMenu);
    }

    private void updateMenuSelection() {
        if (selectedOption == 0) {
            continueLabel.setStyle("-fx-text-fill: yellow; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            exitLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
        } else {
            continueLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
            exitLabel.setStyle("-fx-text-fill: yellow; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
        }
    }

    private void updateBricks() {
        List<Ground> toRemove = new ArrayList<>(); // Liste temporaire pour supprimer les briques

        for (Ground ground : grounds) {
            player.handleCollision(ground); // Gestion de la collision dans la classe Player
            if (isMultiplayer)
            {
                player2.handleCollision(ground);
            }


            if (ground.isBroken()) { // Si la brique est cassée
                toRemove.add(ground); // Ajoute la brique à la liste de suppression
            }
        }

        // Supprime les briques cassées
        for (Ground ground : toRemove) {
            gameContainer.getChildren().remove(ground); // Retire la brique de l'affichage
            grounds.remove(ground); // Retire la brique de la liste
        }
    }



    private void updateEnemies() {

        for (Enemy enemy : enemies) {
            enemy.update(); // Mise à jour de l'état de l'ennemi

            player.handleEnemyCollision(enemy);
            if (isMultiplayer)
            {
                player2.handleEnemyCollision(enemy);
                enemy.handleCollision(player2);
            }

            // Vérifiez les collisions avec le joueur
            enemy.handleCollision(player);

            // Vérifiez les collisions avec les briques et tuyaux
            for (Ground ground : grounds) {
                enemy.handleCollision(ground);
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

    private void updateCamera(Player activePlayer) {

        // Calculer la position cible pour centrer la caméra sur le joueur
        double offsetX = activePlayer.getLayoutX() - WIDTH / 2.0;
        double offsetY = activePlayer.getLayoutY() - HEIGHT / 2.0;

        if (activePlayer.getLayoutX() > WORLD_WIDTH || activePlayer.getLayoutY() > WORLD_HEIGHT) {
            activePlayer.die();
            return;
        }

        // Déplacer le conteneur du jeu
        gameContainer.setLayoutX(-offsetX);
        gameContainer.setLayoutY(-offsetY);

        // Mise à jour du background pour suivre la caméra
        updateBackground(offsetX, offsetY);
    }

    private void drawInitialBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        if (background != null) {
            gc.drawImage(background, 0, 0, canvas.getWidth(), canvas.getHeight()); // Dessine le fond à la taille de la scène
        } else {
            System.out.println("Background image is null!");
        }
    }

    private void loadBackground() {
        try {
            background = new Image(getClass().getResource("/images/city.png").toExternalForm());
            System.out.println("Background loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void drawBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Effacez l'ancien contenu
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (background != null) {
            // Dessiner le background pour couvrir toute la scène
            gc.drawImage(background, 0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            System.out.println("Background image is null!");
        }
    }

    private void initializeScoreLabel() {
        URL fontResource = getClass().getResource("/font/gameFont.ttf");
        if (fontResource == null) {
            throw new RuntimeException("Font file not found! Check the path: /font/gameFont.ttf");
        }

        try {
            // Décodage du chemin (supprime %20)
            String decodedFontPath = URLDecoder.decode(fontResource.toExternalForm(), StandardCharsets.UTF_8);

            // Chargement de la police
            Font pixelFont = Font.loadFont(decodedFontPath, 20);
            if (pixelFont == null) {
                throw new RuntimeException("Failed to load font from: " + decodedFontPath);
            }

            // Configurez le label du score
            scoreLabel = new Label("Score: 0");
            scoreLabel.setLayoutX(10);
            scoreLabel.setLayoutY(10);
            scoreLabel.setFont(pixelFont); // Appliquez la police
            scoreLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");

            livesLabel = new Label("Lives: " + player.getRemainingLives());
            livesLabel.setLayoutX(10);
            livesLabel.setLayoutY(40); // Position sous le score
            livesLabel.setFont(pixelFont);
            livesLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");

            energyLabel = new Label("Energy: " + player.getEnergy());
            energyLabel.setLayoutX(10); // Position du label
            energyLabel.setLayoutY(70); // Position sous les vies et shurikens
            energyLabel.setFont(scoreLabel.getFont()); // Réutiliser la police
            energyLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");

            root.getChildren().addAll(scoreLabel, livesLabel, energyLabel);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading font: " + e.getMessage());
        }

        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/player/Ultimate_Ninja_Spritesheet.png"));
            Image shurikenImage = convertToFXImage(spriteSheet.getSubimage(6 * 48, 0 * 48, 48, 48)); // Découpe le shuriken

            ImageView shurikenImageView = new ImageView(shurikenImage);

            shurikenImageView.setFitWidth(96); // Taille de l'icône
            shurikenImageView.setFitHeight(96);

            shurikenLabel = new Label(" x " + player.getShurikens(), shurikenImageView);
            shurikenLabel.setLayoutX(10);
            shurikenLabel.setLayoutY(80); // Position sous le label des vies
            shurikenLabel.setFont(scoreLabel.getFont());
            shurikenLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");

            root.getChildren().add(shurikenLabel);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading shuriken icon: " + e.getMessage());
        }
    }

    private Image convertToFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    private void updateLives(Player activePlayer) {
        livesLabel.setText("Lives: " + activePlayer.getRemainingLives());
    }

    private void updateFireballs() {
        List<FireBall> fireBallsToRemove = new ArrayList<>();

        for (FireBall fireball : fireBalls) {
            fireball.update(); // Mise à jour de la position

            // Vérifier les collisions avec les briques
            for (Ground ground : grounds) {
                if (fireball.handleCollision(ground)) {
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
            projectile.update(); // Appelle la méthode update du projectile, incluant checkCollision()

            if (!projectile.isActive()) {
                projectilesToRemove.add(projectile);
            }
        }

        // Retirer les projectiles désactivés
        for (Projectile projectile : projectilesToRemove) {
            projectiles.remove(projectile);
            gameContainer.getChildren().remove(projectile); // Retirer de la scène
        }
    }


    private void fireBallAction(Player activePlayer) {
        if (!activePlayer.isAttacking() && activePlayer.useEnergy(activePlayer.ENERGY_COST_FIREBALL)) {
            activePlayer.animateJutsuLaunch();

            // Charger l'image de la fireball
            Image fireballImage = new Image(getClass().getResource("/images/powerUp/fireball.png").toExternalForm());

            // Créer une nouvelle fireball à la position du joueur
            FireBall fireball = new FireBall(
                    activePlayer.getX() + (activePlayer.getVelX() >= 0 ? 48 : -24), // Position basée sur la direction
                    activePlayer.getY() + 24, // Position ajustée pour être au niveau de la main
                    activePlayer.getVelX() >= 0, // Direction de la fireball
                    fireballImage
            );

            // Ajouter la fireball à la liste et à la scène
            fireBalls.add(fireball);
            gameContainer.getChildren().add(fireball);

        } else {
            System.out.println("Not enough energy to launch a fireball!");
        }
    }

    private void launchShuriken(Player activePlayer) {
        if (!activePlayer.isAttacking() && activePlayer.useShuriken()) { // Empêcher de lancer plusieurs shurikens en même temps
            activePlayer.animateProjectileLaunch(); // Animer le lancement du shuriken

            BufferedImage spriteSheet = null;
            try {
                spriteSheet = ImageIO.read(getClass().getResource("/images/player/Ultimate_Ninja_Spritesheet.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Shuriken shuriken = new Shuriken(
                    activePlayer.getX() + (activePlayer.getVelX() >= 0 ? 48 : -24), // Position de départ
                    activePlayer.getY() + 16, // Position de départ
                    activePlayer.getVelX() >= 0 || activePlayer.getScaleX() > 0, // Direction
                    spriteSheet,
                    false
            );

            projectiles.add(shuriken); // Ajouter à la liste des projectiles
            gameContainer.getChildren().add(shuriken); // Ajouter au conteneur
        }
    }

    private void updateShurikens(Player activePlayer) {
        shurikenLabel.setText(" x " + activePlayer.getShurikens());
    }

    public void updateEnergy(Player activePlayer) {
        activePlayer.regenerateEnergy(); // Régénérer l'énergie
        energyLabel.setText("Energy: " + activePlayer.getEnergy()); // Mettre à jour l'affichage de l'énergie
    }

    private void updateBackground(double offsetX, double offsetY) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Effacez l'ancien contenu
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Ajustez la position du background en fonction de la caméra
        double bgOffsetX = -offsetX % background.getWidth();
        double bgOffsetY = -offsetY % background.getHeight();

        // Dessinez le background en fonction de son décalage
        gc.drawImage(background, bgOffsetX, bgOffsetY, canvas.getWidth(), canvas.getHeight());

        // Boucle horizontale du background
        if (bgOffsetX + background.getWidth() < canvas.getWidth()) {
            gc.drawImage(background, bgOffsetX + background.getWidth(), bgOffsetY, canvas.getWidth(), canvas.getHeight());
        }

        // Boucle verticale du background
        if (bgOffsetY + background.getHeight() < canvas.getHeight()) {
            gc.drawImage(background, bgOffsetX, bgOffsetY + background.getHeight(), canvas.getWidth(), canvas.getHeight());
        }

        // Boucle diagonale
        if (bgOffsetX + background.getWidth() < canvas.getWidth() && bgOffsetY + background.getHeight() < canvas.getHeight()) {
            gc.drawImage(background, bgOffsetX + background.getWidth(), bgOffsetY + background.getHeight(), canvas.getWidth(), canvas.getHeight());
        }
    }

    private void updateScore(Player activePlayer) {
        scoreLabel.setText("Score: " + activePlayer.getPoints());
    }

    // Réception des actions utilisateur
    private void handleKeyPressed(String keyCode) {
        if(gameStatus == GameStatus.RUNNING && !player.isDead()) {
            switch (keyCode) {
                case "RIGHT" -> {
                    if (isPlayer1) {
                        player.moveRight();
                        sendActionToServer("PLAYER1_MOVE_RIGHT");
                    } else {
                        player2.moveRight();
                        sendActionToServer("PLAYER2_MOVE_RIGHT");
                    }
                }
                case "LEFT" -> {
                    if (isPlayer1) {
                        player.moveLeft();
                        sendActionToServer("PLAYER1_MOVE_LEFT");
                    } else {
                        player2.moveLeft();
                        sendActionToServer("PLAYER2_MOVE_LEFT");
                    }
                }
                case "UP" -> {
                    if (isPlayer1) {
                        player.jump();
                        sendActionToServer("PLAYER1_JUMP");
                    } else {
                        player2.jump();
                        sendActionToServer("PLAYER2_JUMP");
                    }
                }
                case "DOWN" -> {
                    if (isPlayer1 && !player.isInvincible()) {
                        player.startTeleportation();
                        sendActionToServer("PLAYER1_TELEPORT");
                    } else if (!isPlayer1 && !player2.isInvincible()) {
                        player2.startTeleportation();
                        sendActionToServer("PLAYER2_TELEPORT");
                    }
                }
                case "SPACE" -> {
                    if (isPlayer1 && !player.isAttacking()) {
                        player.attack();
                        sendActionToServer("PLAYER1_ATTACK");
                    } else if (!isPlayer1 && !player2.isAttacking()) {
                        player2.attack();
                        sendActionToServer("PLAYER2_ATTACK");
                    }
                }
                case "W" -> {
                    if (isPlayer1) {
                        launchShuriken(player);
                        sendActionToServer("PLAYER1_LAUNCH_SHURIKEN");
                    } else {
                        launchShuriken(player2);
                        sendActionToServer("PLAYER2_LAUNCH_SHURIKEN");
                    }
                }
                case "X" -> {
                    if (isPlayer1) {
                        fireBallAction(player);
                        sendActionToServer("PLAYER1_LAUNCH_FIREBALL");
                    } else {
                        fireBallAction(player2);
                        sendActionToServer("PLAYER2_LAUNCH_FIREBALL");
                    }
                }
                case "ESCAPE" -> sendActionToServer("PAUSE_GAME");
                default -> System.out.println("Unhandled key: " + keyCode);
            }
        }else if (gameStatus == GameStatus.PAUSED) {
            handlePauseMenuNavigation(keyCode);
        }
    }

    private void handleKeyReleased(String keyCode) {
        switch (keyCode) {
            case "RIGHT", "LEFT" ->
                    {
                        if (isPlayer1) {
                            player.stopMoving();
                            sendActionToServer("PLAYER1_STOP");
                        } else {
                            player2.stopMoving();
                            sendActionToServer("PLAYER2_STOP");
                        }
                    } // Arrêt des déplacements horizontaux
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

    private void handlePauseMenuNavigation(String keyCode) {
        switch (keyCode) {
            case "UP" -> {
                selectedOption = (selectedOption - 1 + 2) % 2; // Navigation circulaire
                updateMenuSelection();
            }
            case "DOWN" -> {
                selectedOption = (selectedOption + 1) % 2; // Navigation circulaire
                updateMenuSelection();
            }
            case "ENTER" -> {
                if (selectedOption == 0) { // Continue
                    sendActionToServer("PAUSE_GAME");
                } else if (selectedOption == 1) { // Exit
                    System.exit(0); // Quitter le jeu
                    sendActionToServer("EXIT");
                }
            }
        }
    }

    private void togglePauseMenu() {
        if (gameStatus == GameStatus.PAUSED) {
            pauseMenu.setVisible(false);
            gameStatus = GameStatus.RUNNING;
            isRunning = true;

            // Remettre en plein écran lorsque le jeu reprend
            if (primaryStage != null && !primaryStage.isFullScreen()) {
                primaryStage.setFullScreen(true);
            }

        } else {
            pauseMenu.setVisible(true);
            gameStatus = GameStatus.PAUSED;
            isRunning = false;
        }
    }

    private void stopGame() {
        System.exit(0);
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

    public void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            serverOut = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverIn.readLine()) != null) {
                        if (serverMessage.startsWith("ROLE:")) {
                            System.out.println("Server assigned role: " + serverMessage);
                            String role = serverMessage.split(":")[1].trim(); // Récupère la partie après ":" et supprime les espaces inutiles
                            isPlayer1 = "PLAYER1".equals(role); // Configure le rôle local
                            System.out.println("Assigned role: " + (isPlayer1 ? "PLAYER1" : "PLAYER2") + " (" + isPlayer1 + ")");
                        } else if ("BOTH_PLAYERS_CONNECTED".equals(serverMessage)) {
                            bothPlayersConnected = true;
                            System.out.println("Both players are connected. Starting game...");
                        } else {
                            handleServerMessage(serverMessage); // Appliquer les mises à jour
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendActionToServer(String action) {
        if (serverOut != null) {
            serverOut.println(action);
        }
      }

    private void handleServerMessage(String message) {
        if (message == null || message.isEmpty()) {
            System.err.println("Empty or null message received.");
            return;
        }

        // Si le message n'a pas de délimiteur, il est traité directement comme une action
        if (!message.contains(":")) {
            Platform.runLater(() -> handleAction(message));
            return;
        }

        // Divisez le message si un délimiteur est présent
        String[] parts = message.split(":", 2);
        if (parts.length < 2) {
            System.err.println("Incomplete message received: " + message);
            return;
        }

        String action = parts[0].trim();
        String playerId = parts[1].trim();

        Platform.runLater(() -> handleAction(action, playerId));
    }

    private void handleAction(String action) {
        switch (action) {
            case "PLAYER1_MOVE_RIGHT" -> player.moveRight();
            case "PLAYER1_MOVE_LEFT" -> player.moveLeft();
            case "PLAYER1_JUMP" -> player.jump();
            case "PLAYER1_STOP" -> player.stopMoving();
            case "PLAYER1_ATTACK" -> player.attack();
            case "PLAYER1_LAUNCH_SHURIKEN" -> launchShuriken(player);
            case "PLAYER1_LAUNCH_FIREBALL" -> fireBallAction(player);
            case "PLAYER1_TELEPORT" -> player.startTeleportation();
            case "PLAYER2_MOVE_RIGHT" -> player2.moveRight();
            case "PLAYER2_MOVE_LEFT" -> player2.moveLeft();
            case "PLAYER2_JUMP" -> player2.jump();
            case "PLAYER2_STOP" -> player2.stopMoving();
            case "PLAYER2_ATTACK" -> player2.attack();
            case "PLAYER2_LAUNCH_SHURIKEN" -> launchShuriken(player2);
            case "PLAYER2_LAUNCH_FIREBALL" -> fireBallAction(player2);
            case "PLAYER2_TELEPORT" -> player2.startTeleportation();
            case "PAUSE_GAME" -> togglePauseMenu();
            case "EXIT" -> stopGame();
            default -> System.out.println("Unhandled action: " + action);
        }
    }

    private void handleAction(String action, String playerId) {
        switch (action) {
            case "MOVE_RIGHT" -> {
                if ("PLAYER1".equals(playerId)) {
                    player.moveRight();
                } else if ("PLAYER2".equals(playerId)) {
                    player2.moveRight();
                }
            }
            case "MOVE_LEFT" -> {
                if ("PLAYER1".equals(playerId)) {
                    player.moveLeft();
                } else if ("PLAYER2".equals(playerId)) {
                    player2.moveLeft();
                }
            }
            case "JUMP" -> {
                if ("PLAYER1".equals(playerId)) {
                    player.jump();
                } else if ("PLAYER2".equals(playerId)) {
                    player2.jump();
                }
            }
            case "STOP" -> {
                if ("PLAYER1".equals(playerId)) {
                    player.stopMoving();
                } else if ("PLAYER2".equals(playerId)) {
                    player2.stopMoving();
                }
            }
            case "ATTACK" -> {
                if ("PLAYER1".equals(playerId)) {
                    player.attack();
                } else if ("PLAYER2".equals(playerId)) {
                    player2.attack();
                }
            }
            case "LAUNCH_SHURIKEN" -> {
                if ("PLAYER1".equals(playerId)) {
                    launchShuriken(player);
                } else if ("PLAYER2".equals(playerId)) {
                    launchShuriken(player2);
                }
            }
            case "LAUNCH_FIREBALL" -> {
                if ("PLAYER1".equals(playerId)) {
                    fireBallAction(player);
                } else if ("PLAYER2".equals(playerId)) {
                    fireBallAction(player2);
                }
            }
            default -> System.out.println("Unhandled action: " + action);
        }
    }



    private Player getActivePlayer() {
        return isPlayer1 ? player : player2;
    }


    public static void main(String[] args) {
        launch(args); // Démarrage de l'application JavaFX
    }
}
