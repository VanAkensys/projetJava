package com.devops.ninjava.utils;

import com.devops.ninjava.model.decor.Brick;
import com.devops.ninjava.model.decor.Pipe;
import com.devops.ninjava.model.enemy.Enemy;
import com.devops.ninjava.model.enemy.Enemy1;
import com.devops.ninjava.model.enemy.Goomba;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MapLoader {

    private static final int TILE_SIZE = 32; // Taille d'une tuile

    public static void loadMapFromFile(String filePath, Pane gameContainer, List<Brick> bricks, List<Enemy> enemies) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            for (int col = 0; col < line.length(); col++) {
                char tileType = line.charAt(col);

                switch (tileType) {
                    case '2': // Sol ou brique
                        Brick brick = new Brick(col * TILE_SIZE, row * TILE_SIZE);
                        bricks.add(brick);
                        gameContainer.getChildren().add(brick);
                        break;

                    case '3': // Enemy1
                        Enemy1 enemy1 = new Enemy1(col * TILE_SIZE, row * TILE_SIZE - TILE_SIZE);
                        enemies.add(enemy1);
                        gameContainer.getChildren().add(enemy1);
                        break;
                    // Vous pouvez ajouter d'autres types de tiles ici
                }
            }
        }
    }
}