package com.devops.ninjava.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

class PlayerHandler implements Runnable {
    private final Socket socket;
    private final List<PlayerHandler> players;
    private BufferedReader in;
    private PrintWriter out;

    public PlayerHandler(Socket socket, List<PlayerHandler> players) {
        this.socket = socket;
        this.players = players;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String input = in.readLine();
                if (input == null) break;

                // Diffuser l'action à tous les joueurs
                for (PlayerHandler player : players) {
                    player.out.println(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
