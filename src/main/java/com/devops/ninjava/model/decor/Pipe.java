package com.devops.ninjava.model.decor;

import com.devops.ninjava.model.enemy.Goomba;
import com.devops.ninjava.model.hero.FireBall;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Pipe extends Pane {

    private final int x;
    private final int y;
    private final Image image;
    private ImageView pipeView;

    public Pipe(int x, Image image) {
        this.x = x;
        this.image = image;
        this.y = (int) (550 - 85 - image.getHeight()); // Calcul de la hauteur
        this.setLayoutX(x);
        this.setLayoutY(y);
        this.setPrefSize(image.getWidth(), image.getHeight());
        this.setStyle("-fx-background-image: url('" + image.getUrl() + "');");
    }

    private void initializeImages() {
        try {
            // Initialiser l'Image et assigner à ImageView
            Image pipeImage = new Image(getClass().getResource("/images/pipe/pipeSmall.png").toExternalForm());
            pipeView = new ImageView(pipeImage);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Image getImage() {
        return image;
    }

    /**
     * Vérifie si une collision s'est produite avec un ennemi.
     */
    public boolean collision(Goomba enemy) {
        return this.getBoundsInParent().intersects(enemy.getBoundsInParent());
    }

    /**
     * Vérifie si une collision s'est produite avec une boule de feu.
     */
    public boolean collision(FireBall fireball) {
        return this.getBoundsInParent().intersects(fireball.getBoundsInParent());
    }

    /**
     * Vérifie si une collision s'est produite avec le joueur.
     */
    public boolean collision(Pane player) {
        return this.getBoundsInParent().intersects(player.getBoundsInParent());
    }
}
