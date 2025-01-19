package com.devops.ninjava.model.enemy;

import com.devops.ninjava.model.item.Heart;
import com.devops.ninjava.model.item.ShurikenItem;
import com.devops.ninjava.model.item.Tombstone;
import com.devops.ninjava.model.projectile.EnemysMissile;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MissileEnemy extends Enemy {

    private Image[] idleFrames; // Frames pour l'animation d'inactivité
    private Image missileImage; // Image du missile
    private int frameCounter = 0; // Compteur pour gérer l'animation
    private static final int ANIMATION_DELAY = 20; // Délai entre les frames pour l'animation
    private static final int SHOOT_INTERVAL = 100; // Intervalle pour tirer un missile (en frames)
    private List<EnemysMissile> missiles; // Liste des missiles tirés
    private double x, y;

    public MissileEnemy(double x, double y) {
        super(x, y, 48, 48); // Taille des frames : 48x48
        this.missiles = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.health = 150;      // Santé spécifique au boss
        this.maxHealth = 150;
        loadSprites();
    }

    private void loadSprites() {
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/enemy/Enemies.png"));
            int frameWidth = 48;
            int frameHeight = 48;
            int yOffset = 4 * frameHeight; // Ligne correspondant à Enemy2

            // Chargement des frames d'animation d'inactivité
            idleFrames = new Image[3];
            for (int i = 0; i < 3; i++) {
                BufferedImage frame = spriteSheet.getSubimage(i * frameWidth, yOffset, frameWidth, frameHeight);
                idleFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            // Chargement de l'image du missile (colonne 4)
            missileImage = SwingFXUtils.toFXImage(spriteSheet.getSubimage(3 * frameWidth, yOffset, frameWidth, frameHeight), null);

            setImageViewFrame(idleFrames[0]); // Définit l'image initiale
        } catch (IOException e) {
            System.err.println("Error loading Enemy2 sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (isDead) return;

        applyGravity();

        // Animation d'inactivité
        if (frameCounter % ANIMATION_DELAY == 0) {
            int frameIndex = (frameCounter / ANIMATION_DELAY) % idleFrames.length;
            setImageViewFrame(idleFrames[frameIndex]);
        }

        // Tir de missile à intervalles réguliers
        if (frameCounter % SHOOT_INTERVAL == 0) {
            shootMissile();
        }

        // Mettre à jour les missiles
        updateMissiles();

        frameCounter++;
    }

    private void shootMissile() {
        // Crée un nouveau missile
        double missileX = getX();
        double missileY = getY();

        System.out.println("Tir missile à : " + missileX + ", " + missileY);

        EnemysMissile missile = new EnemysMissile(
                missileX, // Position initiale X
                missileY -5, // Position initiale Y
                false, // Direction (par défaut vers la gauche)
                missileImage // Image du missile
        );

        missiles.add(missile);

        if (this.getParent() instanceof Pane) {
            ((Pane) this.getParent()).getChildren().add(missile);
        }
    }

    public void die() {
        isDead = true;

        clearMissiles();
        // Créer la tombe
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

    private void clearMissiles() {
        // Désactive tous les missiles restants
        for (EnemysMissile missile : missiles) {
            missile.deactivate();
            // Supprime le missile de la scène s'il est encore présent
            if (this.getParent() instanceof Pane) {
                ((Pane) this.getParent()).getChildren().remove(missile);
            }
        }

        // Vide la liste des missiles
        missiles.clear();
    }

    private void updateMissiles() {
        // Parcourt les missiles et les met à jour
        for (EnemysMissile missile : missiles) {
            missile.update();
        }

        // Supprime les missiles désactivés de la liste et de la scène
        missiles.removeIf(missile -> {
            if (!missile.isActive()) {
                if (this.getParent() instanceof Pane) {
                    ((Pane) this.getParent()).getChildren().remove(missile);
                }
                return true; // Retirer le missile de la liste
            }
            return false;
        });
    }


    public List<EnemysMissile> getMissiles() {
        return missiles;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
