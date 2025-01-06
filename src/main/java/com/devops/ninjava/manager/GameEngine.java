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
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    private List<FireBall> fireBalls = new ArrayList<>();
    private List<FireBall> fireBallsToRemove = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Projectile> projectilesToRemove = new ArrayList<>();
    private List<Ground> grounds = new ArrayList<>();
    private List<Ground> toRemove = new ArrayList<>();
    private Camera camera;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Enemy> enemiesToRemove = new ArrayList<>();



    @Override
    public void start(Stage stage) {


        // Initialisation de la scène et du conteneur principal
        root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        stage.setFullScreen(true);
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


        // Initialisation du terrain
        loadBackground();
        drawInitialBackground();

        // Initialisation de l'état du jeu
        gameStatus = GameStatus.RUNNING;

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
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning && gameStatus == GameStatus.RUNNING && !player.isDead()) {
                    player.update(); // Mise à jour de l'état du joueur
                    drawBackground();
                    updateLives();
                    updateScore(); // Mise à jour du score
                    updateEnergy();
                    updateBricks();
                    updateFireballs();
                    updateCamera();
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

    private void updateLives() {
        livesLabel.setText("Lives: " + player.getRemainingLives());
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


    private void fireBallAction() {
        if (!player.isAttacking() && player.useEnergy(player.ENERGY_COST_FIREBALL)) {
            player.animateJutsuLaunch();
            Image fireballImage = new Image(getClass().getResource("/images/powerUp/fireball.png").toExternalForm());
            FireBall fireball = new FireBall(
                    player.getX() + (player.getVelX() >= 0 ? 48 : -24),
                    player.getY() + 24,
                    player.getVelX() >= 0,
                    fireballImage
            );
            fireBalls.add(fireball);
            gameContainer.getChildren().add(fireball);
        }else {
            System.out.println("Not enough energy to launch a fireball!");
        }
    }

    private void launchShuriken() {
        if (!player.isAttacking() && player.useShuriken()) { // Empêcher de lancer plusieurs shurikens en même temps
            player.animateProjectileLaunch(); // Animer le lancement du shuriken

            BufferedImage spriteSheet = null;
            try {
                spriteSheet = ImageIO.read(getClass().getResource("/images/player/Ultimate_Ninja_Spritesheet.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Shuriken shuriken = new Shuriken(
                    player.getX() + (player.getVelX() >= 0 ? 48 : -24), // Position de départ
                    player.getY() + 16, // Position de départ
                    player.getVelX() >= 0 || player.getScaleX() > 0, // Direction
                    spriteSheet,
                    false
            );

            updateShurikens();

            projectiles.add(shuriken); // Ajouter à la liste des projectiles
            gameContainer.getChildren().add(shuriken); // Ajouter au conteneur
        }
    }

    private void updateShurikens() {
        shurikenLabel.setText(" x " + player.getShurikens());
    }

    public void updateEnergy() {
        player.regenerateEnergy(); // Régénérer l'énergie
        energyLabel.setText("Energy: " + player.getEnergy()); // Mettre à jour l'affichage de l'énergie
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

    private void updateScore() {
        scoreLabel.setText("Score: " + player.getPoints());
    }

    // Réception des actions utilisateur
    private void handleKeyPressed(String keyCode) {
        if(gameStatus == GameStatus.RUNNING && !player.isDead()) {
            switch (keyCode) {
                case "RIGHT" -> player.moveRight(); // Déplacement à droite
                case "LEFT" -> player.moveLeft(); // Déplacement à gauche
                case "UP" -> {
                    player.jump(); // Saut
                    player.update();
                    System.out.println("jump");
                }
                case "DOWN" -> {
                    if (!player.isInvincible())
                    {
                        player.startTeleportation();
                    }

                }
                case "SPACE" -> {
                    if (!player.isAttacking())
                    {
                        player.attack();
                    }
                }
                case "W" -> {
                    launchShuriken(); // Lancer un shuriken
                    System.out.println("Shuriken launched!");
                }
                case "X" -> {
                    fireBallAction(); // Lancer un shuriken
                    System.out.println("fireBall launched!");
                }
                case "ESCAPE" -> togglePauseMenu(); // Affiche ou masque le menu de pause
                default -> System.out.println("Unhandled key: " + keyCode);
            }
        }else if (gameStatus == GameStatus.PAUSED) {
            handlePauseMenuNavigation(keyCode);
        }
    }

    private void handleKeyReleased(String keyCode) {
        switch (keyCode) {
            case "RIGHT", "LEFT" -> player.stopMoving(); // Arrêt des déplacements horizontaux
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
                    togglePauseMenu();
                } else if (selectedOption == 1) { // Exit
                    System.exit(0); // Quitter le jeu
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
