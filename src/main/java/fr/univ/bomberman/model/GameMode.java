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
    REAL_TIME("Temps réel"),

    /**
     * Mode Capture the Flag : récupérer les drapeaux des adversaires
     * Les joueurs éliminés peuvent encore poser des bombes
     */
    CAPTURE_THE_FLAG("Capture the Flag");

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

    /**
     * Vérifie si ce mode permet aux joueurs éliminés de poser des bombes
     */
    public boolean allowsEliminatedPlayerActions() {
        return this == CAPTURE_THE_FLAG;
    }

    /**
     * Vérifie si ce mode utilise des drapeaux
     */
    public boolean usesFlags() {
        return this == CAPTURE_THE_FLAG;
    }
}