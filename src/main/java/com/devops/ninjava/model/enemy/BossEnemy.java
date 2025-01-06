package com.devops.ninjava.model.enemy;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BossEnemy extends Enemy {

    private Image[] flyingFrames; // Frames pour l'animation de déplacement
    private Image[] attackFrames; // Frames pour l'animation d'attaque
    private Image[] hurtFrames;   // Frames pour l'animation lorsqu'il est touché
    private Image[] dieFrames;    // Frames pour l'animation de mort

    private int frameCounter = 0;
    private int attackFrameCounter = 0;
    private int hurtFrameCounter = 0;
    private int dieFrameCounter = 0;

    private static final int ANIMATION_DELAY = 10;      // Délai entre les frames de déplacement
    private static final int ATTACK_INTERVAL = 3000;    // Temps entre deux attaques
    private static final int ATTACK_FRAME_DELAY = 8;    // Délai entre les frames d'attaque
    private static final int HURT_FRAME_DELAY = 12;     // Délai entre les frames d'animation "touché"
    private static final int DIE_FRAME_DELAY = 15;      // Délai entre les frames d'animation "mort"
    private static final double DEFAULT_SPEED = 1.5;    // Vitesse du Boss
    private static final double MAX_DISTANCE = 150.0;   // Distance maximale à parcourir

    private final double initialX; // Position de départ
    private long lastAttackTime = 0; // Temps de la dernière attaque
    private boolean isAttacking = false; // Indique si le boss est en train d'attaquer
    private boolean isHurt = false;     // Indique si le boss est en état "touché"
    private boolean isDying = false;    // Indique si le boss est en train de mourir

    public BossEnemy(double x, double y) {
        super(x, y, 96, 96); // Taille des frames du boss : 96x96
        this.velX = DEFAULT_SPEED; // Initialisation de la vitesse
        this.initialX = x; // Stockage de la position initiale
        this.health = 5000;      // Santé spécifique au boss
        this.maxHealth = 5000;
        loadSprites(); // Chargement des sprites
    }

    private void loadSprites() {
        try {
            BufferedImage spriteSheetFlying = ImageIO.read(getClass().getResource("/images/enemyBoss/FLYING.png"));
            BufferedImage spriteSheetAttack = ImageIO.read(getClass().getResource("/images/enemyBoss/ATTACK.png"));
            BufferedImage spriteSheetHurt = ImageIO.read(getClass().getResource("/images/enemyBoss/HURT.png"));
            BufferedImage spriteSheetDie = ImageIO.read(getClass().getResource("/images/enemyBoss/DEATH.png"));

            int frameWidth = 162;  // Largeur d'une frame
            int frameHeight = 148; // Hauteur d'une frame

            flyingFrames = new Image[4];
            for (int i = 0; i < 4; i++) {
                BufferedImage frame = spriteSheetFlying.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                flyingFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            attackFrames = new Image[6];
            for (int i = 0; i < 6; i++) {
                BufferedImage frame = spriteSheetAttack.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                attackFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            hurtFrames = new Image[3];
            for (int i = 0; i < 3; i++) {
                BufferedImage frame = spriteSheetHurt.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                hurtFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            dieFrames = new Image[10]; // Supposons qu'il y a 5 frames pour l'animation de mort
            for (int i = 0; i < 10; i++) {
                BufferedImage frame = spriteSheetDie.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                dieFrames[i] = SwingFXUtils.toFXImage(frame, null);
            }

            setImageViewFrame(flyingFrames[0]); // Image initiale
        } catch (IOException e) {
            System.err.println("Error loading BossEnemy sprites: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        if (isDead) return;

        if (isDying) {
            animateDie();
            return;
        }

        if (isHurt) {
            animateHurt();
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Gestion de l'attaque
        if (currentTime - lastAttackTime >= ATTACK_INTERVAL) {
            isAttacking = true;
            lastAttackTime = currentTime;
        }

        if (isAttacking) {
            animateAttack();
        } else {
            // Gestion du déplacement horizontal
            double currentX = getLayoutX();
            if (currentX <= initialX - MAX_DISTANCE || currentX >= initialX + MAX_DISTANCE) {
                velX = -velX; // Inverser la direction à chaque limite atteinte
            }
            setLayoutX(currentX + velX);

            applyGravity();

            // Animation de déplacement
            if (frameCounter % ANIMATION_DELAY == 0) {
                int frameIndex = (frameCounter / ANIMATION_DELAY) % flyingFrames.length;
                setImageViewFrame(flyingFrames[frameIndex]); // Mettre à jour l'image
            }
            frameCounter++;
        }
    }

    private void animateAttack() {
        if (attackFrameCounter < attackFrames.length * ATTACK_FRAME_DELAY) {
            int frameIndex = (attackFrameCounter / ATTACK_FRAME_DELAY) % attackFrames.length;
            setImageViewFrame(attackFrames[frameIndex]);
            attackFrameCounter++;
        } else {
            // Fin de l'animation d'attaque
            attackFrameCounter = 0;
            isAttacking = false;
        }
    }

    private void animateHurt() {
        if (hurtFrameCounter < hurtFrames.length * HURT_FRAME_DELAY) {
            int frameIndex = (hurtFrameCounter / HURT_FRAME_DELAY) % hurtFrames.length;
            setImageViewFrame(hurtFrames[frameIndex]);
            hurtFrameCounter++;
        } else {
            // Fin de l'animation "touché"
            hurtFrameCounter = 0;
            isHurt = false;

            if (health <= 0) {
                die(); // Appeler la méthode pour tuer le boss
            }
        }
    }

    private void animateDie() {
        if (dieFrameCounter < dieFrames.length * DIE_FRAME_DELAY) {
            int frameIndex = (dieFrameCounter / DIE_FRAME_DELAY) % dieFrames.length;
            setImageViewFrame(dieFrames[frameIndex]);
            dieFrameCounter++;
        } else {
            // Animation de mort terminée
            isDead = true;
            System.out.println("Boss defeated and animation completed!");
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (isDead || isDying) return; // Ignorer si déjà mort ou mourant

        super.takeDamage(damage); // Appelle la logique de la classe parent
        isHurt = true; // Déclenche l'état "touché"

        if (health <= 0 && !isDying) {
            isDying = true; // Passer en état "mourant"
            dieFrameCounter = 0; // Réinitialiser le compteur pour l'animation
        }
    }

    @Override
    public void die() {
        if (isDead || isDying) return; // Éviter d'appeler plusieurs fois
        isDying =true;
        dieFrameCounter = 0; // Initialiser le compteur pour l'animation
        System.out.println("Boss is dying...");
    }
}
