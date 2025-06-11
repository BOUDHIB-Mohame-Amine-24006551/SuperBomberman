// FILE: src/main/java/fr/univ/bomberman/model/GameMode.java
package fr.univ.bomberman.model;

/**
 * Énumération des différents modes de jeu disponibles
 */
public enum GameMode {
    REAL_TIME("Temps Réel", "🎮", "Mode de jeu en temps réel avec mouvements continus"),
    TURN_BASED("Tour par Tour", "⏱️", "Mode de jeu tour par tour classique"),
    CAPTURE_THE_FLAG("Capture the Flag", "🏁", "Mode stratégique avec capture de drapeaux"),
    BATTLE_ROYALE("Bataille Royale", "⚔️", "Combat à 4 joueurs, dernier survivant gagne"),
    BOT_CHALLENGE("Défi IA", "🤖", "Affrontement contre intelligence artificielle");

    private final String displayName;
    private final String emoji;
    private final String description;

    /**
     * Constructeur pour chaque mode de jeu
     */
    GameMode(String displayName, String emoji, String description) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.description = description;
    }

    /**
     * Obtient le nom d'affichage du mode
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtient l'emoji représentant le mode
     */
    public String getEmoji() {
        return emoji;
    }

    /**
     * Obtient la description du mode
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtient le nom complet avec emoji
     */
    public String getFullDisplayName() {
        return emoji + " " + displayName;
    }

    /**
     * Détermine si ce mode supporte plusieurs joueurs humains
     */
    public boolean isMultiplayer() {
        return this == REAL_TIME || this == TURN_BASED ||
                this == CAPTURE_THE_FLAG || this == BATTLE_ROYALE;
    }

    /**
     * Détermine si ce mode inclut une IA
     */
    public boolean hasBotPlayer() {
        return this == BOT_CHALLENGE;
    }

    /**
     * Obtient le nombre maximum de joueurs pour ce mode
     */
    public int getMaxPlayers() {
        switch (this) {
            case REAL_TIME:
            case TURN_BASED:
                return 2;
            case CAPTURE_THE_FLAG:
            case BATTLE_ROYALE:
                return 4;
            case BOT_CHALLENGE:
                return 2; // Joueur + Bot
            default:
                return 2;
        }
    }

    /**
     * Obtient le nombre minimum de joueurs pour ce mode
     */
    public int getMinPlayers() {
        return 2; // Tous les modes nécessitent au moins 2 participants
    }

    /**
     * Détermine si ce mode nécessite un placement de drapeaux
     */
    public boolean requiresFlagPlacement() {
        return this == CAPTURE_THE_FLAG;
    }

    /**
     * Détermine si ce mode permet les mouvements continus
     */
    public boolean allowsContinuousMovement() {
        return this == REAL_TIME || this == BOT_CHALLENGE;
    }

    /**
     * Obtient une description détaillée du mode pour l'aide
     */
    public String getDetailedDescription() {
        switch (this) {
            case REAL_TIME:
                return "Mode dynamique où les joueurs se déplacent en temps réel. " +
                        "Mouvements fluides et action continue. Parfait pour des duels rapides.";

            case TURN_BASED:
                return "Mode classique au tour par tour. Chaque joueur joue à son tour, " +
                        "permettant une approche plus réfléchie et stratégique.";

            case CAPTURE_THE_FLAG:
                return "Mode stratégique où chaque joueur doit capturer tous les drapeaux adverses. " +
                        "Placement initial des drapeaux, puis phase de combat et capture. " +
                        "Les joueurs éliminés peuvent encore poser des bombes !";

            case BATTLE_ROYALE:
                return "Combat à 4 joueurs en mode discret (une touche = un mouvement). " +
                        "Dernier survivant remporte la victoire. Action pure et éliminations rapides.";

            case BOT_CHALLENGE:
                return "Affrontez une intelligence artificielle avec trois niveaux de difficulté. " +
                        "Testez vos compétences contre un adversaire qui ne commet jamais d'erreurs !";

            default:
                return description;
        }
    }

    /**
     * Obtient les contrôles recommandés pour ce mode
     */
    public String getControlsDescription() {
        switch (this) {
            case REAL_TIME:
            case BOT_CHALLENGE:
                return "Joueur 1: ZQSD + ESPACE | Mouvements continus";

            case TURN_BASED:
                return "Joueur 1: ZQSD + ESPACE | Joueur 2: ↑↓←→ + ENTRÉE | Au tour par tour";

            case CAPTURE_THE_FLAG:
                return "J1: ZQSD+A | J2: ↑↓←→+ENTRÉE | J3: IJKL+U | J4: 8456+7 | Mode discret";

            case BATTLE_ROYALE:
                return "J1: ZQSD+A | J2: ↑↓←→+ENTRÉE | J3: IJKL+U | J4: 8456+7 | Une touche = un mouvement";

            default:
                return "Voir instructions en jeu";
        }
    }

    /**
     * Méthode utilitaire pour convertir depuis une chaîne
     */
    public static GameMode fromString(String modeString) {
        if (modeString == null) return REAL_TIME;

        try {
            return GameMode.valueOf(modeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Fallback pour compatibilité
            switch (modeString.toLowerCase()) {
                case "real_time":
                case "realtime":
                case "temps_reel":
                    return REAL_TIME;
                case "turn_based":
                case "turnbased":
                case "tour_par_tour":
                    return TURN_BASED;
                case "ctf":
                case "capture_flag":
                case "capture_the_flag":
                    return CAPTURE_THE_FLAG;
                case "battle_royale":
                case "battleroyale":
                case "4_players":
                    return BATTLE_ROYALE;
                case "bot":
                case "bot_challenge":
                case "ai":
                    return BOT_CHALLENGE;
                default:
                    return REAL_TIME;
            }
        }
    }
}