package com.devops.ninjava.model.hero;

import com.devops.ninjava.model.decor.Brick;
import com.devops.ninjava.model.decor.Pipe;
import com.devops.ninjava.model.enemy.Enemy;
import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Pane {

    private int remainingLives;
    private int coins;
    private int points;
    private boolean isJumping;
    private boolean isFalling;
    private boolean isAttacking;
    private double velX;
    private double velY;
    private int floor; // Hauteur minimale atteignable par le joueur
    private int jumpHeight; // Hauteur du saut
    private int jumpCounter;
    private int attackCounter;
    private int currentAttackIndex = -1; // Indique le coup en cours (-1 = pas d'attaque)
    private boolean isComboActive = false;

    private Image[] walkRightFrames; // Images pour l'animation droite
    private Image[] walkLeftFrames;  // Images pour l'animation gauche
    private Image[] jumpRightFrames;
    private Image[] jumpLeftFrames;
    private Image[][] attackFrames;
    private ImageView playerView;
    private Image[] projectileLaunchFrames;
    private Image[] deathFrames;
    private Image[] idleFrames; // Frames pour l'animation d'inactivité
    private int idleCounter = 0; // Compteur pour l'animation d'inactivité
    private static final int IDLE_FRAME_DELAY = 30;

    private static final int ATTACK_FRAME_DELAY = 2;

    public Player(double x, double y) {
        this.remainingLives = 3;
        this.coins = 0;
        this.points = 0;
        this.floor = (int) (550 - 85);
        this.isJumping = false;
        this.isFalling = false;
        this.isAttacking = false;
        this.jumpHeight = 5;
        this.jumpCounter = 0;


        initializeImages();
        initializePlayerView(x, y);
    }

    private void initializeImages() {
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/images/player/Ultimate_Ninja_Spritesheet.png"));

            // Découper les frames pour les différentes animations
            walkRightFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 2 * 48, 48, 48)), // Frame (col=1, row=1)
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 2 * 48, 48, 48)), // Frame (col=2, row=1)
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 2 * 48, 48, 48)), // Frame (col=3, row=1)
            };

            walkLeftFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 2 * 48, 48, 48)), // Frame (col=1, row=1)
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 2 * 48, 48, 48)), // Frame (col=2, row=1)
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 2 * 48, 48, 48)), // Frame (col=3, row=1)
            };

            jumpRightFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 6 * 48, 48, 48)), // Frame (col=1, row=1)
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 6 * 48, 48, 48)), // Frame (col=2, row=1)
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 6 * 48, 48, 48)), // Frame (col=3, row=1)
                    convertToFXImage(spriteSheet.getSubimage(3 * 48, 6 * 48, 48, 48)), // Frame (col=1, row=1)
                    convertToFXImage(spriteSheet.getSubimage(4 * 48, 6 * 48, 48, 48)), // Frame (col=1, row=1)
            };

            jumpLeftFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 5 * 48, 48, 48)), // Frame (col=1, row=1)
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 5 * 48, 48, 48)), // Frame (col=2, row=1)
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 5 * 48, 48, 48)), // Frame (col=3, row=1)
                    convertToFXImage(spriteSheet.getSubimage(3 * 48, 5 * 48, 48, 48)), // Frame (col=1, row=1)
            };

            projectileLaunchFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 12 * 48, 48, 48)),
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 12 * 48, 48, 48)),
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 12 * 48, 48, 48)),
                    convertToFXImage(spriteSheet.getSubimage(3 * 48, 12 * 48, 48, 48)),
                    convertToFXImage(spriteSheet.getSubimage(4 * 48, 12 * 48, 48, 48))
            };

            idleFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 1 * 48, 48, 48)), // Frame 1
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 1 * 48, 48, 48)), // Frame 2
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 1 * 48, 48, 48))  // Frame 3
            };

            attackFrames = new Image[4][];

            for (int i = 0; i < 2; i++) {
                attackFrames[i] = new Image[5];
                for (int j = 0; j < 5; j++) {
                    attackFrames[i][j] = convertToFXImage(spriteSheet.getSubimage(j * 48, (7 + i) * 48, 48, 48));
                }
            }

            attackFrames[2] = new Image[4];
            for (int j = 0; j < 4; j++) {
                attackFrames[2][j] = convertToFXImage(spriteSheet.getSubimage(j * 48, (7 + 2) * 48, 48, 48));
            }

            attackFrames[3] = new Image[4];
            for (int j = 0; j < 4; j++) {
                attackFrames[3][j] = convertToFXImage(spriteSheet.getSubimage(j * 48, (7 + 3) * 48, 48, 48));
            }

            deathFrames = new Image[10];
            for (int i = 0; i < 10; i++) {
                deathFrames[i] = convertToFXImage(spriteSheet.getSubimage(i * 48, 17 * 48, 48, 48));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour découper une frame
    private BufferedImage getSubImage(BufferedImage image, int col, int row, int width, int height) {
        return image.getSubimage((col - 1) * width, (row - 1) * height, width, height);
    }

    private Image convertToFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    private void initializePlayerView(double x, double y) {
        playerView = new ImageView(walkRightFrames[0]); // Image par défaut
        playerView.setFitWidth(96);
        playerView.setFitHeight(96);
        this.getChildren().add(playerView); // Ajoute l'ImageView au Pane

        // Position initiale
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void moveRight() {
        velX = 3;
        animateMovement(true);
    }

    public void moveLeft() {
        velX = -3;
        animateMovement(false);
    }

    private void animateMovement(boolean toRight) {
        Image[] frames = toRight ? walkRightFrames : walkLeftFrames;
        int frameIndex = (int) (Math.abs(getLayoutX()) / 10) % frames.length; // Change d'image toutes les 10 unités
        playerView.setImage(frames[frameIndex]);

        // Mise à jour de la position
        setLayoutX(getLayoutX() + velX);
    }

    public void jump() {
        if (!isJumping && !isFalling) {
            isJumping = true;
            velY = -10; // Vitesse initiale pour le saut
        } else {
            System.out.println("Already jumping or falling");
        }
    }

    public void stopFalling(double groundY) {
        isFalling = false;
        isJumping = false;
        velY = 0;
        setLayoutY(groundY); // Positionner sur le sol

        if (velX > 0) {
            animateMovement(true); // Animation vers la droite
        } else if (velX < 0) {
            animateMovement(false); // Animation vers la gauche
        } else {
            playerView.setImage(walkRightFrames[0]); // Image par défaut
        }

    }

    private void animateAttack() {
        if (!isAttacking) return;

        if (currentAttackIndex >= 0 && currentAttackIndex < attackFrames.length) {
            // Vérifier le délai entre les frames
            int frameIndex = (attackCounter / ATTACK_FRAME_DELAY) % attackFrames[currentAttackIndex].length;

            // Mettre à jour l'image pour le coup en cours
            playerView.setImage(attackFrames[currentAttackIndex][frameIndex]);
            attackCounter++;

            // Si toutes les frames de ce coup ont été affichées
            if (attackCounter / ATTACK_FRAME_DELAY >= attackFrames[currentAttackIndex].length) {
                attackCounter = 0;

                // Si un coup suivant est prévu
                if (isComboActive && currentAttackIndex < attackFrames.length - 1) {
                    currentAttackIndex++;
                } else {
                    // Terminer l'attaque si aucun nouveau coup n'est demandé
                    isAttacking = false;
                    isComboActive = false;
                    currentAttackIndex = -1;
                    playerView.setImage(walkRightFrames[0]); // Réinitialiser à l'image par défaut
                }
            }
        } else {
            // Si aucune attaque valide n'est active, terminer
            isAttacking = false;
            isComboActive = false;
            currentAttackIndex = -1;
            playerView.setImage(walkRightFrames[0]); // Réinitialiser à l'image par défaut
        }
    }

    public void animateProjectileLaunch() {
        if (!isAttacking) {
            isAttacking = true; // Empêcher d'autres actions pendant l'animation
            int frameDuration = 5; // Durée de chaque frame
            attackCounter = 0; // Réinitialise le compteur pour l'animation

            AnimationTimer launchAnimation = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    int frameIndex = attackCounter / frameDuration;

                    if (frameIndex < projectileLaunchFrames.length) {
                        playerView.setImage(projectileLaunchFrames[frameIndex]);
                        attackCounter++;
                    } else {
                        stop();
                        isAttacking = false; // Fin de l'animation
                    }
                }
            };

            launchAnimation.start();
        }
    }

    private void playDeathAnimation() {
        isAttacking = false; // Stoppe les autres actions
        int frameDuration = 5; // Durée de chaque frame

        AnimationTimer deathAnimation = new AnimationTimer() {
            private int frameCounter = 0;

            @Override
            public void handle(long now) {
                int frameIndex = frameCounter / frameDuration;

                if (frameIndex < deathFrames.length) {
                    playerView.setImage(deathFrames[frameIndex]);
                    frameCounter++;
                } else {
                    stop(); // Arrêter l'animation après la dernière frame
                    setVisible(false); // Masquer le joueur après la mort
                }
            }
        };

        deathAnimation.start();
    }

    private void animateIdle() {
        if (idleCounter % IDLE_FRAME_DELAY == 0) {
            int frameIndex = (idleCounter / IDLE_FRAME_DELAY) % idleFrames.length;
            playerView.setImage(idleFrames[frameIndex]);
        }
        idleCounter++;
    }


    public void update() {

        // Appliquer la gravité
        velY += 0.3; // Gravité qui augmente la vitesse verticale
        setLayoutY(getLayoutY() + velY);

        // Gestion des sauts
        if (isJumping) {
            animateJump(velX >= 0);
            if (velY >= 0) { // Lorsque la vitesse devient positive, le joueur commence à tomber
                isJumping = false;
                isFalling = true;
            }
        }

        if (isFalling) {
            animateJump(velX >= 0);
        }

        // Vérification des limites avec le sol
        if (getLayoutY() >= floor) {
            stopFalling(floor); // Arrêter la chute et repositionner le joueur au sol
        }
        // Mise à jour de la position horizontale
        setLayoutX(getLayoutX() + velX);

        if (!isAttacking && !isJumping && !isFalling && velX == 0) {
            animateIdle();
        } else {
            idleCounter = 0; // Réinitialiser le compteur si une autre action est en cours
        }

        if (isAttacking) {
            animateAttack();
        }

    }

    public void attack() {
        if (!isAttacking || isComboActive) {
            if (currentAttackIndex < attackFrames.length - 1) {
                System.out.println("attack");
                isComboActive = true;
                currentAttackIndex++;
                attackCounter = 0;
                isAttacking = true; // Active l'attaque même si le joueur est immobile
            }
        }
    }

    private void animateJump(boolean toRight) {
//        Image[] frames = toRight ? jumpRightFrames : jumpLeftFrames;
//        playerView.setImage(frames[0]); // Pour simplifier, on prend la première image de saut

        Image[] frames = toRight ? jumpRightFrames : jumpLeftFrames;

        // Utilisez un index basé sur le temps ou le compteur pour parcourir les frames
        int frameIndex = (jumpCounter / 10) % frames.length;

        // Mettez à jour l'image affichée pour le joueur
        playerView.setImage(frames[frameIndex]);

        // Incrémentez le compteur pour les frames
        jumpCounter++;
    }


    public void stop() {
        velX = 0; // Arrêter le mouvement horizontal
    }

    // Gestion des interactions avec des ennemis ou des objets
    public void onTouchEnemy() {
        if (remainingLives > 0) {
            remainingLives--;
            System.out.println("Ouch! Lost a life. Remaining lives: " + remainingLives);
        } else {
            System.out.println("Game Over!");
        }
    }

    public void die() {
        System.out.println("Player has died!");
        playDeathAnimation();
    }

    public void handleCollision(Pane object) {
        if (this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            if (object instanceof Brick) {
                handleBrickCollision((Brick) object);
            }
            if (object instanceof Enemy) {
                handleEnemyCollision((Enemy) object);
            }
            if (object instanceof Pipe) {
                handlePipeCollision((Pipe) object);
            }
            // Vous pouvez ajouter d'autres types d'objets ici
        }
    }

    public void handleEnemyCollision(Enemy enemy) {
        if (this.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
            double playerBottom = this.getLayoutY() + this.playerView.getFitHeight();
            double playerTop = this.getLayoutY();
            double playerRight = this.getLayoutX() + this.playerView.getFitWidth();
            double playerLeft = this.getLayoutX();

            double enemyBottom = enemy.getLayoutY() + enemy.getHeight();
            double enemyTop = enemy.getLayoutY();
            double enemyRight = enemy.getLayoutX() + enemy.getWidth();
            double enemyLeft = enemy.getLayoutX();


            if (isAttacking) {
                enemy.die();
                acquirePoints(100); // Ajouter des points pour avoir tué un ennemi
                System.out.println("Enemy killed!");
            }
            // Sinon, le joueur subit des dégâts
            else {
                if (playerBottom > enemyTop && playerTop < enemyTop &&
                        playerRight > enemyLeft && playerLeft < enemyRight &&
                        velY > 0) {
                    enemy.die();
                    this.setVelY(-8); // Rebond vers le haut
                    System.out.println("Enemy killed!" + isAttacking);
                }

                else {
                    this.onTouchEnemy(); // Réduit les vies
                    if (this.getRemainingLives() <= 0) {
                        this.die(); // Fin du jeu si plus de vies
                    } else {
                        // Déplacement du joueur pour simuler un impact
                        if (playerLeft < enemyLeft) {
                            this.setX(this.getX() - 50); // Pousser vers la gauche
                        } else {
                            this.setX(this.getX() + 50); // Pousser vers la droite
                        }
                    }
                }
            }
        }
    }

    private void handlePipeCollision(Pipe pipe) {
        double playerBottom = this.getLayoutY() + this.playerView.getFitHeight();
        double playerTop = this.getLayoutY();
        double playerRight = this.getLayoutX() + this.playerView.getFitWidth();
        double playerLeft = this.getLayoutX();

        double pipeBottom = pipe.getY() + pipe.getImage().getHeight();
        double pipeTop = pipe.getY();
        double pipeRight = pipe.getX() + pipe.getImage().getWidth();
        double pipeLeft = pipe.getX();

        // Collision par-dessus
        if (playerBottom > pipeTop && playerTop < pipeTop &&
                playerRight > pipeLeft && playerLeft < pipeRight) {
            this.setY(pipeTop - this.playerView.getFitHeight());
            this.setVelY(0); // Arrêter le mouvement vertical
            this.isJumping = false;
            this.isFalling = false;

            if (!isFalling)
            {
                playerView.setImage(walkRightFrames[0]);
            }
            if (!isAttacking && !isJumping && !isFalling && velX == 0) {
                animateIdle();
            } else {
                idleCounter = 0; // Réinitialiser le compteur si une autre action est en cours
            }

            return;

        }

        // Collision sur les côtés
        if (playerRight > pipeLeft && playerLeft < pipeLeft &&
                playerBottom > pipeTop && playerTop < pipeBottom) {
            this.setX(pipeLeft - this.playerView.getFitWidth());
            this.setVelX(0); // Arrêter le mouvement horizontal
            return;
        }

        if (playerLeft < pipeRight && playerRight > pipeRight &&
                playerBottom > pipeTop && playerTop < pipeBottom) {
            this.setX(pipeRight);
            this.setVelX(0); // Arrêter le mouvement horizontal
            return;
        }
    }

    private void handleBrickCollision(Brick brick) {
        double playerBottom = this.getLayoutY() + this.playerView.getFitHeight();
        double playerTop = this.getLayoutY();
        double playerRight = this.getLayoutX() + this.playerView.getFitWidth();
        double playerLeft = this.getLayoutX();

        double brickBottom = brick.getLayoutY() + brick.getHeight();
        double brickTop = brick.getLayoutY();
        double brickRight = brick.getLayoutX() + brick.getWidth();
        double brickLeft = brick.getLayoutX();

        // Priorité : Collision par-dessus la brique
        if (velY > 0 && playerBottom > brickTop && playerBottom <= brickTop + 10 &&
                playerRight > brickLeft && playerLeft < brickRight) {
            // Positionner le joueur sur la brique
            this.setY(brickTop - this.playerView.getFitHeight());
            this.setVelY(0); // Arrêter le mouvement vertical
            this.isJumping = false;
            this.isFalling = false;

            if (!isAttacking && !isJumping && !isFalling && velX == 0) {
                animateIdle();
            } else {
                idleCounter = 0; // Réinitialiser le compteur si une autre action est en cours
            }

            return; // Empêche les autres collisions de s'exécuter
        }

        // Priorité : Collision par-dessous la brique
        if (velY < 0 && playerTop < brickBottom && playerBottom > brickBottom &&
                playerRight > brickLeft && playerLeft < brickRight) {
            // Simuler un rebond et casser la brique
            this.setY(brickBottom);
            brick.breakBrick();
            this.setVelY(2); // Simuler un rebond léger
            return; // Empêche les autres collisions de s'exécuter
        }

        // Priorité : Collision côté gauche
        if (playerRight > brickLeft && playerLeft < brickLeft &&
                playerBottom > brickTop && playerTop < brickBottom) {
            // Bloquer le joueur à gauche
            this.setX(brickLeft - this.playerView.getFitWidth());
            this.setVelX(0); // Arrêter le mouvement horizontal
            return; // Empêche les autres collisions de s'exécuter
        }

        // Priorité : Collision côté droit
        if (playerLeft < brickRight && playerRight > brickRight &&
                playerBottom > brickTop && playerTop < brickBottom) {
            // Bloquer le joueur à droite
            this.setX(brickRight);
            this.setVelX(0); // Arrêter le mouvement horizontal
            return; // Empêche les autres collisions de s'exécuter
        }

        // Collision par-dessous la brique
        if (playerTop < brickBottom && playerBottom > brickBottom && playerRight > brickLeft && playerLeft < brickRight) {
            brick.breakBrick(); // Casser la brique
            this.setVelY(2); // Simuler un rebond léger
        }
    }



    public boolean isDead() {
        return remainingLives <= 0;
    }


    public void acquireCoin() {
        coins++;
    }

    public void acquirePoints(int points) {
        this.points += points;
    }

    // Getters pour les propriétés spécifiques
    public int getRemainingLives() {
        return remainingLives;
    }

    public int getCoins() {
        return coins;
    }

    public int getPoints() {
        return points;
    }

    public double getX() {
        return getLayoutX();
    }

    public void setX(double x) {
        setLayoutX(x);
    }


    public double getY() {
        return getLayoutY();
    }

    public void setY(double y) {
        setLayoutY(y);
    }


    // Getter et Setter pour velX
    public double getVelX() {
        return velX;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    // Getter et Setter pour velY
    public double getVelY() {
        return velY;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void setAttacking(boolean attacking) {
        isAttacking = attacking;
    }
}
