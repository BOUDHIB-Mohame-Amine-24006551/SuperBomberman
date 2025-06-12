// FILE: src/main/java/fr/univ/bomberman/model/ActivityLevel.java
package fr.univ.bomberman.model;

/**
 * Énumération pour les niveaux d'activité des joueurs
 */
public enum ActivityLevel {
    NOUVEAU("🆕 Nouveau", "#95a5a6"),
    DÉBUTANT("🌱 Débutant", "#2ecc71"),
    ACTIF("🔥 Actif", "#f39c12"),
    VÉTÉRAN("⭐ Vétéran", "#9b59b6"),
    LÉGENDE("👑 Légende", "#e74c3c");

    private final String displayName;
    private final String color;

    /**
     * Constructeur de l'énumération ActivityLevel
     * @param displayName Nom affiché du niveau
     * @param color Code couleur associé au niveau
     */
    ActivityLevel(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    /**
     * Retourne le nom affiché du niveau 
     * @return le nom affiché du niveau
     */
    public String getDisplayName() { return displayName; }

    /**
     * Retourne la couleur associée au niveau
     * @return le code couleur 
     */
    public String getColor() { return color; }

    /**
     * Calcule le niveau d'activité basé sur le nombre de parties jouées
     * @param gamesPlayed nombre total de parties jouées
     * @return le niveau d'activité correspondant
     */
    public static ActivityLevel calculateActivityLevel(int gamesPlayed) {
        if (gamesPlayed == 0) return NOUVEAU;
        if (gamesPlayed < 10) return DÉBUTANT;
        if (gamesPlayed < 50) return ACTIF;
        if (gamesPlayed < 200) return VÉTÉRAN;
        return LÉGENDE;
    }

    /**
     * Obtient le niveau suivant
     */
    public ActivityLevel getNextLevel() {
        switch (this) {
            case NOUVEAU: return DÉBUTANT;
            case DÉBUTANT: return ACTIF;
            case ACTIF: return VÉTÉRAN;
            case VÉTÉRAN: return LÉGENDE;
            case LÉGENDE: return LÉGENDE; // Niveau maximum
            default: return NOUVEAU;
        }
    }

    /**
     * Obtient le nombre de parties nécessaires pour atteindre le niveau suivant
     * @return le nombre de parties requis pour le prochain niveau
     */
    public int getGamesToNextLevel(int currentGames) {
        switch (this) {
            case NOUVEAU: return Math.max(0, 1 - currentGames);
            case DÉBUTANT: return Math.max(0, 10 - currentGames);
            case ACTIF: return Math.max(0, 50 - currentGames);
            case VÉTÉRAN: return Math.max(0, 200 - currentGames);
            case LÉGENDE: return 0; // Niveau maximum
            default: return 0;
        }
    }
}