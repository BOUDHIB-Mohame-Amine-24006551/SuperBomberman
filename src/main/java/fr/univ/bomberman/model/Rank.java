// FILE: src/main/java/fr/univ/bomberman/model/Rank.java
package fr.univ.bomberman.model;

/**
 * Énumération pour les rangs des joueurs dans Super Bomberman
 */
public enum Rank {
    BRONZE("🥉 Bronze", 0, "#cd7f32"),
    SILVER("🥈 Argent", 10, "#c0c0c0"),
    GOLD("🥇 Or", 25, "#ffd700"),
    PLATINUM("💎 Platine", 50, "#e5e4e2"),
    DIAMOND("👑 Diamant", 100, "#b9f2ff");

    private final String displayName;
    private final int winsRequired;
    private final String color;

    /**
     * Constructeur de l'énumération Rank
     * @param displayName Nom affiché du rang avec emoji
     * @param winsRequired Nombre de victoires nécessaires pour atteindre ce rang
     * @param color Code couleur hexadécimal associé au rang
     */
    Rank(String displayName, int winsRequired, String color) {
        this.displayName = displayName;
        this.winsRequired = winsRequired;
        this.color = color;
    }

    // ============================================================================
    // GETTERS
    // ============================================================================

    /**
     * @return Le nom affiché du rang avec emoji
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Le nombre de victoires nécessaires pour atteindre ce rang
     */
    public int getWinsRequired() {
        return winsRequired;
    }

    /**
     * @return Le code couleur hexadécimal associé au rang
     */
    public String getColor() {
        return color;
    }

    // ============================================================================
    // MÉTHODES STATIQUES
    // ============================================================================

    /**
     * Calcule le rang approprié basé sur le nombre de victoires
     * @param totalWins Nombre total de victoires du joueur
     * @return Le rang correspondant au nombre de victoires
     */
    public static Rank calculateRank(int totalWins) {
        if (totalWins >= DIAMOND.winsRequired) return DIAMOND;
        if (totalWins >= PLATINUM.winsRequired) return PLATINUM;
        if (totalWins >= GOLD.winsRequired) return GOLD;
        if (totalWins >= SILVER.winsRequired) return SILVER;
        return BRONZE;
    }

    /**
     * Obtient tous les rangs dans l'ordre croissant
     * @return Tableau de tous les rangs
     */
    public static Rank[] getAllRanks() {
        return values();
    }

    /**
     * Obtient un rang depuis son nom (insensible à la casse)
     * @param rankName Nom du rang
     * @return Le rang correspondant ou BRONZE si non trouvé
     */
    public static Rank fromString(String rankName) {
        try {
            return valueOf(rankName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BRONZE; // Valeur par défaut
        }
    }

    // ============================================================================
    // MÉTHODES D'INSTANCE
    // ============================================================================

    /**
     * Obtient le rang suivant dans la hiérarchie
     * @return Le rang supérieur ou le même rang si c'est le maximum
     */
    public Rank getNextRank() {
        switch (this) {
            case BRONZE: return SILVER;
            case SILVER: return GOLD;
            case GOLD: return PLATINUM;
            case PLATINUM: return DIAMOND;
            case DIAMOND: return DIAMOND; // Rang maximum atteint
            default: return BRONZE;
        }
    }

    /**
     * Obtient le rang précédent dans la hiérarchie
     * @return Le rang inférieur ou le même rang si c'est le minimum
     */
    public Rank getPreviousRank() {
        switch (this) {
            case SILVER: return BRONZE;
            case GOLD: return SILVER;
            case PLATINUM: return GOLD;
            case DIAMOND: return PLATINUM;
            case BRONZE: return BRONZE; // Rang minimum
            default: return BRONZE;
        }
    }

    /**
     * Calcule le nombre de victoires nécessaires pour atteindre le prochain rang
     * @param currentWins Nombre actuel de victoires du joueur
     * @return Nombre de victoires manquantes (0 si rang maximum atteint)
     */
    public int getWinsToNextRank(int currentWins) {
        Rank nextRank = getNextRank();
        if (nextRank == this) {
            return 0; // Rang maximum atteint
        }
        return Math.max(0, nextRank.winsRequired - currentWins);
    }

    /**
     * Vérifie si ce rang est supérieur à un autre rang
     * @param otherRank Le rang à comparer
     * @return true si ce rang est supérieur
     */
    public boolean isHigherThan(Rank otherRank) {
        return this.ordinal() > otherRank.ordinal();
    }

    /**
     * Vérifie si ce rang est inférieur à un autre rang
     * @param otherRank Le rang à comparer
     * @return true si ce rang est inférieur
     */
    public boolean isLowerThan(Rank otherRank) {
        return this.ordinal() < otherRank.ordinal();
    }

    /**
     * Calcule le pourcentage de progression vers le prochain rang
     * @param currentWins Nombre actuel de victoires
     * @return Pourcentage de progression (0-100)
     */
    public double getProgressToNextRank(int currentWins) {
        Rank nextRank = getNextRank();
        if (nextRank == this) {
            return 100.0; // Rang maximum atteint
        }

        int currentRankWins = this.winsRequired;
        int nextRankWins = nextRank.winsRequired;
        int winsInCurrentRank = currentWins - currentRankWins;
        int winsNeededForNextRank = nextRankWins - currentRankWins;

        if (winsNeededForNextRank == 0) {
            return 100.0;
        }

        return Math.min(100.0, Math.max(0.0,
                (double) winsInCurrentRank / winsNeededForNextRank * 100.0));
    }

    /**
     * Obtient une description détaillée du rang
     * @return Description avec informations sur le rang
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(displayName);

        if (this != DIAMOND) {
            Rank nextRank = getNextRank();
            int winsNeeded = nextRank.winsRequired - this.winsRequired;
            desc.append("\n📈 Victoires pour ").append(nextRank.displayName)
                    .append(": ").append(nextRank.winsRequired);
        } else {
            desc.append("\n👑 Rang maximum atteint !");
        }

        return desc.toString();
    }

    /**
     * Obtient l'emoji associé au rang
     * @return L'emoji du rang (sans le texte)
     */
    public String getEmoji() {
        return displayName.substring(0, 2); // Récupère juste l'emoji
    }

    /**
     * Obtient le nom du rang sans emoji
     * @return Le nom du rang sans emoji
     */
    public String getNameOnly() {
        return displayName.substring(3); // Retire l'emoji et l'espace
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    /**
     * Obtient des informations de progression formatées
     * @param currentWins Nombre actuel de victoires
     * @return String formatée avec les informations de progression
     */
    public String getFormattedProgressInfo(int currentWins) {
        if (this == DIAMOND) {
            return "👑 Rang maximum atteint avec " + currentWins + " victoires !";
        }

        Rank nextRank = getNextRank();
        int winsNeeded = getWinsToNextRank(currentWins);
        double progress = getProgressToNextRank(currentWins);

        return String.format("%s ➜ %s\n📊 Progression: %.1f%%\n🎯 Victoires restantes: %d",
                this.displayName,
                nextRank.displayName,
                progress,
                winsNeeded);
    }

    /**
     * Génère une barre de progression textuelle
     * @param currentWins Nombre actuel de victoires
     * @param barLength Longueur de la barre (en caractères)
     * @return Barre de progression sous forme de string
     */
    public String getProgressBar(int currentWins, int barLength) {
        if (this == DIAMOND) {
            return "█".repeat(barLength) + " 100%";
        }

        double progress = getProgressToNextRank(currentWins) / 100.0;
        int filledLength = (int) (barLength * progress);
        int emptyLength = barLength - filledLength;

        return "█".repeat(filledLength) +
                "░".repeat(emptyLength) +
                String.format(" %.1f%%", progress * 100);
    }

    @Override
    public String toString() {
        return displayName;
    }
}