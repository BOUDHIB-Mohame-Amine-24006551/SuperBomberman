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


    // ============================================================================
    // MÉTHODES D'INSTANCE
    // ============================================================================

    /**
     * Vérifie si ce rang est supérieur à un autre rang
     * @param otherRank Le rang à comparer
     * @return true si ce rang est supérieur
     */
    public boolean isHigherThan(Rank otherRank) {
        return this.ordinal() > otherRank.ordinal();
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================


    @Override
    public String toString() {
        return displayName;
    }
}