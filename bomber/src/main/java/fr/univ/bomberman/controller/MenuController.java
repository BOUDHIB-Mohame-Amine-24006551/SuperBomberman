// FILE: src/main/java/fr/univ/bomberman/controller/MenuController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.BombermanApp;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.model.PlayerProfileManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Contrôleur pour le menu principal avec support des modes de jeu.
 */
public class MenuController {

    private BombermanApp bombermanApp;
    private String player1Name = "Joueur 1";
    private String player2Name = "Joueur 2";

    /**
     * Définit la référence vers l'application principale
     */
    public void setBombermanApp(BombermanApp app) {
        this.bombermanApp = app;
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

    /**
     * NOUVELLE MÉTHODE: Lance une partie 2 joueurs (ancien comportement)
     */
    private void start2PlayerGame(ActionEvent event) {
        try {
            Alert choiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
            choiceAlert.setTitle("Type de jeu");
            choiceAlert.setHeaderText("Choisissez le type de jeu");
            choiceAlert.setContentText("Quel type de jeu voulez-vous lancer ?\n\n" +
                    "Joueurs actuels:\n" +
                    "🔵 " + player1Name + "\n" +
                    "🟢 " + player2Name);

            ButtonType canvasButton = new ButtonType("Jeu Canvas (Avec Images)");
            ButtonType fxmlButton = new ButtonType("Jeu FXML (Interface)");
            ButtonType cancelButton = new ButtonType("Annuler");

            choiceAlert.getButtonTypes().setAll(canvasButton, fxmlButton, cancelButton);

            Optional<ButtonType> result = choiceAlert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == canvasButton) {
                    if (bombermanApp != null) {
                        bombermanApp.startCanvasGameWithNames(player1Name, player2Name);
                    } else {
                        showError("Erreur", "Référence vers l'application principale non trouvée");
                    }
                } else if (result.get() == fxmlButton) {
                    if (bombermanApp != null) {
                        bombermanApp.startFXMLGame();
                    } else {
                        startFXMLGameDirect(event);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors du lancement du jeu 2 joueurs: " + e.getMessage());
        }
    }

    @FXML
    private void onBotMode(ActionEvent event) {
        try {
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

            // Ouvrir la boîte de dialogue de sélection de profil
            openProfileSelectionForBotMode(botDifficulty, event);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de lancer le mode bot: " + e.getMessage());
        }
    }

    /**
     * Ouvre la boîte de dialogue de sélection de profil pour le mode bot.
     */
    private void openProfileSelectionForBotMode(int botDifficulty, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/selection.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            PlayerProfileSelectionController controller = loader.getController();
            controller.setBombermanApp(bombermanApp);
            controller.setupBotMode(botDifficulty);

            // Créer la scène
            Scene scene = new Scene(root);
            Stage dialog = new Stage();
            dialog.setTitle("Super Bomberman - Sélection du profil");
            dialog.setResizable(false);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la sélection de profil: " + e.getMessage());
        }
    }

    @FXML
    private void onBotInfo(ActionEvent event) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("🤖 Mode Bot - Informations");
        infoAlert.setHeaderText("Intelligence Artificielle");

        StringBuilder info = new StringBuilder();
        info.append("🎯 COMMENT ÇA MARCHE:\n");
        info.append("• L'IA joue automatiquement\n");
        info.append("• Elle analyse le plateau en temps réel\n");
        info.append("• Elle adapte sa stratégie à la situation\n\n");

        info.append("🎮 CONTRÔLES (Vous):\n");
        info.append("• ZQSD pour se déplacer\n");
        info.append("• ESPACE pour poser une bombe\n");
        info.append("• T pour changer de thème\n");
        info.append("• R pour recommencer\n\n");

        info.append("🧠 NIVEAUX D'IA:\n");
        info.append("😊 Facile - Pour débuter\n");
        info.append("😐 Moyen - Défi équilibré\n");
        info.append("😈 Difficile - Pour les experts\n\n");

        info.append("💡 ASTUCES:\n");
        info.append("• L'IA peut vous piéger, soyez vigilant !\n");
        info.append("• Utilisez votre créativité humaine\n");
        info.append("• Les bots forts anticipent vos mouvements");

        infoAlert.setContentText(info.toString());
        infoAlert.showAndWait();
    }

    @FXML
    private void onQuickBot(ActionEvent event) {
        try {
            Alert quickAlert = new Alert(Alert.AlertType.CONFIRMATION);
            quickAlert.setTitle("🚀 Défi Rapide");
            quickAlert.setHeaderText("Affrontement Express contre l'IA");
            quickAlert.setContentText("Lancer rapidement un duel contre un Bot Moyen ?\n\n" +
                    "👤 Joueur VS 🤖 Bot Moyen\n\n" +
                    "Parfait pour une partie rapide !");

            Optional<ButtonType> result = quickAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (bombermanApp != null) {
                    bombermanApp.startBotGame("Joueur", 2); // Difficulté moyenne
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de lancer le défi rapide: " + e.getMessage());
        }
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

    /**
     * NOUVELLE MÉTHODE: Lance une partie 4 joueurs
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
            // Utiliser TextInputDialog directement
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(defaultNames[i]);
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

    /**
     * NOUVELLE MÉTHODE: Démarre directement en mode tour par tour
     */
    @FXML
    private void onStartTurnBasedGame(ActionEvent event) {
        // Initialiser le gestionnaire de profils
        PlayerProfileManager.initialize();
        
        // Ouvrir la boîte de dialogue de sélection de profil
        openProfileSelectionDialog(2, "Mode Tour par Tour", event);
    }

    /**
     * NOUVELLE MÉTHODE: Démarre directement en mode temps réel
     */
    @FXML
    private void onStartRealTimeGame(ActionEvent event) {
        // Initialiser le gestionnaire de profils
        PlayerProfileManager.initialize();
        
        // Ouvrir la boîte de dialogue de sélection de profil
        openProfileSelectionDialog(2, "Mode Temps Réel", event);
    }

    /**
     * Ouvre la boîte de dialogue de sélection de profil pour une partie standard.
     */
    private void openProfileSelectionDialog(int playerCount, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/selection.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            PlayerProfileSelectionController controller = loader.getController();
            controller.setBombermanApp(bombermanApp);
            controller.setup(playerCount, title);

            // Créer la scène
            Scene scene = new Scene(root);
            Stage dialog = new Stage();
            dialog.setTitle("Super Bomberman - Sélection des profils");
            dialog.setResizable(false);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la sélection de profil: " + e.getMessage());
        }
    }

    /**
     * Affiche des informations sur les modes de jeu
     */
    @FXML
    private void onGameModeInfo(ActionEvent event) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Modes de jeu");
        infoAlert.setHeaderText("Modes de jeu disponibles");

        StringBuilder info = new StringBuilder();
        info.append("🎮 TOUR PAR TOUR (Mode original)\n");
        info.append("• Les joueurs alternent leurs tours\n");
        info.append("• Un seul joueur actif à la fois\n");
        info.append("• Contrôles: Flèches + ESPACE\n");
        info.append("• TAB pour changer de joueur\n\n");

        info.append("⚡ TEMPS RÉEL (Mode nouveau)\n");
        info.append("• Les deux joueurs bougent simultanément\n");
        info.append("• Action continue et dynamique\n");
        info.append("• Joueur 1: WASD + ESPACE\n");
        info.append("• Joueur 2: Flèches + ENTRÉE\n\n");

        info.append("💡 Vous pouvez changer de mode en jeu avec la touche M\n");
        info.append("🎨 Changez de thème avec la touche T");

        infoAlert.setContentText(info.toString());
        infoAlert.showAndWait();
    }

    /**
     * Méthode helper pour afficher les infos d'un mode spécifique
     */
    private void showGameModeInfo(String modeName, String description) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Mode " + modeName);
        infoAlert.setHeaderText("Mode de jeu: " + modeName);
        infoAlert.setContentText(description);
        infoAlert.showAndWait();
    }

    /**
     * Lance le jeu FXML (interface alternative)
     */
    @FXML
    private void onStartFXMLGame(ActionEvent event) {
        try {
            if (bombermanApp != null) {
                bombermanApp.startFXMLGame();
            } else {
                startFXMLGameDirect(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu FXML: " + e.getMessage());
        }
    }

    /**
     * Méthode de fallback pour lancer directement le jeu FXML
     */
    private void startFXMLGameDirect(ActionEvent event) throws BombermanException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/game/view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            try {
                scene.getStylesheets().add(getClass().getResource("/fr/univ/bomberman/css/pokemon/default_theme.css").toExternalForm());
            } catch (Exception cssEx) {
                System.out.println("CSS non trouvé, utilisation du style par défaut");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Super Bomberman - Jeu FXML");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BombermanException("Impossible de démarrer le jeu", e);
        }
    }

    /**
     * Ouvre la fenêtre de gestion de profil
     */
    @FXML
    private void onProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/dialog.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage dialog = new Stage();
            dialog.setTitle("Super Bomberman - Profil");
            dialog.setResizable(false);
            dialog.setScene(scene);
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de profil", "Impossible d'ouvrir la gestion de profil: " + e.getMessage());
        }
    }

    /**
     * Ouvre l'éditeur de niveau
     */
    @FXML
    private void onLevelEditor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/level_editor.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Super Bomberman - Éditeur de Niveau");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'éditeur", "Impossible d'ouvrir l'éditeur de niveau: " + e.getMessage());
        }
    }

    /**
     * Ferme l'application
     */
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
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    /**
     * MÉTHODE SIMPLIFIÉE: Ouvre un dialogue simple pour changer les noms (sans FXML)
     */
    @FXML
    private void onPlayerNames(ActionEvent event) {
        try {
            // Utiliser TextInputDialog pour une solution simple
            javafx.scene.control.TextInputDialog dialog1 = new javafx.scene.control.TextInputDialog(player1Name);
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

                javafx.scene.control.TextInputDialog dialog2 = new javafx.scene.control.TextInputDialog(player2Name);
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

                    // Sauvegarder les nouveaux noms
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

    // Ajoutez cette méthode à votre MenuController.java :

    /**
     * NOUVELLE MÉTHODE: Bouton dédié au mode 4 joueurs
     */
    @FXML
    private void onFourPlayerBattle(ActionEvent event) {
        // Initialiser le gestionnaire de profils
        PlayerProfileManager.initialize();
        
        // Ouvrir la boîte de dialogue de sélection de profil
        openProfileSelectionDialog(4, "Bataille à 4 Joueurs", event);
    }

    /**
     * NOUVELLE MÉTHODE: Lancement rapide avec noms par défaut
     */
    @FXML
    private void onQuickFourPlayer(ActionEvent event) {
        // Initialiser le gestionnaire de profils
        PlayerProfileManager.initialize();
        
        // Ouvrir la boîte de dialogue de sélection de profil
        openProfileSelectionDialog(4, "Bataille Rapide à 4 Joueurs", event);
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    private String showNameDialog(String title, String header, String currentName) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(currentName);
        dialog.setTitle("Super Bomberman - " + title);
        dialog.setHeaderText(header);
        dialog.setContentText("Nom:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get().trim();

            if (name.isEmpty()) {
                showError("Nom invalide", "Le nom ne peut pas être vide !");
                return null;
            }

            if (name.length() > 15) {
                showError("Nom trop long", "Le nom ne peut pas dépasser 15 caractères !");
                return null;
            }

            return name;
        }

        return null; // Annulé
    }
}