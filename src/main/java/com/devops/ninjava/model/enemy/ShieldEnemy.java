package com.devops.ninjava.model.enemy;

import com.devops.ninjava.model.object.Tombstone;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ShieldEnemy extends Enemy {

    private Image[] idleFrames; // Frames pour l'animation d'inactivité
    private int frameCounter = 0; // Compteur pour gérer l'animation
    private static final int ANIMATION_DELAY = 20; // Délai entre les frames pour l'animation

    public ShieldEnemy(double x, double y) {
        super(x, y, 48, 48); // Taille des frames : 48x48
        this.health = 400;      // Santé spécifique au boss
        this.maxHealth = 400;
        loadSprites();
    }

    private void loadSprites() {
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/enemy/Enemies.png"));
            int frameWidth = 48;
            int frameHeight = 48;
            int yOffset = 11 * frameHeight; // Ligne correspondant à Enemy3 (12ème ligne)

            // Chargement des frames d'animation d'inactivité
            idleFrames = new Image[5];
            for (int i = 0; i < 5; i++) {
                BufferedImage frame = spriteSheet.getSubimage(i * frameWidth, yOffset, frameWidth, frameHeight);
                idleFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            setImageViewFrame(idleFrames[0]); // Définit l'image initiale
        } catch (IOException e) {
            System.err.println("Error loading Enemy3 sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (isDead) return;

        // Animation d'inactivité
        if (frameCounter % ANIMATION_DELAY == 0) {
            int frameIndex = (frameCounter / ANIMATION_DELAY) % idleFrames.length;
            setImageViewFrame(idleFrames[frameIndex]);
        }

        // Appliquer la gravité pour Enemy3
        applyGravity();

        frameCounter++;
    }

    public void die() {
        isDead = true;
        // Créer la tombe
        Tombstone tombstone = new Tombstone(this.getLayoutX(), this.getLayoutY());

        // Ajouter la tombe au conteneur parent
        if (this.getParent() != null) {
            Pane parent = (Pane) this.getParent();
            parent.getChildren().add(tombstone); // Ajouter la tombe au conteneur
        }

        this.setVisible(false); // Masquer l'ennemi après la mort
    }

}
