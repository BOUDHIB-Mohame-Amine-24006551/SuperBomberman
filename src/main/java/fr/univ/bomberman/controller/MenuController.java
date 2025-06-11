// FILE: src/main/java/fr/univ/bomberman/controller/MenuController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.BombermanApp;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.util.ProfileManager;
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
 * Contrôleur pour le menu principal avec gestion complète des profils
 */
public class MenuController {

    @FXML private Button playButton;
    @FXML private Button playerNamesButton;
    @FXML private Button profileButton;
    @FXML private Button quitButton;

    private BombermanApp bombermanApp;
    private ProfileManager profileManager;
    private String currentSelectedProfile = null;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        profileManager = ProfileManager.getInstance();
        updateUI();
        System.out.println("MenuController avec profils initialisé");
    }

    /**
     * Définit la référence vers l'application principale
     */
    public void setBombermanApp(BombermanApp app) {
        this.bombermanApp = app;
    }

    /**
     * Lance le jeu - vérifie d'abord qu'un profil est sélectionné
     */
    @FXML
    private void onStartGame(ActionEvent event) {
        // Vérifier qu'un profil est sélectionné
        if (currentSelectedProfile == null) {
            Alert profileAlert = new Alert(Alert.AlertType.INFORMATION);
            profileAlert.setTitle("Profil requis");
            profileAlert.setHeaderText("Sélectionnez d'abord un profil");
            profileAlert.setContentText("Pour jouer, vous devez d'abord sélectionner ou créer un profil joueur.\n\n" +
                    "Cliquez sur 'Profil' pour gérer vos profils.");

            Optional<ButtonType> result = profileAlert.showAndWait();
            if (result.isPresent()) {
                onProfile(); // Ouvrir directement la gestion des profils
            }
            return;
        }

        // Afficher le menu de sélection des modes
        showGameModeSelection();
    }

    /**
     * Affiche le menu de sélection du mode de jeu
     */
    private void showGameModeSelection() {
        Alert modeAlert = new Alert(Alert.AlertType.CONFIRMATION);
        modeAlert.setTitle("Sélection du Mode de Jeu");
        modeAlert.setHeaderText("🎮 Choisissez votre mode de jeu");
        modeAlert.setContentText("Profil actuel: " + currentSelectedProfile + "\n\n" +
                "Quel type de partie voulez-vous jouer ?");

        ButtonType twoPlayerBtn = new ButtonType("👥 2 Joueurs");
        ButtonType fourPlayerBtn = new ButtonType("⚔️ Bataille Royale");
        ButtonType botBtn = new ButtonType("🤖 Contre IA");
        ButtonType ctfBtn = new ButtonType("🏁 Capture Flag");
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        modeAlert.getButtonTypes().setAll(twoPlayerBtn, fourPlayerBtn, botBtn, ctfBtn, cancelBtn);

        Optional<ButtonType> result = modeAlert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == twoPlayerBtn) {
                startTwoPlayerGame();
            } else if (result.get() == fourPlayerBtn) {
                startFourPlayerGame();
            } else if (result.get() == botBtn) {
                startBotGame();
            } else if (result.get() == ctfBtn) {
                startCTFGame();
            }
        }
    }

    /**
     * Lance une partie 2 joueurs
     */
    private void startTwoPlayerGame() {
        TextInputDialog dialog = new TextInputDialog("Joueur 2");
        dialog.setTitle("Partie 2 Joueurs");
        dialog.setHeaderText("🎮 " + currentSelectedProfile + " vs ?");
        dialog.setContentText("Nom du second joueur:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String player2Name = result.get().trim();

            if (player2Name.equals(currentSelectedProfile)) {
                showError("Noms identiques", "Les deux joueurs ne peuvent pas avoir le même nom");
                return;
            }

            if (player2Name.length() > 15) {
                showError("Nom trop long", "Maximum 15 caractères");
                return;
            }

            // Lancer le jeu
            if (bombermanApp != null) {
                bombermanApp.startCanvasGameWithNames(currentSelectedProfile, player2Name);
            }
        }
    }

    /**
     * Lance une partie 4 joueurs
     */
    private void startFourPlayerGame() {
        String[] playerNames = get4PlayerNames();
        if (playerNames != null && bombermanApp != null) {
            bombermanApp.startFourPlayerGame(playerNames);
        }
    }

    /**
     * Lance une partie contre IA
     */
    private void startBotGame() {
        // Charger les préférences du profil pour la difficulté
        int preferredDifficulty = 2; // Par défaut
        try {
            PlayerProfile profile = profileManager.loadProfile(currentSelectedProfile);
            preferredDifficulty = profile.getPreferredBotDifficulty();
        } catch (BombermanException e) {
            System.err.println("Impossible de charger les préférences: " + e.getMessage());
        }

        // Demander la difficulté
        Alert difficultyAlert = new Alert(Alert.AlertType.CONFIRMATION);
        difficultyAlert.setTitle("Difficulté IA");
        difficultyAlert.setHeaderText("🤖 Choisissez votre adversaire");
        difficultyAlert.setContentText("Niveau de difficulté de l'IA:");

        ButtonType easyBtn = new ButtonType("😊 Facile");
        ButtonType mediumBtn = new ButtonType("😐 Moyen");
        ButtonType hardBtn = new ButtonType("😈 Difficile");
        ButtonType cancelBtn = new ButtonType("Annuler");

        difficultyAlert.getButtonTypes().setAll(easyBtn, mediumBtn, hardBtn, cancelBtn);

        Optional<ButtonType> result = difficultyAlert.showAndWait();
        if (result.isPresent() && result.get() != cancelBtn) {
            int difficulty = preferredDifficulty;

            if (result.get() == easyBtn) difficulty = 1;
            else if (result.get() == mediumBtn) difficulty = 2;
            else if (result.get() == hardBtn) difficulty = 3;

            if (bombermanApp != null) {
                bombermanApp.startBotGame(currentSelectedProfile, difficulty);
            }
        }
    }

    /**
     * Lance une partie CTF
     */
    private void startCTFGame() {
        String[] playerNames = getCTFPlayerNames();
        if (playerNames != null && bombermanApp != null) {
            bombermanApp.startCTFGame(playerNames);
        }
    }

    /**
     * Gestion des profils
     */
    @FXML
    private void onProfile() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/selection.fxml"));
            Parent root = loader.load();

            ProfileSelectionController controller = loader.getController();
            controller.setBombermanApp(bombermanApp);

            Stage stage = new Stage();
            stage.setTitle("Gestion des Profils");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(profileButton.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

            // Vérifier si un profil a été sélectionné
            if (controller.isProfileSelected()) {
                currentSelectedProfile = controller.getSelectedProfileName();
                updateUI();

                showInfo("Profil sélectionné", "Profil actuel: " + currentSelectedProfile);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la gestion des profils");
        }
    }

    /**
     * Gestion des noms de joueurs (mode traditionnel)
     */
    @FXML
    private void onPlayerNames() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/player/names.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Noms des Joueurs");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(playerNamesButton.getScene().getWindow());
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la fenêtre des noms");
        }
    }

    /**
     * Quitter l'application
     */
    @FXML
    private void onQuit(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Quitter");
        confirmAlert.setHeaderText("Confirmation");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir quitter Super Bomberman ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Met à jour l'interface selon le profil sélectionné
     */
    private void updateUI() {
        if (currentSelectedProfile != null) {
            playButton.setText("🎮 JOUER (" + currentSelectedProfile + ")");
            profileButton.setText("👤 " + currentSelectedProfile);

            // Charger les informations du profil pour affichage
            try {
                PlayerProfile profile = profileManager.loadProfile(currentSelectedProfile);
                String rankInfo = profile.getRank().getDisplayName();
                playButton.setTooltip(new Tooltip("Profil: " + currentSelectedProfile +
                        " | Rang: " + rankInfo +
                        " | Parties: " + profile.getTotalGamesPlayed()));
            } catch (BombermanException e) {
                System.err.println("Erreur lors du chargement du profil: " + e.getMessage());
            }
        } else {
            playButton.setText("🎮 JOUER");
            profileButton.setText("👤 Profil");
            playButton.setTooltip(new Tooltip("Sélectionnez d'abord un profil"));
        }
    }

    /**
     * Demande les noms des joueurs pour le mode 4 joueurs
     */
    private String[] get4PlayerNames() {
        String[] names = new String[4];
        names[0] = currentSelectedProfile; // Le profil actuel est le joueur 1

        String[] defaultNames = {"", "Joueur 2", "Joueur 3", "Joueur 4"};
        String[] descriptions = {
                "🔴 " + currentSelectedProfile + " (ZQSD + A)",
                "🔵 Joueur 2 (↑↓←→ + ENTRÉE)",
                "🟡 Joueur 3 (IJKL + U)",
                "🟢 Joueur 4 (8456 + 7)"
        };

        for (int i = 1; i < 4; i++) {
            TextInputDialog dialog = new TextInputDialog(defaultNames[i]);
            dialog.setTitle("Bataille Royale - Joueur " + (i + 1));
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

            // Vérifier l'unicité
            boolean nameExists = name.equals(currentSelectedProfile);
            for (int j = 1; j < i; j++) {
                if (name.equals(names[j])) {
                    nameExists = true;
                    break;
                }
            }

            if (nameExists) {
                showError("Nom existant", "Ce nom est déjà utilisé");
                i--; // Recommencer
                continue;
            }

            names[i] = name;
        }

        return names;
    }

    /**
     * Demande les noms des joueurs pour le mode CTF
     */
    private String[] getCTFPlayerNames() {
        // Demander d'abord le nombre de joueurs
        Alert playerCountAlert = new Alert(Alert.AlertType.CONFIRMATION);
        playerCountAlert.setTitle("CTF - Nombre de joueurs");
        playerCountAlert.setHeaderText("🏁 Combien de joueurs pour le CTF ?");
        playerCountAlert.setContentText("Vous êtes: " + currentSelectedProfile);

        ButtonType twoBtn = new ButtonType("👥 2 Joueurs");
        ButtonType threeBtn = new ButtonType("👥👤 3 Joueurs");
        ButtonType fourBtn = new ButtonType("👥👥 4 Joueurs");
        ButtonType cancelBtn = new ButtonType("Annuler");

        playerCountAlert.getButtonTypes().setAll(twoBtn, threeBtn, fourBtn, cancelBtn);

        Optional<ButtonType> countResult = playerCountAlert.showAndWait();
        if (!countResult.isPresent() || countResult.get() == cancelBtn) {
            return null;
        }

        int playerCount = 2;
        if (countResult.get() == threeBtn) playerCount = 3;
        else if (countResult.get() == fourBtn) playerCount = 4;

        // Créer le tableau avec le profil actuel en premier
        String[] names = new String[playerCount];
        names[0] = currentSelectedProfile;

        String[] defaultNames = {"", "Stratège", "Tacticien", "Commandant"};
        String[] descriptions = {
                "🔴 " + currentSelectedProfile + " (ZQSD + A)",
                "🔵 Joueur 2 (↑↓←→ + ENTRÉE)",
                "🟡 Joueur 3 (IJKL + U)",
                "🟢 Joueur 4 (8456 + 7)"
        };

        for (int i = 1; i < playerCount; i++) {
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
                showError("Nom invalide", "Le nom ne peut pas être vide");
                i--; // Recommencer
                continue;
            }

            if (name.length() > 15) {
                showError("Nom trop long", "Maximum 15 caractères");
                i--; // Recommencer
                continue;
            }

            // Vérifier l'unicité
            boolean nameExists = name.equals(currentSelectedProfile);
            for (int j = 1; j < i; j++) {
                if (name.equals(names[j])) {
                    nameExists = true;
                    break;
                }
            }

            if (nameExists) {
                showError("Nom existant", "Ce nom est déjà utilisé");
                i--; // Recommencer
                continue;
            }

            names[i] = name;
        }

        return names;
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
     * Affiche une boîte de dialogue d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Obtient le profil actuellement sélectionné
     */
    public String getCurrentSelectedProfile() {
        return currentSelectedProfile;
    }

    /**
     * Définit le profil sélectionné (utilisé par d'autres contrôleurs)
     */
    public void setCurrentSelectedProfile(String profileName) {
        this.currentSelectedProfile = profileName;
        updateUI();
    }
}