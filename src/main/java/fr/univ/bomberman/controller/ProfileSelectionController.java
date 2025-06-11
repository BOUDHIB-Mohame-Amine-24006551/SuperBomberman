// FILE: src/main/java/fr/univ/bomberman/controller/ProfileSelectionController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.utils.ProfileManager;
import fr.univ.bomberman.model.PlayerProfile;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.BombermanApp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la sélection et gestion des profils
 */
public class ProfileSelectionController {

    // Composants FXML correspondant exactement au fichier profile_selection.fxml
    @FXML private TableView<ProfileData> profilesTable;
    @FXML private TableColumn<ProfileData, String> nameColumn;
    @FXML private TableColumn<ProfileData, String> rankColumn;
    @FXML private TableColumn<ProfileData, Integer> gamesPlayedColumn;
    @FXML private TableColumn<ProfileData, String> winRateColumn;
    @FXML private TableColumn<ProfileData, String> creationDateColumn;
    @FXML private TableColumn<ProfileData, String> lastPlayColumn;

    @FXML private Label selectedProfileLabel;
    @FXML private Label profileDetailsLabel;
    @FXML private TextField newProfileNameField;

    @FXML private Button refreshButton;
    @FXML private Button createButton;
    @FXML private Button statsButton;
    @FXML private Button duplicateButton;
    @FXML private Button deleteButton;
    @FXML private Button importButton;
    @FXML private Button exportButton;
    @FXML private Button selectButton;
    @FXML private Button cancelButton;

    private ProfileManager profileManager;
    private BombermanApp bombermanApp;
    private ProfileData selectedProfile = null;
    private boolean profileSelected = false;

    /**
     * Classe interne pour représenter les données d'un profil dans la table
     */
    public static class ProfileData {
        private String name;
        private String rank;
        private Integer gamesPlayed;
        private String winRate;
        private String creationDate;
        private String lastPlay;
        private PlayerProfile playerProfile;

        public ProfileData(PlayerProfile profile) {
            this.playerProfile = profile;
            this.name = profile.getPlayerName();
            this.rank = profile.getRank().getDisplayName();
            this.gamesPlayed = profile.getTotalGamesPlayed();
            this.winRate = String.format("%.1f%%", profile.getWinRatio());
            this.creationDate = profile.getFormattedCreationDate();
            this.lastPlay = profile.getFormattedLastPlayDate();
        }

        // Getters pour JavaFX
        public String getName() { return name; }
        public String getRank() { return rank; }
        public Integer getGamesPlayed() { return gamesPlayed; }
        public String getWinRate() { return winRate; }
        public String getCreationDate() { return creationDate; }
        public String getLastPlay() { return lastPlay; }
        public PlayerProfile getPlayerProfile() { return playerProfile; }
    }

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        profileManager = ProfileManager.getInstance();

        // Configuration de la table
        setupProfileTable();

        // Charger les profils existants
        loadProfiles();

        // Désactiver certains boutons par défaut
        updateButtonStates();

        System.out.println("ProfileSelectionController initialisé");
    }

    /**
     * Configure la table des profils
     */
    private void setupProfileTable() {
        // Configuration des colonnes avec des cellValueFactory manuelles pour éviter les problèmes de modules
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        rankColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRank()));

        gamesPlayedColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getGamesPlayed()));

        winRateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getWinRate()));

        creationDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreationDate()));

        lastPlayColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastPlay()));

        // Listener pour la sélection
        profilesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedProfile = newValue;
                    updateSelectedProfileInfo();
                    updateButtonStates();
                }
        );

        // Double-clic pour sélectionner
        profilesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedProfile != null) {
                onSelectProfile();
            }
        });
    }

    /**
     * Charge la liste des profils
     */
    private void loadProfiles() {
        try {
            List<String> profileNames = profileManager.listProfiles();
            ObservableList<ProfileData> profileItems = FXCollections.observableArrayList();

            for (String profileName : profileNames) {
                try {
                    PlayerProfile profile = profileManager.loadProfile(profileName);
                    profileItems.add(new ProfileData(profile));
                } catch (BombermanException e) {
                    System.err.println("Erreur lors du chargement du profil " + profileName + ": " + e.getMessage());
                }
            }

            profilesTable.setItems(profileItems);

            if (profileItems.isEmpty()) {
                selectedProfileLabel.setText("Aucun profil trouvé");
                profileDetailsLabel.setText("Créez votre premier profil pour commencer !");
            } else {
                selectedProfileLabel.setText(profileItems.size() + " profil(s) disponible(s)");
                profileDetailsLabel.setText("Sélectionnez un profil dans la liste");
            }

        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les profils: " + e.getMessage());
        }
    }

    /**
     * Met à jour les informations du profil sélectionné
     */
    private void updateSelectedProfileInfo() {
        if (selectedProfile == null) {
            selectedProfileLabel.setText("Aucun profil sélectionné");
            profileDetailsLabel.setText("Sélectionnez un profil pour voir ses informations");
            return;
        }

        PlayerProfile profile = selectedProfile.getPlayerProfile();
        selectedProfileLabel.setText("📂 " + profile.getPlayerName());

        StringBuilder info = new StringBuilder();
        info.append("🏆 Rang: ").append(profile.getRank().getDisplayName()).append("\n");
        info.append("🎮 Parties jouées: ").append(profile.getTotalGamesPlayed()).append("\n");
        info.append("📈 Victoires: ").append(profile.getTotalWins()).append(" (")
                .append(String.format("%.1f%%", profile.getWinRatio())).append(")\n");
        info.append("💣 Bombes posées: ").append(profile.getTotalBombsPlaced()).append("\n");
        info.append("⚔️ Éliminations: ").append(profile.getTotalEliminatonsDealt()).append("\n");
        info.append("⏱️ Temps de jeu: ").append(profile.getFormattedTotalPlayTime()).append("\n");
        info.append("📅 Dernière partie: ").append(profile.getFormattedLastPlayDate());

        profileDetailsLabel.setText(info.toString());
    }

    /**
     * Met à jour l'état des boutons selon la sélection
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedProfile != null;

        selectButton.setDisable(!hasSelection);
        statsButton.setDisable(!hasSelection);
        duplicateButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        exportButton.setDisable(!hasSelection);
    }

    /**
     * Actualise la liste des profils
     */
    @FXML
    private void onRefresh() {
        loadProfiles();
        selectedProfile = null;
        updateSelectedProfileInfo();
        updateButtonStates();
    }

    /**
     * Crée un nouveau profil
     */
    @FXML
    private void onCreateProfile() {
        String newProfileName = newProfileNameField.getText().trim();

        if (newProfileName.isEmpty()) {
            showError("Nom invalide", "Veuillez entrer un nom de profil");
            return;
        }

        // Validation
        if (newProfileName.length() > 15) {
            showError("Nom trop long", "Le nom ne peut pas dépasser 15 caractères");
            return;
        }

        if (profileManager.profileExists(newProfileName)) {
            showError("Profil existant", "Un profil avec ce nom existe déjà");
            return;
        }

        try {
            // Créer et sauvegarder le nouveau profil
            PlayerProfile newProfile = new PlayerProfile(newProfileName);
            profileManager.saveProfile(newProfile);

            // Vider le champ de texte
            newProfileNameField.clear();

            // Actualiser la liste
            loadProfiles();

            // Sélectionner le nouveau profil
            for (ProfileData profileData : profilesTable.getItems()) {
                if (profileData.getName().equals(newProfileName)) {
                    profilesTable.getSelectionModel().select(profileData);
                    break;
                }
            }

            showInfo("Profil créé", "Nouveau profil créé avec succès: " + newProfileName);

        } catch (BombermanException e) {
            showError("Erreur de création", "Impossible de créer le profil: " + e.getMessage());
        }
    }

    /**
     * Affiche les statistiques du profil sélectionné
     */
    @FXML
    private void onShowStats() {
        if (selectedProfile == null) {
            showError("Aucun profil", "Veuillez sélectionner un profil");
            return;
        }

        try {
            PlayerProfile profile = selectedProfile.getPlayerProfile();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/stats.fxml"));
            Parent root = loader.load();

            ProfileStatsController controller = loader.getController();
            controller.setProfile(profile);

            Stage stage = new Stage();
            stage.setTitle("Statistiques - " + profile.getPlayerName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(statsButton.getScene().getWindow());
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les statistiques: " + e.getMessage());
        }
    }

    /**
     * Duplique le profil sélectionné
     */
    @FXML
    private void onDuplicateProfile() {
        if (selectedProfile == null) {
            showError("Aucun profil", "Veuillez sélectionner un profil");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedProfile.getName() + "_copie");
        dialog.setTitle("Dupliquer le profil");
        dialog.setHeaderText("Duplication du profil: " + selectedProfile.getName());
        dialog.setContentText("Nouveau nom:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();

            if (profileManager.profileExists(newName)) {
                showError("Nom existant", "Un profil avec ce nom existe déjà");
                return;
            }

            try {
                PlayerProfile originalProfile = selectedProfile.getPlayerProfile();
                PlayerProfile duplicatedProfile = new PlayerProfile(newName);

                // Copier les préférences (sans les statistiques)
                duplicatedProfile.setPreferredTheme(originalProfile.getPreferredTheme());
                duplicatedProfile.setSoundEnabled(originalProfile.isSoundEnabled());
                duplicatedProfile.setPreferredBotDifficulty(originalProfile.getPreferredBotDifficulty());

                profileManager.saveProfile(duplicatedProfile);
                loadProfiles();

                showInfo("Profil dupliqué", "Profil dupliqué avec succès: " + newName);

            } catch (BombermanException e) {
                showError("Erreur de duplication", e.getMessage());
            }
        }
    }

    /**
     * Supprime le profil sélectionné
     */
    @FXML
    private void onDeleteProfile() {
        if (selectedProfile == null) {
            showError("Aucun profil", "Veuillez sélectionner un profil");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Supprimer le profil");
        confirmAlert.setHeaderText("Êtes-vous sûr ?");
        confirmAlert.setContentText("Cette action supprimera définitivement le profil de " +
                selectedProfile.getName() + " et toutes ses statistiques.\n\nCette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = profileManager.deleteProfile(selectedProfile.getName());
            if (deleted) {
                showInfo("Profil supprimé", "Le profil a été supprimé avec succès");

                // Actualiser la liste
                loadProfiles();

                // Réinitialiser la sélection
                selectedProfile = null;
                updateSelectedProfileInfo();
                updateButtonStates();
            } else {
                showError("Erreur", "Impossible de supprimer le profil");
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

        Stage stage = (Stage) importButton.getScene().getWindow();
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

                loadProfiles();
                showInfo("Import réussi", "Profil importé: " + importedProfile.getPlayerName());

            } catch (BombermanException e) {
                showError("Erreur d'import", e.getMessage());
            }
        }
    }

    /**
     * Exporte le profil sélectionné vers un fichier
     */
    @FXML
    private void onExportProfile() {
        if (selectedProfile == null) {
            showError("Aucun profil", "Veuillez sélectionner un profil");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le profil");
        fileChooser.setInitialFileName(selectedProfile.getName() + "_profile.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers JSON", "*.json")
        );

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                profileManager.exportProfile(selectedProfile.getName(), file.getAbsolutePath());
                showInfo("Export réussi", "Profil exporté vers: " + file.getName());
            } catch (BombermanException e) {
                showError("Erreur d'export", e.getMessage());
            }
        }
    }

    /**
     * Sélectionne le profil actuel
     */
    @FXML
    private void onSelectProfile() {
        if (selectedProfile == null) {
            showError("Aucun profil", "Veuillez sélectionner un profil");
            return;
        }

        profileSelected = true;

        // Fermer la fenêtre
        Stage stage = (Stage) selectButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Annule la sélection
     */
    @FXML
    private void onCancel() {
        profileSelected = false;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Définit la référence vers l'application principale
     */
    public void setBombermanApp(BombermanApp app) {
        this.bombermanApp = app;
    }

    /**
     * @return true si un profil a été sélectionné
     */
    public boolean isProfileSelected() {
        return profileSelected;
    }

    /**
     * @return le nom du profil sélectionné
     */
    public String getSelectedProfileName() {
        return selectedProfile != null ? selectedProfile.getName() : null;
    }

    /**
     * Retourne le profil sélectionné sous forme de PlayerProfile
     */
    public PlayerProfile getSelectedProfile() {
        return selectedProfile != null ? selectedProfile.getPlayerProfile() : null;
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
}