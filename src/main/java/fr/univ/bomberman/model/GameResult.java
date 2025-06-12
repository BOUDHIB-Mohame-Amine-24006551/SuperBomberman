// FILE: src/main/java/fr/univ/bomberman/model/GameResult.java
package fr.univ.bomberman.model;

/**
 * Énumération représentant les différents résultats possibles d'une partie
 */
public enum GameResult {
    WIN("Victoire"),
    LOSE("Défaite"),
    TIE("Égalité"),
    DISCONNECT("Déconnexion"),
    FORFEIT("Abandon");

    private final String displayName;

    GameResult(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Vérifie si le résultat est considéré comme une victoire
     */
    public boolean isWin() {
        return this == WIN;
    }

    /**
     * Vérifie si le résultat est considéré comme une défaite
     */
    public boolean isLoss() {
        return this == LOSE || this == DISCONNECT || this == FORFEIT;
    }


}