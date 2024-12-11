package com.devops.ninjava.model.enemy;


import com.devops.ninjava.model.GameObject;

public abstract class Enemy extends GameObject {

    public Enemy(double x, double y, int width, int height) {
        super(x, y, width, height);
        this.velX = 0;  // Par défaut, pas de mouvement
        this.velY = 0;
    }

    // Méthode abstraite pour que chaque ennemi puisse définir sa logique de mise à jour
    @Override
    public abstract void update();
}
