package fr.univ.bomberman.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class GameController {

    @FXML
    private GridPane boardGrid;

    @FXML
    private Label currentPlayerLabel;

    @FXML
    private Label bombsRemainingLabel;


    private int currentPlayer = 1;
    private int bombsRemaining = 3;

    @FXML
    public void initialize() {
        System.out.println("GameController initialisé");
        int cols = 15;
        int rows = 13;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Pane cellPane = new Pane();
                cellPane.setPrefSize(40, 40);
                cellPane.setMinSize(40, 40);
                cellPane.setMaxSize(40, 40);

                // Colorer différemment selon le type de case
                if (x == 0 || y == 0 || x == cols-1 || y == rows-1) {
                    // Murs extérieurs
                    cellPane.setStyle("-fx-background-color: #8B4513;");
                } else if (x % 2 == 0 && y % 2 == 0) {
                    // Murs indestructibles
                    cellPane.setStyle("-fx-background-color: #696969;");
                } else if (Math.random() < 0.3) {
                    // Briques destructibles (30% de chance)
                    cellPane.setStyle("-fx-background-color: #CD853F;");
                } else {
                    // Cases vides
                    cellPane.setStyle("-fx-background-color: #90EE90;");
                }

                // Ajouter une bordure pour voir les cases
                cellPane.setStyle(cellPane.getStyle() + " -fx-border-color: black; -fx-border-width: 1;");

                boardGrid.add(cellPane, x, y);
            }
        }

        // Mettre à jour le HUD
        updateHUD();
    }


    @FXML
    private void onKeyPressed(KeyEvent event) {
        System.out.println("Touche pressée: " + event.getCode());

        switch (event.getCode()) {
            case Z: // Joueur 1 haut
                System.out.println("Joueur 1 - Haut");
                break;
            case Q: // Joueur 1 gauche
                System.out.println("Joueur 1 - Gauche");
                break;
            case S: // Joueur 1 bas
                System.out.println("Joueur 1 - Bas");
                break;
            case D: // Joueur 1 droite
                System.out.println("Joueur 1 - Droite");
                break;
            case UP: // Joueur 2 haut
                System.out.println("Joueur 2 - Haut");
                break;
            case LEFT: // Joueur 2 gauche
                System.out.println("Joueur 2 - Gauche");
                break;
            case DOWN: // Joueur 2 bas
                System.out.println("Joueur 2 - Bas");
                break;
            case RIGHT: // Joueur 2 droite
                System.out.println("Joueur 2 - Droite");
                break;
            case SPACE:
            case ENTER:
                System.out.println("Bombe posée!");
                if (bombsRemaining > 0) {
                    bombsRemaining--;
                }
                break;
            case ESCAPE:
                // Retour au menu
                System.out.println("Retour au menu");
                // TODO: Implémenter le retour au menu
                return;
            default:
                // Touche non utilisée
                return;
        }

        // Changer de joueur après chaque action (pour tester)
        currentPlayer = (currentPlayer == 1) ? 2 : 1;

        // Mettre à jour le HUD
        updateHUD();
    }


    /**
     * Met à jour les informations du HUD (joueur courant, bombes restantes, etc.).
     */
    private void updateHUD() {
        if (currentPlayerLabel != null) {
            currentPlayerLabel.setText("Joueur actuel : Joueur " + currentPlayer);
        }
        if (bombsRemainingLabel != null) {
            bombsRemainingLabel.setText("Bombes restantes : " + bombsRemaining);
        }
    }
}