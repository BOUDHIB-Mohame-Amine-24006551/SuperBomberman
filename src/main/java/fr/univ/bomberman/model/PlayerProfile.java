// FILE: src/main/java/fr/univ/bomberman/model/PlayerProfile.java
package fr.univ.bomberman.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant le profil d'un joueur avec ses statistiques
 */
public class PlayerProfile {

    // Informations de base
    private String playerName;
    private LocalDateTime creationDate;
    private LocalDateTime lastPlayDate;

    // Statistiques générales
    private int totalGamesPlayed;
    private int totalWins;
    private int totalLosses;
    private int totalBombsPlaced;
    private int totalEliminatonsDealt;
    private int totalDeaths;
    private long totalPlayTimeSeconds;

    // Statistiques par mode de jeu
    private GameModeStats classicModeStats;      // Mode 2 joueurs
    private GameModeStats battleRoyaleStats;    // Mode 4 joueurs
    private GameModeStats botModeStats;         // Contre IA
    private GameModeStats ctfModeStats;         // Capture the Flag

    // Historique des parties récentes
    private List<GameSession> recentGames;

    // Paramètres de préférence
    private String preferredTheme;
    private boolean soundEnabled;
    private int preferredBotDifficulty;

    // Rang du joueur
    private Rank rank;



    /**
     * Constructeur avec nom de joueur
     */
    public PlayerProfile(String playerName) {
        this.playerName = playerName;
        this.creationDate = LocalDateTime.now();
        this.lastPlayDate = LocalDateTime.now();

        // Initialisation du rang
        this.rank = Rank.BRONZE;

        // Initialisation des préférences
        this.preferredTheme = "default";
        this.soundEnabled = true;
        this.preferredBotDifficulty = 2;

        // Initialisation des statistiques
        this.classicModeStats = new GameModeStats();
        this.battleRoyaleStats = new GameModeStats();
        this.botModeStats = new GameModeStats();
        this.ctfModeStats = new GameModeStats();
        this.recentGames = new ArrayList<>();
        this.totalPlayTimeSeconds = 0;
    }

    // ============================================================================
    // MÉTHODES DE GESTION DES PARTIES
    // ============================================================================



    /**
     * Obtient les statistiques pour un mode de jeu donné
     */
    public GameModeStats getStatsForMode(GameMode mode) {
        switch (mode) {
            case REAL_TIME:
            case TURN_BASED:
                return classicModeStats;
            case BATTLE_ROYALE:
                return battleRoyaleStats;
            case BOT_GAME:
                return botModeStats;
            case CAPTURE_THE_FLAG:
                return ctfModeStats;
            default:
                return classicModeStats;
        }
    }

    // ============================================================================
    // MÉTHODES DE CALCUL DES STATISTIQUES
    // ============================================================================

    /**
     * Calcule le ratio victoires/défaites
     */
    public double getWinRatio() {
        if (totalGamesPlayed == 0) return 0.0;
        return (double) totalWins / totalGamesPlayed * 100.0;
    }


    /**
     * Calcule le ratio éliminations/morts
     */
    public double getKillDeathRatio() {
        if (totalDeaths == 0) return totalEliminatonsDealt;
        return (double) totalEliminatonsDealt / totalDeaths;
    }

    // ============================================================================
    // MÉTHODES DE GESTION DU RANG
    // ============================================================================

    /**
     * Obtient le rang actuel du joueur
     */
    public Rank getRank() {
        if (rank == null) {
            updateRank();
        }
        return rank;
    }



    /**
     * Met à jour le rang basé sur le nombre total de victoires
     */
    public void updateRank() {
        this.rank = Rank.calculateRank(getTotalWins());
    }

    /**
     * Vérifie si le joueur peut être promu au rang supérieur
     */
    public boolean canBePromoted() {
        Rank currentRank = getRank();
        Rank calculatedRank = Rank.calculateRank(getTotalWins());
        return calculatedRank.isHigherThan(currentRank);
    }

    // ============================================================================
    // MÉTHODES DE FORMATAGE
    // ============================================================================

    /**
     * Formate la date de création pour l'affichage
     */
    public String getFormattedCreationDate() {
        return creationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Formate la date de dernière partie pour l'affichage
     */
    public String getFormattedLastPlayDate() {
        return lastPlayDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Obtient le temps de jeu total formaté
     */
    public String getFormattedTotalPlayTime() {
        long hours = totalPlayTimeSeconds / 3600;
        long minutes = (totalPlayTimeSeconds % 3600) / 60;
        return String.format("%dh %dm", hours, minutes);
    }

    // ============================================================================
    // GETTERS ET SETTERS
    // ============================================================================

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastPlayDate() {
        return lastPlayDate;
    }

    public void setLastPlayDate(LocalDateTime lastPlayDate) {
        this.lastPlayDate = lastPlayDate;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public void setTotalGamesPlayed(int totalGamesPlayed) {
        this.totalGamesPlayed = totalGamesPlayed;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }

    public int getTotalBombsPlaced() {
        return totalBombsPlaced;
    }

    public void setTotalBombsPlaced(int totalBombsPlaced) {
        this.totalBombsPlaced = totalBombsPlaced;
    }

    public int getTotalEliminatonsDealt() {
        return totalEliminatonsDealt;
    }

    public void setTotalEliminatonsDealt(int totalEliminatonsDealt) {
        this.totalEliminatonsDealt = totalEliminatonsDealt;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public long getTotalPlayTimeSeconds() {
        return totalPlayTimeSeconds;
    }

    public void setTotalPlayTimeSeconds(long totalPlayTimeSeconds) {
        this.totalPlayTimeSeconds = totalPlayTimeSeconds;
    }

    public GameModeStats getClassicModeStats() {
        return classicModeStats;
    }

    public GameModeStats getBattleRoyaleStats() {
        return battleRoyaleStats;
    }

    public GameModeStats getBotModeStats() {
        return botModeStats;
    }

    public GameModeStats getCtfModeStats() {
        return ctfModeStats;
    }

    public List<GameSession> getRecentGames() {
        return recentGames;
    }

    public String getPreferredTheme() {
        return preferredTheme;
    }

    public void setPreferredTheme(String preferredTheme) {
        this.preferredTheme = preferredTheme;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public int getPreferredBotDifficulty() {
        return preferredBotDifficulty;
    }

    public void setPreferredBotDifficulty(int preferredBotDifficulty) {
        this.preferredBotDifficulty = preferredBotDifficulty;
    }
}