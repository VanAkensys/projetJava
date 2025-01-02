package com.devops.ninjava.model.enemy;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Enemy1 extends Enemy {

    private Image[] walkFrames; // Frames pour l'animation de déplacement
    private int frameCounter = 0;
    private static final int ANIMATION_DELAY = 10; // Délai entre les frames
    private static final double DEFAULT_SPEED = 2.0;

    public Enemy1(double x, double y) {
        super(x, y, 48, 48); // Taille des frames : 32x32
        this.velX = DEFAULT_SPEED;
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

        // Déplacement horizontal
        setLayoutX(getLayoutX() + velX);

        // Inversion de direction aux bords
        if (getLayoutX() <= 0 || getLayoutX() + getWidth() >= 20000) { // Ajuster les limites
            velX = -velX;
        }

        // Animation de déplacement
        if (frameCounter % ANIMATION_DELAY == 0) {
            int frameIndex = (frameCounter / ANIMATION_DELAY) % walkFrames.length;
            setImageViewFrame(walkFrames[frameIndex]);
        }
        frameCounter++;
    }
}
