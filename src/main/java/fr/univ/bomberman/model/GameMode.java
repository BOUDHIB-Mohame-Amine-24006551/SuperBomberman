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

    /**
     * @return La description du mode de jeu
     */
    public String getDescription() {
        return description;
    }

    /**
     * Vérifie si ce mode supporte plusieurs joueurs
     * @return true si le mode supporte plus de 2 joueurs
     */
    public boolean isMultiplayer() {
        return this == BATTLE_ROYALE || this == CAPTURE_THE_FLAG;
    }

    /**
     * Vérifie si ce mode utilise l'IA
     * @return true si le mode inclut des bots
     */
    public boolean hasBot() {
        return this == BOT_GAME;
    }

    /**
     * Obtient le nombre maximum de joueurs pour ce mode
     * @return nombre maximum de joueurs
     */
    public int getMaxPlayers() {
        switch (this) {
            case REAL_TIME:
            case TURN_BASED:
            case BOT_GAME:
                return 2;
            case BATTLE_ROYALE:
            case CAPTURE_THE_FLAG:
                return 4;
            default:
                return 2;
        }
    }

    /**
     * Obtient le nombre minimum de joueurs pour ce mode
     * @return nombre minimum de joueurs
     */
    public int getMinPlayers() {
        return 2; // Tous les modes nécessitent au moins 2 joueurs
    }

    /**
     * Obtient l'emoji associé au mode de jeu
     * @return emoji représentant le mode
     */
    public String getEmoji() {
        switch (this) {
            case REAL_TIME: return "⚡";
            case TURN_BASED: return "🎯";
            case BATTLE_ROYALE: return "⚔️";
            case BOT_GAME: return "🤖";
            case CAPTURE_THE_FLAG: return "🏁";
            default: return "🎮";
        }
    }

    /**
     * Obtient une description formatée avec emoji
     * @return description complète avec emoji
     */
    public String getFormattedDescription() {
        return getEmoji() + " " + displayName + " - " + description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}