package com.devops.ninjava.model.item;

import com.devops.ninjava.model.hero.Player;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Heart extends Item {

    private int healAmount; // Quantité de PV restaurée

    public Heart(double x, double y, double width, double height, int healAmount) {
        super(x, y, width, height);
        this.healAmount = healAmount;
    }

    @Override
    protected void initializeImages() {
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/powerUp/heart.png"));
            Image heartImage = SwingFXUtils.toFXImage(spriteSheet, null);
            itemView = new javafx.scene.image.ImageView(heartImage);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'image Heart : " + e.getMessage());
        }
    }

    @Override
    public void applyEffect(Player player) {
        player.remainingLives = Math.min(player.remainingLives + healAmount, 10); // Limiter au maximum de vies
        System.out.println("Heart collected! Lives restored: " + healAmount);
    }
}
