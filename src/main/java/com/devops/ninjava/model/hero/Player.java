package com.devops.ninjava.model.hero;

import com.devops.ninjava.model.decor.Ground;
import com.devops.ninjava.model.enemy.Enemy;
import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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
    private boolean isDead;
    private double velX;
    private double velY;
    private int jumpHeight; // Hauteur du saut
    private int jumpCounter;
    private int attackCounter;
    private int currentAttackIndex = -1; // Indique le coup en cours (-1 = pas d'attaque)
    private boolean isComboActive = false;
    private boolean isTeleporting = false;
    private Rectangle hitbox;
    private int attackDamage = 5;
    private int dashDamage = 10;
    private boolean isDashing = false; // Indique si le joueur est en train de dacher
    private int idleCounter = 0; // Compteur pour l'animation d'inactivité
    public boolean isInvincible = false; // Détermine si le joueur est invincible
    private long invincibilityStartTime;
    private long movementStartTime = 0; // Temps du début du mouvement
    private boolean isMoving = false;
    private int shurikens = 200;

    private Rectangle debugRectangle;

    private Image[] walkRightFrames; // Images pour l'animation droite
    private Image[] walkLeftFrames;  // Images pour l'animation gauche
    private Image[] jumpRightFrames;
    private Image[] jumpLeftFrames;
    private Image[][] attackFrames;
    private ImageView playerView;
    private Image[] projectileLaunchFrames;
    private Image[] fireBallLaunchFrames;
    private Image[] deathFrames;
    private Image[] idleFrames; // Frames pour l'animation d'inactivité
    private Image[] recoveryFrames;
    private Image[] teleportFrames;
    private Image[] dashFrames;
    private Image[][] dashAttackFrames;


     // Temps de début de l'invincibilité
    private static final int INVINCIBILITY_DURATION = 3000;
    private static final int ATTACK_FRAME_DELAY = 2;
    private static final int IDLE_FRAME_DELAY = 30;
    private static final double DASH_SPEED_MULTIPLIER = 2; // Multiplicateur de vitesse pendant le dash
    private static final int DASH_DURATION = 500;
    private static final long DASH_TRIGGER_DELAY = 1500;
     // Indique si le joueur est en déplacement continu

    private int energy = 100; // Énergie actuelle
    private static final int MAX_ENERGY = 100; // Maximum d'énergie
    private static final int ENERGY_REGEN_RATE = 2; // Régénération par intervalle
    public static final int ENERGY_COST_FIREBALL = 5; // Coût pour lancer une boule de feu
    public static final int ENERGY_COST_TELEPORT = 20; // Coût pour se téléporter

    private long lastEnergyRegenTime = System.currentTimeMillis();


    public Player(double x, double y) {
        this.remainingLives = 10;
        this.coins = 0;
        this.points = 0;
        this.isJumping = false;
        this.isFalling = false;
        this.isAttacking = false;
        this.jumpHeight = 5;
        this.jumpCounter = 0;


        initializeImages();
        initializePlayerView(x, y);
        initializeHitbox();

    }


    private void initializeHitbox() {
        double debugWidth = playerView.getFitWidth();   // Utiliser la largeur de l'image
        double debugHeight = playerView.getFitHeight(); // Utiliser la hauteur de l'image

        hitbox = new Rectangle(debugWidth, debugHeight);
        hitbox.setFill(Color.TRANSPARENT); // Transparent à l'intérieur
        hitbox.setStroke(Color.RED);       // Contour rouge
        hitbox.setStrokeWidth(2);// Épaisseur du contour
        hitbox.setVisible(false);

        this.getChildren().add(hitbox);    // Ajouter le rectangle au joueur
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

            fireBallLaunchFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48,  14* 48, 48, 48)),
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 14 * 48, 48, 48)),
            };

            idleFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 1 * 48, 48, 48)), // Frame 1
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 1 * 48, 48, 48)), // Frame 2
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 1 * 48, 48, 48))  // Frame 3
            };

            recoveryFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 16 * 48, 48, 48)), // Frame 1
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 16 * 48, 48, 48)), // Frame 2
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 16 * 48, 48, 48)), // Frame 3
                    convertToFXImage(spriteSheet.getSubimage(3 * 48, 16 * 48, 48, 48)), // Frame 3
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

            dashAttackFrames = new Image[2][];
            for (int i = 0; i < 2; i++) {
                dashAttackFrames[i] = new Image[4];
                for (int j = 0; j < 4; j++) {
                    dashAttackFrames[i][j] = convertToFXImage(spriteSheet.getSubimage(j * 48, (20 + i) * 48, 48, 48));
                }
            }

            deathFrames = new Image[10];
            for (int i = 0; i < 10; i++) {
                deathFrames[i] = convertToFXImage(spriteSheet.getSubimage(i * 48, 17 * 48, 48, 48));
            }

            teleportFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 15 * 48, 48, 48)), // Frame 1
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 15 * 48, 48, 48)), // Frame 2
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 15 * 48, 48, 48)), // Frame 3
                    convertToFXImage(spriteSheet.getSubimage(3 * 48, 15 * 48, 48, 48)), // Frame 4
            };

            dashFrames = new Image[] {
                    convertToFXImage(spriteSheet.getSubimage(0 * 48, 18 * 48, 48, 48)), // Frame 1
                    convertToFXImage(spriteSheet.getSubimage(1 * 48, 18 * 48, 48, 48)), // Frame 2
                    convertToFXImage(spriteSheet.getSubimage(2 * 48, 18 * 48, 48, 48)), // Frame 3
            };

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

        double height =playerView.getFitHeight() -16;
        double width = playerView.getFitWidth() -16;
        this.getChildren().add(playerView); // Ajoute l'ImageView au Pane

        // Position initiale
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void update() {

        if (isDead())
        {
            playerView.setImage(deathFrames[9]);
            return;
        }

        if (isTeleporting) return;

        // Appliquer la gravité
        velY += 0.3; // Gravité qui augmente la vitesse verticale
        if (velY > 6) velY = 6;
        setLayoutY(getLayoutY() + velY);

        // Vérifier si le joueur a atteint le temps nécessaire pour le dash
        if (isMoving && !isDashing && System.currentTimeMillis() - movementStartTime >= DASH_TRIGGER_DELAY) {
            startDash(velX > 0); // Déclenche un dash dans la direction actuelle
        }

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
//        if (getLayoutY() >= floor) {
//            stopFalling(floor); // Arrêter la chute et repositionner le joueur au sol
//        }
        // Mise à jour de la position horizontale
        setLayoutX(getLayoutX() + velX);
        updateHitbox();



        if (!isAttacking && !isJumping && !isFalling && velX == 0) {
            animateIdle();
        } else {
            idleCounter = 0; // Réinitialiser le compteur si une autre action est en cours
        }

        if (isAttacking) {
            animateAttack();
        }

        if (isDashing && isAttacking){
            animateDashAttack();
        }

        if (isInvincible && System.currentTimeMillis() - invincibilityStartTime > INVINCIBILITY_DURATION) {
            isInvincible = false; // Désactiver l'invincibilité
            System.out.println("Invincibility ended.");
        }


    }

    private void updateHitbox() {
        hitbox.setLayoutX(0); // Synchroniser avec le joueur
        hitbox.setLayoutY(0);
    }

    public void moveRight() {
        if (isDashing) return;

        // Si le joueur commence un nouveau déplacement
        if (!isMoving) {
            isMoving = true;
            movementStartTime = System.currentTimeMillis();
        }

        velX = 3;
        animateMovement(true);
    }

    public void moveLeft() {
        if (isDashing) return;

        // Si le joueur commence un nouveau déplacement
        if (!isMoving) {
            isMoving = true;
            movementStartTime = System.currentTimeMillis();
        }

        velX = -3;
        animateMovement(false);
    }

    public void stopMoving() {
        isMoving = false;
        movementStartTime = 0; // Réinitialise le temps de début du mouvement
        velX = 0;
    }

    private void animateMovement(boolean toRight) {

        if (isAttacking) return;

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

    private void startDash(boolean toRight) {
        if (isDashing) return;

        isDashing = true;
        velX = (toRight ? 1 : -1) * 3 * DASH_SPEED_MULTIPLIER; // Augmente la vitesse dans la direction

        // Lancer l'animation du dash
        AnimationTimer dashTimer = new AnimationTimer() {
            private long startTime = System.currentTimeMillis();
            private int frameIndex = 0;

            @Override
            public void handle(long now) {
                if (!isDashing || System.currentTimeMillis() - startTime > DASH_DURATION) {
                    stop(); // Arrêter le dash
                    isDashing = false;
                    stop(); // Arrêter le mouvement horizontal
                    this.stop();
                } else {
                    if (!isAttacking) { // Ne change pas d'image si une attaque est active
                        if (frameIndex < dashFrames.length) {
                            playerView.setImage(dashFrames[frameIndex]);
                            frameIndex = (frameIndex + 1) % dashFrames.length;
                        }
                    }
                }
            }
        };

        dashTimer.start();
    }

    public void attack() {
        if (isDashing) {
            // Déclencher l'animation de dash attack
            isAttacking = true;
            animateDashAttack();

            isDashing = false;
            velX /= DASH_SPEED_MULTIPLIER;
        } else {
            // Déclencher l'animation d'attaque normale
            if (!isAttacking || isComboActive) {
                if (currentAttackIndex < attackFrames.length - 1) {
                    isComboActive = true;
                    currentAttackIndex++;
                    attackCounter = 0;
                    isAttacking = true;
                    animateAttack();
                }
            }
        }
    }

    private void animateDashAttack() {
        int frameDuration = 5; // Durée de chaque frame
        attackCounter = 0; // Réinitialise le compteur pour l'animation

        AnimationTimer dashAttackAnimation = new AnimationTimer() {
            private int frameIndex = 0;  // Frame actuelle dans une série
            private int seriesIndex = 0; // Série actuelle (ligne dans le sprite sheet)
            private long lastFrameTime = System.currentTimeMillis();

            @Override
            public void handle(long now) {
                if (seriesIndex < dashAttackFrames.length) {
                    // Contrôle de la fréquence de changement de frames
                    if (System.currentTimeMillis() - lastFrameTime > frameDuration * 10) {
                        playerView.setImage(dashAttackFrames[seriesIndex][frameIndex]);
                        frameIndex = (frameIndex + 1) % dashAttackFrames[seriesIndex].length;
                        lastFrameTime = System.currentTimeMillis();

                        // Si la série est terminée, passe à la suivante
                        if (frameIndex == 0) {
                            seriesIndex++;
                        }

                        // Maintenir l'avancement pendant l'attaque
                        setLayoutX(getLayoutX() + velX / DASH_SPEED_MULTIPLIER);
                    }
                } else {
                    stop(); // Terminer l'animation après les séries
                    isAttacking = false; // Désactiver l'état d'attaque
                    isComboActive = false; // Réinitialiser le combo
                    currentAttackIndex = -1; // Réinitialiser l'indice d'attaque
                }
            }
        };

        dashAttackAnimation.start();
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

    private void animateRecovery() {
        AnimationTimer recoveryAnimation = new AnimationTimer() {
            private int frameIndex = 0; // Index de la frame actuelle
            private long lastFrameTime = 0;

            @Override
            public void handle(long now) {
                // Contrôler la fréquence de changement de frame (toutes les 150ms par exemple)
                if (now - lastFrameTime > 150_000_000) {
                    playerView.setImage(recoveryFrames[frameIndex]); // Mettre à jour l'image
                    frameIndex = (frameIndex + 1) % recoveryFrames.length; // Passer à la frame suivante
                    lastFrameTime = now; // Mise à jour du temps
                }
            }
        };

        // Lancer l'animation de récupération
        recoveryAnimation.start();

        // Arrêter l'animation après la durée d'invincibilité
        new Thread(() -> {
            try {
                Thread.sleep(INVINCIBILITY_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recoveryAnimation.stop(); // Arrêter l'animation
            setVisible(true); // S'assurer que le joueur est visible
        }).start();
    }

    private void startInvincibility() {
        isInvincible = true; // Rendre le joueur invincible
        invincibilityStartTime = System.currentTimeMillis(); // Stocker le temps de début

        // Déclenche l'animation de récupération
        animateRecovery();

        // Désactiver l'invincibilité après la durée définie
        new Thread(() -> {
            try {
                Thread.sleep(INVINCIBILITY_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isInvincible = false; // Désactiver l'invincibilité
        }).start();
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

    public void animateJutsuLaunch() {
        if (isAttacking || !useEnergy(ENERGY_COST_FIREBALL)) return; // Empêcher d'autres actions pendant l'animation

        isAttacking = true; // Activer l'état d'attaque
        attackCounter = 0; // Réinitialiser le compteur pour l'animation
        int frameDuration = 5; // Durée de chaque frame (ajustez si nécessaire)

        AnimationTimer fireballLaunchAnimation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                int frameIndex = attackCounter / frameDuration;

                if (frameIndex < fireBallLaunchFrames.length) {
                    playerView.setImage(fireBallLaunchFrames[frameIndex]);
                    attackCounter++;
                } else {
                    stop(); // Arrêter l'animation
                    isAttacking = false;
                }
            }
        };

        fireballLaunchAnimation.start();
    }

    public void startTeleportation() {
        if (isTeleporting || !useEnergy(ENERGY_COST_TELEPORT)) return; // Ne pas lancer une autre téléportation si déjà en cours

        isTeleporting = true; // Activer l'état de téléportation
        isInvincible = true; // Rendre le joueur invincible
        invincibilityStartTime = System.currentTimeMillis(); // Stocker le temps de début

        // Déclenche l'animation de récupération
        animateTeleportation();
        // Désactiver l'invincibilité après la durée définie
        new Thread(() -> {
            try {
                Thread.sleep(INVINCIBILITY_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isTeleporting = false;
            isInvincible = false; // Désactiver l'invincibilité
        }).start();
    }

    private void animateTeleportation() {
        AnimationTimer recoveryAnimation = new AnimationTimer() {
            private int frameIndex = 0; // Index de la frame actuelle
            private long lastFrameTime = 0;

            @Override
            public void handle(long now) {
                // Contrôler la fréquence de changement de frame (toutes les 150ms par exemple)
                if (now - lastFrameTime > 250_000_000) {
                    playerView.setImage(teleportFrames[frameIndex]); // Mettre à jour l'image
                    frameIndex = (frameIndex + 1) % teleportFrames.length; // Passer à la frame suivante
                    lastFrameTime = now; // Mise à jour du temps
                }
            }
        };

        // Lancer l'animation de récupération
        recoveryAnimation.start();

        // Arrêter l'animation après la durée d'invincibilité
        new Thread(() -> {
            try {
                Thread.sleep(INVINCIBILITY_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recoveryAnimation.stop(); // Arrêter l'animation
            setVisible(true); // S'assurer que le joueur est visible
        }).start();
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

    public Rectangle getExtendedHurtbox() {
        double xOffset = isAttacking ? (velX >= 0 ? 30 : -30) : 0; // Étendre devant selon la direction
        double width = playerView.getFitWidth() + (isAttacking ? 20 : 0); // Ajouter à la largeur si attaque
        double height = playerView.getFitHeight();

        return new Rectangle(getLayoutX() + xOffset, getLayoutY(), width, height);
    }

    public void debugHurtbox(GraphicsContext gc) {
        Rectangle hurtbox = getExtendedHurtbox();
        gc.setStroke(Color.RED);
        gc.strokeRect(hurtbox.getX(), hurtbox.getY(), hurtbox.getWidth(), hurtbox.getHeight());
    }


    public void stop() {
        velX = 0; // Arrêter le mouvement horizontal
    }

    // Gestion des interactions avec des ennemis ou des objets
    public void onTouchEnemy() {
        if (!isInvincible) {
            // Réduire le nombre de vies
            if (remainingLives > 1) {
                remainingLives--;
                System.out.println("Ouch! Lost a life. Remaining lives: " + remainingLives);
            } else {
                System.out.println("Game Over!");
                die(); // Déclencher l'animation de mort
                return;
            }

            // Activer l'état d'invincibilité et lancer l'animation de récupération
            startInvincibility();

            // Faire reculer le joueur pour simuler l'impact
            if (velX > 0) { // Si le joueur allait à droite
                setX(getX() - 30); // Reculer vers la gauche
            } else { // Si le joueur allait à gauche
                setX(getX() + 30); // Reculer vers la droite
            }

            // Optionnel : Annuler le mouvement pendant un court instant après l'impact
            stop();
        }
    }

    public void die() {
        System.out.println("Player has died!");
        isDead = true;
        playDeathAnimation();
    }

    public void handleCollision(Pane object) {
        if (this.getBoundsInParent().intersects(object.getBoundsInParent())) {
            if (object instanceof Ground) {
                handleBrickCollision((Ground) object);
            }
            if (object instanceof Enemy) {
                handleEnemyCollision((Enemy) object);
            }
            // Vous pouvez ajouter d'autres types d'objets ici
        }
    }

    public void handleEnemyCollision(Enemy enemy) {
        Rectangle extendedHurtbox = getExtendedHurtbox();

        // Vérifier si l'ennemi intersecte avec la hurtbox étendue
        if (extendedHurtbox.intersects(enemy.getBoundsInParent())) {
            if (isAttacking) {
                // Si le joueur attaque, tuer l'ennemi
                enemy.takeDamage(attackDamage);
                acquirePoints(10); // Ajouter des points
                enemy.setX(enemy.getX() - 50);
                System.out.println("Enemy touched by attack!");
            }
            if (isDashing && isAttacking) {
                // Si le joueur attaque, tuer l'ennemi
                enemy.takeDamage(dashDamage);
                acquirePoints(20); // Ajouter des points
                enemy.setX(enemy.getX() - 100);
                System.out.println("Enemy touched by Dash attack!");
            }
        } else if (!isInvincible && this.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
            double playerBottom = this.getLayoutY() + this.playerView.getFitHeight();
            double playerTop = this.getLayoutY();
            double playerRight = this.getLayoutX() + this.playerView.getFitWidth();
            double playerLeft = this.getLayoutX();

            double enemyBottom = enemy.getLayoutY() + enemy.getHeight();
            double enemyTop = enemy.getLayoutY();
            double enemyRight = enemy.getLayoutX() + enemy.getWidth();
            double enemyLeft = enemy.getLayoutX();


            // Sinon, le joueur subit des dégâts
                if (playerBottom > enemyTop && playerTop < enemyTop &&
                        playerRight > enemyLeft && playerLeft < enemyRight &&
                        velY > 0) {
                    enemy.takeDamage(50);
                    this.setVelY(-8); // Rebond vers le haut
                    System.out.println("Enemy touched!" + isAttacking);
                }

                else {
                    this.onTouchEnemy(); // Réduit les vies
                    if (this.getRemainingLives() == 0) {
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

    private void handleBrickCollision(Ground ground) {
        double playerBottom = this.getLayoutY() + this.playerView.getFitHeight();
        double playerTop = this.getLayoutY();
        double playerRight = this.getLayoutX() + this.playerView.getFitWidth();
        double playerLeft = this.getLayoutX();

        double brickBottom = ground.getLayoutY() + ground.getHeight();
        double brickTop = ground.getLayoutY();
        double brickRight = ground.getLayoutX() + ground.getWidth();
        double brickLeft = ground.getLayoutX();

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
            } else if (velX>0)
            {
                animateMovement(true);
            }
            else if (velX<0)
            {
                animateMovement(false);
            }
            else {
                idleCounter = 0; // Réinitialiser le compteur si une autre action est en cours
            }

            return; // Empêche les autres collisions de s'exécuter
        }

        // Priorité : Collision par-dessous la brique
        if (velY < 0 && playerTop < brickBottom && playerBottom > brickBottom &&
                playerRight > brickLeft && playerLeft < brickRight) {
            // Simuler un rebond et casser la brique
            this.setY(brickBottom);
            ground.breakBrick();
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
            ground.breakBrick(); // Casser la brique
            this.setVelY(2); // Simuler un rebond léger
        }
    }

    public void regenerateEnergy() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEnergyRegenTime >= 1000) { // Régénération toutes les secondes
            energy = Math.min(MAX_ENERGY, energy + ENERGY_REGEN_RATE);
            lastEnergyRegenTime = currentTime;
        }
    }


    public boolean isDead() {
        return isDead;
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

    public boolean isInvincible() {
        return isInvincible;
    }

    public void setInvincible(boolean invincible) {
        isInvincible = invincible;
    }

    public Rectangle getHitbox() {
        return hitbox; // Retourne la hitbox définie précédemment
    }

    public boolean useShuriken() {
        if (shurikens > 0) {
            shurikens--;
            return true;
        }
        return false; // Pas de shurikens restants
    }

    public int getShurikens() {
        return shurikens;
    }

    public boolean useEnergy(int cost) {
        if (energy >= cost) {
            energy -= cost;
            return true;
        }
        return false; // Pas assez d'énergie
    }

    public int getEnergy() {
        return energy;
    }


}
