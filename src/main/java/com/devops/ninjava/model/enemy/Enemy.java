package com.devops.ninjava.model.enemy;

import com.devops.ninjava.model.decor.Ground;
import com.devops.ninjava.model.hero.Player;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public abstract class Enemy extends Pane {

    protected double x;
    protected double y;
    protected double velX;  // Vitesse horizontale
    protected double velY;  // Vitesse verticale
    protected boolean isDead = false;
    protected boolean isFalling = true;
    protected boolean isHurt = false;
    protected boolean isDying = false;

    protected int health = 100; // Points de vie par défaut (modifiable dans les sous-classes)
    protected int maxHealth = 100; // Points de vie maximum
    protected int width;  // Largeur de l'ennemi
    protected int height; // Hauteur de l'ennemi
    protected ImageView enemyView; // Gestion de l'affichage des sprites
    private static final double GRAVITY = 0.3; // Gravité constante
    private int floor = 550 - 85;

    public Enemy(double x, double y, int width, int height) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        this.health = 100;
        this.maxHealth = 100;

        initializeEnemyView(x, y);
    }

    private void initializeEnemyView(double x, double y) {
        enemyView = new ImageView();
        enemyView.setFitWidth(width * 2);
        enemyView.setFitHeight(height * 2);
        this.getChildren().add(enemyView);

        // Position initiale
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void setImageViewFrame(javafx.scene.image.Image frame) {
        enemyView.setImage(frame); // Change l'image affichée
    }
    // Méthode abstraite pour que chaque ennemi définisse son propre comportement
    public abstract void update();

    public void applyGravity() {
        if (!isDead && isFalling) {
            // Appliquer la gravité
            velY += GRAVITY;
            setLayoutY(getLayoutY() + velY);

            // Vérifier si l'ennemi atteint le sol
            if (getParent() != null) {
                getParent().getChildrenUnmodifiable().stream()
                        .filter(node -> node instanceof Ground)
                        .map(node -> (Pane) node)
                        .forEach(this::handleCollisionWithDecor);
            }
        }
    }

    // Gestion des collisions avec un objet générique
    public void handleCollision(Pane object) {
        if (this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            if (object instanceof Player) {
                handleCollisionWithPlayer((Player) object);
            } else if (object instanceof Ground) {
                handleCollisionWithDecor(object);
            }
        }
    }

    // Gestion des collisions avec le joueur
    public void handleCollisionWithPlayer(Player player) {
        if (this.getBoundsInParent().intersects(player.getBoundsInParent())) {
            double enemyBottom = this.getLayoutY() + height;
            double playerTop = player.getLayoutY();

            // Si le joueur saute sur l'ennemi
            if (playerTop < enemyBottom && player.getVelY() > 0) {
                takeDamage(50);
                player.setVelY(-8); // Rebond du joueur
            } else if (!player.isInvincible()){
                player.onTouchEnemy(); // Le joueur perd une vie
            }
        }
    }

    // Gestion des collisions avec les décors (briques, tuyaux, etc.)
//    public void handleCollisionWithDecor(Pane decor) {
//        // Inverser la direction horizontale lors d'une collision
//        velX = -velX;
//    }

    public void handleCollisionWithDecor(Pane decor) {
        if (this.getBoundsInParent().intersects(decor.getBoundsInParent())) {
            double enemyBottom = this.getLayoutY() + this.enemyView.getFitHeight();
            double enemyTop = this.getLayoutY();
            double enemyRight = this.getLayoutX() + this.enemyView.getFitHeight();
            double enemyLeft = this.getLayoutX();

            double decorBottom = decor.getLayoutY() + decor.getBoundsInLocal().getHeight();
            double decorTop = decor.getLayoutY();
            double decorRight = decor.getLayoutX() + decor.getBoundsInLocal().getWidth();
            double decorLeft = decor.getLayoutX();

            // Collision par-dessus le décor
            if (velY > 0 && enemyBottom >= decorTop && enemyBottom <= decorTop + 10 &&
                    enemyRight > decorLeft && enemyLeft < decorRight) {
                // Positionner l'ennemi juste au-dessus du décor
                this.setLayoutY(decorTop - this.enemyView.getFitHeight());
                this.velY = 0; // Arrêter le mouvement vertical
                this.isFalling = false; // L'ennemi est stable
                return;
            }

            // Collision par-dessous le décor
            if (velY < 0 && enemyTop < decorBottom && enemyBottom > decorBottom &&
                    enemyRight > decorLeft && enemyLeft < decorRight) {
                // Rebondir légèrement vers le bas
                this.setLayoutY(decorBottom);
                this.velY = 2; // Simuler un rebond léger
                return;
            }

            // Collision côté gauche du décor
            if (enemyRight > decorLeft && enemyLeft < decorLeft &&
                    enemyBottom > decorTop && enemyTop < decorBottom) {
                // Bloquer à gauche
                this.setLayoutX(decorLeft - this.width);
                this.velX = -this.velX; // Inverser la direction
                return;
            }

            // Collision côté droit du décor
            if (enemyLeft < decorRight && enemyRight > decorRight &&
                    enemyBottom > decorTop && enemyTop < decorBottom) {
                // Bloquer à droite
                this.setLayoutX(decorRight);
                this.velX = -this.velX; // Inverser la direction
                return;
            }
        } else {
            // Si l'ennemi ne touche pas de décor, il tombe
            this.isFalling = true;
        }
    }

    public void takeDamage(int damage) {
        if (isDead) return; // Ignore les nouveaux dégâts si déjà en état "touché" ou mourant

        health -= damage; // Réduction de la vie en fonction des dégâts infligés
        System.out.println("Enemy hit! Remaining health: " + health);
        isHurt = true;

        if (health <= 0) {
            die();
        }
    }


    public abstract void die();

    public boolean isDead() {
        return isDead;
    }

    public double getVelX() {
        return velX;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public double getVelY() {
        return velY;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public void setDead(boolean dead) {
        isDead = dead;
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
