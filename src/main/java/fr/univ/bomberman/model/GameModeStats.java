package fr.univ.bomberman.model;

/**
 * Statistiques pour un mode de jeu spécifique
 */
public class GameModeStats {
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int bombsPlaced;
    private int eliminationsDealt;
    private int deaths;
    private long totalPlayTimeSeconds;
    private int bestStreak;           // Meilleure série de victoires
    private int currentStreak;        // Série actuelle

    // Statistiques spéciales pour certains modes
    private int flagsCaptured;        // Pour CTF
    private int botWins;              // Victoires contre IA
    private int[] botWinsByDifficulty; // [facile, moyen, difficile]

    public GameModeStats() {
        this.botWinsByDifficulty = new int[3];
    }

    public void recordGame(GameSession session) {
        gamesPlayed++;
        bombsPlaced += session.getBombsPlaced();
        eliminationsDealt += session.getEliminationsDealt();
        deaths += session.getDeaths();
        totalPlayTimeSeconds += session.getDurationSeconds();

        if (session.isWon()) {
            wins++;
            currentStreak++;
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
            }

            // Statistiques spéciales
            if (session.isBotGame()) {
                botWins++;
                int difficulty = session.getBotDifficulty() - 1; // 0, 1, 2
                if (difficulty >= 0 && difficulty < 3) {
                    botWinsByDifficulty[difficulty]++;
                }
            }
        } else {
            losses++;
            currentStreak = 0;
        }

        // CTF spécifique
        if (session.getGameMode() == GameMode.CAPTURE_THE_FLAG) {
            flagsCaptured += session.getFlagsCaptured();
        }
    }

    public double getWinRatio() {
        if (gamesPlayed == 0) return 0.0;
        return (double) wins / gamesPlayed * 100.0;
    }

    // Getters et setters
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


    public long getTotalPlayTimeSeconds() {
        return totalPlayTimeSeconds;
    }

    public void setTotalPlayTimeSeconds(long totalPlayTimeSeconds) {
        this.totalPlayTimeSeconds = totalPlayTimeSeconds;
    }

    public int getBotWins() {
        return botWins;
    }

    public void setBotWins(int botWins) {
        this.botWins = botWins;
    }

    public void addGame(boolean won) {
        gamesPlayed++;
        if (won) {
            wins++;
        } else {
            losses++;
        }
    }

}
