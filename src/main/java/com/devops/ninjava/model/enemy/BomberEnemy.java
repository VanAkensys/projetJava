package com.devops.ninjava.model.enemy;

import com.devops.ninjava.model.item.Heart;
import com.devops.ninjava.model.item.ShurikenItem;
import com.devops.ninjava.model.item.Tombstone;
import com.devops.ninjava.model.projectile.Bomb;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BomberEnemy extends Enemy {

    private Image[] idleFrames; // Frames pour l'animation en position
    private Image bombImage; // Image de la bombe
    private int frameIndex = 0; // Frame courante pour l'animation
    private static final int FRAME_DELAY = 10; // Délai entre les frames
    private static final int BOMB_THROW_INTERVAL = 200; // Intervalle entre les bombes
    private int frameCounter = 0; // Compteur pour le délai entre les frames
    private int bombTimer = 0; // Compteur pour le délai entre les bombes
    private BufferedImage spriteSheet;
    private List<Bomb> bombs;

    public BomberEnemy(double x, double y) {
        super(x, y, 48, 48);
        this.bombs = new ArrayList<>(); // Initialiser la liste des bombes
        loadSprites();
    }

    private void loadSprites() {
        try {
            spriteSheet = ImageIO.read(getClass().getResource("/images/enemy/Enemies.png"));
            int frameWidth = 48;
            int frameHeight = 48;
            int yOffset = 5 * frameHeight;

            // Chargement des frames d'animation d'inactivité
            idleFrames = new Image[8];
            for (int i = 0; i < 8; i++) {
                BufferedImage frame = spriteSheet.getSubimage(i * frameWidth, yOffset, frameWidth, frameHeight);
                idleFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            // Chargement de l'image de la bombe
            bombImage = SwingFXUtils.toFXImage(spriteSheet.getSubimage(8 * frameWidth, yOffset, 12, 12), null);

            setImageViewFrame(idleFrames[0]); // Définit l'image initiale
        } catch (IOException e) {
            System.err.println("Error loading BomberEnemy sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        // Appliquer la gravité
        applyGravity();

        // Gérer l'animation idle
        frameCounter++;
        bombTimer++;
        if (frameCounter >= FRAME_DELAY) {
            frameIndex = (frameIndex + 1) % idleFrames.length;
            setImageViewFrame(idleFrames[frameIndex]);
            frameCounter = 0;

            // Lancer une bombe si l'animation atteint la dernière frame
            if (frameIndex == idleFrames.length - 1 && bombTimer >= BOMB_THROW_INTERVAL) {
                throwBomb();
                bombTimer = 0; // Réinitialiser le compteur
            }
        }

        // Mettre à jour les bombes
        updateBombs();
    }

    private void throwBomb() {
        // Vérifiez si l'image de la bombe est valide
        if (bombImage == null) {
            System.err.println("Erreur : L'image de la bombe n'a pas été chargée !");
            return;
        }

        // Coordonnées de lancement
        double bombX = getLayoutX() + getWidth() / 2;
        double bombY = getLayoutY()  ;

        System.out.println("Lancer bombe à : " + bombX + ", " + bombY);

        // Créez une nouvelle bombe
        Bomb bomb = new Bomb(
                bombX,  // Position X
                bombY,  // Position Y
                false,   // Direction
                bombImage // Spritesheet contenant l'image
        );

        bombs.add(bomb);

        // Ajoutez la bombe à la scène
        if (this.getParent() instanceof Pane pane) {
            pane.getChildren().add(bomb);
            System.out.println("Bombe ajoutée au conteneur parent.");
        } else {
            System.err.println("Erreur : Impossible d'ajouter la bombe car le parent est null.");
        }
    }

    private void updateBombs() {
        bombs.removeIf(bomb -> {
            bomb.update(); // Mettre à jour la position et l'état de la bombe
            if (!bomb.isActive()) { // Si la bombe est inactive
                if (this.getParent() instanceof Pane pane) {
                    pane.getChildren().remove(bomb); // Supprimer de la scène
                }
                return true; // Retirer de la liste
            }
            return false; // Garder dans la liste
        });
    }

    private void clearBombs() {
        for (Bomb bomb : bombs) {
            bomb.deactivate(); // Désactiver la bombe
            if (this.getParent() instanceof Pane pane) {
                pane.getChildren().remove(bomb); // Supprimer de la scène
            }
        }
        bombs.clear(); // Vider la liste
    }

    public void die() {
        isDead = true;

        clearBombs();
        // Créer la tombe
        Tombstone tombstone = new Tombstone(this.getLayoutX(), this.getLayoutY());

        // Ajouter la tombe et un item aléatoire au conteneur parent
        if (this.getParent() != null) {
            Pane parent = (Pane) this.getParent();
            parent.getChildren().add(tombstone);

            // Générer un item aléatoire
            if (Math.random() < 0.5) {
                // Créer un Heart
                Heart heart = new Heart(this.getLayoutX(), this.getLayoutY() - 32, 48, 48, 1);
                parent.getChildren().add(heart);
            } else {
                // Créer un Shuriken
                ShurikenItem shuriken = new ShurikenItem(this.getLayoutX(), this.getLayoutY() - 32, 96, 96, 5);
                parent.getChildren().add(shuriken);
            }
        }

        this.setVisible(false); // Masquer l'ennemi après la mort
    }
}
