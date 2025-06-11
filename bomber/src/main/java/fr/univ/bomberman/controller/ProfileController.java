// FILE: src/main/java/fr/univ/bomberman/controller/ProfileController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.model.PlayerProfile;
import fr.univ.bomberman.model.PlayerProfileManager;
import fr.univ.bomberman.utils.JsonUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Contrôleur pour la fenêtre de gestion de profil.
 */
public class ProfileController {

    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private ComboBox<String> avatarComboBox;
    
    @FXML
    private Label gamesPlayedLabel;
    
    @FXML
    private Label gamesWonLabel;
    
    @FXML
    private Label winRateLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;
    
    @FXML
    private ListView<String> profilesListView;
    
    @FXML
    private GridPane profileStatsGrid;
    
    private PlayerProfile currentProfile;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

    /**
     * Initialisation du contrôleur de profil.
     * Charge le profil existant s'il existe.
     */
    @FXML
    public void initialize() {
        System.out.println("ProfileController initialisé");
        
        // Initialiser le gestionnaire de profils
        PlayerProfileManager.initialize();
        
        // Initialiser les options d'avatar
        avatarComboBox.getItems().addAll(Arrays.asList("default", "pokemon", "retro"));
        avatarComboBox.setValue("default");
        
        // Charger le profil existant
        currentProfile = PlayerProfileManager.getCurrentProfile();
        if (currentProfile == null) {
            currentProfile = new PlayerProfile();
            PlayerProfileManager.setCurrentProfile(currentProfile);
        }
            
        // Remplir les champs avec les données du profil
        firstNameField.setText(currentProfile.getFirstName());
        lastNameField.setText(currentProfile.getLastName());
        avatarComboBox.setValue(currentProfile.getAvatar());
        
        // Afficher les statistiques
        updateStatisticsDisplay();
        
        // Initialiser la liste des profils
        updateProfilesList();
        
        // Configurer la liste des profils
        if (profilesListView != null) {
            profilesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            profilesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadSelectedProfile(newVal);
                }
            });
        }
    }
    
    /**
     * Charge le profil sélectionné dans la liste
     */
    private void loadSelectedProfile(String profileDisplay) {
        List<PlayerProfile> profiles = PlayerProfileManager.getProfiles();
        for (PlayerProfile profile : profiles) {
            if (formatProfileForList(profile).equals(profileDisplay)) {
                currentProfile = profile;
                PlayerProfileManager.setCurrentProfile(currentProfile);
                
                // Mettre à jour les champs
                firstNameField.setText(currentProfile.getFirstName());
                lastNameField.setText(currentProfile.getLastName());
                avatarComboBox.setValue(currentProfile.getAvatar());
                
                // Afficher les statistiques
                updateStatisticsDisplay();
                break;
            }
        }
    }
    
    /**
     * Met à jour la liste des profils
     */
    private void updateProfilesList() {
        if (profilesListView != null) {
            List<PlayerProfile> profiles = PlayerProfileManager.getProfiles();
            String[] displayProfiles = profiles.stream()
                .map(this::formatProfileForList)
                .toArray(String[]::new);
            
            profilesListView.setItems(FXCollections.observableArrayList(displayProfiles));
        }
    }
    
    /**
     * Formate un profil pour l'affichage dans la liste
     */
    private String formatProfileForList(PlayerProfile profile) {
        return profile.getFullName() + " (" + profile.getGamesWon() + " victoires)";
    }
    
    /**
     * Met à jour l'affichage des statistiques
     */
    private void updateStatisticsDisplay() {
        gamesPlayedLabel.setText(String.valueOf(currentProfile.getGamesPlayed()));
        gamesWonLabel.setText(String.valueOf(currentProfile.getGamesWon()));
        winRateLabel.setText(DECIMAL_FORMAT.format(currentProfile.getWinRate()) + "%");
    }

    /**
     * Sauvegarde le profil utilisateur.
     *
     * @throws BombermanException si la sauvegarde échoue
     */
    @FXML
    private void onSave() throws BombermanException {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        
        if ((firstName == null || firstName.trim().isEmpty()) && 
            (lastName == null || lastName.trim().isEmpty())) {
            // Afficher une alerte d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Nom invalide");
            alert.setContentText("Vous devez renseigner au moins un prénom ou un nom.");
            alert.showAndWait();
            return;
        }

        try {
            // Mettre à jour le profil
            currentProfile.setFirstName(firstName != null ? firstName.trim() : "");
            currentProfile.setLastName(lastName != null ? lastName.trim() : "");
            currentProfile.setAvatar(avatarComboBox.getValue());
            
            // Sauvegarder le profil
            PlayerProfileManager.saveProfile(currentProfile);
            PlayerProfileManager.setCurrentProfile(currentProfile);

            System.out.println("Profil sauvegardé: " + currentProfile.getFullName());
            
            // Mettre à jour la liste des profils
            updateProfilesList();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Profil sauvegardé");
            alert.setContentText("Le profil '" + currentProfile.getFullName() + "' a été sauvegardé avec succès.");
            alert.showAndWait();

            // Fermer la fenêtre
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new BombermanException("Impossible de sauvegarder le profil", e);
        }
    }

    /**
     * Annule et ferme la fenêtre de gestion de profil.
     */
    @FXML
    private void onCancel() {
        System.out.println("Annulation de la modification du profil");
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Getter pour le profil courant
     * 
     * @return le profil joueur actuel
     */
    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }
    
    /**
     * Incrémente le nombre de parties jouées pour le profil courant et sauvegarde
     */
    public static void incrementGamesPlayed() {
        PlayerProfileManager.incrementGamesPlayed();
    }
    
    /**
     * Incrémente le nombre de parties gagnées pour le profil courant et sauvegarde
     */
    public static void incrementGamesWon() {
        PlayerProfileManager.incrementGamesWon();
    }
}