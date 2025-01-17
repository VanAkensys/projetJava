package com.devops.ninjava.manager;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 3303;
    private static final Map<Socket, String> clientRoles = new HashMap<>();
    private static final List<PrintWriter> clients = new ArrayList<>();

    private static boolean player1Connected = false;
    private static boolean player2Connected = false;
    private static String role = "";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Game server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                role = assignRole();

                if (role == null) {
                    System.out.println("New client connected as SPECTATOR");
                    role = "SPECTATOR";
                } else {
                    System.out.println("New client connected as " + role);
                }

                clientRoles.put(clientSocket, role);

                new Thread(() -> handleClient(clientSocket, role)).start();

                // Si les deux joueurs sont connectés, envoyer un message global
                if (player1Connected && player2Connected) {
                    delay(10000);
                    sendToAllClients("BOTH_PLAYERS_CONNECTED");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String assignRole() {
        if (!player1Connected) {
            player1Connected = true;
            return "PLAYER1";
        } else if (!player2Connected) {
            player2Connected = true;
            return "PLAYER2";
        }
        return null; // SPECTATOR si les deux joueurs sont déjà connectés
    }

    private static void handleClient(Socket clientSocket, String role) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            clients.add(out);

            // Envoyer le rôle au client
            out.println("ROLE:" + role);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from " + role + ": " + message);
                broadcast(message, clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanupClient(clientSocket, role);
        }
    }

    private static void cleanupClient(Socket clientSocket, String role) {
        try {
            clients.removeIf(client -> client.equals(clientSocket));
            clientRoles.remove(clientSocket);

            if ("PLAYER1".equals(role)) {
                player1Connected = false;
            } else if ("PLAYER2".equals(role)) {
                player2Connected = false;
            }

            System.out.println(role + " disconnected.");
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcast(String message, Socket sender) {
        for (PrintWriter client : clients) {
            client.println(message);
        }
    }

    private static void sendToAllClients(String message) {
        System.out.println("Broadcasting message: " + message);
        for (PrintWriter client : clients) {
            if (client != null) {
                client.println(message);
            }
        }
    }

    private static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Delay interrupted: " + e.getMessage());
        }
    }
}
