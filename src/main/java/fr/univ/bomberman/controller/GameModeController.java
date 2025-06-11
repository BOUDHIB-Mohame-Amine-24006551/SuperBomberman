// FILE: src/main/java/fr/univ/bomberman/controller/GameModeController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.BombermanApp;
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
import javafx.stage.FileChooser;
import java.io.File;

import java.util.Optional;

/**
 * Contrôleur pour le menu de sélection du mode de jeu
 */
public class GameModeController {

    @FXML private Button twoPlayerButton;
    @FXML private Button fourPlayerButton;
    @FXML private Button settingsButton;
    @FXML private Button themeButton;
    @FXML private Button playerNamesButton;
    @FXML private Button backButton;
    @FXML private Button quitButton;
    @FXML private Button selectProfileButton;
    @FXML private Button selectLevelButton;
    private PlayerProfile currentProfile;
    private static PlayerProfile currentGameProfile = null;

    private BombermanApp bombermanApp;
    private String player1Name = "Joueur 1";
    private String player2Name = "Joueur 2";

    @FXML private Label currentProfileLabel;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        System.out.println("GameModeController initialisé");

        // Effets visuels sur les boutons principaux
        setupButtonEffects();
    }

    /**
     * Configure les effets visuels des boutons
     */
    private void setupButtonEffects() {
        // Effet hover sur le bouton 2 joueurs
        twoPlayerButton.setOnMouseEntered(e ->
                twoPlayerButton.setStyle(twoPlayerButton.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        twoPlayerButton.setOnMouseExited(e ->
                twoPlayerButton.setStyle(twoPlayerButton.getStyle() + "-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        // Effet hover sur le bouton 4 joueurs
        fourPlayerButton.setOnMouseEntered(e ->
                fourPlayerButton.setStyle(fourPlayerButton.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        fourPlayerButton.setOnMouseExited(e ->
                fourPlayerButton.setStyle(fourPlayerButton.getStyle() + "-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
    }

    /**
     * Définit la référence vers l'application principale
     */
    public void setBombermanApp(BombermanApp app) {
        this.bombermanApp = app;
    }

    /**
     * Lance le mode 2 joueurs
     */
    @FXML
    private void onTwoPlayerMode(ActionEvent event) {
        try {
            System.out.println("Lancement du mode 2 joueurs");

            // Afficher une confirmation avec les noms actuels
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Mode 2 Joueurs");
            confirmAlert.setHeaderText("🔥 PRÊT POUR LE DUEL ? 🔥");
            confirmAlert.setContentText("Joueurs:\n" +
                    "🔵 " + player1Name + " (ZQSD + ESPACE)\n" +
                    "🟢 " + player2Name + " (↑↓←→ + ENTRÉE)\n\n" +
                    "Que le meilleur gagne !");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (bombermanApp != null) {
                    bombermanApp.startCanvasGameWithNames(player1Name, player2Name);
                } else {
                    showError("Erreur", "Référence vers l'application non trouvée");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de lancer le mode 2 joueurs: " + e.getMessage());
        }
    }

    /**
     * Lance le mode 4 joueurs (Bataille Royale)
     */
    @FXML
    private void onFourPlayerMode(ActionEvent event) {
        try {
            System.out.println("Lancement du mode 4 joueurs - Bataille Royale");

            // Demander les noms des 4 joueurs
            String[] playerNames = get4PlayerNames();
            if (playerNames == null) return; // Annulé

            // Confirmation avec aperçu
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("⚔️ BATAILLE ROYALE 4 JOUEURS ⚔️");
            confirmAlert.setHeaderText("DERNIÈRE CHANCE AVANT LA GUERRE !");
            confirmAlert.setContentText("Combattants:\n" +
                    "🔴 " + playerNames[0] + " (ZQSD + A)\n" +
                    "🔵 " + playerNames[1] + " (↑↓←→ + ENTRÉE)\n" +
                    "🟡 " + playerNames[2] + " (IJKL + U)\n" +
                    "🟢 " + playerNames[3] + " (8456 + 7)\n\n" +
                    "⚡ Mode discret: 1 touche = 1 mouvement\n" +
                    "💀 Dernier survivant remporte tout !");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (bombermanApp != null) {
                    bombermanApp.startFourPlayerGame(playerNames);
                } else {
                    showError("Erreur", "Référence vers l'application non trouvée");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de lancer la bataille royale: " + e.getMessage());
        }
    }

    /**
     * Ouvre les paramètres
     */
    @FXML
    private void onSettings(ActionEvent event) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("⚙️ Paramètres");
        infoAlert.setHeaderText("Paramètres du jeu");
        infoAlert.setContentText("🎮 Contrôles en jeu:\n" +
                "• T = Changer de thème\n" +
                "• R = Redémarrer la partie\n" +
                "• ESC = Retour au menu\n" +
                "• E = Forcer la fin de partie\n\n" +
                "🔥 Les bombes tuent les adversaires !\n" +
                "💛 Vous êtes protégé de vos propres bombes");
        infoAlert.showAndWait();
    }

    /**
     * Gestion des thèmes
     */
    @FXML
    private void onThemes(ActionEvent event) {
        Alert themeAlert = new Alert(Alert.AlertType.INFORMATION);
        themeAlert.setTitle("🎨 Thèmes visuels");
        themeAlert.setHeaderText("Changement de thème");
        themeAlert.setContentText("🎨 Thèmes disponibles:\n" +
                "• Default - Thème classique\n" +
                "• Pokemon - Personnages Pokemon\n\n" +
                "💡 Changez de thème en jeu avec la touche T\n" +
                "🖼️ Ajoutez vos propres images dans:\n" +
                "resources/fr/univ/bomberman/image/[theme]/");
        themeAlert.showAndWait();
    }

    /**
     * Modification des noms des joueurs
     */
    @FXML
    private void onPlayerNames(ActionEvent event) {
        try {
            // Joueur 1
            TextInputDialog dialog1 = new TextInputDialog(player1Name);
            dialog1.setTitle("Nom du Joueur 1");
            dialog1.setHeaderText("🔵 Joueur 1 (ZQSD + ESPACE)");
            dialog1.setContentText("Nom:");

            Optional<String> result1 = dialog1.showAndWait();
            if (result1.isPresent() && !result1.get().trim().isEmpty()) {
                String newName1 = result1.get().trim();

                if (newName1.length() > 15) {
                    showError("Nom trop long", "Maximum 15 caractères");
                    return;
                }

                // Joueur 2
                TextInputDialog dialog2 = new TextInputDialog(player2Name);
                dialog2.setTitle("Nom du Joueur 2");
                dialog2.setHeaderText("🟢 Joueur 2 (↑↓←→ + ENTRÉE)");
                dialog2.setContentText("Nom:");

                Optional<String> result2 = dialog2.showAndWait();
                if (result2.isPresent() && !result2.get().trim().isEmpty()) {
                    String newName2 = result2.get().trim();

                    if (newName2.length() > 15) {
                        showError("Nom trop long", "Maximum 15 caractères");
                        return;
                    }

                    if (newName1.equals(newName2)) {
                        showError("Noms identiques", "Les joueurs doivent avoir des noms différents");
                        return;
                    }

                    // Sauvegarder
                    player1Name = newName1;
                    player2Name = newName2;

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("✅ Noms mis à jour");
                    successAlert.setContentText("🔵 " + player1Name + " vs 🟢 " + player2Name + "\n\nPrêts pour le combat !");
                    successAlert.showAndWait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Problème lors de la modification des noms");
        }
    }

    /**
     * Retour au menu principal
     */
    @FXML
    private void onBack(ActionEvent event) {
        try {
            if (bombermanApp != null) {
                bombermanApp.showMenu();
            } else {
                // Fallback: retour au menu principal
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/main/menu.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner au menu");
        }
    }

    /**
     * Quitter l'application
     */
    @FXML
    private void onQuit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Quitter");
        confirmAlert.setHeaderText("🚪 Partir déjà ?");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir quitter Super Bomberman ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Demande les noms des 4 joueurs
     */
    private String[] get4PlayerNames() {
        String[] names = new String[4];
        String[] defaultNames = {"Rouge", "Bleu", "Jaune", "Vert"};
        String[] descriptions = {
                "🔴 Joueur 1 (ZQSD + A)",
                "🔵 Joueur 2 (↑↓←→ + ENTRÉE)",
                "🟡 Joueur 3 (IJKL + U)",
                "🟢 Joueur 4 (8456 + 7)"
        };

        for (int i = 0; i < 4; i++) {
            TextInputDialog dialog = new TextInputDialog(defaultNames[i]);
            dialog.setTitle("Nom du Joueur " + (i + 1));
            dialog.setHeaderText(descriptions[i]);
            dialog.setContentText("Nom:");

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return null; // Annulé
            }

            String name = result.get().trim();
            if (name.isEmpty()) {
                showError("Nom invalide", "Le nom ne peut pas être vide");
                i--; // Recommencer
                continue;
            }

            if (name.length() > 15) {
                showError("Nom trop long", "Maximum 15 caractères");
                i--; // Recommencer
                continue;
            }

            names[i] = name;
        }

        // Vérifier unicité
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (names[i].equals(names[j])) {
                    showError("Noms identiques", "Tous les joueurs doivent avoir des noms différents");
                    return get4PlayerNames(); // Recommencer
                }
            }
        }

        return names;
    }

    /**
     * Affiche une erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onBotMode(ActionEvent event) {
        try {
            System.out.println("Lancement du mode Bot");

            // Interface de sélection de difficulté améliorée
            Alert botAlert = new Alert(Alert.AlertType.CONFIRMATION);
            botAlert.setTitle("🤖 DÉFI CONTRE L'IA");
            botAlert.setHeaderText("Choisissez votre adversaire artificiel !");

            StringBuilder content = new StringBuilder();
            content.append("🎯 Testez vos compétences contre l'Intelligence Artificielle !\n\n");
            content.append("🎮 Vous contrôlez avec ZQSD + ESPACE\n");
            content.append("🤖 L'IA joue automatiquement\n\n");
            content.append("Quelle difficulté choisissez-vous ?");

            botAlert.setContentText(content.toString());

            ButtonType easyButton = new ButtonType("😊 Facile - IA Débutante");
            ButtonType mediumButton = new ButtonType("😐 Moyen - IA Équilibrée");
            ButtonType hardButton = new ButtonType("😈 Difficile - IA Redoutable");
            ButtonType cancelButton = new ButtonType("Annuler");

            botAlert.getButtonTypes().setAll(easyButton, mediumButton, hardButton, cancelButton);

            Optional<ButtonType> result = botAlert.showAndWait();
            if (!result.isPresent()) return;

            int difficulty = 2;
            String difficultyName = "Moyen";

            if (result.get() == easyButton) {
                difficulty = 1;
                difficultyName = "Facile";
            } else if (result.get() == mediumButton) {
                difficulty = 2;
                difficultyName = "Moyen";
            } else if (result.get() == hardButton) {
                difficulty = 3;
                difficultyName = "Difficile";
            } else {
                return; // Annulé
            }

            // Demander le nom du joueur
            javafx.scene.control.TextInputDialog nameDialog = new javafx.scene.control.TextInputDialog("Champion");
            nameDialog.setTitle("Nom du Joueur");
            nameDialog.setHeaderText("🏆 Vous défiez le Bot " + difficultyName);
            nameDialog.setContentText("Votre nom de guerre:");

            Optional<String> nameResult = nameDialog.showAndWait();
            if (!nameResult.isPresent()) return;

            String playerName = nameResult.get().trim();
            if (playerName.isEmpty()) {
                showError("Nom invalide", "Le nom ne peut pas être vide");
                return;
            }
            if (playerName.length() > 15) {
                showError("Nom trop long", "Maximum 15 caractères");
                return;
            }

            // Confirmation finale avec récapitulatif
            Alert finalAlert = new Alert(Alert.AlertType.CONFIRMATION);
            finalAlert.setTitle("⚔️ DUEL IMMINENT");
            finalAlert.setHeaderText("Le combat va commencer !");
            finalAlert.setContentText("👤 " + playerName + "\n" +
                    "        VS\n" +
                    "🤖 Bot " + difficultyName + "\n\n" +
                    getDetailedBotInfo(difficulty) + "\n\n" +
                    "🔥 Prêt pour l'affrontement ?");

            Optional<ButtonType> finalResult = finalAlert.showAndWait();
            if (finalResult.isPresent() && finalResult.get() == ButtonType.OK) {
                if (bombermanApp != null) {
                    bombermanApp.startBotGame(playerName, difficulty);
                } else {
                    showError("Erreur", "Référence vers l'application non trouvée");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de lancer le mode bot: " + e.getMessage());
        }
    }

    private String getDetailedBotInfo(int difficulty) {
        switch (difficulty) {
            case 1:
                return "😊 IA Débutante:\n" +
                        "• Mouvements simples et prévisibles\n" +
                        "• Pose des bombes aléatoirement\n" +
                        "• Idéal pour apprendre le jeu";

            case 2:
                return "😐 IA Équilibrée:\n" +
                        "• Stratégie de base efficace\n" +
                        "• Évite les dangers principaux\n" +
                        "• Défi équilibré et amusant";

            case 3:
                return "😈 IA Redoutable:\n" +
                        "• Stratégies avancées\n" +
                        "• Vous traque intelligemment\n" +
                        "• Réagit rapidement aux menaces\n" +
                        "• Défie même les experts !";

            default:
                return "IA de difficulté standard";
        }
    }

    @FXML
    private void onBotTutorial(ActionEvent event) {
        Alert tutorialAlert = new Alert(Alert.AlertType.INFORMATION);
        tutorialAlert.setTitle("📖 Tutoriel Mode Bot");
        tutorialAlert.setHeaderText("Comment jouer contre l'IA");

        StringBuilder tutorial = new StringBuilder();
        tutorial.append("🎯 OBJECTIF:\n");
        tutorial.append("Éliminez le bot avec vos bombes avant qu'il ne vous élimine !\n\n");

        tutorial.append("🎮 VOS CONTRÔLES:\n");
        tutorial.append("• Z/Q/S/D : Se déplacer\n");
        tutorial.append("• ESPACE : Poser une bombe\n");
        tutorial.append("• T : Changer de thème visuel\n");
        tutorial.append("• R : Recommencer la partie\n");
        tutorial.append("• ESC : Retour au menu\n\n");

        tutorial.append("🤖 COMPORTEMENT DE L'IA:\n");
        tutorial.append("• Analyse le plateau en temps réel\n");
        tutorial.append("• Adapte sa stratégie selon la difficulté\n");
        tutorial.append("• Peut vous surprendre avec des tactiques inattendues\n\n");

        tutorial.append("⚠️ RÈGLES IMPORTANTES:\n");
        tutorial.append("• Vos propres bombes vous tuent !\n");
        tutorial.append("• Explosions durent 1.5 seconde\n");
        tutorial.append("• Cooldown de 10 secondes entre bombes\n");
        tutorial.append("• Pas de protection contre ses bombes\n\n");

        tutorial.append("💡 CONSEILS POUR BATTRE L'IA:\n");
        tutorial.append("• Utilisez votre créativité humaine\n");
        tutorial.append("• Anticipez ses mouvements\n");
        tutorial.append("• Piégez-la dans des coins\n");
        tutorial.append("• Restez imprévisible !");

        tutorialAlert.setContentText(tutorial.toString());
        tutorialAlert.showAndWait();
    }
    // Ajout de la méthode pour le mode CTF dans GameModeController.java

    /**
     * Lance le mode Capture The Flag
     */


    /**
     * Demande les noms des joueurs pour le mode CTF
     */
    private String[] getCTFPlayerNames(int playerCount) {
        String[] names = new String[playerCount];
        String[] defaultNames = {"Conquérant", "Défenseur", "Stratège", "Tacticien"};
        String[] colors = {"🔴", "🔵", "🟡", "🟢"};
        String[] controls = {"ZQSD + A", "↑↓←→ + ENTRÉE", "IJKL + U", "8456 + 7"};

        for (int i = 0; i < playerCount; i++) {
            TextInputDialog dialog = new TextInputDialog(defaultNames[i]);
            dialog.setTitle("Nom du Joueur " + (i + 1));
            dialog.setHeaderText(colors[i] + " Joueur " + (i + 1) + " (" + controls[i] + ")");
            dialog.setContentText("Nom du guerrier:");

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return null; // Annulé
            }

            String name = result.get().trim();
            if (name.isEmpty()) {
                showError("Nom invalide", "Le nom ne peut pas être vide");
                i--; // Recommencer
                continue;
            }

            if (name.length() > 15) {
                showError("Nom trop long", "Maximum 15 caractères");
                i--; // Recommencer
                continue;
            }

            names[i] = name;
        }

        // Vérifier unicité
        for (int i = 0; i < playerCount; i++) {
            for (int j = i + 1; j < playerCount; j++) {
                if (names[i].equals(names[j])) {
                    showError("Noms identiques", "Tous les joueurs doivent avoir des noms différents");
                    return getCTFPlayerNames(playerCount); // Recommencer
                }
            }
        }

        return names;
    }

    /**
     * Affiche le tutoriel du mode Capture The Flag
     */
    @FXML
    private void onCTFTutorial(ActionEvent event) {
        Alert tutorialAlert = new Alert(Alert.AlertType.INFORMATION);
        tutorialAlert.setTitle("📖 Tutoriel Capture The Flag");
        tutorialAlert.setHeaderText("Comment maîtriser le mode CTF");

        StringBuilder tutorial = new StringBuilder();
        tutorial.append("🎯 OBJECTIF PRINCIPAL:\n");
        tutorial.append("Soyez le premier à capturer TOUS les drapeaux adverses !\n\n");

        tutorial.append("🏁 PHASE 1 - PLACEMENT DES DRAPEAUX:\n");
        tutorial.append("• Chaque joueur place son drapeau à tour de rôle\n");
        tutorial.append("• Choisissez bien votre position (facile à défendre ?)\n");
        tutorial.append("• Une fois placé, le drapeau ne bouge plus\n\n");

        tutorial.append("⚔️ PHASE 2 - COMBAT ET CAPTURE:\n");
        tutorial.append("• Marchez sur un drapeau ennemi pour le capturer\n");
        tutorial.append("• Éliminez vos adversaires avec des bombes\n");
        tutorial.append("• Défendez votre propre drapeau\n\n");

        tutorial.append("💀 RÈGLE SPÉCIALE - ÉLIMINATION:\n");
        tutorial.append("• Joueur éliminé = peut encore poser des bombes !\n");
        tutorial.append("• Ses drapeaux capturés retournent à leur place\n");
        tutorial.append("• Il peut aider/gêner les survivants\n\n");

        tutorial.append("🏆 CONDITIONS DE VICTOIRE:\n");
        tutorial.append("• Capturer tous les drapeaux des autres joueurs\n");
        tutorial.append("• Être le dernier non-éliminé avec des drapeaux\n\n");

        tutorial.append("💡 STRATÉGIES GAGNANTES:\n");
        tutorial.append("• Placez votre drapeau dans un coin protégé\n");
        tutorial.append("• Éliminez d'abord les joueurs avec beaucoup de drapeaux\n");
        tutorial.append("• Utilisez les joueurs éliminés comme alliés\n");
        tutorial.append("• Restez mobile, ne campez pas trop\n");
        tutorial.append("• Anticipez les mouvements adverses");

        tutorialAlert.setContentText(tutorial.toString());
        tutorialAlert.showAndWait();
    }
    /**
     * Lance le mode Capture The Flag
     */
    @FXML
    private void onCTFMode(ActionEvent event) {
        try {
            System.out.println("Lancement du mode Capture The Flag");

            // Sélection du nombre de joueurs pour CTF
            Alert playerCountAlert = new Alert(Alert.AlertType.CONFIRMATION);
            playerCountAlert.setTitle("🏁 CAPTURE THE FLAG");
            playerCountAlert.setHeaderText("Combien de joueurs pour ce mode stratégique ?");

            StringBuilder content = new StringBuilder();
            content.append("🎯 Mode Capture The Flag - Stratégie pure !\n\n");
            content.append("🏁 Objectif : Capturez TOUS les drapeaux adverses\n");
            content.append("💀 Les joueurs éliminés peuvent encore bombarder\n");
            content.append("📍 Phase 1 : Placement des drapeaux\n");
            content.append("⚔️ Phase 2 : Combat et capture\n\n");
            content.append("Choisissez le nombre de participants :");

            playerCountAlert.setContentText(content.toString());

            ButtonType twoPlayersButton = new ButtonType("👥 2 Joueurs");
            ButtonType threePlayersButton = new ButtonType("👥👥 3 Joueurs");
            ButtonType fourPlayersButton = new ButtonType("👥👥👥 4 Joueurs");
            ButtonType cancelButton = new ButtonType("Annuler");

            playerCountAlert.getButtonTypes().setAll(twoPlayersButton, threePlayersButton, fourPlayersButton, cancelButton);

            Optional<ButtonType> result = playerCountAlert.showAndWait();
            if (!result.isPresent()) return;

            int playerCount = 2;
            if (result.get() == twoPlayersButton) {
                playerCount = 2;
            } else if (result.get() == threePlayersButton) {
                playerCount = 3;
            } else if (result.get() == fourPlayersButton) {
                playerCount = 4;
            } else {
                return; // Annulé
            }

            // Demander les noms des joueurs
            String[] playerNames = getCTFPlayerNames(playerCount);
            if (playerNames == null) return; // Annulé

            // Confirmation finale avec détails CTF
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("🏁 BATAILLE CTF IMMINENTE");
            confirmAlert.setHeaderText("La guerre des drapeaux va commencer !");

            StringBuilder confirmContent = new StringBuilder();
            confirmContent.append("⚔️ Participants CTF :\n");
            String[] colors = {"🔴", "🔵", "🟡", "🟢"};
            String[] controls = {"ZQSD + A", "↑↓←→ + ENTRÉE", "IJKL + U", "8456 + 7"};

            for (int i = 0; i < playerCount; i++) {
                confirmContent.append(colors[i]).append(" ").append(playerNames[i]);
                confirmContent.append(" (").append(controls[i]).append(")\n");
            }

            confirmContent.append("\n🎯 RÈGLES SPÉCIALES CTF :\n");
            confirmContent.append("• Placez votre drapeau stratégiquement\n");
            confirmContent.append("• Capturez TOUS les drapeaux adverses\n");
            confirmContent.append("• Joueurs éliminés = peuvent encore bombarder\n");
            confirmContent.append("• Drapeaux tombent si porteur éliminé\n\n");
            confirmContent.append("🔥 Prêt pour cette guerre tactique ?");

            confirmAlert.setContentText(confirmContent.toString());

            Optional<ButtonType> finalResult = confirmAlert.showAndWait();
            if (finalResult.isPresent() && finalResult.get() == ButtonType.OK) {
                if (bombermanApp != null) {
                    bombermanApp.startCTFGame(playerNames);
                } else {
                    showError("Erreur", "Référence vers l'application non trouvée");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de lancer le mode CTF: " + e.getMessage());
        }
    }

    //profil
    @FXML
    private void onSelectProfile() {
        openProfileSelection();
    }

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
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void setCurrentGameProfile(PlayerProfile profile) {
        currentGameProfile = profile;
        System.out.println("🎮 Profil défini pour la partie: " + (profile != null ? profile.getPlayerName() : "Aucun"));
    }

    public static PlayerProfile getCurrentGameProfile() {
        return currentGameProfile;
    }

    /**
     * Vérifie si un profil est sélectionné
     */
    public static boolean hasProfileSelected() {
        return currentGameProfile != null;
    }



    // Modifiez la méthode onSelectProfile existante pour définir le profil courant
    private void openProfileSelection() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/profile_selection.fxml"));
            Parent root = loader.load();

            ProfileSelectionController controller = loader.getController();
            controller.setBombermanApp(bombermanApp);

            Stage stage = new Stage();
            stage.setTitle("🎮 Gestion des Profils - Super Bomberman");
            stage.setScene(new Scene(root, 900, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);

            if (bombermanApp != null && bombermanApp.getPrimaryStage() != null) {
                Stage primaryStage = bombermanApp.getPrimaryStage();
                stage.initOwner(primaryStage);
                stage.setX(primaryStage.getX() + (primaryStage.getWidth() - 900) / 2);
                stage.setY(primaryStage.getY() + (primaryStage.getHeight() - 600) / 2);
            }

            stage.showAndWait();

            if (controller.isProfileSelected()) {
                currentProfile = controller.getSelectedProfile();
                // ✅ NOUVEAU: Définir le profil pour toute la session de jeu
                setCurrentGameProfile(currentProfile);

                updateCurrentProfileDisplay();

                showInfo("Profil sélectionné",
                        "✅ Profil actif: " + currentProfile.getPlayerName() + "\n" +
                                "🏆 Rang: " + currentProfile.getRank().getDisplayName() + "\n" +
                                "🎮 Parties jouées: " + currentProfile.getTotalGamesPlayed() + "\n" +
                                "📊 Taux de victoire: " + String.format("%.1f%%", currentProfile.getWinRatio()));

                applyProfilePreferences();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la gestion des profils: " + e.getMessage());
        }
    }

    /**
     * Gère la sélection d'un niveau via FileChooser
     */
    @FXML
    private void onSelectLevel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un niveau");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers XML", "*.json")
        );
        
        // Définir le répertoire par défaut
        File defaultDir = new File("src/main/resources/fr/univ/bomberman/level");
        if (defaultDir.exists()) {
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            try {
                // Convertir le chemin en chemin relatif si possible
                String relativePath = getRelativePath(file);
                if (bombermanApp != null) {
                    bombermanApp.setSelectedLevelPath(relativePath);
                    showInfo("Niveau sélectionné", "Le niveau a été sélectionné avec succès !");
                }
            } catch (Exception e) {
                showError("Erreur", "Impossible de sélectionner le niveau: " + e.getMessage());
            }
        }
    }

    /**
     * Convertit un chemin absolu en chemin relatif par rapport au dossier resources
     */
    private String getRelativePath(File file) {
        String absolutePath = file.getAbsolutePath();
        String resourcesPath = "src/main/resources/fr/univ/bomberman/level";
        
        // Si le fichier est dans le dossier resources, retourner le chemin relatif
        if (absolutePath.contains(resourcesPath)) {
            int index = absolutePath.indexOf(resourcesPath);
            return absolutePath.substring(index + resourcesPath.length() + 1);
        }
        
        // Sinon, retourner le chemin absolu
        return absolutePath;
    }

}