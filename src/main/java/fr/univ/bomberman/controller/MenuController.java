// FILE: src/main/java/fr/univ/bomberman/controller/MenuController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.BombermanApp;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.utils.ProfileManager;
import fr.univ.bomberman.model.PlayerProfile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour le menu principal avec support avancé des profils.
 */
public class MenuController {

    private BombermanApp bombermanApp;
    private String player1Name = "Joueur 1";
    private String player2Name = "Joueur 2";
    private PlayerProfile currentProfile; // ✅ NOUVEAU: Profil actuellement sélectionné

    // ✅ AJOUT DES RÉFÉRENCES AUX NOUVEAUX BOUTONS
    @FXML private Button ctfButton;
    @FXML private Button botButton;
    @FXML private Button fourPlayerButton;
    @FXML private Button profileButton;
    @FXML private Button selectProfileButton;
    @FXML private Button globalStatsButton;

    // ✅ NOUVEAU: Label pour afficher le profil actuel
    @FXML private Label currentProfileLabel;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        updateCurrentProfileDisplay();
        System.out.println("MenuController initialisé avec gestion avancée des profils");
    }

    /**
     * Définit la référence vers l'application principale
     */
    public void setBombermanApp(BombermanApp app) {
        this.bombermanApp = app;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Met à jour l'affichage du profil actuel
     */
    private void updateCurrentProfileDisplay() {
        if (currentProfileLabel != null) {
            if (currentProfile != null) {
                currentProfileLabel.setText("👤 " + currentProfile.getPlayerName() +
                        " (" + currentProfile.getRank().getDisplayName() + ")");
                currentProfileLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                currentProfileLabel.setText("👤 Aucun profil sélectionné");
                currentProfileLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        }
    }

    /**
     * ✅ MÉTHODE AMÉLIORÉE: Gestion de profil avec nouvelle interface
     */
    @FXML
    private void onProfile() {
        openProfileSelection();
    }

    /**
     * ✅ MÉTHODE AMÉLIORÉE: Sélection de profil avec nouvelle interface
     */
    @FXML
    private void onSelectProfile() {
        openProfileSelection();
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Ouvre l'interface de sélection de profils
     */
    private void openProfileSelection() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/profile_selection.fxml"));
            Parent root = loader.load();

            ProfileSelectionController controller = loader.getController();
            controller.setBombermanApp(bombermanApp);

            Stage stage = new Stage();
            stage.setTitle("🎮 Gestion des Profils - Super Bomberman");
            stage.setScene(new Scene(root, 700, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);

            // Centrer la fenêtre
            if (bombermanApp != null && bombermanApp.getPrimaryStage() != null) {
                Stage primaryStage = bombermanApp.getPrimaryStage();
                stage.initOwner(primaryStage);
                stage.setX(primaryStage.getX() + (primaryStage.getWidth() - 900) / 2);
                stage.setY(primaryStage.getY() + (primaryStage.getHeight() - 600) / 2);
            }

            stage.showAndWait();

            // Récupérer le profil sélectionné
            if (controller.isProfileSelected()) {
                currentProfile = controller.getSelectedProfile();
                updateCurrentProfileDisplay();

                // Message de confirmation
                showInfo("Profil sélectionné",
                        "✅ Profil actif: " + currentProfile.getPlayerName() + "\n" +
                                "🏆 Rang: " + currentProfile.getRank().getDisplayName() + "\n" +
                                "🎮 Parties jouées: " + currentProfile.getTotalGamesPlayed() + "\n" +
                                "📊 Taux de victoire: " + String.format("%.1f%%", currentProfile.getWinRatio()));

                // Appliquer les préférences du profil
                applyProfilePreferences();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la gestion des profils: " + e.getMessage());
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Applique les préférences du profil sélectionné
     */
    private void applyProfilePreferences() {
        if (currentProfile == null) return;

        try {
            // Mettre à jour les noms par défaut avec le profil
            player1Name = currentProfile.getPlayerName();
            player2Name = "Adversaire"; // Nom par défaut pour le second joueur

            // Autres préférences pourraient être appliquées ici
            // (thème, son, etc.)

            System.out.println("Préférences appliquées pour: " + currentProfile.getPlayerName());

        } catch (Exception e) {
            System.err.println("Erreur lors de l'application des préférences: " + e.getMessage());
        }
    }

    /**
     * ✅ MÉTHODE CORRIGÉE: Statistiques globales améliorées
     */
    @FXML
    private void onGlobalStats() {
        try {
            ProfileManager profileManager = ProfileManager.getInstance();
            List<String> profiles = profileManager.listProfiles();

            if (profiles.isEmpty()) {
                showInfo("Aucune statistique",
                        "🚫 Aucun profil trouvé\n\n" +
                                "Créez des profils et jouez des parties pour générer des statistiques.");
                return;
            }

            ProfileManager.ProfileStats globalStats = profileManager.getGlobalStats();

            StringBuilder message = new StringBuilder();
            message.append("📊 STATISTIQUES GLOBALES BOMBERMAN\n");
            message.append("═══════════════════════════════════\n\n");

            message.append("👥 Profils créés: ").append(globalStats.getTotalProfiles()).append("\n");
            message.append("🎮 Parties totales: ").append(globalStats.getTotalGamesPlayed()).append("\n");
            message.append("🏆 Victoires totales: ").append(globalStats.getTotalWins()).append("\n");
            message.append("📈 Taux de victoire global: ").append(String.format("%.1f%%", globalStats.getGlobalWinRate())).append("\n");
            message.append("⏱️ Temps de jeu total: ").append(globalStats.getFormattedTotalPlayTime()).append("\n\n");

            message.append("🔥 TOP JOUEUR:\n");
            message.append("👤 ").append(globalStats.getMostActivePlayer()).append("\n");
            message.append("🎮 ").append(globalStats.getMostGamesPlayed()).append(" parties jouées\n\n");

            // Ajouter le top 3 si plusieurs profils
            if (profiles.size() > 1) {
                message.append("🏆 CLASSEMENT PAR TAUX DE VICTOIRE:\n");

                // Charger et trier les profils par taux de victoire
                profiles.stream()
                        .map(name -> {
                            try {
                                return profileManager.loadProfile(name);
                            } catch (BombermanException e) {
                                return null;
                            }
                        })
                        .filter(profile -> profile != null && profile.getTotalGamesPlayed() > 0)
                        .sorted((p1, p2) -> Double.compare(p2.getWinRatio(), p1.getWinRatio()))
                        .limit(3)
                        .forEach(profile -> {
                            String rank = "🥇";
                            if (message.toString().contains("🥇")) rank = "🥈";
                            if (message.toString().contains("🥈")) rank = "🥉";

                            message.append(rank).append(" ").append(profile.getPlayerName())
                                    .append(" - ").append(String.format("%.1f%%", profile.getWinRatio()))
                                    .append(" (").append(profile.getTotalGamesPlayed()).append(" parties)\n");
                        });
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("📊 Statistiques Globales");
            alert.setHeaderText("Récapitulatif de tous les profils");
            alert.setContentText(message.toString());
            alert.getDialogPane().setPrefWidth(500);
            alert.getDialogPane().setPrefHeight(400);
            alert.setResizable(true);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les statistiques globales: " + e.getMessage());
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Obtient le nom du joueur (depuis profil ou saisie)
     */
    private String getPlayerNameForGame() {
        if (currentProfile != null) {
            // Utiliser le profil sélectionné
            return currentProfile.getPlayerName();
        } else {
            // Demander le nom
            TextInputDialog nameDialog = new TextInputDialog("Joueur");
            nameDialog.setTitle("Nom du joueur");
            nameDialog.setHeaderText("💡 Conseil: Sélectionnez un profil pour sauvegarder vos statistiques !");
            nameDialog.setContentText("Votre nom:");

            Optional<String> nameResult = nameDialog.showAndWait();
            if (!nameResult.isPresent() || nameResult.get().trim().isEmpty()) {
                return null; // Annulé
            }

            String playerName = nameResult.get().trim();
            if (playerName.length() > 15) {
                showError("Nom trop long", "Maximum 15 caractères");
                return null;
            }

            return playerName;
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Lance CTF avec gestion de profil
     */
    private void startCTFGameWithProfile() {
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

            // Obtenir les noms des joueurs (avec profil pour le premier)
            String[] playerNames = getCTFPlayerNamesWithProfile(playerCount);
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
                content.append(emojis[i]).append(" ").append(playerNames[i]);
                if (i == 0 && currentProfile != null) {
                    content.append(" (👤 Profil)");
                }
                content.append(" (").append(controls[i]).append(")\n");
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
     * ✅ NOUVELLE MÉTHODE: Lance 4 joueurs avec gestion de profil
     */
    private void start4PlayerGameWithProfile() {
        try {
            // Demander les noms des 4 joueurs (avec profil pour le premier)
            String[] playerNames = get4PlayerNamesWithProfile();
            if (playerNames == null) return; // Annulé

            // Confirmer le lancement
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Bataille Royale 4 Joueurs");
            confirmAlert.setHeaderText("⚔️ PRÊT POUR LA BATAILLE ? ⚔️");

            StringBuilder content = new StringBuilder();
            content.append("Joueurs:\n");
            content.append("🔴 ").append(playerNames[0]);
            if (currentProfile != null) content.append(" (👤 Profil)");
            content.append(" (ZQSD + A)\n");
            content.append("🔵 ").append(playerNames[1]).append(" (↑↓←→ + ENTRÉE)\n");
            content.append("🟡 ").append(playerNames[2]).append(" (IJKL + U)\n");
            content.append("🟢 ").append(playerNames[3]).append(" (8456 + 7)\n\n");
            content.append("Dernier survivant remporte tout !");

            confirmAlert.setContentText(content.toString());

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

    /**
     * ✅ NOUVELLE MÉTHODE: Obtient les noms pour CTF avec profil
     */
    private String[] getCTFPlayerNamesWithProfile(int playerCount) {
        String[] names = new String[playerCount];
        String[] defaultNames = {"Stratège", "Tacticien", "Commandant", "Général"};
        String[] descriptions = {
                "🔴 Joueur 1 (ZQSD + A)",
                "🔵 Joueur 2 (↑↓←→ + ENTRÉE)",
                "🟡 Joueur 3 (IJKL + U)",
                "🟢 Joueur 4 (8456 + 7)"
        };

        for (int i = 0; i < playerCount; i++) {
            if (i == 0 && currentProfile != null) {
                // Utiliser le profil pour le premier joueur
                names[i] = currentProfile.getPlayerName();
            } else {
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
        }

        // Vérifier l'unicité des noms
        for (int i = 0; i < playerCount; i++) {
            for (int j = i + 1; j < playerCount; j++) {
                if (names[i].equals(names[j])) {
                    showError("Noms identiques", "Tous les joueurs doivent avoir des noms différents !");
                    return getCTFPlayerNamesWithProfile(playerCount); // Recommencer
                }
            }
        }

        return names;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Obtient les noms pour 4 joueurs avec profil
     */
    private String[] get4PlayerNamesWithProfile() {
        String[] names = new String[4];
        String[] defaultNames = {"Alex", "Blake", "Charlie", "Dana"};
        String[] descriptions = {
                "🔴 Joueur 1 (ZQSD + A)",
                "🔵 Joueur 2 (↑↓←→ + ENTRÉE)",
                "🟡 Joueur 3 (IJKL + U)",
                "🟢 Joueur 4 (8456 + 7)"
        };

        for (int i = 0; i < 4; i++) {
            if (i == 0 && currentProfile != null) {
                // Utiliser le profil pour le premier joueur
                names[i] = currentProfile.getPlayerName();
            } else {
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
        }

        // Vérifier que tous les noms sont différents
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (names[i].equals(names[j])) {
                    showError("Noms identiques", "Tous les joueurs doivent avoir des noms différents !");
                    return get4PlayerNamesWithProfile(); // Recommencer complètement
                }
            }
        }

        return names;
    }

    // ============================================================================
    // MÉTHODES EXISTANTES CONSERVÉES
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

    /**
     * ✅ NOUVELLE MÉTHODE: Créer un nouveau profil rapidement
     */
    @FXML
    private void onCreateQuickProfile() {
        try {
            TextInputDialog dialog = new TextInputDialog("NouveauJoueur");
            dialog.setTitle("🆕 Création rapide de profil");
            dialog.setHeaderText("Créer un nouveau profil");
            dialog.setContentText("Nom du profil:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String name = result.get().trim();

                if (name.isEmpty()) {
                    showError("Nom invalide", "Le nom ne peut pas être vide.");
                    return;
                }

                if (name.length() > 15) {
                    showError("Nom trop long", "Maximum 15 caractères.");
                    return;
                }

                ProfileManager profileManager = ProfileManager.getInstance();
                if (profileManager.profileExists(name)) {
                    showError("Profil existant", "Un profil avec ce nom existe déjà.");
                    return;
                }

                PlayerProfile newProfile = profileManager.loadProfile(name);
                profileManager.saveProfile(newProfile);

                currentProfile = newProfile;
                updateCurrentProfileDisplay();

                showInfo("Profil créé",
                        "✅ Nouveau profil créé: " + name + "\n\n" +
                                "🎮 Prêt à jouer !\n" +
                                "📊 Vos statistiques seront sauvegardées.");
            }

        } catch (BombermanException e) {
            showError("Erreur", "Impossible de créer le profil: " + e.getMessage());
        }
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ============================================================================
    // GETTERS POUR COMPATIBILITÉ
    // ============================================================================

    public String getPlayer1Name() {
        return currentProfile != null ? currentProfile.getPlayerName() : player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(PlayerProfile profile) {
        this.currentProfile = profile;
        updateCurrentProfileDisplay();
    }

    // ✅ AJOUTEZ CES MÉTHODES À VOTRE MenuController.java EXISTANT

    /**
     * ✅ NOUVELLE MÉTHODE: Affiche le tutoriel complet
     */
    @FXML
    private void onShowTutorial() {
        Alert tutorialAlert = new Alert(Alert.AlertType.INFORMATION);
        tutorialAlert.setTitle("📖 Tutoriel Super Bomberman");
        tutorialAlert.setHeaderText("Guide complet pour bien commencer");

        StringBuilder tutorial = new StringBuilder();
        tutorial.append("🎯 OBJECTIF DU JEU:\n");
        tutorial.append("Éliminez vos adversaires avec des bombes tout en évitant les explosions !\n\n");

        tutorial.append("🎮 CONTRÔLES DE BASE:\n");
        tutorial.append("• Joueur 1: ZQSD pour se déplacer, ESPACE pour poser une bombe\n");
        tutorial.append("• Joueur 2: ↑↓←→ pour se déplacer, ENTRÉE pour poser une bombe\n");
        tutorial.append("• T: Changer de thème visuel\n");
        tutorial.append("• R: Redémarrer la partie\n");
        tutorial.append("• ESC: Retour au menu\n\n");

        tutorial.append("💣 RÈGLES DES BOMBES:\n");
        tutorial.append("• Les bombes explosent après 3 secondes\n");
        tutorial.append("• Les explosions durent 1.5 seconde\n");
        tutorial.append("• VOS PROPRES bombes vous tuent !\n");
        tutorial.append("• Cooldown de 10 secondes entre chaque bombe\n");
        tutorial.append("• Les explosions détruisent les briques mais pas les murs\n\n");

        tutorial.append("🏆 CONDITIONS DE VICTOIRE:\n");
        tutorial.append("• Mode Classique: Éliminez votre adversaire\n");
        tutorial.append("• Bataille Royale: Soyez le dernier survivant\n");
        tutorial.append("• CTF: Capturez tous les drapeaux adverses\n");
        tutorial.append("• Contre IA: Battez l'intelligence artificielle\n\n");

        tutorial.append("💡 CONSEILS STRATÉGIQUES:\n");
        tutorial.append("• Utilisez les briques comme couverture\n");
        tutorial.append("• Anticipez les mouvements adverses\n");
        tutorial.append("• Attention aux explosions en chaîne\n");
        tutorial.append("• Restez mobile, ne restez pas dans les coins\n");
        tutorial.append("• Observez le cooldown de vos adversaires");

        tutorialAlert.setContentText(tutorial.toString());
        tutorialAlert.getDialogPane().setPrefWidth(600);
        tutorialAlert.getDialogPane().setPrefHeight(500);
        tutorialAlert.setResizable(true);
        tutorialAlert.showAndWait();
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Affiche les contrôles détaillés
     */
    @FXML
    private void onShowControls() {
        Alert controlsAlert = new Alert(Alert.AlertType.INFORMATION);
        controlsAlert.setTitle("🎮 Contrôles Détaillés");
        controlsAlert.setHeaderText("Guide complet des contrôles pour tous les modes");

        StringBuilder controls = new StringBuilder();
        controls.append("👥 MODE 2 JOUEURS:\n");
        controls.append("═══════════════════════\n");
        controls.append("🔵 Joueur 1:\n");
        controls.append("• Z = Monter\n");
        controls.append("• Q = Aller à gauche\n");
        controls.append("• S = Descendre\n");
        controls.append("• D = Aller à droite\n");
        controls.append("• ESPACE = Poser une bombe\n\n");

        controls.append("🟢 Joueur 2:\n");
        controls.append("• ↑ = Monter\n");
        controls.append("• ← = Aller à gauche\n");
        controls.append("• ↓ = Descendre\n");
        controls.append("• → = Aller à droite\n");
        controls.append("• ENTRÉE = Poser une bombe\n\n");

        controls.append("👥👥 MODE 4 JOUEURS (Bataille Royale):\n");
        controls.append("══════════════════════════════════════\n");
        controls.append("🔴 Joueur 1: ZQSD + A (bombe)\n");
        controls.append("🔵 Joueur 2: ↑↓←→ + ENTRÉE (bombe)\n");
        controls.append("🟡 Joueur 3: IJKL + U (bombe)\n");
        controls.append("🟢 Joueur 4: 8456 (pavé num.) + 7 (bombe)\n\n");

        controls.append("🤖 MODE BOT:\n");
        controls.append("═══════════════\n");
        controls.append("👤 Vous: ZQSD + ESPACE\n");
        controls.append("🤖 IA: Contrôlée automatiquement\n\n");

        controls.append("⌨️ CONTRÔLES GÉNÉRAUX:\n");
        controls.append("════════════════════════\n");
        controls.append("• T = Changer de thème visuel\n");
        controls.append("• R = Redémarrer la partie\n");
        controls.append("• ESC = Retour au menu principal\n");
        controls.append("• E = Forcer la fin de partie\n\n");

        controls.append("💡 ASTUCES:\n");
        controls.append("• Maintenez une direction pour mouvement continu (mode 2J)\n");
        controls.append("• Une touche = un mouvement (mode 4J et CTF)\n");
        controls.append("• Les cooldowns s'affichent en temps réel");

        controlsAlert.setContentText(controls.toString());
        controlsAlert.getDialogPane().setPrefWidth(550);
        controlsAlert.getDialogPane().setPrefHeight(600);
        controlsAlert.setResizable(true);
        controlsAlert.showAndWait();
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Affiche les informations sur les thèmes
     */
    @FXML
    private void onShowThemes() {
        Alert themesAlert = new Alert(Alert.AlertType.INFORMATION);
        themesAlert.setTitle("🎨 Thèmes Visuels");
        themesAlert.setHeaderText("Personnalisez l'apparence de votre jeu");

        StringBuilder themes = new StringBuilder();
        themes.append("🎨 THÈMES DISPONIBLES:\n");
        themes.append("═══════════════════════════\n\n");

        themes.append("🔹 THÈME PAR DÉFAUT:\n");
        themes.append("• Style classique Bomberman\n");
        themes.append("• Couleurs traditionnelles\n");
        themes.append("• Adapté à tous les joueurs\n\n");

        themes.append("🔸 THÈME POKÉMON:\n");
        themes.append("• Personnages inspirés de Pokémon\n");
        themes.append("• Couleurs vives et amusantes\n");
        themes.append("• Parfait pour les fans d'anime\n\n");

        themes.append("⚙️ COMMENT CHANGER DE THÈME:\n");
        themes.append("════════════════════════════════\n");
        themes.append("• En jeu: Appuyez sur la touche T\n");
        themes.append("• Dans les profils: Sélectionnez votre thème préféré\n");
        themes.append("• Le thème sera sauvegardé avec votre profil\n\n");

        themes.append("🛠️ AJOUTER VOS PROPRES THÈMES:\n");
        themes.append("════════════════════════════════════\n");
        themes.append("1. Créez un dossier dans: resources/fr/univ/bomberman/image/[nom_theme]/\n");
        themes.append("2. Ajoutez vos images (player1.png, bomb.png, etc.)\n");
        themes.append("3. Redémarrez le jeu\n");
        themes.append("4. Votre thème apparaîtra automatiquement\n\n");

        themes.append("📝 IMAGES REQUISES:\n");
        themes.append("• player1.png, player2.png, player3.png, player4.png\n");
        themes.append("• bomb.png, explosion.png\n");
        themes.append("• wall.png, brick.png, ground.png\n\n");

        themes.append("💡 Format recommandé: 40x40 pixels, PNG avec transparence");

        themesAlert.setContentText(themes.toString());
        themesAlert.getDialogPane().setPrefWidth(600);
        themesAlert.getDialogPane().setPrefHeight(500);
        themesAlert.setResizable(true);
        themesAlert.showAndWait();
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Affiche les informations sur le jeu
     */
    @FXML
    private void onAbout() {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("ℹ️ À propos de Super Bomberman");
        aboutAlert.setHeaderText("Informations sur le jeu");

        StringBuilder about = new StringBuilder();
        about.append("💣 SUPER BOMBERMAN\n");
        about.append("═══════════════════════\n\n");

        about.append("🎮 VERSION: 2.0 Enhanced Edition\n");
        about.append("👨‍💻 DÉVELOPPÉ AVEC: JavaFX + Maven\n");
        about.append("📅 DERNIÈRE MISE À JOUR: 2025\n\n");

        about.append("🌟 FONCTIONNALITÉS:\n");
        about.append("════════════════════════\n");
        about.append("✅ Mode Classique 2 joueurs\n");
        about.append("✅ Bataille Royale 4 joueurs\n");
        about.append("✅ Mode Capture The Flag\n");
        about.append("✅ Intelligence Artificielle (3 niveaux)\n");
        about.append("✅ Système de profils et statistiques\n");
        about.append("✅ Thèmes visuels personnalisables\n");
        about.append("✅ Sauvegarde automatique des parties\n");
        about.append("✅ Classements et rangs\n\n");

        about.append("🎯 MODES DE JEU:\n");
        about.append("═══════════════════\n");
        about.append("• 🔥 Classique: Duel traditionnel\n");
        about.append("• ⚔️ Bataille Royale: Combat à 4 joueurs\n");
        about.append("• 🏁 CTF: Stratégie et capture\n");
        about.append("• 🤖 IA: Défi contre l'ordinateur\n\n");

        about.append("📊 SYSTÈME DE PROFILS:\n");
        about.append("═══════════════════════════\n");
        about.append("• Sauvegarde automatique des statistiques\n");
        about.append("• Système de rangs et niveaux\n");
        about.append("• Import/Export des profils\n");
        about.append("• Préférences personnalisées\n\n");

        about.append("🏆 RANGS DISPONIBLES:\n");
        about.append("🥉 Bronze → 🥈 Argent → 🥇 Or → 💎 Platine → 👑 Diamant\n\n");

        about.append("💡 CONSEIL: Créez un profil pour sauvegarder vos exploits !\n\n");

        about.append("🎮 Amusez-vous bien et que le meilleur gagne ! 💥");

        aboutAlert.setContentText(about.toString());
        aboutAlert.getDialogPane().setPrefWidth(550);
        aboutAlert.getDialogPane().setPrefHeight(600);
        aboutAlert.setResizable(true);
        aboutAlert.showAndWait();
    }
}