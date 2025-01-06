package com.devops.ninjava.model.projectile;

import com.devops.ninjava.model.decor.Ground;
import com.devops.ninjava.model.hero.Player;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class Bomb extends Projectile {

    private int lifespan = 300; // Durée de vie en frames
    private int collisionDelay = 30; // Délai avant de commencer à détecter les collisions (en frames)

    public Bomb(double x, double y, boolean toRight, Image bombImage) {
        super(x, y, toRight, bombImage);

        // Définir la vitesse initiale
        this.velX = toRight ? 4 : -4; // Direction horizontale
        this.velY = -8; // Vitesse initiale vers le haut pour simuler un lancement en arc
    }

    @Override
    public void update() {
        if (!isActive) return;

        // Réduire la durée de vie
        lifespan--;
        if (lifespan <= 0) {
            System.out.println("Bombe désactivée car sa durée de vie est écoulée.");
            deactivate();
            return;
        }

        // Mise à jour de la position
        setLayoutX(getLayoutX() + velX); // Déplacement horizontal
        setLayoutY(getLayoutY() + velY); // Déplacement vertical
        velY += 0.3; // Appliquer la gravité

        // Détecter les collisions uniquement après un délai
        if (collisionDelay > 0) {
            collisionDelay--;
        } else {
            checkCollision();
        }

        // Si la bombe dépasse une certaine hauteur, elle est désactivée
        if (getLayoutY() > 600) { // Exemple de hauteur limite
            System.out.println("Bombe désactivée car elle a dépassé la limite verticale.");
            deactivate();
        }
    }

    private void checkCollision() {
        if (getParent() == null) return;

        // Vérifier les collisions avec des objets
        getParent().getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof Ground) {
                if (this.getBoundsInParent().intersects(node.getBoundsInParent())) {
                    System.out.println("Collision détectée avec un Brick.");
                    deactivate(); // Désactiver la bombe si elle touche un mur
                }
            } else if (node instanceof Player) {
                Player player = (Player) node;
                if (this.getBoundsInParent().intersects(player.getBoundsInParent()) && !player.isInvincible()) {
                    System.out.println("Collision détectée avec un joueur.");
                    player.onTouchEnemy(); // Infliger des dégâts au joueur
                    deactivate(); // Désactiver la bombe
                }
            }
        });
    }

    @Override
    public boolean handleCollision(Pane object) {
        // Gérer les collisions avec un objet
        if (isActive && this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            deactivate(); // Désactiver la bombe
            return true;
        }
        return false;
    }

    public void deactivate() {
        this.isActive = false;
        this.setVisible(false); // Masquer visuellement la bombe
        System.out.println("Bombe désactivée.");
    }

    public boolean isActive() {
        return isActive;
    }
}
