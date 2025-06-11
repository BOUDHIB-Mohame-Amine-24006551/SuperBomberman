// FILE: src/main/java/fr/univ/bomberman/model/GameMode.java
package fr.univ.bomberman.model;

/**
 * Énumération des différents modes de jeu disponibles
 */
public enum GameMode {
    /**
     * Mode tour par tour : les joueurs jouent chacun leur tour
     */
    TURN_BASED("Tour par tour"),

    /**
     * Mode temps réel : les joueurs peuvent bouger simultanément
     */
    REAL_TIME("Temps réel");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}