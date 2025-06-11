// FILE: src/main/java/fr/univ/bomberman/controller/MenuController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.BombermanApp;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.util.ProfileManager; // ✅ IMPORT AJOUTÉ

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.List;     // ✅ IMPORT AJOUTÉ
import java.util.Optional;

/**
 * Contrôleur pour le menu principal avec support des modes de jeu.
 */
public class MenuController {

    private BombermanApp bombermanApp;
    private String player1Name = "Joueur 1";
    private String player2Name = "Joueur 2";

    // ✅ AJOUT DES RÉFÉRENCES AUX NOUVEAUX BOUTONS
    @FXML
    private Button ctfButton;

    @FXML
    private Button botButton;

    @FXML
    private Button fourPlayerButton;

    /**
     * Définit la référence vers l'application principale
     */
    public void setBombermanApp(BombermanApp app) {
        this.bombermanApp = app;
    }

    // ============================================================================
    // ✅ NOUVELLES MÉTHODES POUR LES BOUTONS DU MENU FXML
    // ============================================================================

    /**
     * ✅ NOUVELLE MÉTHODE: Lance le mode Capture the Flag (appelée par le bouton CTF)
     */
    @FXML
    private void onStartCTF(ActionEvent event) {
        try {
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("🏁 CAPTURE THE FLAG");
            infoAlert.setHeaderText("Mode de jeu stratégique !");
            infoAlert.setContentText("🎯 OBJECTIF: Capturez TOUS les drapeaux adverses !\n\n" +
                    "📋 RÈGLES:\n" +
                    "• Chaque joueur place son drapeau au début\n" +
                    "• Ramassez les drapeaux ennemis en marchant dessus\n" +
                    "• Les joueurs éliminés peuvent encore bombarder !\n" +
                    "• Premier à capturer tous les drapeaux gagne\n\n" +
                    "🚀 Prêt pour la stratégie ultime ?");

            Optional<ButtonType> result = infoAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                startCTFGame();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur CTF", "Impossible de lancer le mode CTF: " + e.getMessage());
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Lance le mode contre bot (appelée par le bouton Bot)
     */
    @FXML
    private void onStartBotGame(ActionEvent event) {
        try {
            // Demander le nom du joueur
            TextInputDialog nameDialog = new TextInputDialog("Joueur");
            nameDialog.setTitle("Mode Bot - Nom du joueur");
            nameDialog.setHeaderText("🤖 Affrontez l'Intelligence Artificielle !");
            nameDialog.setContentText("Votre nom:");

            Optional<String> nameResult = nameDialog.showAndWait();
            if (!nameResult.isPresent() || nameResult.get().trim().isEmpty()) {
                return; // Annulé
            }

            String playerName = nameResult.get().trim();
            if (playerName.length() > 15) {
                showError("Nom trop long", "Maximum 15 caractères");
                return;
            }

            // Choisir la difficulté du bot
            Alert difficultyAlert = new Alert(Alert.AlertType.CONFIRMATION);
            difficultyAlert.setTitle("Difficulté du Bot");
            difficultyAlert.setHeaderText("🤖 Choisissez votre adversaire IA");
            difficultyAlert.setContentText("Quel niveau de défi voulez-vous ?");

            ButtonType easyButton = new ButtonType("😊 Facile");
            ButtonType mediumButton = new ButtonType("😐 Moyen");
            ButtonType hardButton = new ButtonType("😈 Difficile");
            ButtonType cancelButton = new ButtonType("Annuler");

            difficultyAlert.getButtonTypes().setAll(easyButton, mediumButton, hardButton, cancelButton);

            Optional<ButtonType> difficultyResult = difficultyAlert.showAndWait();
            if (!difficultyResult.isPresent()) {
                return; // Annulé
            }

            int botDifficulty = 2; // Moyen par défaut
            String difficultyName = "Moyen";

            if (difficultyResult.get() == easyButton) {
                botDifficulty = 1;
                difficultyName = "Facile";
            } else if (difficultyResult.get() == mediumButton) {
                botDifficulty = 2;
                difficultyName = "Moyen";
            } else if (difficultyResult.get() == hardButton) {
                botDifficulty = 3;
                difficultyName = "Difficile";
            } else {
                return; // Annulé
            }

            // Confirmation finale
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("🤖 Défi contre l'IA");
            confirmAlert.setHeaderText("Prêt pour le combat ?");
            confirmAlert.setContentText("👤 " + playerName + "\n" +
                    "        VS\n" +
                    "🤖 Bot " + difficultyName + "\n\n" +
                    getBotDescription(botDifficulty) + "\n\n" +
                    "Commencer le duel ?");

            Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                if (bombermanApp != null) {
                    bombermanApp.startBotGame(playerName, botDifficulty);
                } else {
                    showError("Erreur", "Référence vers l'application non trouvée");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de lancer le mode bot: " + e.getMessage());
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Lance le mode 4 joueurs (appelée par le bouton 4 joueurs)
     */
    @FXML
    private void onStartFourPlayer(ActionEvent event) {
        try {
            Alert introAlert = new Alert(Alert.AlertType.INFORMATION);
            introAlert.setTitle("⚔️ BATAILLE ROYALE 4 JOUEURS ⚔️");
            introAlert.setHeaderText("Mode de combat ultime !");
            introAlert.setContentText("🔥 Affrontement à 4 joueurs !\n" +
                    "🏆 Dernier survivant remporte tout !\n" +
                    "💣 Bombes mortelles pour tous !\n\n" +
                    "Prêt pour la bataille ?");

            Optional<ButtonType> result = introAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                start4PlayerGame();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de lancer le mode 4 joueurs: " + e.getMessage());
        }
    }

    // ============================================================================
    // ✅ MÉTHODES EXISTANTES (conservées)
    // ============================================================================

    @FXML
    private void onStartGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/game/mode_selection.fxml"));
            Parent root = loader.load();

            // Injecter la référence à l'application
            GameModeController controller = loader.getController();
            if (controller != null) {
                controller.setBombermanApp(bombermanApp);
            }

            // Charger le CSS si disponible
            Scene scene = new Scene(root);
            try {
                scene.getStylesheets().add(getClass().getResource("/fr/univ/bomberman/css/game/mode_selection.css").toExternalForm());
            } catch (Exception cssEx) {
                System.out.println("CSS du menu de sélection non trouvé, utilisation du style par défaut");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Super Bomberman - Sélection du Mode");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le menu de sélection: " + e.getMessage());
        }
    }

    /**
     * Configuration et lancement du jeu CTF
     */
    private void startCTFGame() {
        try {
            // Demander le nombre de joueurs
            Alert playerCountAlert = new Alert(Alert.AlertType.CONFIRMATION);
            playerCountAlert.setTitle("Nombre de joueurs CTF");
            playerCountAlert.setHeaderText("🏁 Combien de joueurs pour le CTF ?");
            playerCountAlert.setContentText("Plus il y a de joueurs, plus c'est stratégique !");

            ButtonType twoPlayers = new ButtonType("👥 2 Joueurs");
            ButtonType threePlayers = new ButtonType("👥👤 3 Joueurs");
            ButtonType fourPlayers = new ButtonType("👥👥 4 Joueurs");
            ButtonType cancel = new ButtonType("Annuler");

            playerCountAlert.getButtonTypes().setAll(twoPlayers, threePlayers, fourPlayers, cancel);

            Optional<ButtonType> countResult = playerCountAlert.showAndWait();
            if (!countResult.isPresent() || countResult.get() == cancel) {
                return;
            }

            int playerCount = 2;
            if (countResult.get() == threePlayers) playerCount = 3;
            else if (countResult.get() == fourPlayers) playerCount = 4;

            // Obtenir les noms des joueurs
            String[] playerNames = getCTFPlayerNames(playerCount);
            if (playerNames == null) return; // Annulé

            // Confirmer le lancement
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("🏁 Lancement CTF");
            confirmAlert.setHeaderText("Capture the Flag - Configuration finale");

            StringBuilder content = new StringBuilder();
            content.append("🎮 Mode: Capture the Flag\n");
            content.append("👥 Joueurs: ").append(playerCount).append("\n\n");

            String[] emojis = {"🔴", "🔵", "🟡", "🟢"};
            String[] controls = {"ZQSD + A", "↑↓←→ + ENTRÉE", "IJKL + U", "8456 + 7"};

            for (int i = 0; i < playerCount; i++) {
                content.append(emojis[i]).append(" ").append(playerNames[i])
                        .append(" (").append(controls[i]).append(")\n");
            }

            content.append("\n🎯 Capturez tous les drapeaux pour gagner !");
            content.append("\n💀 Les éliminés peuvent encore bombarder !");

            confirmAlert.setContentText(content.toString());

            Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                if (bombermanApp != null) {
                    bombermanApp.startCTFGame(playerNames);
                } else {
                    showError("Erreur", "Référence vers l'application non trouvée");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur CTF", "Erreur lors du lancement CTF: " + e.getMessage());
        }
    }

    /**
     * Obtient les noms des joueurs pour CTF
     */
    private String[] getCTFPlayerNames(int playerCount) {
        String[] names = new String[playerCount];
        String[] defaultNames = {"Stratège", "Tacticien", "Commandant", "Général"};
        String[] descriptions = {
                "🔴 Joueur 1 (ZQSD + A)",
                "🔵 Joueur 2 (↑↓←→ + ENTRÉE)",
                "🟡 Joueur 3 (IJKL + U)",
                "🟢 Joueur 4 (8456 + 7)"
        };

        for (int i = 0; i < playerCount; i++) {
            TextInputDialog dialog = new TextInputDialog(defaultNames[i]);
            dialog.setTitle("CTF - Joueur " + (i + 1));
            dialog.setHeaderText(descriptions[i]);
            dialog.setContentText("Nom du stratège:");

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return null; // Annulé
            }

            String name = result.get().trim();
            if (name.isEmpty()) {
                showError("Nom invalide", "Le nom ne peut pas être vide !");
                i--; // Recommencer ce joueur
                continue;
            }

            if (name.length() > 15) {
                showError("Nom trop long", "Maximum 15 caractères !");
                i--; // Recommencer ce joueur
                continue;
            }

            names[i] = name;
        }

        // Vérifier l'unicité des noms
        for (int i = 0; i < playerCount; i++) {
            for (int j = i + 1; j < playerCount; j++) {
                if (names[i].equals(names[j])) {
                    showError("Noms identiques", "Tous les joueurs doivent avoir des noms différents !");
                    return getCTFPlayerNames(playerCount); // Recommencer
                }
            }
        }

        return names;
    }

    /**
     * Lance une partie 4 joueurs
     */
    private void start4PlayerGame() {
        try {
            // Demander les noms des 4 joueurs
            String[] playerNames = get4PlayerNames();
            if (playerNames == null) return; // Annulé

            // Confirmer le lancement
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Bataille Royale 4 Joueurs");
            confirmAlert.setHeaderText("⚔️ PRÊT POUR LA BATAILLE ? ⚔️");
            confirmAlert.setContentText("Joueurs:\n" +
                    "🔴 " + playerNames[0] + " (ZQSD + A)\n" +
                    "🔵 " + playerNames[1] + " (↑↓←→ + ENTRÉE)\n" +
                    "🟡 " + playerNames[2] + " (IJKL + U)\n" +
                    "🟢 " + playerNames[3] + " (8456 + 7)\n\n" +
                    "Dernier survivant remporte tout !");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (bombermanApp != null) {
                    bombermanApp.startFourPlayerGame(playerNames);
                } else {
                    showError("Erreur", "Référence vers l'application principale non trouvée");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors du lancement du jeu 4 joueurs: " + e.getMessage());
        }
    }

    private String[] get4PlayerNames() {
        String[] names = new String[4];
        String[] defaultNames = {"Alex", "Blake", "Charlie", "Dana"};
        String[] descriptions = {
                "🔴 Joueur 1 (ZQSD + A)",
                "🔵 Joueur 2 (↑↓←→ + ENTRÉE)",
                "🟡 Joueur 3 (IJKL + U)",
                "🟢 Joueur 4 (8456 + 7)"
        };

        for (int i = 0; i < 4; i++) {
            TextInputDialog dialog = new TextInputDialog(defaultNames[i]);
            dialog.setTitle("Super Bomberman - Joueur " + (i + 1));
            dialog.setHeaderText(descriptions[i]);
            dialog.setContentText("Nom:");

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return null; // Annulé
            }

            String name = result.get().trim();
            if (name.isEmpty()) {
                showError("Nom invalide", "Le nom ne peut pas être vide !");
                i--; // Recommencer ce joueur
                continue;
            }

            if (name.length() > 15) {
                showError("Nom trop long", "Le nom ne peut pas dépasser 15 caractères !");
                i--; // Recommencer ce joueur
                continue;
            }

            names[i] = name;
        }

        // Vérifier que tous les noms sont différents
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (names[i].equals(names[j])) {
                    showError("Noms identiques", "Tous les joueurs doivent avoir des noms différents !");
                    return get4PlayerNames(); // Recommencer complètement
                }
            }
        }

        return names;
    }

    private String getBotDescription(int difficulty) {
        switch (difficulty) {
            case 1:
                return "🟢 IA Débutante:\n" +
                        "• Mouvements aléatoires\n" +
                        "• Bombes occasionnelles\n" +
                        "• Réactions lentes";

            case 2:
                return "🟡 IA Équilibrée:\n" +
                        "• Stratégie de base\n" +
                        "• Fuit les dangers\n" +
                        "• Cible les briques";

            case 3:
                return "🔴 IA Redoutable:\n" +
                        "• Stratégie avancée\n" +
                        "• Vous traque activement\n" +
                        "• Réactions rapides\n" +
                        "• Bombes tactiques";

            default:
                return "IA de niveau moyen";
        }
    }

    // ============================================================================
    // ✅ MÉTHODES EXISTANTES CONSERVÉES
    // ============================================================================

    @FXML
    private void onProfile() {
        try {
            // ✅ MODIFICATION: Demander le nom du joueur d'abord
            TextInputDialog dialog = new TextInputDialog("Joueur1");
            dialog.setTitle("Profil");
            dialog.setHeaderText("Gestion de profil");
            dialog.setContentText("Entrez votre nom de joueur:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String playerName = result.get().trim();

                // Charger et afficher les statistiques
                openProfileStats(playerName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le profil");
        }
    }

    // ✅ AJOUT DES MÉTHODES MANQUANTES POUR LES PROFILS
    private void openProfileStats(String playerName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/stats.fxml"));
            Parent root = loader.load();

            ProfileStatsController controller = loader.getController();
            controller.loadProfile(playerName);

            Stage stage = new Stage();
            stage.setTitle("Profil - " + playerName);
            stage.setScene(new Scene(root, 500, 500));
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les statistiques");
        }
    }

    /**
     * ✅ MÉTHODE CORRIGÉE: Sélection de profil existant
     */
    @FXML
    private void onSelectProfile() {
        try {
            ProfileManager profileManager = ProfileManager.getInstance();
            List<String> profiles = profileManager.listProfiles();

            if (profiles.isEmpty()) {
                showInfo("Aucun profil", "Aucun profil trouvé. Créez-en un nouveau.");
                onProfile(); // Rediriger vers création de profil
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(profiles.get(0), profiles);
            dialog.setTitle("Sélection de profil");
            dialog.setHeaderText("Choisissez votre profil");
            dialog.setContentText("Profil:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                openProfileStats(result.get());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la liste des profils");
        }
    }

    /**
     * ✅ MÉTHODE CORRIGÉE: Statistiques globales
     */
    @FXML
    private void onGlobalStats() {
        try {
            ProfileManager profileManager = ProfileManager.getInstance();
            ProfileManager.ProfileStats globalStats = profileManager.getGlobalStats();

            StringBuilder message = new StringBuilder();
            message.append("📊 STATISTIQUES GLOBALES\n\n");
            message.append("👥 Profils créés: ").append(globalStats.getTotalProfiles()).append("\n");
            message.append("🎮 Parties totales: ").append(globalStats.getTotalGamesPlayed()).append("\n");
            message.append("🏆 Victoires totales: ").append(globalStats.getTotalWins()).append("\n");
            message.append("📈 Taux de victoire global: ").append(String.format("%.1f%%", globalStats.getGlobalWinRate())).append("\n");
            message.append("⏱️ Temps de jeu total: ").append(globalStats.getFormattedTotalPlayTime()).append("\n");
            message.append("🔥 Joueur le plus actif: ").append(globalStats.getMostActivePlayer())
                    .append(" (").append(globalStats.getMostGamesPlayed()).append(" parties)");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Statistiques Globales");
            alert.setHeaderText("Récapitulatif de tous les profils");
            alert.setContentText(message.toString());
            alert.getDialogPane().setPrefWidth(400);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les statistiques globales");
        }
    }

    @FXML
    private void onQuit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Quitter");
        confirmAlert.setHeaderText("Confirmation");
        confirmAlert.setContentText("Tu vas où comme ça ? " + "\n" + " Reste ici");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void onPlayerNames(ActionEvent event) {
        try {
            TextInputDialog dialog1 = new TextInputDialog(player1Name);
            dialog1.setTitle("Super Bomberman - Nom du Joueur 1");
            dialog1.setHeaderText("🔵 Joueur 1 (ZQSD + ESPACE)");
            dialog1.setContentText("Entrez le nom du joueur 1:");

            Optional<String> result1 = dialog1.showAndWait();
            if (result1.isPresent() && !result1.get().trim().isEmpty()) {
                String newName1 = result1.get().trim();

                if (newName1.length() > 15) {
                    showError("Nom trop long", "Le nom ne peut pas dépasser 15 caractères.");
                    return;
                }

                TextInputDialog dialog2 = new TextInputDialog(player2Name);
                dialog2.setTitle("Super Bomberman - Nom du Joueur 2");
                dialog2.setHeaderText("🟢 Joueur 2 (Flèches + ENTRÉE)");
                dialog2.setContentText("Entrez le nom du joueur 2:");

                Optional<String> result2 = dialog2.showAndWait();
                if (result2.isPresent() && !result2.get().trim().isEmpty()) {
                    String newName2 = result2.get().trim();

                    if (newName2.length() > 15) {
                        showError("Nom trop long", "Le nom ne peut pas dépasser 15 caractères.");
                        return;
                    }

                    if (newName1.equals(newName2)) {
                        showError("Noms identiques", "Les deux joueurs ne peuvent pas avoir le même nom.");
                        return;
                    }

                    player1Name = newName1;
                    player2Name = newName2;

                    Alert confirmAlert = new Alert(Alert.AlertType.INFORMATION);
                    confirmAlert.setTitle("Noms mis à jour");
                    confirmAlert.setHeaderText("✅ Noms des joueurs modifiés");
                    confirmAlert.setContentText("🔵 Joueur 1: " + player1Name + "\n" +
                            "🟢 Joueur 2: " + player2Name + "\n\n" +
                            "Les nouveaux noms seront utilisés lors de la prochaine partie.");
                    confirmAlert.showAndWait();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la configuration des noms: " + e.getMessage());
        }
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ MÉTHODE MANQUANTE AJOUTÉE
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }
}