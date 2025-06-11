// FILE: src/main/java/fr/univ/bomberman/model/GameMode.java
package fr.univ.bomberman.model;

/**
 * Énumération des différents modes de jeu disponibles dans Super Bomberman
 */
public enum GameMode {
    REAL_TIME("Temps Réel", "Mode classique 2 joueurs en temps réel"),
    TURN_BASED("Tour par Tour", "Mode classique 2 joueurs au tour par tour"),
    BATTLE_ROYALE("Bataille Royale", "Combat à 4 joueurs - dernier survivant gagne"),
    BOT_GAME("Contre IA", "Affrontement contre l'intelligence artificielle"),
    CAPTURE_THE_FLAG("Capture The Flag", "Mode stratégique de capture de drapeaux");

    private final String displayName;
    private final String description;

    /**
     * Constructeur de l'énumération GameMode
     * @param displayName Nom affiché du mode
     * @param description Description du mode de jeu
     */
    GameMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * @return Le nom affiché du mode de jeu
     */
    public String getDisplayName() {
        return displayName;
    }











    @Override
    public String toString() {
        return displayName;
    }
}