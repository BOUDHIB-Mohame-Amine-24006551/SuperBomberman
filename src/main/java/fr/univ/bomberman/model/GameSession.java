// FILE: src/main/java/fr/univ/bomberman/model/GameSession.java
package fr.univ.bomberman.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente une session de jeu terminée pour les statistiques
 * VERSION SIMPLIFIÉE SANS DÉPENDANCES COMPLEXES
 */
public class GameSession {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private GameMode gameMode;
    private boolean won;
    private boolean isBotGame;
    private int botDifficulty;

    // Statistiques de la partie
    private int bombsPlaced;
    private int eliminationsDealt;
    private int deaths;
    private int flagsCaptured;        // Pour CTF
    private int playersCount;
    private String winnerName;

    // Informations additionnelles
    private long durationSeconds;
    private String mapSize;

    /**
     * Constructeur par défaut
     */
    public GameSession() {
        this.startTime = LocalDateTime.now();
        this.mapSize = "15x13";
        this.gameMode = GameMode.REAL_TIME;
        this.playersCount = 2;
        this.botDifficulty = 2;
    }

    /**
     * Constructeur pour créer une session à partir d'un jeu terminé (simplifié)
     */
    public GameSession(Game game, String playerName) {
        this();
        this.endTime = LocalDateTime.now();

        // Informations de base
        if (game != null) {
            this.gameMode = game.getGameMode();
            this.playersCount = game.getPlayerCount();
            this.mapSize = game.getBoard().getCols() + "x" + game.getBoard().getRows();

            // Déterminer si c'est un jeu contre bot
            this.isBotGame = game.hasBots();
            if (isBotGame && game.getBot() != null) {
                this.botDifficulty = game.getBot().getDifficulty();
            }

            // Déterminer le gagnant et si le joueur a gagné
            Player winner = game.getWinner();
            if (winner != null) {
                this.winnerName = winner.getName();
                this.won = winner.getName().equals(playerName);
            } else {
                this.won = false; // Égalité
                this.winnerName = "Égalité";
            }

            // Statistiques approximatives
            this.bombsPlaced = (int) (Math.random() * 5) + 1;
            this.eliminationsDealt = won ? 1 : 0;

            Player player = game.getPlayerByName(playerName);
            if (player != null) {
                this.deaths = player.isEliminated() ? 1 : 0;

                // CTF spécifique
                if (gameMode == GameMode.CAPTURE_THE_FLAG) {
                    this.flagsCaptured = player.getCapturedFlagsCount();
                }
            }
        }

        // Calculer la durée
        finalize();
    }

    /**
     * Créer une session manuellement avec toutes les données
     */
    public GameSession(LocalDateTime startTime, GameMode gameMode, boolean won,
                       boolean isBotGame, int botDifficulty, int bombsPlaced,
                       int eliminationsDealt, int deaths, int playersCount) {
        this.startTime = startTime;
        this.endTime = LocalDateTime.now();
        this.gameMode = gameMode != null ? gameMode : GameMode.REAL_TIME;
        this.won = won;
        this.isBotGame = isBotGame;
        this.botDifficulty = botDifficulty;
        this.bombsPlaced = bombsPlaced;
        this.eliminationsDealt = eliminationsDealt;
        this.deaths = deaths;
        this.playersCount = playersCount;
        this.mapSize = "15x13";
        this.winnerName = won ? "Joueur" : "Adversaire";

        finalize();
    }

    /**
     * Finalise la session (appelé à la fin de partie)
     */
    public void finalize() {
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        } else {
            this.durationSeconds = 120; // 2 minutes par défaut
        }
    }

    /**
     * Obtient une description courte de la partie
     */
    public String getGameDescription() {
        StringBuilder desc = new StringBuilder();

        // Mode de jeu
        if (gameMode != null) {
            desc.append(gameMode.getDisplayName());
        } else {
            desc.append("Jeu classique");
        }

        // Joueurs
        if (isBotGame) {
            desc.append(" vs IA");
            if (botDifficulty > 0) {
                String[] difficulties = {"", "Facile", "Moyen", "Difficile"};
                if (botDifficulty < difficulties.length) {
                    desc.append(" (").append(difficulties[botDifficulty]).append(")");
                }
            }
        } else {
            desc.append(" (").append(playersCount).append(" joueurs)");
        }

        return desc.toString();
    }

    /**
     * Obtient le résultat de la partie formaté
     */
    public String getResultText() {
        if (won) {
            return "🏆 VICTOIRE";
        } else if ("Égalité".equals(winnerName)) {
            return "🤝 ÉGALITÉ";
        } else {
            return "💀 DÉFAITE";
        }
    }

    /**
     * Obtient la durée formatée
     */
    public String getFormattedDuration() {
        if (durationSeconds < 60) {
            return durationSeconds + "s";
        } else {
            long minutes = durationSeconds / 60;
            long seconds = durationSeconds % 60;
            return String.format("%dm %02ds", minutes, seconds);
        }
    }

    /**
     * Obtient la date formatée
     */
    public String getFormattedDate() {
        if (endTime != null) {
            return endTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "Inconnue";
    }

    /**
     * Calcule un score de performance (pour classement)
     */
    public int getPerformanceScore() {
        int score = 0;

        // Points pour la victoire
        if (won) score += 100;

        // Points pour les éliminations
        score += eliminationsDealt * 25;

        // Points pour survivre
        if (deaths == 0) score += 50;

        // Bonus pour bot difficile
        if (isBotGame && won) {
            score += botDifficulty * 20;
        }

        // Bonus pour parties longues
        if (durationSeconds > 120) score += 10;

        // Bonus CTF
        if (gameMode == GameMode.CAPTURE_THE_FLAG) {
            score += flagsCaptured * 30;
        }

        return score;
    }

    // ============================================================================
    // GETTERS ET SETTERS
    // ============================================================================

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public boolean isBotGame() {
        return isBotGame;
    }

    public void setBotGame(boolean botGame) {
        this.isBotGame = botGame;
    }

    public int getBotDifficulty() {
        return botDifficulty;
    }

    public void setBotDifficulty(int botDifficulty) {
        this.botDifficulty = botDifficulty;
    }

    public int getBombsPlaced() {
        return bombsPlaced;
    }

    public void setBombsPlaced(int bombsPlaced) {
        this.bombsPlaced = bombsPlaced;
    }

    public int getEliminationsDealt() {
        return eliminationsDealt;
    }

    public void setEliminationsDealt(int eliminationsDealt) {
        this.eliminationsDealt = eliminationsDealt;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getFlagsCaptured() {
        return flagsCaptured;
    }

    public void setFlagsCaptured(int flagsCaptured) {
        this.flagsCaptured = flagsCaptured;
    }

    public int getPlayersCount() {
        return playersCount;
    }

    public void setPlayersCount(int playersCount) {
        this.playersCount = playersCount;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getMapSize() {
        return mapSize;
    }

    public void setMapSize(String mapSize) {
        this.mapSize = mapSize;
    }
}