package com.devops.ninjava.model;

public class Camera {

    private double offsetX;
    private double offsetY;
    private final int worldWidth;
    private final int worldHeight;
    private final int viewportWidth;
    private final int viewportHeight;

    public Camera(int viewportWidth, int viewportHeight, int worldWidth, int worldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void update(double targetX, double targetY) {
        // Calculer l'offset pour centrer la caméra sur la cible
        offsetX = targetX - viewportWidth / 2.0;
        offsetY = targetY - viewportHeight / 2.0;

        // Appliquer des limites pour rester dans le monde
        offsetX = Math.max(0, Math.min(offsetX, worldWidth - viewportWidth));
        offsetY = Math.max(0, Math.min(offsetY, worldHeight - viewportHeight));
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }
}
