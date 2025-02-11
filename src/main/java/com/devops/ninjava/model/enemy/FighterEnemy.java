package com.devops.ninjava.model.enemy;

import com.devops.ninjava.model.item.Heart;
import com.devops.ninjava.model.item.ShurikenItem;
import com.devops.ninjava.model.item.Tombstone;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FighterEnemy extends Enemy {

    private Image[] walkFrames; // Frames pour l'animation de déplacement
    private int frameCounter = 0;
    private static final int ANIMATION_DELAY = 10; // Délai entre les frames
    private static final double DEFAULT_SPEED = 2.0;
    private static final double MAX_DISTANCE = 100.0;
    private final double initialX;

    public FighterEnemy(double x, double y) {
        super(x, y, 48, 48); // Taille des frames : 32x32
        this.velX = DEFAULT_SPEED;
        this.initialX = x;
        this.health = 200;      // Santé spécifique au boss
        this.maxHealth = 200;
        loadSprites();
    }


    private void loadSprites() {
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/enemy/Enemies.png"));
            int frameWidth = 48;
            int frameHeight = 48;
            int yOffset = 1 * frameHeight; // Deuxième ligne

            walkFrames = new Image[8]; // 8 frames sur la ligne
            for (int i = 0; i < 8; i++) {
                BufferedImage frame = spriteSheet.getSubimage(i * frameWidth, yOffset, frameWidth, frameHeight);
                walkFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            setImageViewFrame(walkFrames[0]); // Image initiale
        } catch (IOException e) {
            System.err.println("Error loading Enemy1 sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (isDead) return;

        double currentX = getLayoutX();
        if (currentX <= initialX - MAX_DISTANCE || currentX >= initialX + MAX_DISTANCE) {
            velX = -velX; // Inverser la direction
        }
        // Déplacement horizontal
        setLayoutX(currentX + velX);

        applyGravity();

        // Animation de déplacement
        if (frameCounter % ANIMATION_DELAY == 0) {
            int frameIndex = (frameCounter / ANIMATION_DELAY) % walkFrames.length;
            setImageViewFrame(walkFrames[frameIndex]);
        }
        frameCounter++;
    }

    public void die() {
        isDead = true;
        // Créer une tombe
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
