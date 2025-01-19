package com.devops.ninjava.manager;

import com.devops.ninjava.model.environnement.Decoration;
import com.devops.ninjava.model.environnement.Ground;
import com.devops.ninjava.model.environnement.Wall;
import com.devops.ninjava.model.enemy.Enemy;
import com.devops.ninjava.model.item.Item;
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
import javafx.scene.layout.VBox;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GameEngine extends Application  {


    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final int WORLD_WIDTH = 20000; // Largeur du monde
    private static final int WORLD_HEIGHT = 2000; // Hauteur du monde


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
    private SoundManager soundManager = new SoundManager();

    //éléments du jeu
    private Player player;
    private Player player2;
    private List<FireBall> fireBalls = new ArrayList<>();
    private List<FireBall> fireBallsToRemove = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Projectile> projectilesToRemove = new ArrayList<>();
    private List<Ground> grounds = new ArrayList<>();
    private List<Wall> walls = new ArrayList<>();
    private List<Decoration> decorations = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Enemy> enemiesToRemove = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Item> itemsToRemove = new ArrayList<>();

    private PrintWriter serverOut;
    private boolean isPlayer1 = true;
    private boolean isPlayer2Connected = false;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 3303;
    private boolean isMultiplayer = false; // Détermine si le jeu est en mode multijoueur
    private boolean isVictory = false;
    private boolean bothPlayersConnected = false;

    @Override
    public void start(Stage stage) {
        // Créer le menu principal
        Pane mainMenu = new Pane();
        mainMenu.setPrefSize(WIDTH, HEIGHT);
        mainMenu.setStyle("-fx-background-color: black;");

        // Charger la police du menu
        URL fontResource = getClass().getResource("/font/gameFont.ttf");
        Font menuFont = (fontResource != null) ?
                Font.loadFont(URLDecoder.decode(fontResource.toExternalForm(), StandardCharsets.UTF_8), 40) :
                new Font("Arial", 40);

        Font titleFont = (fontResource != null) ?
                Font.loadFont(URLDecoder.decode(fontResource.toExternalForm(), StandardCharsets.UTF_8), 60) :
                new Font("Arial", 60);

        // Options de menu
        Label startGameLabel = new Label("Start Game");
        Label multiplayerLabel = new Label("Multiplayer");
        Label exitLabel = new Label("Exit");
        Label helpLabel = new Label("Help");

        Label titleLabel = new Label("Ninjava");
        titleLabel.setFont(titleFont);
        titleLabel.setStyle("-fx-text-fill: blue; -fx-effect: dropshadow(gaussian, black, 3, 0.7, 0, 0);");


        Label[] menuOptions = {startGameLabel, multiplayerLabel,helpLabel, exitLabel};

        for (Label label : menuOptions) {
            label.setFont(menuFont);
            label.setStyle("-fx-text-fill: white;");
            label.setLayoutX(WIDTH / 2.0 );
        }


        VBox menuBox = new VBox(30); // Espacement entre les éléments
        menuBox.getChildren().add(titleLabel);
        menuBox.getChildren().addAll(startGameLabel, multiplayerLabel,helpLabel, exitLabel);
        menuBox.setAlignment(javafx.geometry.Pos.CENTER); // Centrer le contenu
        menuBox.setLayoutX(WIDTH / 2.0 - 200); // Centrer horizontalement
        menuBox.setLayoutY(HEIGHT / 4.0); // Ajuster pour placer le titre en haut

        mainMenu.getChildren().add(menuBox);

        Scene menuScene = new Scene(mainMenu, WIDTH, HEIGHT);
        stage.setScene(menuScene);
        stage.setTitle("NinJava - Main Menu");
        stage.setResizable(false);
        stage.show();

        menuScene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = (double) newValue;
            menuBox.setLayoutX(newWidth / 2.0 - 200);
        });

        menuScene.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeight = (double) newValue;
            menuBox.setLayoutY(newHeight / 4.0);
        });

        soundManager.playTitleMusic();

        // Navigation dans le menu
        final int[] selectedOption = {0}; // Indique quelle option est actuellement sélectionnée
        highlightMenuOption(selectedOption[0], menuOptions);

        menuScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> {
                    selectedOption[0] = (selectedOption[0] - 1 + menuOptions.length) % menuOptions.length;
                    highlightMenuOption(selectedOption[0], menuOptions);
                }
                case DOWN -> {
                    selectedOption[0] = (selectedOption[0] + 1) % menuOptions.length;
                    highlightMenuOption(selectedOption[0], menuOptions);
                }
                case ENTER -> {
                    soundManager.stopBackgroundMusic();
                    switch (selectedOption[0]) {
                        case 0 -> startGame(stage, false); // Solo
                        case 1 -> startGame(stage, true); // Multijoueur
                        case 2 -> showHelpScreen(stage); // Aide
                        case 3 -> Platform.exit(); // Quitter
                    }
                }
            }
        });
    }

    private void highlightMenuOption(int selectedIndex, Label[] menuOptions) {
        for (int i = 0; i < menuOptions.length; i++) {
            if (i == selectedIndex) {
                menuOptions[i].setStyle("-fx-text-fill: blue light; -fx-effect: dropshadow(gaussian, white, 2, 0.7, 0, 0);");
            } else {
                menuOptions[i].setStyle("-fx-text-fill: white;");
            }
        }
    }

    private void showWaitingForPlayersScreen(Stage stage) {
        Pane waitingScreen = new Pane();
        waitingScreen.setPrefSize(WIDTH, HEIGHT);
        waitingScreen.setStyle("-fx-background-color: black;");

        stage.setResizable(false);

        URL fontResource = getClass().getResource("/font/gameFont.ttf");

        Font menuFont = (fontResource != null) ?
                Font.loadFont(URLDecoder.decode(fontResource.toExternalForm(), StandardCharsets.UTF_8), 20) :
                new Font("Arial", 20);



        Label waitingLabel = new Label("Waiting for players...");
        waitingLabel.setFont(menuFont);
        waitingLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, blue, 5, 0.5, 0, 0);");
        waitingLabel.setLayoutX(WIDTH / 2.0 - 200);
        waitingLabel.setLayoutY(HEIGHT / 2.0 - 20);

        waitingScreen.getChildren().add(waitingLabel);

        Scene waitingScene = new Scene(waitingScreen, WIDTH, HEIGHT);
        stage.setScene(waitingScene);
        stage.setTitle("NinJava - Waiting for Players");

        // Vérifiez périodiquement si les deux joueurs sont connectés
        new Thread(() -> {
            try {
                while (!bothPlayersConnected) {
                    Thread.sleep(500); // Vérification toutes les 500 ms
                }
                Platform.runLater(() -> initializeGame(stage)); // Démarrer le jeu une fois connecté
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startGame(Stage stage, boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;

        if (isMultiplayer) {
            // Afficher l’écran "Waiting for players..."
            showWaitingForPlayersScreen(stage);
            connectToServer(); // Démarrer la connexion au serveur
        } else {
            initializeGame(stage); // Démarrer directement pour le mode solo
            bothPlayersConnected = true;
        }
    }


    private void initializeGame(Stage stage) {
        // Initialisation de la scène et du conteneur principal
        root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        gameStatus = GameStatus.WAITING_FOR_PLAYER;

        stage.setResizable(!isMultiplayer); // Permettre l'agrandissement uniquement en solo

        stage.setFullScreen(false); // Mode plein écran uniquement en solo
        stage.setFullScreenExitHint(""); // Désactiver le message de sortie
        stage.setFullScreenExitKeyCombination(null);
        stage.setResizable(false);


        backgroundContainer = new Pane();
        gameContainer = new Pane();

        canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);

        // Initialisation du joueur
        player = new Player(100, 300);
        gameContainer.getChildren().add(player);

        if (isMultiplayer) {
            initializePlayer2();
        } else {
            bothPlayersConnected = true;
        }

        // Initialisation du terrain
        loadBackground();
        drawInitialBackground();

        // Initialisation de l'état du jeu
        initializeScoreLabel();
        initializePauseMenu();

        this.primaryStage = stage;

        root.getChildren().addAll(backgroundContainer, gameContainer);

        try {
            MapLoader.loadMapFromFile("src/main/resources/images/mapDemo.txt", gameContainer, grounds, enemies, walls, decorations);
        } catch (IOException e) {
            System.err.println("Error loading map: " + e.getMessage());
        }

        // Gestion des entrées utilisateur
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());

        stage.widthProperty().addListener((observable, oldValue, newValue) -> drawBackground());
        stage.heightProperty().addListener((observable, oldValue, newValue) -> drawBackground());

        soundManager.playBackgroundMusic();
        // Boucle de jeu
        gameStatus = GameStatus.RUNNING;
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning && gameStatus == GameStatus.RUNNING && bothPlayersConnected) {

                        scene.setOnKeyPressed(event -> handleKeyPressed(event.getCode().toString()));
                        scene.setOnKeyReleased(event -> handleKeyReleased(event.getCode().toString()));

                    if (!player.isDead()) {
                        player.update();
                        updateEnergy(getActivePlayer());
                        updateLives(getActivePlayer());
                        updateScore(getActivePlayer());
                        updateCamera(getActivePlayer());
                        updateShurikens(getActivePlayer());
                    } else if (!isMultiplayer) {
                        soundManager.stopBackgroundMusic();
                        soundManager.playSound("die.mp3",0.4);
                        stop();
                        showGameOverScreen(primaryStage);
                        return;
                    }

                    if (isMultiplayer && bothPlayersConnected && !player2.isDead()) {
                        player2.update();
                        updateEnergy(getActivePlayer());
                        updateLives(getActivePlayer());
                        updateScore(getActivePlayer());
                        updateCamera(getActivePlayer());
                        updateShurikens(getActivePlayer());
                    } else if (isMultiplayer && player.isDead() && player2.isDead()) {
                        soundManager.stopBackgroundMusic();
                        soundManager.playSound("die.mp3",0.4);
                        stop();
                        showGameOverScreen(primaryStage);
                        return;
                    }

                    drawBackground();
                    updateBricks();
                    updateWall();
                    updateItems();
                    updateFireballs();
                    updateProjectiles();
                    updateEnemies();

                    if (isVictory)
                    {
                        soundManager.stopBackgroundMusic();
                        stop();
                        showVictoryScreen(primaryStage);
                    }
                }

                else {
                    System.out.println("Waiting for players");
                }
            }
        };

        gameLoop.start();
        isRunning = true;

        stage.setScene(scene);
        stage.setTitle("NinJava");
        stage.show();
    }

    private void showHelpScreen(Stage stage) {
        Pane helpPane = new Pane();
        helpPane.setPrefSize(WIDTH, HEIGHT);
        helpPane.setStyle("-fx-background-color: black;");

        // Charger la police
        URL fontResource = getClass().getResource("/font/gameFont.ttf");
        Font helpFont = (fontResource != null) ?
                Font.loadFont(URLDecoder.decode(fontResource.toExternalForm(), StandardCharsets.UTF_8), 15) :
                new Font("Arial", 15);

        Label title = new Label("Help - Key Bindings");
        title.setFont(helpFont);
        title.setStyle("-fx-text-fill: yellow; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 0, 0);");
        title.setLayoutX(WIDTH / 2.0 - 100);
        title.setLayoutY(20);

        // Liste des commandes
        String helpText = """
        Movement:
        - Arrow Right: Move Right
        - Arrow Left: Move Left
        - Arrow Up: Jump
        - Arrow Down: Teleport (if energy available)
        
        Combat:
        - Space: Melee Attack
        - W: Launch Shuriken
        - X: Launch Fireball (requires energy)
        
        System:
        - Escape: Pause Menu
        """;

        Label helpContent = new Label(helpText);
        helpContent.setFont(helpFont);
        helpContent.setStyle("-fx-text-fill: white;");
        helpContent.setLayoutX(50);
        helpContent.setLayoutY(80);

        // Bouton de retour
        Label backLabel = new Label("Press ENTER to return to the menu");
        backLabel.setFont(helpFont);
        backLabel.setStyle("-fx-text-fill: green;");
        backLabel.setLayoutX(WIDTH / 2.0 - 150);
        backLabel.setLayoutY(HEIGHT - 50);

        helpPane.getChildren().addAll(title, helpContent, backLabel);

        Scene helpScene = new Scene(helpPane, WIDTH, HEIGHT);
        stage.setResizable(false);
        stage.setScene(helpScene);

        // Gérer la touche ENTER pour revenir au menu principal
        helpScene.setOnKeyPressed(event -> {
            if ("ENTER".equals(event.getCode().toString())) {
                start(stage); // Retour au menu principal
            }
        });
    }

    private void centerLabel(Label label, double parentWidth, double parentHeight, double offsetX, double offsetY) {
        label.setLayoutX(parentWidth / 2.0 - label.getWidth() / 2 + offsetX);
        label.setLayoutY(parentHeight / 2.0 - label.getHeight() / 2 + offsetY);
    }



    private void initializePlayer2() {
        if (!isPlayer2Connected) {
            player2 = new Player(200, 300); // Position initiale
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

    private void updateWall() {// Liste temporaire pour supprimer les briques

        for (Wall wall : walls) {
            player.handleCollision(wall); // Gestion de la collision dans la classe Player

            if (isMultiplayer)
            {
                player2.handleCollision(wall);
            }
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

            for (Wall wall : walls) {
                enemy.handleCollision(wall);
            }

            if (enemy.isBoss() && enemy.isDead()) {
                System.out.println("Boss defeated! Launching victory screen...");
                isVictory = true;
                return; // Sortir de la méthode
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

            for (Wall wall : walls) {
                if (fireball.handleCollision(wall)) {
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

    private void updateScore(Player activePlayer) {
        scoreLabel.setText("Score: " + activePlayer.getPoints());
    }

    private void updateShurikens(Player activePlayer) {
        shurikenLabel.setText(" x " + activePlayer.getShurikens());
    }

    public void updateEnergy(Player activePlayer) {
        activePlayer.regenerateEnergy(); // Régénérer l'énergie
        energyLabel.setText("Energy: " + activePlayer.getEnergy()); // Mettre à jour l'affichage de l'énergie
    }

    private void updateItems() {
        // Liste temporaire pour les items collectés
        itemsToRemove.clear();

        for (Item item : items) {
            // Vérifier les collisions avec le joueur principal
            if (player.getBoundsInParent().intersects(item.getBoundsInParent())) {
                item.applyEffect(player); // Appliquer l'effet de l'item
                itemsToRemove.add(item); // Marquer l'item pour suppression
            }

            // Si multijoueur, vérifier les collisions avec le deuxième joueur
            if (isMultiplayer && player2.getBoundsInParent().intersects(item.getBoundsInParent())) {
                item.applyEffect(player2); // Appliquer l'effet de l'item
                itemsToRemove.add(item); // Marquer l'item pour suppression
            }
        }

        // Supprimer les items collectés
        for (Item item : itemsToRemove) {
            gameContainer.getChildren().remove(item);
            items.remove(item);
        }
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

    // Réception des actions utilisateur
    private void handleKeyPressed(String keyCode) {
        if (gameStatus == GameStatus.RUNNING) {
            if (isMultiplayer) {
                // Multijoueur : envoyer les actions au serveur
                String action = null;
                switch (keyCode) {
                    case "RIGHT" -> action = isPlayer1 ? "PLAYER1_MOVE_RIGHT" : "PLAYER2_MOVE_RIGHT";
                    case "LEFT" -> action = isPlayer1 ? "PLAYER1_MOVE_LEFT" : "PLAYER2_MOVE_LEFT";
                    case "UP" -> action = isPlayer1 ? "PLAYER1_JUMP" : "PLAYER2_JUMP";
                    case "DOWN" -> action = isPlayer1 ? "PLAYER1_TELEPORT" : "PLAYER2_TELEPORT";
                    case "SPACE" -> action = isPlayer1 ? "PLAYER1_ATTACK" : "PLAYER2_ATTACK";
                    case "W" -> action = isPlayer1 ? "PLAYER1_LAUNCH_SHURIKEN" : "PLAYER2_LAUNCH_SHURIKEN";
                    case "X" -> action = isPlayer1 ? "PLAYER1_LAUNCH_FIREBALL" : "PLAYER2_LAUNCH_FIREBALL";
                    case "ESCAPE" -> action = "PAUSE_GAME";
                }

                if (action != null) {
                    sendActionToServer(action); // Envoyer l'action au serveur
                }
            } else {
                // Mode local : actions exécutées directement
                switch (keyCode) {
                    case "RIGHT" -> player.moveRight();
                    case "LEFT" -> player.moveLeft();
                    case "UP" -> player.jump();
                    case "DOWN" -> {
                        if (!player.isInvincible()) player.startTeleportation();
                    }
                    case "SPACE" -> {
                        if (!player.isAttacking()) player.attack();
                    }
                    case "W" -> launchShuriken(player);
                    case "X" -> fireBallAction(player);
                    case "ESCAPE" -> togglePauseMenu();
                }
            }
        } else if (gameStatus == GameStatus.PAUSED) {
            handlePauseMenuNavigation(keyCode);
        }
    }

    private void handleKeyReleased(String keyCode) {
        if (isMultiplayer) {
            // Multijoueur : envoyer l'action "STOP" au serveur
            String action = null;
            if ("RIGHT".equals(keyCode) || "LEFT".equals(keyCode)) {
                action = isPlayer1 ? "PLAYER1_STOP" : "PLAYER2_STOP";
            }
            if (action != null) {
                sendActionToServer(action);
            }
        } else {
            // Mode local : arrêter le déplacement directement
            if ("RIGHT".equals(keyCode) || "LEFT".equals(keyCode)) {
                if (isPlayer1) {
                    player.stopMoving();
                } else {
                    player2.stopMoving();
                }
            }
        }
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
                    if (isMultiplayer) {
                        // Multijoueur : envoyer au serveur
                        sendActionToServer("PAUSE_GAME");
                    } else {
                        // Mode local : reprendre le jeu directement
                        togglePauseMenu();
                    }
                } else if (selectedOption == 1) { // Exit
                    if (isMultiplayer) {
                        // Multijoueur : envoyer au serveur
                        sendActionToServer("EXIT");
                    } else {
                        // Mode local : quitter directement
                        System.exit(0);
                    }
                }
            }
        }
    }

    private void togglePauseMenu() {
        if (gameStatus == GameStatus.PAUSED) {
            pauseMenu.setVisible(false);
            System.out.println("Resuming game...");
            gameStatus = GameStatus.RUNNING;
            isRunning = true;
        } else {
            pauseMenu.setVisible(true);
            System.out.println("Game paused.");
            gameStatus = GameStatus.PAUSED;
            isRunning = false;
        }
    }

    private void stopGame() {
        System.exit(0);
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

    private Player getActivePlayer() {
        return isPlayer1 ? player : player2;
    }


    private void showGameOverScreen(Stage stage) {
        // Attendre 3 secondes avant d'afficher l'écran Game Over
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Attendre 3 secondes pour laisser l'animation se terminer
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                Pane gameOverScreen = new Pane();
                gameOverScreen.setPrefSize(WIDTH, HEIGHT);
                gameOverScreen.setStyle("-fx-background-color: black;");

                // Charger la police du menu
                URL fontResource = getClass().getResource("/font/gameFont.ttf");
                Font gameOverFont = (fontResource != null) ?
                        Font.loadFont(URLDecoder.decode(fontResource.toExternalForm(), StandardCharsets.UTF_8), 40) :
                        new Font("Arial", 40);

                Label gameOverLabel = new Label("GAME OVER");
                gameOverLabel.setFont(gameOverFont);
                gameOverLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, white, 3, 0.7, 0, 0);");
                gameOverLabel.setLayoutX(WIDTH / 2.0 - 200); // Centrer horizontalement
                gameOverLabel.setLayoutY(HEIGHT / 2.0 - 30); // Centrer verticalement

                gameOverScreen.getChildren().add(gameOverLabel);

                Scene gameOverScene = new Scene(gameOverScreen, WIDTH, HEIGHT);
                stage.setScene(gameOverScene);
                stage.setResizable(false);
                stage.setTitle("Game Over");

                gameOverScene.widthProperty().addListener((observable, oldValue, newValue) -> {
                    centerLabel(gameOverLabel, (double) newValue, gameOverScreen.getHeight(), 0, 0);
                });

                gameOverScene.heightProperty().addListener((observable, oldValue, newValue) -> {
                    centerLabel(gameOverLabel, gameOverScreen.getWidth(), (double) newValue, 0, 0);
                });

                soundManager.playSound("gameover.mp3",0.5);

                // Revenir au menu principal après 5 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(5000); // Attendre 5 secondes
                        Platform.runLater(() -> start(stage)); // Revenir au menu principal
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        }).start();
    }

    private void showVictoryScreen(Stage stage) {
        // Attendre la fin de l'animation de mort avant d'afficher l'écran Victory
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Ajustez le temps pour correspondre à l'animation du boss
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                Pane victoryScreen = new Pane();
                victoryScreen.setPrefSize(WIDTH, HEIGHT);
                victoryScreen.setStyle("-fx-background-color: black;");

                URL fontResource = getClass().getResource("/font/gameFont.ttf");
                Font victoryFont = (fontResource != null) ?
                        Font.loadFont(URLDecoder.decode(fontResource.toExternalForm(), StandardCharsets.UTF_8), 40) :
                        new Font("Arial", 40);

                Label victoryLabel = new Label("YOU WIN!");
                victoryLabel.setFont(victoryFont);
                victoryLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, white, 3, 0.7, 0, 0);");
                victoryLabel.setLayoutX(WIDTH / 2.0 - 150);
                victoryLabel.setLayoutY(HEIGHT / 2.0 - 30);

                victoryScreen.getChildren().add(victoryLabel);

                Scene victoryScene = new Scene(victoryScreen, WIDTH, HEIGHT);
                stage.setScene(victoryScene);
                stage.setTitle("Victory!");
                stage.setResizable(false);
                soundManager.playSound("victory.mp3", 0.5);

                // Revenir au menu principal après 10 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(10000); // Attendre 10 secondes
                        Platform.runLater(() -> start(stage)); // Revenir au menu principal
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        }).start();
    }

    public static void main(String[] args) {
        launch(args); // Démarrage de l'application JavaFX
    }
}
