package com.devops.ninjava.model.item;

import com.devops.ninjava.model.hero.Player;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ShurikenItem extends Item {

    private int shurikenAmount; // Quantité de shurikens ajoutés

    public ShurikenItem(double x, double y, double width, double height, int shurikenAmount) {
        super(x, y, width, height);
        this.shurikenAmount = shurikenAmount;
    }

    @Override
    protected void initializeImages() {
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/player/Ultimate_Ninja_Spritesheet.png"));
            Image shurikenImage = convertToFXImage(spriteSheet.getSubimage(6 * 48, 0 * 48, 48, 48)); // Découpe le shuriken

            itemView = new ImageView(shurikenImage);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'image ShurikenItem : " + e.getMessage());
        }
    }

    private Image convertToFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }


    @Override
    public void applyEffect(Player player) {
        player.shurikens += shurikenAmount; // Ajouter des shurikens
        System.out.println("Shuriken collected! Shurikens added: " + shurikenAmount);
    }
}
