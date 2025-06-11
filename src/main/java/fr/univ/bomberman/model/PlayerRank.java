package fr.univ.bomberman.model;

/**
 * Énumération des rangs de joueur
 */
public enum PlayerRank {
    ROOKIE("🥉 Débutant", "Bienvenue dans l'arène !"),
    BEGINNER("🥈 Novice", "Vous progressez bien !"),
    INTERMEDIATE("🥇 Intermédiaire", "De bonnes bases !"),
    ADVANCED("💎 Avancé", "Solides compétences !"),
    EXPERT("⭐ Expert", "Très impressionnant !"),
    MASTER("👑 Maître", "Redoutable adversaire !"),
    LEGEND("🏆 Légende", "Vous êtes une légende !");

    private final String displayName;
    private final String description;

    PlayerRank(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
