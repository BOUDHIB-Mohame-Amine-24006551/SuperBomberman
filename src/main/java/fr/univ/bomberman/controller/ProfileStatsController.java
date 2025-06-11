
package fr.univ.bomberman.controller;

import fr.univ.bomberman.model.PlayerProfile;
import fr.univ.bomberman.model.GameSession;
import fr.univ.bomberman.model.GameModeStats;
import fr.univ.bomberman.utils.ProfileManager;
import fr.univ.bomberman.exceptions.BombermanException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Contrôleur pour l'interface des statistiques de profil
 */
public class ProfileStatsController {

    @FXML private Label playerNameLabel;
    @FXML private Label rankLabel;
    @FXML private Label creationDateLabel;
    @FXML private Label lastPlayDateLabel;

    // Statistiques générales
    @FXML private Label totalGamesLabel;
    @FXML private Label totalWinsLabel;
    @FXML private Label totalLossesLabel;
    @FXML private Label winRatioLabel;
    @FXML private Label totalPlayTimeLabel;
    @FXML private Label totalBombsLabel;
    @FXML private Label eliminationsLabel;
    @FXML private Label deathsLabel;
    @FXML private Label kdRatioLabel;

    // Statistiques par mode
    @FXML private Label classicWinsLabel;
    @FXML private Label classicGamesLabel;
    @FXML private Label classicWinRateLabel;
    @FXML private Label battleRoyaleWinsLabel;
    @FXML private Label battleRoyaleGamesLabel;
    @FXML private Label battleRoyaleWinRateLabel;
    @FXML private Label botWinsLabel;
    @FXML private Label botGamesLabel;
    @FXML private Label botWinRateLabel;
    @FXML private Label ctfWinsLabel;
    @FXML private Label ctfGamesLabel;
    @FXML private Label ctfWinRateLabel;

    // Préférences
    @FXML private TextField playerNameField;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private CheckBox soundCheckBox;
    @FXML private ComboBox<String> botDifficultyComboBox;

    // Historique des parties
    @FXML private TableView<GameSession> historyTable;
    @FXML private TableColumn<GameSession, String> dateColumn;
    @FXML private TableColumn<GameSession, String> modeColumn;
    @FXML private TableColumn<GameSession, String> resultColumn;
    @FXML private TableColumn<GameSession, String> durationColumn;
    @FXML private TableColumn<GameSession, Integer> bombsColumn;
    @FXML private TableColumn<GameSession, Integer> eliminationsColumn;

    // Boutons
    @FXML private Button savePreferencesButton;
    @FXML private Button exportProfileButton;
    @FXML private Button importProfileButton;
    @FXML private Button deleteProfileButton;
    @FXML private Button closeButton;

    private PlayerProfile currentProfile;
    private ProfileManager profileManager;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        profileManager = ProfileManager.getInstance();

        // Initialiser les ComboBox
        initializeComboBoxes();

        // Initialiser la table d'historique
        initializeHistoryTable();

        System.out.println("ProfileStatsController initialisé");
    }

    /**
     * Initialise les ComboBox avec les valeurs par défaut
     */
    private void initializeComboBoxes() {
        // Thèmes disponibles
        themeComboBox.getItems().addAll("default", "pokemon");
        themeComboBox.setValue("default");

        // Difficultés de bot
        botDifficultyComboBox.getItems().addAll("Facile", "Moyen", "Difficile");
        botDifficultyComboBox.setValue("Moyen");
    }

    /**
     * Initialise la table d'historique des parties
     */
    private void initializeHistoryTable() {
        // Configuration des colonnes
        dateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));

        modeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGameDescription()));

        resultColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getResultText()));

        durationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDuration()));

        bombsColumn.setCellValueFactory(new PropertyValueFactory<>("bombsPlaced"));
        eliminationsColumn.setCellValueFactory(new PropertyValueFactory<>("eliminationsDealt"));

        // Style conditionnel pour les résultats
        resultColumn.setCellFactory(column -> new TableCell<GameSession, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Couleur selon le résultat
                    if (item.contains("VICTOIRE")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item.contains("DÉFAITE")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    /**
     * Affiche les informations du profil
     */
    private void displayProfile() {
        if (currentProfile == null) return;

        // Informations de base
        playerNameLabel.setText(currentProfile.getPlayerName());
        rankLabel.setText(currentProfile.getRank().getDisplayName());
        creationDateLabel.setText(currentProfile.getFormattedCreationDate());
        lastPlayDateLabel.setText(currentProfile.getFormattedLastPlayDate());

        // Statistiques générales
        totalGamesLabel.setText(String.valueOf(currentProfile.getTotalGamesPlayed()));
        totalWinsLabel.setText(String.valueOf(currentProfile.getTotalWins()));
        totalLossesLabel.setText(String.valueOf(currentProfile.getTotalLosses()));
        winRatioLabel.setText(String.format("%.1f%%", currentProfile.getWinRatio()));
        totalPlayTimeLabel.setText(currentProfile.getFormattedTotalPlayTime());
        totalBombsLabel.setText(String.valueOf(currentProfile.getTotalBombsPlaced()));
        eliminationsLabel.setText(String.valueOf(currentProfile.getTotalEliminatonsDealt()));
        deathsLabel.setText(String.valueOf(currentProfile.getTotalDeaths()));
        kdRatioLabel.setText(String.format("%.2f", currentProfile.getKillDeathRatio()));

        // Statistiques par mode
        displayModeStats();

        // Préférences
        playerNameField.setText(currentProfile.getPlayerName());
        themeComboBox.setValue(currentProfile.getPreferredTheme());
        soundCheckBox.setSelected(currentProfile.isSoundEnabled());

        String[] difficulties = {"Facile", "Moyen", "Difficile"};
        int diffIndex = currentProfile.getPreferredBotDifficulty() - 1;
        if (diffIndex >= 0 && diffIndex < difficulties.length) {
            botDifficultyComboBox.setValue(difficulties[diffIndex]);
        }

        // Historique des parties
        displayGameHistory();
    }

    /**
     * Affiche les statistiques par mode de jeu
     */
    private void displayModeStats() {
        // Mode Classique
        GameModeStats classic = currentProfile.getClassicModeStats();
        classicGamesLabel.setText(String.valueOf(classic.getGamesPlayed()));
        classicWinsLabel.setText(String.valueOf(classic.getWins()));
        classicWinRateLabel.setText(String.format("%.1f%%", classic.getWinRatio()));

        // Bataille Royale
        GameModeStats battleRoyale = currentProfile.getBattleRoyaleStats();
        battleRoyaleGamesLabel.setText(String.valueOf(battleRoyale.getGamesPlayed()));
        battleRoyaleWinsLabel.setText(String.valueOf(battleRoyale.getWins()));
        battleRoyaleWinRateLabel.setText(String.format("%.1f%%", battleRoyale.getWinRatio()));

        // Mode Bot
        GameModeStats bot = currentProfile.getBotModeStats();
        botGamesLabel.setText(String.valueOf(bot.getGamesPlayed()));
        botWinsLabel.setText(String.valueOf(bot.getWins()));
        botWinRateLabel.setText(String.format("%.1f%%", bot.getWinRatio()));

        // CTF
        GameModeStats ctf = currentProfile.getCtfModeStats();
        ctfGamesLabel.setText(String.valueOf(ctf.getGamesPlayed()));
        ctfWinsLabel.setText(String.valueOf(ctf.getWins()));
        ctfWinRateLabel.setText(String.format("%.1f%%", ctf.getWinRatio()));
    }

    /**
     * Affiche l'historique des parties
     */
    private void displayGameHistory() {
        // Version simplifiée : pas d'historique détaillé disponible
        if (currentProfile.getRecentGames() == null || currentProfile.getRecentGames().isEmpty()) {
            // Créer un message d'information dans la table
            GameSession dummySession = new GameSession();
            dummySession.setGameMode(fr.univ.bomberman.model.GameMode.REAL_TIME);
            dummySession.setWon(false);
            dummySession.setDurationSeconds(0);
            dummySession.setBombsPlaced(0);
            dummySession.setEliminationsDealt(0);
            dummySession.setWinnerName("Historique non disponible en version simplifiée");

            // Afficher un message dans la table
            ObservableList<GameSession> emptyData = FXCollections.observableArrayList();
            historyTable.setItems(emptyData);

            // Ajouter un placeholder
            historyTable.setPlaceholder(new Label("📜 Historique détaillé non disponible\nVersion simplifiée du système de profil"));
        } else {
            List<GameSession> recentGames = currentProfile.getRecentGames();
            ObservableList<GameSession> tableData = FXCollections.observableArrayList(recentGames);
            historyTable.setItems(tableData);
        }
    }

    /**
     * Sauvegarde les préférences modifiées
     */
    @FXML
    private void onSavePreferences() {
        if (currentProfile == null) return;

        try {
            // Valider le nouveau nom
            String newName = playerNameField.getText().trim();
            if (newName.isEmpty()) {
                showError("Nom invalide", "Le nom de joueur ne peut pas être vide.");
                return;
            }

            // Vérifier si le nom a changé et s'il n'existe pas déjà
            boolean nameChanged = !newName.equals(currentProfile.getPlayerName());
            if (nameChanged && profileManager.profileExists(newName)) {
                showError("Nom existant", "Un profil avec ce nom existe déjà.");
                return;
            }

            // Supprimer l'ancien profil si le nom a changé
            if (nameChanged) {
                profileManager.deleteProfile(currentProfile.getPlayerName());
                currentProfile.setPlayerName(newName);
            }

            // Mettre à jour les préférences
            String selectedTheme = themeComboBox.getValue();
            boolean soundEnabled = soundCheckBox.isSelected();

            String selectedDifficulty = botDifficultyComboBox.getValue();
            int botDifficulty = 2; // Moyen par défaut
            switch (selectedDifficulty) {
                case "Facile": botDifficulty = 1; break;
                case "Moyen": botDifficulty = 2; break;
                case "Difficile": botDifficulty = 3; break;
            }

            currentProfile.setPreferredTheme(selectedTheme);
            currentProfile.setSoundEnabled(soundEnabled);
            currentProfile.setPreferredBotDifficulty(botDifficulty);

            // Sauvegarder
            profileManager.saveProfile(currentProfile);

            // Actualiser l'affichage
            displayProfile();

            showInfo("Succès", "Préférences sauvegardées avec succès!");

        } catch (BombermanException e) {
            showError("Erreur de sauvegarde", e.getMessage());
        }
    }

    /**
     * Exporte le profil vers un fichier
     */
    @FXML
    private void onExportProfile() {
        if (currentProfile == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le profil");
        fileChooser.setInitialFileName(currentProfile.getPlayerName() + "_profile.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers JSON", "*.json")
        );

        Stage stage = (Stage) exportProfileButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                profileManager.exportProfile(currentProfile.getPlayerName(), file.getAbsolutePath());
                showInfo("Export réussi", "Profil exporté vers: " + file.getName());
            } catch (BombermanException e) {
                showError("Erreur d'export", e.getMessage());
            }
        }
    }

    /**
     * Importe un profil depuis un fichier
     */
    @FXML
    private void onImportProfile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers JSON", "*.json")
        );

        Stage stage = (Stage) importProfileButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                PlayerProfile importedProfile = profileManager.importProfile(file.getAbsolutePath());

                // Vérifier si un profil avec ce nom existe déjà
                if (profileManager.profileExists(importedProfile.getPlayerName())) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Profil existant");
                    confirmAlert.setHeaderText("Un profil avec ce nom existe déjà");
                    confirmAlert.setContentText("Voulez-vous écraser le profil existant ?");

                    if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        return;
                    }
                }

                currentProfile = importedProfile;
                displayProfile();
                showInfo("Import réussi", "Profil importé: " + importedProfile.getPlayerName());

            } catch (BombermanException e) {
                showError("Erreur d'import", e.getMessage());
            }
        }
    }

    /**
     * Supprime le profil actuel
     */
    @FXML
    private void onDeleteProfile() {
        if (currentProfile == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Supprimer le profil");
        confirmAlert.setHeaderText("Êtes-vous sûr ?");
        confirmAlert.setContentText("Cette action supprimera définitivement le profil de " +
                currentProfile.getPlayerName() + " et toutes ses statistiques.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean deleted = profileManager.deleteProfile(currentProfile.getPlayerName());
            if (deleted) {
                showInfo("Profil supprimé", "Le profil a été supprimé avec succès.");
                onClose(); // Fermer la fenêtre
            } else {
                showError("Erreur", "Impossible de supprimer le profil.");
            }
        }
    }

    /**
     * Ferme la fenêtre
     */
    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
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
     * Définit le profil à afficher (appelé depuis l'extérieur)
     */
    public void setProfile(PlayerProfile profile) {
        this.currentProfile = profile;
        displayProfile();
    }

}