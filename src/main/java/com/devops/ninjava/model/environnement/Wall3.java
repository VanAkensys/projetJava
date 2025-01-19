package com.devops.ninjava.model.environnement;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Wall3 extends Wall{

    public Wall3(double x, double y) {
        super(x, y, 32, 32); // Dimensions par défaut
    }

    @Override
    protected void initializeImages() {
        try {
            // Initialiser l'Image et assigner à ImageView
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/map/tileset/Tileset1.png"));
            Image brickImage = SwingFXUtils.toFXImage(spriteSheet.getSubimage(32, 176, 32, 32), null);
            wallView = new ImageView(brickImage);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
        }
    }

}
