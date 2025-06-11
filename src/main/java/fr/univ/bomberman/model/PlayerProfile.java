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

    /**
     * Constructeur par défaut pour la désérialisation JSON
     */
    public PlayerProfile() {
        this.creationDate = LocalDateTime.now();
        this.lastPlayDate = LocalDateTime.now();
        this.classicModeStats = new GameModeStats();
        this.battleRoyaleStats = new GameModeStats();
        this.botModeStats = new GameModeStats();
        this.ctfModeStats = new GameModeStats();
        this.recentGames = new ArrayList<>();
        this.preferredTheme = "default";
        this.soundEnabled = true;
        this.preferredBotDifficulty = 2;
    }

    /**
     * Constructeur avec nom de joueur
     */
    public PlayerProfile(String playerName) {
        this();
        this.playerName = playerName;
    }

    /**
     * Enregistre une nouvelle partie terminée
     */
    public void recordGameSession(GameSession session) {
        // Mettre à jour les statistiques générales
        totalGamesPlayed++;
        if (session.isWon()) {
            totalWins++;
        } else {
            totalLosses++;
        }
        totalBombsPlaced += session.getBombsPlaced();
        totalEliminatonsDealt += session.getEliminationsDealt();
        totalDeaths += session.getDeaths();
        totalPlayTimeSeconds += session.getDurationSeconds();

        // Mettre à jour les statistiques par mode
        GameModeStats modeStats = getStatsForMode(session.getGameMode());
        modeStats.recordGame(session);

        // Ajouter à l'historique (garder seulement les 50 dernières)
        recentGames.add(0, session);
        if (recentGames.size() > 50) {
            recentGames = recentGames.subList(0, 50);
        }

        // Mettre à jour la date de dernière partie
        this.lastPlayDate = LocalDateTime.now();
    }

    /**
     * Obtient les statistiques pour un mode de jeu donné
     */
    public GameModeStats getStatsForMode(GameMode mode) {
        switch (mode) {
            case REAL_TIME:
            case TURN_BASED:
                return classicModeStats;
            case CAPTURE_THE_FLAG:
                return ctfModeStats;
            default:
                return classicModeStats;
        }
    }

    /**
     * Calcule le ratio victoires/défaites
     */
    public double getWinRatio() {
        if (totalGamesPlayed == 0) return 0.0;
        return (double) totalWins / totalGamesPlayed * 100.0;
    }

    /**
     * Calcule le temps de jeu moyen par partie (en minutes)
     */
    public double getAverageGameDuration() {
        if (totalGamesPlayed == 0) return 0.0;
        return (double) totalPlayTimeSeconds / totalGamesPlayed / 60.0;
    }

    /**
     * Calcule le ratio éliminations/morts
     */
    public double getKillDeathRatio() {
        if (totalDeaths == 0) return totalEliminatonsDealt;
        return (double) totalEliminatonsDealt / totalDeaths;
    }

    /**
     * Obtient le rang du joueur basé sur ses performances
     */
    public PlayerRank getRank() {
        double winRate = getWinRatio();
        int gamesPlayed = getTotalGamesPlayed();

        if (gamesPlayed < 5) return PlayerRank.ROOKIE;
        if (winRate >= 80 && gamesPlayed >= 50) return PlayerRank.LEGEND;
        if (winRate >= 70 && gamesPlayed >= 30) return PlayerRank.MASTER;
        if (winRate >= 60 && gamesPlayed >= 20) return PlayerRank.EXPERT;
        if (winRate >= 50 && gamesPlayed >= 10) return PlayerRank.ADVANCED;
        if (winRate >= 30) return PlayerRank.INTERMEDIATE;
        return PlayerRank.BEGINNER;
    }

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

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public LocalDateTime getLastPlayDate() { return lastPlayDate; }
    public void setLastPlayDate(LocalDateTime lastPlayDate) { this.lastPlayDate = lastPlayDate; }

    public int getTotalGamesPlayed() { return totalGamesPlayed; }
    public void setTotalGamesPlayed(int totalGamesPlayed) { this.totalGamesPlayed = totalGamesPlayed; }

    public int getTotalWins() { return totalWins; }
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }

    public int getTotalLosses() { return totalLosses; }
    public void setTotalLosses(int totalLosses) { this.totalLosses = totalLosses; }

    public int getTotalBombsPlaced() { return totalBombsPlaced; }
    public void setTotalBombsPlaced(int totalBombsPlaced) { this.totalBombsPlaced = totalBombsPlaced; }

    public int getTotalEliminatonsDealt() { return totalEliminatonsDealt; }
    public void setTotalEliminatonsDealt(int totalEliminatonsDealt) { this.totalEliminatonsDealt = totalEliminatonsDealt; }

    public int getTotalDeaths() { return totalDeaths; }
    public void setTotalDeaths(int totalDeaths) { this.totalDeaths = totalDeaths; }

    public long getTotalPlayTimeSeconds() { return totalPlayTimeSeconds; }
    public void setTotalPlayTimeSeconds(long totalPlayTimeSeconds) { this.totalPlayTimeSeconds = totalPlayTimeSeconds; }

    public GameModeStats getClassicModeStats() { return classicModeStats; }
    public void setClassicModeStats(GameModeStats classicModeStats) { this.classicModeStats = classicModeStats; }

    public GameModeStats getBattleRoyaleStats() { return battleRoyaleStats; }
    public void setBattleRoyaleStats(GameModeStats battleRoyaleStats) { this.battleRoyaleStats = battleRoyaleStats; }

    public GameModeStats getBotModeStats() { return botModeStats; }
    public void setBotModeStats(GameModeStats botModeStats) { this.botModeStats = botModeStats; }

    public GameModeStats getCtfModeStats() { return ctfModeStats; }
    public void setCtfModeStats(GameModeStats ctfModeStats) { this.ctfModeStats = ctfModeStats; }

    public List<GameSession> getRecentGames() { return recentGames; }
    public void setRecentGames(List<GameSession> recentGames) { this.recentGames = recentGames; }

    public String getPreferredTheme() { return preferredTheme; }
    public void setPreferredTheme(String preferredTheme) { this.preferredTheme = preferredTheme; }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }

    public int getPreferredBotDifficulty() { return preferredBotDifficulty; }
    public void setPreferredBotDifficulty(int preferredBotDifficulty) { this.preferredBotDifficulty = preferredBotDifficulty; }
}

