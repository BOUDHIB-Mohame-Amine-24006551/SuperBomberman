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

    /**
     * NOUVELLE MÉTHODE: Affiche le tutoriel complet
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
     *  NOUVELLE MÉTHODE: Affiche les contrôles détaillés
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