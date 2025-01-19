package com.devops.ninjava.model.projectile;

import com.devops.ninjava.model.environnement.Ground;
import com.devops.ninjava.model.environnement.Wall;
import com.devops.ninjava.model.hero.Player;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public class EnemysMissile extends Projectile {


    public EnemysMissile(double x, double y, boolean toRight, Image initialImage) {
        super(x, y, toRight, initialImage);

        setLayoutX(x);
        setLayoutY(y);
    }

    @Override
    public void update() {
        if (!isActive) return;

        // Déplacement horizontal
        setLayoutX(getLayoutX() + velX);

        if (getParent() != null) {
            getParent().getChildrenUnmodifiable().forEach(node -> {
                if (node instanceof Ground || node instanceof Wall) {
                    if (this.getBoundsInParent().intersects(node.getBoundsInParent())) {
                        handleCollision((Pane) node);
                    }
                }
            });
        }

        // Vérifiez les collisions avec le joueur
        if (getParent() != null) {
            getParent().getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof Player)
                    .map(node -> (Player) node)
                    .forEach(player -> {
                        if (this.getBoundsInParent().intersects(player.getBoundsInParent()) && !player.isInvincible()) {
                            player.onTouchEnemy(); // Infliger des dégâts au joueur
                            deactivate(); // Désactiver le projectile
                        }
                    });
        }
    }
}
