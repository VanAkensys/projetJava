package com.devops.ninjava.model.object;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Tombstone extends Pane {
    private ImageView tombstoneView;

    public Tombstone(double x, double y) {
        // Charger l'image de la tombe
        Image tombstoneImage = new Image(getClass().getResource("/images/map/tombstone/tombstone_base_strip1.png").toExternalForm());
        tombstoneView = new ImageView(tombstoneImage);

        // Ajuster la taille de l'image
        tombstoneView.setFitWidth(48);
        tombstoneView.setFitHeight(48);

        // Positionner la tombe
        this.setLayoutX(x);
        this.setLayoutY(y + 48); // Ajuster pour que la tombe soit alignée avec le sol
        this.getChildren().add(tombstoneView);
    }
}
