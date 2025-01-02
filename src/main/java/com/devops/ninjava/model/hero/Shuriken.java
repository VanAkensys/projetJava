package com.devops.ninjava.model.hero;

import com.devops.ninjava.model.decor.Brick;
import com.devops.ninjava.model.decor.Pipe;
import com.devops.ninjava.model.enemy.Enemy;
import com.devops.ninjava.model.enemy.Goomba;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.awt.image.BufferedImage;

public class Shuriken extends Projectile {

    private Image[] trajectoryFrames; // Images pour l'animation de trajectoire
    private Image[] impactFrames;     // Images pour l'animation d'impact
    private int animationIndex = 0;  // Index de l'animation
    private boolean isImpacting = false; // Indique si le shuriken est en phase d'impact
    private int impactIndex = 0;

    private int frameCounter = 0; // Compteur pour ralentir l'animation

    private Rectangle hitbox;

    public Shuriken(double x, double y, boolean toRight, BufferedImage spriteSheet) {
        super(x, y, toRight, SwingFXUtils.toFXImage(spriteSheet.getSubimage(5 * 48, 0 * 48, 48, 48), null));

        this.hitbox = new Rectangle(48, 48, 48, 48); // Par exemple, une hitbox plus petite
        this.getChildren().add(hitbox); // (Optionnel) Ajoute visuellement la hitbox pour le debug
        hitbox.setVisible(false);

        // Initialisation des frames pour l'animation
        trajectoryFrames = new Image[]{
                convertToFXImage(spriteSheet.getSubimage(6 * 48, 0 * 48, 48, 48)),
                convertToFXImage(spriteSheet.getSubimage(7 * 48, 0 * 48, 48, 48)),
        };

        impactFrames = new Image[]{
                convertToFXImage(spriteSheet.getSubimage(8 * 48, 0 * 48, 48, 48)),
                convertToFXImage(spriteSheet.getSubimage(9 * 48, 0 * 48, 48, 48)),
        };

        // Afficher la première image de la trajectoire
        projectileView.setImage(trajectoryFrames[0]);
    }

    private Image convertToFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }


    @Override
    public void update() {
        if (!isActive) return;

        if (isImpacting) {
            playImpactAnimation();
        } else {
            // Déplacer le shuriken
            setLayoutX(getLayoutX() + velX);
            hitbox.setX(getLayoutX()); // Mettre à jour la position X de la hitbox
            hitbox.setY(getLayoutY()); // Mettre à jour la position Y de la hitbox

            playTrajectoryAnimation();
        }
    }

    private void playTrajectoryAnimation() {
        if (frameCounter % 5 == 0) { // Changer d'image toutes les 5 frames
            animationIndex = (animationIndex + 1) % trajectoryFrames.length;
            projectileView.setImage(trajectoryFrames[animationIndex]);
        }
        frameCounter++;
    }

    private void playImpactAnimation() {
        if (frameCounter % 5 == 0) { // Changer d'image toutes les 5 frames
            if (impactIndex < impactFrames.length) {
                projectileView.setImage(impactFrames[impactIndex]);
                impactIndex++;
            } else {
                deactivate(); // Désactiver après la fin de l'animation
            }
        }
        frameCounter++;
    }

    @Override
    public boolean handleCollision(Pane object) {
        if (!isActive) return false;

        // Vérifiez les collisions avec la hitbox au lieu de l'image complète
        if (object.getBoundsInParent().intersects(hitbox.getBoundsInParent())) {
            if (object instanceof Brick || object instanceof Pipe) {
                startImpactAnimation();
                return true;
            } else if (object instanceof Enemy) {
                ((Enemy) object).die(); // Tuer le Goomba
                startImpactAnimation();
                return true;
            }
        }
        return false;
    }
    private void startImpactAnimation() {
        isImpacting = true;
        impactIndex = 0;
        frameCounter = 0;
        velX = 0; // Arrêter le mouvement pendant l'impact
    }
    public void deactivate() {
        this.isActive = false;
        this.setVisible(false); // Masquer le shuriken
    }

    public boolean isActive() {
        return isActive;
    }
}
