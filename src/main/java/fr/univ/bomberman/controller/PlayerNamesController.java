// FILE: src/main/java/fr/univ/bomberman/controller/PlayerNamesController.java
package fr.univ.bomberman.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Contrôleur pour la fenêtre de saisie des noms de joueurs
 */
public class PlayerNamesController {

    @FXML
    private TextField player1NameField;

    @FXML
    private TextField player2NameField;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    private String player1Name = "Joueur 1";
    private String player2Name = "Joueur 2";
    private boolean confirmed = false;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Valeurs par défaut
        player1NameField.setText(player1Name);
        player2NameField.setText(player2Name);

        // Sélectionner le texte du premier champ pour faciliter la saisie
        player1NameField.selectAll();
        player1NameField.requestFocus();
    }

    /**
     * Définit les noms actuels des joueurs
     */
    public void setPlayerNames(String player1, String player2) {
        this.player1Name = player1;
        this.player2Name = player2;

        if (player1NameField != null) {
            player1NameField.setText(player1);
        }
        if (player2NameField != null) {
            player2NameField.setText(player2);
        }
    }

    /**
     * Confirme et sauvegarde les noms
     */
    @FXML
    private void onConfirm() {
        String name1 = player1NameField.getText().trim();
        String name2 = player2NameField.getText().trim();

        // Validation
        if (name1.isEmpty()) {
            showError("Nom invalide", "Le nom du Joueur 1 ne peut pas être vide.");
            player1NameField.requestFocus();
            return;
        }

        if (name2.isEmpty()) {
            showError("Nom invalide", "Le nom du Joueur 2 ne peut pas être vide.");
            player2NameField.requestFocus();
            return;
        }

        if (name1.equals(name2)) {
            showError("Noms identiques", "Les deux joueurs ne peuvent pas avoir le même nom.");
            player2NameField.requestFocus();
            player2NameField.selectAll();
            return;
        }

        // Limiter la longueur des noms
        if (name1.length() > 15) {
            showError("Nom trop long", "Le nom du Joueur 1 ne peut pas dépasser 15 caractères.");
            player1NameField.requestFocus();
            return;
        }

        if (name2.length() > 15) {
            showError("Nom trop long", "Le nom du Joueur 2 ne peut pas dépasser 15 caractères.");
            player2NameField.requestFocus();
            return;
        }

        // Sauvegarder les noms
        this.player1Name = name1;
        this.player2Name = name2;
        this.confirmed = true;

        // Fermer la fenêtre
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Annule les modifications
     */
    @FXML
    private void onCancel() {
        this.confirmed = false;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * @return true si l'utilisateur a confirmé
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @return le nom du joueur 1
     */
    public String getPlayer1Name() {
        return player1Name;
    }

    /**
     * @return le nom du joueur 2
     */
    public String getPlayer2Name() {
        return player2Name;
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
}