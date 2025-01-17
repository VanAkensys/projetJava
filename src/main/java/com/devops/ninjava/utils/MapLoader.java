package com.devops.ninjava.utils;

import com.devops.ninjava.model.decor.GrassGround;
import com.devops.ninjava.model.decor.Ground;
import com.devops.ninjava.model.decor.InteriorGround;
import com.devops.ninjava.model.enemy.*;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MapLoader {

    private static final int TILE_SIZE = 32; // Taille d'une tuile

    public static void loadMapFromFile(String filePath, Pane gameContainer, List<Ground> grounds, List<Enemy> enemies) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            for (int col = 0; col < line.length(); col++) {
                char tileType = line.charAt(col);

                switch (tileType) {
                    case 'I': // Sol ou brique
                        InteriorGround interiorGround = new InteriorGround(col * TILE_SIZE, row * TILE_SIZE);
                        grounds.add(interiorGround);
                        gameContainer.getChildren().add(interiorGround);
                        break;
                    case '2': // Sol ou brique
                        GrassGround grassGround = new GrassGround(col * TILE_SIZE, row * TILE_SIZE);
                        grounds.add(grassGround);
                        gameContainer.getChildren().add(grassGround);
                        break;

                    case '3': // Enemy1
                        FighterEnemy enemy1 = new FighterEnemy(col * TILE_SIZE, row * TILE_SIZE - TILE_SIZE);
                        enemies.add(enemy1);
                        gameContainer.getChildren().add(enemy1);
                        break;
                    case '4': // Enemy2
                        MissileEnemy enemy2 = new MissileEnemy(col * TILE_SIZE, row * TILE_SIZE - TILE_SIZE);
                        enemies.add(enemy2);
                        gameContainer.getChildren().add(enemy2);
                        break;
                    case '5': // Enemy3
                        ShieldEnemy enemy3 = new ShieldEnemy(col * TILE_SIZE, row * TILE_SIZE - TILE_SIZE);
                        enemies.add(enemy3);
                        gameContainer.getChildren().add(enemy3);
                        break;

                    case '6': // Enemy4
                        BomberEnemy enemy4 = new BomberEnemy(col * TILE_SIZE, row * TILE_SIZE - TILE_SIZE);
                        enemies.add(enemy4);
                        gameContainer.getChildren().add(enemy4);
                        break;
                    case '7': // Enemy5
                        ShurikenEnemy enemy5 = new ShurikenEnemy(col * TILE_SIZE, row * TILE_SIZE - TILE_SIZE);
                        enemies.add(enemy5);
                        gameContainer.getChildren().add(enemy5);
                        break;
                    case 'B': // BossEnemy
                        BossEnemy bossEnemy = new BossEnemy(col * TILE_SIZE, row * TILE_SIZE - TILE_SIZE);
                        enemies.add(bossEnemy);
                        gameContainer.getChildren().add(bossEnemy);
                        break;
                }
            }
        }
    }
}