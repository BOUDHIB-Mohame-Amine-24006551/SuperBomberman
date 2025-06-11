package fr.univ.bomberman.controller;

import fr.univ.bomberman.BombermanApp;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.model.PlayerProfile;
import fr.univ.bomberman.model.PlayerProfileManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la sélection des profils joueurs avant de démarrer une partie.
 */
public class PlayerProfileSelectionController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label player1Label;

    @FXML
    private Label player2Label;

    @FXML
    private Label player3Label;

    @FXML
    private Label player4Label;

    @FXML
    private ComboBox<String> player1ComboBox;

    @FXML
    private ComboBox<String> player2ComboBox;

    @FXML
    private ComboBox<String> player3ComboBox;

    @FXML
    private ComboBox<String> player4ComboBox;

    @FXML
    private Button createProfileButton;

    @FXML
    private Button continueButton;

    @FXML
    private Button cancelButton;
    
    private BombermanApp bombermanApp;
    private String[] playerNames;
    private int numberOfPlayers;
    private String gameModeTitle;
    private List<PlayerProfile> availableProfiles;
    private boolean isBotMode = false;
    private int botDifficulty = 0;

    /**
     * Initialise la boîte de dialogue de sélection de profils.
     */
    @FXML
    public void initialize() {
        // Initialiser le gestionnaire de profils si nécessaire
        PlayerProfileManager.initialize();
        
        // Récupérer tous les profils disponibles
        loadAvailableProfiles();
        
        // Valeurs par défaut
        playerNames = new String[]{"Joueur 1", "Joueur 2", "Joueur 3", "Joueur 4"};
        numberOfPlayers = 2;
        gameModeTitle = "Partie standard";
    }

    /**
     * Configure la boîte de dialogue pour un mode de jeu spécifique.
     *
     * @param playerCount Nombre de joueurs (2 à 4)
     * @param title       Titre descriptif du mode de jeu
     */
    public void setup(int playerCount, String title) {
        numberOfPlayers = playerCount;
        gameModeTitle = title;
        titleLabel.setText("Sélection des profils - " + title);
        
        // Afficher/masquer les éléments en fonction du nombre de joueurs
        player3Label.setVisible(playerCount > 2);
        player3ComboBox.setVisible(playerCount > 2);
        player4Label.setVisible(playerCount > 3);
        player4ComboBox.setVisible(playerCount > 3);
        
        // Définir les profils par défaut dans les combobox
        updateProfilesComboBoxes();
    }

    /**
     * Configure la boîte de dialogue pour le mode contre un bot.
     *
     * @param difficulty Niveau de difficulté du bot (1-3)
     */
    public void setupBotMode(int difficulty) {
        numberOfPlayers = 1;
        isBotMode = true;
        botDifficulty = difficulty;
        gameModeTitle = "Partie contre Bot (" + getBotDifficultyName(difficulty) + ")";
        titleLabel.setText("Sélection du profil - " + gameModeTitle);
        
        // Masquer les éléments pour les autres joueurs
        player2Label.setVisible(false);
        player2ComboBox.setVisible(false);
        player3Label.setVisible(false);
        player3ComboBox.setVisible(false);
        player4Label.setVisible(false);
        player4ComboBox.setVisible(false);
        
        // Renommer le joueur 1
        player1Label.setText("Votre profil:");
        
        // Définir le profil par défaut
        updateProfilesComboBoxes();
    }

    /**
     * Charge les profils disponibles et met à jour les ComboBox.
     */
    private void loadAvailableProfiles() {
        availableProfiles = PlayerProfileManager.getProfiles();
        updateProfilesComboBoxes();
    }

    /**
     * Met à jour les ComboBox avec les profils disponibles.
     */
    private void updateProfilesComboBoxes() {
        List<String> displayProfiles = new ArrayList<>();
        displayProfiles.add("-- Nouveau profil --");
        
        // Ajouter les profils existants
        if (availableProfiles != null) {
            displayProfiles.addAll(availableProfiles.stream()
                    .map(this::formatProfileForComboBox)
                    .collect(Collectors.toList()));
        }
        
        // Mettre à jour les ComboBox
        player1ComboBox.setItems(FXCollections.observableArrayList(displayProfiles));
        player1ComboBox.getSelectionModel().selectFirst();
        
        if (numberOfPlayers > 1) {
            player2ComboBox.setItems(FXCollections.observableArrayList(displayProfiles));
            player2ComboBox.getSelectionModel().selectFirst();
        }
        
        if (numberOfPlayers > 2) {
            player3ComboBox.setItems(FXCollections.observableArrayList(displayProfiles));
            player3ComboBox.getSelectionModel().selectFirst();
        }
        
        if (numberOfPlayers > 3) {
            player4ComboBox.setItems(FXCollections.observableArrayList(displayProfiles));
            player4ComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Formate un profil pour l'affichage dans la ComboBox.
     */
    private String formatProfileForComboBox(PlayerProfile profile) {
        String name = profile.getFullName().trim();
        if (name.isEmpty()) {
            name = "Profil sans nom";
        }
        return name + " (" + profile.getGamesWon() + " victoires)";
    }

    /**
     * Ouvre la fenêtre de création de profil.
     */
    @FXML
    private void onCreateProfile() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/profile/dialog.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage dialog = new Stage();
            dialog.setTitle("Super Bomberman - Création de profil");
            dialog.setResizable(false);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.showAndWait();
            
            // Recharger les profils après fermeture
            loadAvailableProfiles();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de profil", "Impossible d'ouvrir la gestion de profil: " + e.getMessage());
        }
    }

    /**
     * Continue vers le jeu avec les profils sélectionnés.
     */
    @FXML
    private void onContinue() {
        if (bombermanApp == null) {
            showError("Erreur", "Référence à l'application manquante.");
            return;
        }
        
        // Récupérer les profils sélectionnés
        PlayerProfile[] selectedProfiles = new PlayerProfile[numberOfPlayers];
        String[] selectedNames = new String[numberOfPlayers];
        
        // Récupérer le profil du joueur 1
        selectedProfiles[0] = getSelectedProfile(player1ComboBox);
        selectedNames[0] = selectedProfiles[0].getFullName();
        
        if (numberOfPlayers > 1 && !isBotMode) {
            selectedProfiles[1] = getSelectedProfile(player2ComboBox);
            selectedNames[1] = selectedProfiles[1].getFullName();
        }
        
        if (numberOfPlayers > 2) {
            selectedProfiles[2] = getSelectedProfile(player3ComboBox);
            selectedNames[2] = selectedProfiles[2].getFullName();
        }
        
        if (numberOfPlayers > 3) {
            selectedProfiles[3] = getSelectedProfile(player4ComboBox);
            selectedNames[3] = selectedProfiles[3].getFullName();
        }
        
        // Définir le profil actuel
        PlayerProfileManager.setCurrentProfile(selectedProfiles[0]);
        
        // Démarrer le jeu avec les noms sélectionnés
        Stage stage = (Stage) continueButton.getScene().getWindow();
        stage.close();
        
        // Lancer le mode de jeu approprié
        if (isBotMode) {
            bombermanApp.startBotGame(selectedNames[0], botDifficulty);
        } else if (numberOfPlayers == 2) {
            bombermanApp.startCanvasGameWithNames(selectedNames[0], selectedNames[1]);
        } else if (numberOfPlayers > 2) {
            bombermanApp.startFourPlayerGame(selectedNames);
        }
    }

    /**
     * Obtient le profil sélectionné depuis une ComboBox.
     * Si "Nouveau profil" est sélectionné, crée un profil par défaut.
     */
    private PlayerProfile getSelectedProfile(ComboBox<String> comboBox) {
        String selected = comboBox.getValue();
        
        // Si "Nouveau profil" est sélectionné
        if (selected.equals("-- Nouveau profil --")) {
            // Créer un profil par défaut
            String defaultName = getDefaultName(comboBox);
            PlayerProfile newProfile = new PlayerProfile();
            newProfile.setFirstName(defaultName);
            newProfile.setLastName("");
            
            try {
                // Sauvegarder le nouveau profil
                PlayerProfileManager.saveProfile(newProfile);
            } catch (BombermanException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de créer le profil par défaut: " + e.getMessage());
            }
            
            return newProfile;
        }
        
        // Sinon, chercher le profil correspondant
        for (PlayerProfile profile : availableProfiles) {
            if (formatProfileForComboBox(profile).equals(selected)) {
                return profile;
            }
        }
        
        // Si aucun profil correspondant (ne devrait pas arriver), créer un par défaut
        PlayerProfile defaultProfile = new PlayerProfile();
        defaultProfile.setFirstName(getDefaultName(comboBox));
        return defaultProfile;
    }
    
    /**
     * Obtient un nom par défaut pour un joueur en fonction de la ComboBox.
     */
    private String getDefaultName(ComboBox<String> comboBox) {
        if (comboBox == player1ComboBox) return "Joueur 1";
        if (comboBox == player2ComboBox) return "Joueur 2";
        if (comboBox == player3ComboBox) return "Joueur 3";
        if (comboBox == player4ComboBox) return "Joueur 4";
        return "Joueur";
    }

    /**
     * Annule et retourne au menu principal.
     */
    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Définit la référence à l'application principale.
     */
    public void setBombermanApp(BombermanApp app) {
        this.bombermanApp = app;
    }
    
    /**
     * Retourne le nom du niveau de difficulté du bot.
     */
    private String getBotDifficultyName(int difficulty) {
        switch (difficulty) {
            case 1: return "Facile";
            case 2: return "Moyen";
            case 3: return "Difficile";
            default: return "Moyen";
        }
    }
} 