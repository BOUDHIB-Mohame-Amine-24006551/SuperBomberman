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
        this.rank = Rank.BRONZE; // Rang initial
    }

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

        // Mettre à jour le rang si victoire
        if (session.isWon()) {
            Rank oldRank = getRank();
            updateRank();

            // Log de promotion si applicable
            if (getRank().isHigherThan(oldRank)) {
                System.out.println("🎉 PROMOTION ! " + playerName +
                        " passe du rang " + oldRank.getDisplayName() +
                        " au rang " + getRank().getDisplayName() + " !");
            }
        }
    }

    /**
     * Met à jour le profil après une partie
     */
    public void updateAfterGame(boolean won, GameMode gameMode, int durationSeconds) {
        // Mettre à jour la date de dernière partie
        this.lastPlayDate = LocalDateTime.now();

        // Ajouter le temps de jeu
        this.totalPlayTimeSeconds += durationSeconds;

        // Mettre à jour les statistiques selon le mode
        GameModeStats stats = getStatsForMode(gameMode);
        if (stats != null) {
            stats.addGame(won);
        }

        // Mettre à jour le rang si victoire
        if (won) {
            Rank oldRank = getRank();
            updateRank();

            // Log de promotion si applicable
            if (getRank().isHigherThan(oldRank)) {
                System.out.println("🎉 " + playerName + " a été promu au rang " + getRank().getDisplayName() + " !");
            }
        }
    }

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
     * Définit le rang du joueur
     */
    public void setRank(Rank rank) {
        this.rank = rank;
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

    /**
     * Obtient les informations de progression vers le prochain rang
     */
    public String getRankProgressInfo() {
        return getRank().getFormattedProgressInfo(getTotalWins());
    }

    /**
     * Obtient une barre de progression vers le prochain rang
     */
    public String getRankProgressBar() {
        return getRank().getProgressBar(getTotalWins(), 20);
    }

    /**
     * Obtient le pourcentage de progression vers le prochain rang
     */
    public double getRankProgressPercentage() {
        return getRank().getProgressToNextRank(getTotalWins());
    }

    /**
     * Vérifie si le joueur a atteint le rang maximum
     */
    public boolean hasMaxRank() {
        return getRank() == Rank.DIAMOND;
    }

    /**
     * Obtient le nombre de victoires nécessaires pour le prochain rang
     */
    public int getWinsToNextRank() {
        return getRank().getWinsToNextRank(getTotalWins());
    }

    /**
     * Obtient le rang sous forme de string pour la sauvegarde
     */
    public String getRankAsString() {
        return getRank().name();
    }

    /**
     * Définit le rang depuis une string lors du chargement
     */
    public void setRankFromString(String rankString) {
        this.rank = Rank.fromString(rankString);
    }

    // ============================================================================
    // MÉTHODES DE GESTION DE L'ACTIVITÉ
    // ============================================================================

    /**
     * Obtient le niveau d'activité du joueur
     */
    public ActivityLevel getActivityLevel() {
        return ActivityLevel.calculateActivityLevel(getTotalGamesPlayed());
    }

    /**
     * Obtient les informations de progression d'activité
     */
    public String getActivityProgressInfo() {
        ActivityLevel currentLevel = getActivityLevel();
        ActivityLevel nextLevel = currentLevel.getNextLevel();

        if (nextLevel == currentLevel) {
            return "🏆 Niveau d'activité maximum atteint !";
        }

        int gamesNeeded = currentLevel.getGamesToNextLevel(getTotalGamesPlayed());
        return String.format("Prochain niveau: %s (%d parties nécessaires)",
                nextLevel.getDisplayName(), gamesNeeded);
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    /**
     * Duplique ce profil avec un nouveau nom
     */
    public PlayerProfile duplicate(String newName) {
        PlayerProfile duplicated = new PlayerProfile(newName);

        // Copier les préférences
        duplicated.setPreferredTheme(this.preferredTheme);
        duplicated.setSoundEnabled(this.soundEnabled);
        duplicated.setPreferredBotDifficulty(this.preferredBotDifficulty);

        // Nouvelle date de création
        duplicated.creationDate = LocalDateTime.now();
        duplicated.lastPlayDate = LocalDateTime.now();

        return duplicated;
    }

    /**
     * Met à jour la date de dernière connexion
     */
    public void updateLastPlayDate() {
        this.lastPlayDate = LocalDateTime.now();
    }

    /**
     * Vérifie si le profil a été utilisé récemment
     */
    public boolean isRecentlyUsed(int days) {
        return java.time.Duration.between(lastPlayDate, LocalDateTime.now()).toDays() <= days;
    }

    /**
     * Obtient une description courte du profil
     */
    public String getShortDescription() {
        return String.format("%s (%s) - %d parties, %.1f%% victoires",
                playerName,
                getRank().getDisplayName(),
                getTotalGamesPlayed(),
                getWinRatio());
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

    public void setClassicModeStats(GameModeStats classicModeStats) {
        this.classicModeStats = classicModeStats;
    }

    public GameModeStats getBattleRoyaleStats() {
        return battleRoyaleStats;
    }

    public void setBattleRoyaleStats(GameModeStats battleRoyaleStats) {
        this.battleRoyaleStats = battleRoyaleStats;
    }

    public GameModeStats getBotModeStats() {
        return botModeStats;
    }

    public void setBotModeStats(GameModeStats botModeStats) {
        this.botModeStats = botModeStats;
    }

    public GameModeStats getCtfModeStats() {
        return ctfModeStats;
    }

    public void setCtfModeStats(GameModeStats ctfModeStats) {
        this.ctfModeStats = ctfModeStats;
    }

    public List<GameSession> getRecentGames() {
        return recentGames;
    }

    public void setRecentGames(List<GameSession> recentGames) {
        this.recentGames = recentGames;
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