// FILE: src/main/java/fr/univ/bomberman/controller/ProfileSelectionController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.model.PlayerProfile;
import fr.univ.bomberman.util.ProfileManager;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.BombermanApp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la sélection et gestion avancée des profils
 */
public class ProfileSelectionController {

    @FXML private TableView<PlayerProfile> profilesTable;
    @FXML private TableColumn<PlayerProfile, String> nameColumn;
    @FXML private TableColumn<PlayerProfile, String> rankColumn;
    @FXML private TableColumn<PlayerProfile, String> creationDateColumn;
    @FXML private TableColumn<PlayerProfile, String> lastPlayColumn;
    @FXML private TableColumn<PlayerProfile, Integer> gamesPlayedColumn;
    @FXML private TableColumn<PlayerProfile, String> winRateColumn;

    @FXML private Label selectedProfileLabel;
    @FXML private Label profileDetailsLabel;
    @FXML private TextField newProfileNameField;

    @FXML private Button selectButton;
    @FXML private Button createButton;
    @FXML private Button deleteButton;
    @FXML private Button duplicateButton;
    @FXML private Button importButton;
    @FXML private Button exportButton;
    @FXML private Button statsButton;
    @FXML private Button refreshButton;
    @FXML private Button cancelButton;

    private ProfileManager profileManager;
    private PlayerProfile selectedProfile;
    private BombermanApp bombermanApp;
    private boolean profileSelected = false;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        profileManager = ProfileManager.getInstance();

        // Configuration des colonnes du tableau
        setupTableColumns();

        // Configuration des événements
        setupEventHandlers();

        // Chargement initial des profils
        refreshProfiles();

        // État initial des boutons
        updateButtonStates();

        System.out.println("ProfileSelectionController initialisé");
    }

    /**
     * Configuration des colonnes du tableau
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("playerName"));

        rankColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRank().getDisplayName()));

        creationDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFormattedCreationDate()));

        lastPlayColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFormattedLastPlayDate()));

        gamesPlayedColumn.setCellValueFactory(new PropertyValueFactory<>("totalGamesPlayed"));

        winRateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f%%", cellData.getValue().getWinRatio())));

        // Style conditionnel pour le taux de victoire
        winRateColumn.setCellFactory(column -> new TableCell<PlayerProfile, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    double winRate = Double.parseDouble(item.replace("%", ""));
                    if (winRate >= 70) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (winRate >= 50) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });
    }

    /**
     * Configuration des gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Sélection dans le tableau
        profilesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedProfile = newValue;
                    updateProfileDetails();
                    updateButtonStates();
                });

        // Double-clic pour sélectionner
        profilesTable.setOnMouseClicked(this::onTableDoubleClick);

        // Validation du nom lors de la saisie
        newProfileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNewProfileName();
        });
    }

    /**
     * Gestion du double-clic sur le tableau
     */
    private void onTableDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2 && selectedProfile != null) {
            onSelectProfile();
        }
    }

    /**
     * Actualise la liste des profils
     */
    @FXML
    public void refreshProfiles() {
        try {
            List<String> profileNames = profileManager.listProfiles();
            ObservableList<PlayerProfile> profiles = FXCollections.observableArrayList();

            for (String name : profileNames) {
                try {
                    PlayerProfile profile = profileManager.loadProfile(name);
                    profiles.add(profile);
                } catch (BombermanException e) {
                    System.err.println("Erreur lors du chargement du profil " + name + ": " + e.getMessage());
                }
            }

            // Trier par date de dernière connexion (plus récent en premier)
            profiles.sort((p1, p2) -> p2.getLastPlayDate().compareTo(p1.getLastPlayDate()));

            profilesTable.setItems(profiles);

            // Message si aucun profil
            if (profiles.isEmpty()) {
                profilesTable.setPlaceholder(new Label("🆕 Aucun profil trouvé\nCréez votre premier profil ci-dessous"));
            }

        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les profils: " + e.getMessage());
        }
    }

    /**
     * Met à jour les détails du profil sélectionné
     */
    private void updateProfileDetails() {
        if (selectedProfile == null) {
            selectedProfileLabel.setText("Aucun profil sélectionné");
            profileDetailsLabel.setText("Sélectionnez un profil pour voir ses détails");
        } else {
            selectedProfileLabel.setText("📋 " + selectedProfile.getPlayerName());

            StringBuilder details = new StringBuilder();
            details.append("🏆 Rang: ").append(selectedProfile.getRank().getDisplayName()).append("\n");
            details.append("🎮 Parties jouées: ").append(selectedProfile.getTotalGamesPlayed()).append("\n");
            details.append("✅ Victoires: ").append(selectedProfile.getTotalWins()).append("\n");
            details.append("📊 Taux de victoire: ").append(String.format("%.1f%%", selectedProfile.getWinRatio())).append("\n");
            details.append("⏱️ Temps de jeu: ").append(selectedProfile.getFormattedTotalPlayTime()).append("\n");
            details.append("🎨 Thème préféré: ").append(selectedProfile.getPreferredTheme()).append("\n");
            details.append("📅 Créé le: ").append(selectedProfile.getFormattedCreationDate()).append("\n");
            details.append("🕐 Dernière partie: ").append(selectedProfile.getFormattedLastPlayDate());

            profileDetailsLabel.setText(details.toString());
        }
    }

    /**
     * Met à jour l'état des boutons
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedProfile != null;

        selectButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        duplicateButton.setDisable(!hasSelection);
        exportButton.setDisable(!hasSelection);
        statsButton.setDisable(!hasSelection);

        validateNewProfileName();
    }

    /**
     * Valide le nom du nouveau profil
     */
    private void validateNewProfileName() {
        String name = newProfileNameField.getText().trim();
        boolean isValid = !name.isEmpty() &&
                name.length() <= 15 &&
                !profileManager.profileExists(name);

        createButton.setDisable(!isValid);

        // Feedback visuel
        if (name.isEmpty()) {
            newProfileNameField.setStyle("");
        } else if (name.length() > 15) {
            newProfileNameField.setStyle("-fx-border-color: orange;");
        } else if (profileManager.profileExists(name)) {
            newProfileNameField.setStyle("-fx-border-color: red;");
        } else {
            newProfileNameField.setStyle("-fx-border-color: green;");
        }
    }

    /**
     * Sélectionne le profil et ferme la fenêtre
     */
    @FXML
    public void onSelectProfile() {
        if (selectedProfile != null) {
            profileSelected = true;

            // Mettre à jour la date de dernière connexion
            try {
                selectedProfile.updateLastPlayDate();
                profileManager.saveProfile(selectedProfile);
            } catch (BombermanException e) {
                System.err.println("Erreur lors de la mise à jour du profil: " + e.getMessage());
            }

            closeWindow();
        }
    }

    /**
     * Crée un nouveau profil
     */
    @FXML
    public void onCreateProfile() {
        String name = newProfileNameField.getText().trim();

        if (name.isEmpty()) {
            showError("Nom invalide", "Le nom ne peut pas être vide.");
            return;
        }

        if (name.length() > 15) {
            showError("Nom trop long", "Le nom ne peut pas dépasser 15 caractères.");
            return;
        }

        if (profileManager.profileExists(name)) {
            showError("Profil existant", "Un profil avec ce nom existe déjà.");
            return;
        }

        try {
            PlayerProfile newProfile = profileManager.loadProfile(name); // Crée un nouveau profil
            profileManager.saveProfile(newProfile);

            showInfo("Profil créé", "Le profil '" + name + "' a été créé avec succès!");

            newProfileNameField.clear();
            refreshProfiles();

            // Sélectionner le nouveau profil
            profilesTable.getSelectionModel().select(newProfile);

        } catch (BombermanException e) {
            showError("Erreur", "Impossible de créer le profil: " + e.getMessage());
        }
    }

    /**
     * Supprime le profil sélectionné
     */
    @FXML
    public void onDeleteProfile() {
        if (selectedProfile == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Supprimer le profil");
        confirmAlert.setHeaderText("⚠️ Confirmation de suppression");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le profil '" +
                selectedProfile.getPlayerName() + "' ?\n\n" +
                "Toutes les statistiques seront perdues définitivement.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = profileManager.deleteProfile(selectedProfile.getPlayerName());
            if (deleted) {
                showInfo("Profil supprimé", "Le profil a été supprimé avec succès.");
                refreshProfiles();
            } else {
                showError("Erreur", "Impossible de supprimer le profil.");
            }
        }
    }

    /**
     * Duplique le profil sélectionné
     */
    @FXML
    public void onDuplicateProfile() {
        if (selectedProfile == null) return;

        TextInputDialog dialog = new TextInputDialog(selectedProfile.getPlayerName() + "_Copie");
        dialog.setTitle("Dupliquer le profil");
        dialog.setHeaderText("📋 Duplication de profil");
        dialog.setContentText("Nom du nouveau profil:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get().trim();

            if (newName.isEmpty()) {
                showError("Nom invalide", "Le nom ne peut pas être vide.");
                return;
            }

            if (newName.length() > 15) {
                showError("Nom trop long", "Le nom ne peut pas dépasser 15 caractères.");
                return;
            }

            if (profileManager.profileExists(newName)) {
                showError("Profil existant", "Un profil avec ce nom existe déjà.");
                return;
            }

            try {
                PlayerProfile duplicatedProfile = selectedProfile.duplicate(newName);
                profileManager.saveProfile(duplicatedProfile);

                showInfo("Profil dupliqué", "Le profil a été dupliqué avec succès!");
                refreshProfiles();

                // Sélectionner le profil dupliqué
                profilesTable.getSelectionModel().select(duplicatedProfile);

            } catch (BombermanException e) {
                showError("Erreur", "Impossible de dupliquer le profil: " + e.getMessage());
            }
        }
    }

    /**
     * Importe un profil depuis un fichier
     */
    @FXML
    public void onImportProfile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers de profil JSON", "*.json"));

        Stage stage = (Stage) importButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                PlayerProfile importedProfile = profileManager.importProfile(file.getAbsolutePath());

                // Vérifier si le profil existe déjà
                if (profileManager.profileExists(importedProfile.getPlayerName())) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Profil existant");
                    confirmAlert.setHeaderText("Conflit de nom");
                    confirmAlert.setContentText("Un profil avec le nom '" + importedProfile.getPlayerName() +
                            "' existe déjà.\n\nVoulez-vous l'écraser ?");

                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        return;
                    }
                }

                profileManager.saveProfile(importedProfile);
                showInfo("Import réussi", "Le profil '" + importedProfile.getPlayerName() +
                        "' a été importé avec succès!");

                refreshProfiles();

                // Sélectionner le profil importé
                profilesTable.getSelectionModel().select(importedProfile);

            } catch (BombermanException e) {
                showError("Erreur d'import", "Impossible d'importer le profil: " + e.getMessage());
            }
        }
    }

    /**
     * Exporte le profil sélectionné
     */
    @FXML
    public void onExportProfile() {
        if (selectedProfile == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le profil");
        fileChooser.setInitialFileName(selectedProfile.getPlayerName() + "_profile.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers de profil JSON", "*.json"));

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                profileManager.exportProfile(selectedProfile.getPlayerName(), file.getAbsolutePath());
                showInfo("Export réussi", "Le profil a été exporté vers:\n" + file.getName());
            } catch (BombermanException e) {
                showError("Erreur d'export", "Impossible d'exporter le profil: " + e.getMessage());
            }
        }
    }

    /**
     * Ouvre les statistiques détaillées du profil sélectionné
     */
    @FXML
    public void onShowStats() {
        if (selectedProfile == null) return;

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/stats.fxml"));
            Parent root = loader.load();

            ProfileStatsController controller = loader.getController();
            controller.setProfile(selectedProfile);

            Stage stage = new Stage();
            stage.setTitle("Statistiques - " + selectedProfile.getPlayerName());
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les statistiques: " + e.getMessage());
        }
    }

    /**
     * Annule la sélection et ferme la fenêtre
     */
    @FXML
    public void onCancel() {
        profileSelected = false;
        selectedProfile = null;
        closeWindow();
    }

    /**
     * Ferme la fenêtre
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
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

    // Getters pour accès externe
    public boolean isProfileSelected() { return profileSelected; }
    public PlayerProfile getSelectedProfile() { return selectedProfile; }
    public void setBombermanApp(BombermanApp app) { this.bombermanApp = app; }
}