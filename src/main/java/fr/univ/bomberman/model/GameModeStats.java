// FILE: src/main/java/fr/univ/bomberman/model/GameModeStats.java
package fr.univ.bomberman.model;

/**
 * Statistiques pour un mode de jeu spécifique
 */
public class GameModeStats {

    private int gamesPlayed;
    private int wins;
    private int losses;
    private int totalBombs;
    private int totalEliminations;
    private int totalDeaths;
    private long totalPlayTimeSeconds;
    private int bestPerformanceScore;
    private String fastestWinTime; // Format: "2m 15s"

    /**
     * Constructeur par défaut
     */
    public GameModeStats() {
        this.gamesPlayed = 0;
        this.wins = 0;
        this.losses = 0;
        this.totalBombs = 0;
        this.totalEliminations = 0;
        this.totalDeaths = 0;
        this.totalPlayTimeSeconds = 0;
        this.bestPerformanceScore = 0;
        this.fastestWinTime = "N/A";
    }

    /**
     * Enregistre une nouvelle partie dans ces statistiques
     */
    public void recordGame(GameSession session) {
        gamesPlayed++;

        if (session.isWon()) {
            wins++;

            // Mettre à jour le temps de victoire le plus rapide
            updateFastestWin(session.getDurationSeconds());
        } else {
            losses++;
        }

        totalBombs += session.getBombsPlaced();
        totalEliminations += session.getEliminationsDealt();
        totalDeaths += session.getDeaths();
        totalPlayTimeSeconds += session.getDurationSeconds();

        // Mettre à jour le meilleur score de performance
        int sessionScore = session.getPerformanceScore();
        if (sessionScore > bestPerformanceScore) {
            bestPerformanceScore = sessionScore;
        }
    }

    /**
     * Met à jour le temps de victoire le plus rapide
     */
    private void updateFastestWin(long durationSeconds) {
        if ("N/A".equals(fastestWinTime)) {
            fastestWinTime = formatDuration(durationSeconds);
        } else {
            // Convertir le temps actuel en secondes pour comparer
            long currentFastestSeconds = parseDuration(fastestWinTime);
            if (durationSeconds < currentFastestSeconds) {
                fastestWinTime = formatDuration(durationSeconds);
            }
        }
    }

    /**
     * Formate une durée en secondes vers le format "Xm Ys"
     */
    private String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%dm %02ds", minutes, remainingSeconds);
    }

    /**
     * Parse une durée du format "Xm Ys" vers des secondes
     */
    private long parseDuration(String duration) {
        try {
            String[] parts = duration.replace("m", "").replace("s", "").split(" ");
            long minutes = Long.parseLong(parts[0]);
            long seconds = Long.parseLong(parts[1]);
            return minutes * 60 + seconds;
        } catch (Exception e) {
            return 300; // 5 minutes par défaut
        }
    }

    /**
     * Calcule le ratio victoires/défaites
     */
    public double getWinRatio() {
        if (gamesPlayed == 0) return 0.0;
        return (double) wins / gamesPlayed * 100.0;
    }

    /**
     * Calcule le ratio kills/deaths
     */
    public double getKillDeathRatio() {
        if (totalDeaths == 0) return totalEliminations;
        return (double) totalEliminations / totalDeaths;
    }

    /**
     * Calcule la moyenne de bombes par partie
     */
    public double getAverageBombsPerGame() {
        if (gamesPlayed == 0) return 0.0;
        return (double) totalBombs / gamesPlayed;
    }

    /**
     * Calcule le temps de jeu moyen par partie (en minutes)
     */
    public double getAverageGameDuration() {
        if (gamesPlayed == 0) return 0.0;
        return (double) totalPlayTimeSeconds / gamesPlayed / 60.0;
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

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getTotalBombs() {
        return totalBombs;
    }

    public void setTotalBombs(int totalBombs) {
        this.totalBombs = totalBombs;
    }

    public int getTotalEliminations() {
        return totalEliminations;
    }

    public void setTotalEliminations(int totalEliminations) {
        this.totalEliminations = totalEliminations;
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

    public int getBestPerformanceScore() {
        return bestPerformanceScore;
    }

    public void setBestPerformanceScore(int bestPerformanceScore) {
        this.bestPerformanceScore = bestPerformanceScore;
    }

    public String getFastestWinTime() {
        return fastestWinTime;
    }

    public void setFastestWinTime(String fastestWinTime) {
        this.fastestWinTime = fastestWinTime;
    }
}