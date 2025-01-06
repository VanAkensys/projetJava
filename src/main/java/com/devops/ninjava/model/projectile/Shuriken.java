package com.devops.ninjava.model.projectile;

import com.devops.ninjava.model.decor.Ground;
import com.devops.ninjava.model.enemy.Enemy;
import com.devops.ninjava.model.hero.Player;
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
    private final int damage = 10;

    private int frameCounter = 0; // Compteur pour ralentir l'animation

    private boolean isEnemyShuriken; // Indique si le shuriken provient d'un ennemi
    private Rectangle hitbox;
    private int collisionDelay = 10;

    public Shuriken(double x, double y, boolean toRight, BufferedImage spriteSheet, boolean isEnemyShuriken) {
        super(x, y, toRight, SwingFXUtils.toFXImage(spriteSheet.getSubimage(5 * 48, 0 * 48, 48, 48), null));

        this.isEnemyShuriken = isEnemyShuriken;

        this.hitbox = new Rectangle(12, 12, 12, 12); // Par exemple, une hitbox plus petite
        this.getChildren().add(hitbox); // (Optionnel) Ajoute visuellement la hitbox pour le debug
        this.hitbox.setX(x); // Position ajustée pour centrer la hitbox
        this.hitbox.setY(y);
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
            setLayoutX(getLayoutX() + velX); // Déplacer le shuriken
            playTrajectoryAnimation();

            hitbox.setX(getLayoutX()); // Ajustez selon la taille réelle de la hitbox
            hitbox.setY(getLayoutY());


            // Réduire le délai de collision
            if (collisionDelay > 0) {
                collisionDelay--;
            } else {
                checkCollision(); // Vérifier les collisions seulement après le délai
            }
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

    private void checkCollision() {
        if (getParent() == null) return;

        getParent().getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof Ground) {
                if (collisionDelay <= 0 && hitbox.getBoundsInParent().intersects(node.getBoundsInParent())) {
                    System.out.println("Collision détectée avec un décor.");
                    startImpactAnimation();
                }
            } else if (isEnemyShuriken && node instanceof Player) {
                Player player = (Player) node;
                if (hitbox.getBoundsInParent().intersects(player.getBoundsInParent()) && !player.isInvincible()) {
                    System.out.println("Collision détectée avec le joueur.");
                    player.onTouchEnemy();
                    startImpactAnimation();
                }
            } else if (!isEnemyShuriken && node instanceof Enemy) {
                Enemy enemy = (Enemy) node;
                if (hitbox.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                    System.out.println("Collision détectée avec un ennemi.");
                    enemy.takeDamage(damage);
                    startImpactAnimation();
                }
            }
        });
    }

    @Override
    public boolean handleCollision(Pane object) {
        // Gérer les collisions spécifiques si besoin
        if (isActive && this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            deactivate();
            return true;
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
