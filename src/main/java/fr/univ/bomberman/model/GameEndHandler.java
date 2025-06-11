// FILE: src/main/java/fr/univ/bomberman/util/GameEndHandler.java
package fr.univ.bomberman.model;

import fr.univ.bomberman.controller.GameModeController;
import fr.univ.bomberman.model.*;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.utils.ProfileManager;

import java.time.LocalDateTime;

/**
 * Gestionnaire pour traiter la fin des parties et mettre à jour les profils
 */
public class GameEndHandler {

    /**
     * Traite la fin d'une partie et met à jour les statistiques du profil
     */
    public static void handleGameEnd(Game game, String playerName, long gameDurationSeconds) {
        try {
            // Récupérer le profil actuel
            PlayerProfile currentProfile = GameModeController.getCurrentGameProfile();

            if (currentProfile == null) {
                System.out.println("⚠️ Aucun profil sélectionné, pas de mise à jour des statistiques");
                return;
            }

            // Vérifier que c'est bien le bon joueur
            if (!currentProfile.getPlayerName().equals(playerName)) {
                System.out.println("⚠️ Le joueur de la partie ne correspond pas au profil sélectionné");
                return;
            }

            // Déterminer le résultat de la partie
            GameResult result = determineGameResult(game, playerName);

            // Créer une session de jeu
            GameSession session = createGameSession(game, result, gameDurationSeconds);

            // Mettre à jour le profil avec la session
            updateProfileWithSession(currentProfile, session, game.getGameMode());

            // Sauvegarder le profil
            ProfileManager.getInstance().saveProfile(currentProfile);

            // Afficher un résumé
            displayGameSummary(currentProfile, result, session);

            System.out.println("✅ Statistiques mises à jour pour " + playerName);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Détermine le résultat de la partie pour le joueur
     */
    private static GameResult determineGameResult(Game game, String playerName) {
        if (!game.isGameOver()) {
            return GameResult.FORFEIT; // Partie abandonnée
        }

        Player winner = game.getWinner();

        if (winner == null) {
            return GameResult.TIE; // Égalité (tous éliminés en même temps)
        }

        if (winner.getName().equals(playerName)) {
            return GameResult.WIN; // Victoire
        } else {
            return GameResult.LOSE; // Défaite
        }
    }

    /**
     * Crée une session de jeu à partir des données de la partie
     */
    private static GameSession createGameSession(Game game, GameResult result, long durationSeconds) {
        GameSession session = new GameSession();

        session.setGameMode(game.getGameMode());
        session.setWon(result.isWin());
        session.setDurationSeconds((int) durationSeconds);
        session.setEndTime(LocalDateTime.now());

        // Statistiques approximatives (vous pouvez les améliorer en trackant plus de données)
        session.setBombsPlaced(estimateBombsPlaced(game));
        session.setEliminationsDealt(estimateEliminations(game, result));
        session.setDeaths(result.isLoss() ? 1 : 0);

        return session;
    }

    /**
     * Estime le nombre de bombes posées (basé sur la durée et le mode)
     */
    private static int estimateBombsPlaced(Game game) {
        // Estimation simple : 1 bombe toutes les 15-20 secondes
        int bombCount = game.getActiveBombs().size(); // Bombes actuellement actives
        return Math.max(bombCount, 1); // Au minimum 1 bombe
    }

    /**
     * Estime le nombre d'éliminations (basé sur le résultat et le nombre de joueurs)
     */
    private static int estimateEliminations(Game game, GameResult result) {
        if (!result.isWin()) {
            return 0; // Pas d'éliminations si on a perdu
        }

        // Si on a gagné, on a éliminé au moins 1 joueur
        int aliveCount = game.getAlivePlayerCount();
        int totalPlayers = game.getPlayerCount();
        return Math.max(totalPlayers - aliveCount - 1, 1);
    }

    /**
     * Met à jour le profil avec les données de la session
     */
    private static void updateProfileWithSession(PlayerProfile profile, GameSession session, GameMode gameMode) {
        // Statistiques générales
        profile.setTotalGamesPlayed(profile.getTotalGamesPlayed() + 1);

        if (session.isWon()) {
            profile.setTotalWins(profile.getTotalWins() + 1);
        } else {
            profile.setTotalLosses(profile.getTotalLosses() + 1);
        }

        profile.setTotalBombsPlaced(profile.getTotalBombsPlaced() + session.getBombsPlaced());
        profile.setTotalEliminatonsDealt(profile.getTotalEliminatonsDealt() + session.getEliminationsDealt());
        profile.setTotalDeaths(profile.getTotalDeaths() + session.getDeaths());
        profile.setTotalPlayTimeSeconds(profile.getTotalPlayTimeSeconds() + session.getDurationSeconds());

        // Statistiques par mode de jeu
        GameModeStats modeStats = profile.getStatsForMode(gameMode);
        if (modeStats != null) {
            modeStats.addGame(session.isWon());
        }

        // Mettre à jour la date de dernière partie
        profile.setLastPlayDate(LocalDateTime.now());

        // Mettre à jour le rang
        profile.updateRank();
    }

    /**
     * Affiche un résumé de la partie et des nouvelles statistiques
     */
    private static void displayGameSummary(PlayerProfile profile, GameResult result, GameSession session) {
        System.out.println("🎮 === RÉSUMÉ DE LA PARTIE ===");
        System.out.println("👤 Joueur: " + profile.getPlayerName());
        System.out.println("🏆 Résultat: " + result.getDisplayName());
        System.out.println("⏱️ Durée: " + session.getDurationSeconds() + " secondes");
        System.out.println("💣 Bombes posées: " + session.getBombsPlaced());
        System.out.println("🎯 Éliminations: " + session.getEliminationsDealt());
        System.out.println("💀 Morts: " + session.getDeaths());
        System.out.println("");
        System.out.println("📊 === NOUVELLES STATISTIQUES ===");
        System.out.println("🎮 Total parties: " + profile.getTotalGamesPlayed());
        System.out.println("🏆 Victoires: " + profile.getTotalWins());
        System.out.println("💀 Défaites: " + profile.getTotalLosses());
        System.out.println("📈 Taux de victoire: " + String.format("%.1f%%", profile.getWinRatio()));
        System.out.println("🥇 Rang: " + profile.getRank().getDisplayName());

        // Vérifier promotion de rang
        if (result.isWin() && profile.canBePromoted()) {
            System.out.println("🎉 PROMOTION ! Nouveau rang disponible !");
        }

        System.out.println("===============================");
    }

    /**
     * Méthode simple pour les parties rapides (sans Game object)
     */
    public static void handleSimpleGameEnd(String playerName, GameResult result, GameMode gameMode, long durationSeconds) {
        try {
            PlayerProfile currentProfile = GameModeController.getCurrentGameProfile();

            if (currentProfile == null || !currentProfile.getPlayerName().equals(playerName)) {
                return;
            }

            // Mise à jour simple
            currentProfile.setTotalGamesPlayed(currentProfile.getTotalGamesPlayed() + 1);

            if (result.isWin()) {
                currentProfile.setTotalWins(currentProfile.getTotalWins() + 1);
            } else if (result.isLoss()) {
                currentProfile.setTotalLosses(currentProfile.getTotalLosses() + 1);
            }

            // Ajout approximatif du temps
            currentProfile.setTotalPlayTimeSeconds(currentProfile.getTotalPlayTimeSeconds() + durationSeconds);

            // Mettre à jour la date
            currentProfile.setLastPlayDate(LocalDateTime.now());

            // Mettre à jour le rang
            currentProfile.updateRank();

            // Sauvegarder
            ProfileManager.getInstance().saveProfile(currentProfile);

            System.out.println("✅ Statistiques mises à jour (mode simple) pour " + playerName);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour simple: " + e.getMessage());
        }
    }
}