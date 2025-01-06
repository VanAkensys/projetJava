package com.devops.ninjava.model.decor;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Ground extends Pane {

    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;

    private ImageView brickView;
    private int floor;
    private boolean isBroken;

    public Ground(double x, double y) {
        this.floor = (int) (575 - 225); // Position de base sur le sol

        initializeImages();
        initializeBrickView(x, y); // Initialisation de la vue de la brique
    }

    private void initializeImages() {
        try {
            // Initialiser l'Image et assigner à ImageView
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/map/tileset/Tileset1.png"));
            Image brickImage = SwingFXUtils.toFXImage(spriteSheet.getSubimage(0, 112, 32, 32), null);
            brickView = new ImageView(brickImage);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
        }
    }

    private void initializeBrickView(double x, double y) {
        brickView.setFitWidth(WIDTH); // Définir la largeur de l'image
        brickView.setFitHeight(HEIGHT); // Définir la hauteur de l'image
        this.getChildren().add(brickView); // Ajouter l'image au Pane

        // Positionner la brique sur la scène
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void breakBrick() {
        if (!isBroken) {
            isBroken = true;
            this.setVisible(false); // Cache la brique cassée
        }
    }

    public boolean isBroken() {
        return isBroken;
    }
}