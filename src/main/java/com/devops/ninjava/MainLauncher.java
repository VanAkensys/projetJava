package com.devops.ninjava;

import com.devops.ninjava.manager.GameServer;
import com.devops.ninjava.manager.GameEngine;

public class MainLauncher {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -jar NinJava.jar <server|game>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "server" -> GameServer.main(new String[]{});
            case "game" -> GameEngine.main(new String[]{});
            default -> System.out.println("Invalid argument. Use 'server' or 'game'.");
        }
    }
}
