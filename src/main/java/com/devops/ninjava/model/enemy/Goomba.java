package com.devops.ninjava.model.enemy;


public class Goomba extends Enemy {

    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;
    private static final double DEFAULT_SPEED = 3.0;

    public Goomba(double x, double y) {
        super(x, y, WIDTH, HEIGHT);
        setVelX(DEFAULT_SPEED); // Le Goomba se déplace vers la droite par défaut
    }

    @Override
    public void update() {
        // Met à jour la position horizontale
        x += velX;

        // Logique simple : inverser la direction si nécessaire (par exemple, collision avec un mur)
        if (x < 0 || x > 800) { // Exemple de limites de la scène
            velX = -velX; // Inverser la direction
        }
    }
}
