package com.devops.ninjava.manager;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {
    private MediaPlayer mediaPlayer;
    private MediaPlayer backgroundMusic;

    public void playSound(String fileName, double volume) {
        try {
            Media sound = new Media(getClass().getResource("/sound/" + fileName).toExternalForm());
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setVolume(volume);
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du fichier son : " + e.getMessage());
        }
    }

    public void playTitleMusic() {
        try {
            Media bgMusic = new Media(getClass().getResource("/sound/title_music.mp3").toExternalForm());
            backgroundMusic = new MediaPlayer(bgMusic);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE); // Boucle infinie
            backgroundMusic.setVolume(0.5); // Réduire le volume si nécessaire
            backgroundMusic.play();
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la musique de fond : " + e.getMessage());
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void playBackgroundMusic() {
        try {
            Media bgMusic = new Media(getClass().getResource("/sound/background.mp3").toExternalForm());
            backgroundMusic = new MediaPlayer(bgMusic);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE); // Boucle infinie
            backgroundMusic.setVolume(0.5); // Réduire le volume si nécessaire
            backgroundMusic.play();
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la musique de fond : " + e.getMessage());
        }
    }

    public void playBossMusic() {
        try {
            Media bgMusic = new Media(getClass().getResource("/sound/boss.mp3").toExternalForm());
            backgroundMusic = new MediaPlayer(bgMusic);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE); // Boucle infinie
            backgroundMusic.setVolume(0.5); // Réduire le volume si nécessaire
            backgroundMusic.play();
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la musique de fond : " + e.getMessage());
        }
    }
}
