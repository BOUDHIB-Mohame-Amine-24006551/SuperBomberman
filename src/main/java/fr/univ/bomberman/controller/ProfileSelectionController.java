// FILE: src/main/java/fr/univ/bomberman/controller/ProfileSelectionController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.util.ProfileManager;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la sélection et gestion des profils
 */
public class ProfileSelectionController {

    @FXML private ListView<String> profileListView;
    @FXML private Label selectedProfileLabel;
    @FXML private Label profileInfoLabel;

    @FXML private Button selectButton;
    @FXML private Button createButton;
    @FXML private Button viewStatsButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button cancelButton;

    private ProfileManager profileManager;
    private BombermanApp bombermanApp;
    private String selectedProfileName = null;
    private boolean profileSelected = false;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        profileManager = ProfileManager.getInstance();

        // Configuration de la liste des profils
        setupProfileList();

        // Charger les profils existants
        loadProfiles();

        // Désactiver certains boutons par défaut
        updateButtonStates();

        System.out.println("ProfileSelectionController initialisé");
    }

    /**
     * Configure la liste des profils
     */
    private void setupProfileList() {
        // Listener pour la sélection
        profileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedProfileName = newValue;
                    updateSelectedProfileInfo();
                    updateButtonStates();
                }
        );

        // Double-clic pour sélectionner
        profileListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedProfileName != null) {
                onSelectProfile();
            }
        });
    }

    /**
     * Charge la liste des profils
     */
    private void loadProfiles() {
        try {
            List<String> profiles = profileManager.listProfiles();
            ObservableList<String> profileItems = FXCollections.observableArrayList(profiles);
            profileListView.setItems(profileItems);

            if (profiles.isEmpty()) {
                selectedProfileLabel.setText("Aucun profil trouvé");
                profileInfoLabel.setText("Créez votre premier profil pour commencer !");
            } else {
                selectedProfileLabel.setText(profiles.size() + " profil(s) disponible(s)");
                profileInfoLabel.setText("Sélectionnez un profil dans la liste");
            }

        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les profils: " + e.getMessage());
        }
    }

    /**
     * Met à jour les informations du profil sélectionné
     */
    private void updateSelectedProfileInfo() {
        if (selectedProfileName == null) {
            selectedProfileLabel.setText("Aucun profil sélectionné");
            profileInfoLabel.setText("Sélectionnez un profil pour voir ses informations");
            return;
        }

        try {
            PlayerProfile profile = profileManager.loadProfile(selectedProfileName);

            selectedProfileLabel.setText("📂 " + profile.getPlayerName());

            StringBuilder info = new StringBuilder();
            info.append("🏆 Rang: ").append(profile.getRank().getDisplayName()).append("\n");
            info.append("🎮 Parties: ").append(profile.getTotalGamesPlayed()).append("\n");
            info.append("📈 Victoires: ").append(profile.getTotalWins()).append(" (")
                    .append(String.format("%.1f%%", profile.getWinRatio())).append(")\n");
            info.append("⏱️ Temps de jeu: ").append(profile.getFormattedTotalPlayTime()).append("\n");
            info.append("📅 Dernière partie: ").append(profile.getFormattedLastPlayDate());

            profileInfoLabel.setText(info.toString());

        } catch (BombermanException e) {
            profileInfoLabel.setText("Erreur lors du chargement du profil");
            System.err.println("Erreur profil: " + e.getMessage());
        }
    }

    /**
     * Met à jour l'état des boutons selon la sélection
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedProfileName != null;
        boolean hasProfiles = !profileListView.getItems().isEmpty();

        selectButton.setDisable(!hasSelection);
        viewStatsButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * Sélectionne le profil actuel
     */
    @FXML
    private void onSelectProfile() {
        if (selectedProfileName == null) {
            showError("Aucun profil", "Veuillez sélectionner un profil");
            return;
        }

        profileSelected = true;

        // Fermer la fenêtre
        Stage stage = (Stage) selectButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Crée un nouveau profil
     */
    @FXML
    private void onCreateProfile() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouveau Profil");
        dialog.setHeaderText("Création d'un nouveau profil");
        dialog.setContentText("Nom du joueur:");

        // Styliser la dialog
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/fr/univ/bomberman/css/default/theme.css").toExternalForm()
        );

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newProfileName = result.get().trim();

            // Validation
            if (newProfileName.length() > 20) {
                showError("Nom trop long", "Le nom ne peut pas dépasser 20 caractères");
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

                // Actualiser la liste
                loadProfiles();

                // Sélectionner le nouveau profil
                profileListView.getSelectionModel().select(newProfileName);

                showInfo("Profil créé", "Nouveau profil créé avec succès: " + newProfileName);

            } catch (BombermanException e) {
                showError("Erreur de création", "Impossible de créer le profil: " + e.getMessage());
            }
        }
    }

    /**
     * Affiche les statistiques du profil sélectionné
     */
    @FXML
    private void onViewStats() {
        if (selectedProfileName == null) {
            showError("Aucun profil", "Veuillez sélectionner un profil");
            return;
        }

        try {
            PlayerProfile profile = profileManager.loadProfile(selectedProfileName);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/stats.fxml"));
            Parent root = loader.load();

            ProfileStatsController controller = loader.getController();
            controller.setProfile(profile);

            Stage stage = new Stage();
            stage.setTitle("Statistiques - " + selectedProfileName);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(viewStatsButton.getScene().getWindow());
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir les statistiques: " + e.getMessage());
        }
    }

    /**
     * Supprime le profil sélectionné
     */
    @FXML
    private void onDeleteProfile() {
        if (selectedProfileName == null) {
            showError("Aucun profil", "Veuillez sélectionner un profil");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Supprimer le profil");
        confirmAlert.setHeaderText("Êtes-vous sûr ?");
        confirmAlert.setContentText("Cette action supprimera définitivement le profil de " +
                selectedProfileName + " et toutes ses statistiques.\n\nCette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = profileManager.deleteProfile(selectedProfileName);
            if (deleted) {
                showInfo("Profil supprimé", "Le profil a été supprimé avec succès");

                // Actualiser la liste
                loadProfiles();

                // Réinitialiser la sélection
                selectedProfileName = null;
                updateSelectedProfileInfo();
                updateButtonStates();
            } else {
                showError("Erreur", "Impossible de supprimer le profil");
            }
        }
    }

    /**
     * Actualise la liste des profils
     */
    @FXML
    private void onRefresh() {
        loadProfiles();
        selectedProfileName = null;
        updateSelectedProfileInfo();
        updateButtonStates();
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
        return selectedProfileName;
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