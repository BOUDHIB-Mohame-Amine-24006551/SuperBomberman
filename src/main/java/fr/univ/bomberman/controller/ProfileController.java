// FILE: src/main/java/fr/univ/bomberman/controller/ProfileController.java
package fr.univ.bomberman.controller;

import fr.univ.bomberman.util.ProfileManager;
import fr.univ.bomberman.model.PlayerProfile;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import fr.univ.bomberman.exceptions.BombermanException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Contrôleur pour la fenêtre de gestion de profil.
 */
public class ProfileController {

    @FXML
    private TextField playerNameField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    /**
     * Initialisation du contrôleur de profil.
     * Charge éventuellement le profil existant.
     */
    @FXML
    public void initialize() {
        System.out.println("ProfileController initialisé");

        // Charger un nom par défaut pour tester
        playerNameField.setText("Joueur1");
    }

    /**
     * Sauvegarde le profil utilisateur.
     *
     * @throws BombermanException si la sauvegarde échoue
     */
    @FXML
    private void onSave() throws BombermanException {
        String name = playerNameField.getText();
        if (name == null || name.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Nom invalide");
            alert.setContentText("Le nom de joueur ne peut pas être vide.");
            alert.showAndWait();
            return;
        }

        try {
            // ✅ NOUVEAU: Utiliser ProfileManager
            ProfileManager profileManager = ProfileManager.getInstance();
            PlayerProfile profile = profileManager.loadProfile(name.trim());
            profileManager.saveProfile(profile);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Profil sauvegardé");
            alert.setContentText("Le profil '" + name.trim() + "' a été sauvegardé avec succès.");
            alert.showAndWait();

            // ✅ NOUVEAU: Ouvrir les statistiques
            openProfileStats(profile);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BombermanException("Impossible de sauvegarder le profil", e);
        }
    }

    private void openProfileStats(PlayerProfile profile) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/stats.fxml"));
            Parent root = loader.load();

            ProfileStatsController controller = loader.getController();
            controller.setProfile(profile);

            Stage stage = new Stage();
            stage.setTitle("Statistiques - " + profile.getPlayerName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Impossible d'ouvrir les statistiques: " + e.getMessage());
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
}