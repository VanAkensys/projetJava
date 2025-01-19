package com.devops.ninjava.model.enemy;

import com.devops.ninjava.model.item.Heart;
import com.devops.ninjava.model.item.ShurikenItem;
import com.devops.ninjava.model.item.Tombstone;
import com.devops.ninjava.model.projectile.Shuriken;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShurikenEnemy extends Enemy {

    private Image[] idleFrames; // Frames pour l'animation en position
    private Image shurikenImage; // Image du shuriken
    private int frameIndex = 0; // Frame courante pour l'animation
    private static final int FRAME_DELAY = 10; // Délai entre les frames
    private static final int SHURIKEN_THROW_INTERVAL = 150; // Intervalle entre les shurikens
    private int frameCounter = 0; // Compteur pour le délai entre les frames
    private int shurikenTimer = 0; // Compteur pour le délai entre les shurikens
    private BufferedImage spriteSheet;
    private List<Shuriken> shurikens;

    public ShurikenEnemy(double x, double y) {
        super(x, y, 48, 48);
        this.shurikens = new ArrayList<>(); // Initialiser la liste des shurikens
        this.health = 100;      // Santé spécifique au boss
        this.maxHealth = 100;
        loadSprites();
    }

    private void loadSprites() {
        try {
            spriteSheet = ImageIO.read(getClass().getResource("/images/enemy/Enemies.png"));
            int frameWidth = 48;
            int frameHeight = 48;
            int yOffset = 12 * frameHeight; // Ligne 13 (indexé à partir de 0)

            // Chargement des frames d'animation
            idleFrames = new Image[5];
            for (int i = 0; i < 5; i++) {
                BufferedImage frame = spriteSheet.getSubimage(i * frameWidth, yOffset, frameWidth, frameHeight);
                idleFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            // Chargement de l'image du shuriken
            shurikenImage = SwingFXUtils.toFXImage(spriteSheet.getSubimage(9 * frameWidth, yOffset, 12, 12), null);

            setImageViewFrame(idleFrames[0]); // Définit l'image initiale
        } catch (IOException e) {
            System.err.println("Error loading ShurikenEnemy sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        // Appliquer la gravité
        applyGravity();

        // Gérer l'animation idle
        frameCounter++;
        shurikenTimer++;
        if (frameCounter >= FRAME_DELAY) {
            frameIndex = (frameIndex + 1) % idleFrames.length;
            setImageViewFrame(idleFrames[frameIndex]);
            frameCounter = 0;

            // Lancer un shuriken si l'animation atteint la dernière frame
            if (frameIndex == idleFrames.length - 1 && shurikenTimer >= SHURIKEN_THROW_INTERVAL) {
                throwShuriken();
                shurikenTimer = 0; // Réinitialiser le compteur
            }
        }

        // Mettre à jour les shurikens
        updateShurikens();
    }

    private void throwShuriken() {
        // Vérifiez si l'image du shuriken est valide
        if (shurikenImage == null) {
            System.err.println("Erreur : L'image du shuriken n'a pas été chargée !");
            return;
        }

        BufferedImage spriteSheet = null;
        try {
            spriteSheet = ImageIO.read(getClass().getResource("/images/player/Ultimate_Ninja_Spritesheet.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Coordonnées de lancement
        double shurikenX = getLayoutX() + getWidth() / 2;
        double shurikenY = getLayoutY();

        System.out.println("Lancer shuriken à : " + shurikenX + ", " + shurikenY);

        // Créez un nouveau shuriken
        Shuriken shuriken = new Shuriken(
                shurikenX,  // Position X
                shurikenY,  // Position Y
                false,      // Direction (vers la gauche)
                spriteSheet,
                true
        );

        shurikens.add(shuriken);

        // Ajoutez le shuriken à la scène
        if (this.getParent() instanceof Pane pane) {
            pane.getChildren().add(shuriken);
            System.out.println("Shuriken ajouté au conteneur parent.");
        } else {
            System.err.println("Erreur : Impossible d'ajouter le shuriken car le parent est null.");
        }
    }

    private void updateShurikens() {
        shurikens.removeIf(shuriken -> {
            shuriken.update(); // Mettre à jour la position et l'état du shuriken
            if (!shuriken.isActive()) { // Si le shuriken est inactif
                if (this.getParent() instanceof Pane pane) {
                    pane.getChildren().remove(shuriken); // Supprimer de la scène
                }
                return true; // Retirer de la liste
            }
            return false; // Garder dans la liste
        });
    }

    private void clearShurikens() {
        for (Shuriken shuriken : shurikens) {
            shuriken.deactivate(); // Désactiver le shuriken
            if (this.getParent() instanceof Pane pane) {
                pane.getChildren().remove(shuriken); // Supprimer de la scène
            }
        }
        shurikens.clear(); // Vider la liste
    }

    public void die() {
        isDead = true;
        // Créer la tombe
        Tombstone tombstone = new Tombstone(this.getLayoutX(), this.getLayoutY());

        clearShurikens();

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
