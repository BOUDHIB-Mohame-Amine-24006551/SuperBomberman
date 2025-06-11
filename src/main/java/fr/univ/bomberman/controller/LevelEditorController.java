// FILE: src/main/java/fr/univ/bomberman/controller/LevelEditorController.java
package fr.univ.bomberman.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.scene.input.MouseEvent;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Contrôleur pour l'éditeur de niveaux (squelette).
 */
public class LevelEditorController {
    private static final int GRID_WIDTH = 15;
    private static final int GRID_HEIGHT = 13;
    private static final int CELL_SIZE = 40;
    private static final Logger LOGGER = Logger.getLogger(LevelEditorController.class.getName());

    @FXML private GridPane gridPane;
    @FXML private RadioButton emptyRadio;
    @FXML private RadioButton wallRadio;
    @FXML private RadioButton destructibleRadio;
    @FXML private RadioButton voidRadio;
    @FXML private CheckBox autoFillCheckBox;

    private int[][] grid;
    private Image groundImage;
    private Image wallImage;
    private Image brickImage;
    private Image voidImage;
    private boolean isDrawing = false;

    private Image chargerImage(String chemin) {
        InputStream stream = getClass().getResourceAsStream(chemin);
        if (stream == null) {
            throw new RuntimeException("Image introuvable : " + chemin);
        }
        return new Image(stream);
    }

    /**
     * Initialisation du contrôleur de l'éditeur de niveaux.
     * Prépare la zone d'édition.
     */
    @FXML
    public void initialize() {
        // Charger les images
        try {
            groundImage = chargerImage("/fr/univ/bomberman/image/default/ground.png");
            wallImage = chargerImage("/fr/univ/bomberman/image/default/wall.png");
            brickImage = chargerImage("/fr/univ/bomberman/image/default/brick.png");
            voidImage = chargerImage("/fr/univ/bomberman/image/default/void.png");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des images", e);
            // Optionnel : afficher une alerte à l'utilisateur
            afficherErreurChargement();
            return; // Arrêter l'initialisation si les images sont critiques
        }

        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        createGrid();
        
        // Ajouter les gestionnaires d'événements pour le drag and draw
        gridPane.setOnMousePressed(e -> {
            isDrawing = true;
            handleMousePosition(e);
        });
        gridPane.setOnMouseReleased(e -> isDrawing = false);
        gridPane.setOnMouseDragged(e -> handleMousePosition(e));
    }

    private void handleMousePosition(MouseEvent e) {
        if (!isDrawing) return;

        // Calculer la position de la souris relative à la grille
        double mouseX = e.getX();
        double mouseY = e.getY();

        // Calculer les indices de la grille
        int col = (int) (mouseX / CELL_SIZE);
        int row = (int) (mouseY / CELL_SIZE);

        // Vérifier que les indices sont valides
        if (row >= 0 && row < GRID_HEIGHT && col >= 0 && col < GRID_WIDTH) {
            Pane cellContainer = (Pane) gridPane.getChildren().get(row * GRID_WIDTH + col);
            ImageView cellImage = (ImageView) cellContainer.getChildren().get(0);
            placeTile(row, col, cellImage);
        }
    }

    private void createGrid() {
        gridPane.getChildren().clear();
        
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                // Créer un conteneur Pane
                Pane cellContainer = new Pane();
                cellContainer.setPrefSize(CELL_SIZE, CELL_SIZE);
                cellContainer.getStyleClass().add("grid-cell");
                
                // Créer l'ImageView pour l'image
                ImageView cellImage = new ImageView();
                cellImage.setFitWidth(CELL_SIZE);
                cellImage.setFitHeight(CELL_SIZE);
                cellImage.setPreserveRatio(true);
                
                // Ajouter l'image au conteneur
                cellContainer.getChildren().add(cellImage);
                
                final int finalRow = row;
                final int finalCol = col;
                
                // Gestionnaire pour le clic
                cellContainer.setOnMouseClicked(e -> {
                    placeTile(finalRow, finalCol, cellImage);
                });
                
                gridPane.add(cellContainer, col, row);

                grid[row][col] = 0;
                updateCellAppearance(row, col);
            }
        }
    }

    private void placeTile(int row, int col, ImageView cellImage) {
        if (emptyRadio.isSelected()) {
            grid[row][col] = 0;
            cellImage.setImage(groundImage);
        } else if (wallRadio.isSelected()) {
            grid[row][col] = 1;
            cellImage.setImage(wallImage);
        } else if (destructibleRadio.isSelected()) {
            grid[row][col] = 2;
            cellImage.setImage(brickImage);
        } else if (voidRadio.isSelected()) {
            grid[row][col] = 3;
            cellImage.setImage(voidImage);
        }
    }

    @FXML
    private void onSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le niveau");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers de niveau", "*.json")
        );
        
        // Définir le répertoire par défaut
        File defaultDir = new File("src/main/resources/fr/univ/bomberman/level");
        if (defaultDir.exists()) {
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        File file = fileChooser.showSaveDialog(gridPane.getScene().getWindow());
        if (file != null) {
            try {
                JSONObject levelData = new JSONObject();
                levelData.put("width", GRID_WIDTH);
                levelData.put("height", GRID_HEIGHT);
                levelData.put("autoFill", autoFillCheckBox.isSelected());

                JSONArray gridData = new JSONArray();
                for (int row = 0; row < GRID_HEIGHT; row++) {
                    JSONArray rowData = new JSONArray();
                    for (int col = 0; col < GRID_WIDTH; col++) {
                        rowData.put(grid[row][col]);
                    }
                    gridData.put(rowData);
                }
                levelData.put("grid", gridData);

                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.write(levelData.toString(2)); // Le 2 indique l'indentation
                }
            } catch (IOException e) {
                showError("Erreur de sauvegarde", "Impossible de sauvegarder le niveau: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger un niveau");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers de niveau", "*.json")
        );
        
        // Définir le répertoire par défaut
        File defaultDir = new File("src/main/resources/fr/univ/bomberman/level");
        if (defaultDir.exists()) {
            fileChooser.setInitialDirectory(defaultDir);
        }
        
        File file = fileChooser.showOpenDialog(gridPane.getScene().getWindow());
        if (file != null) {
            try {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                }

                JSONObject levelData = new JSONObject(content.toString());
                autoFillCheckBox.setSelected(levelData.getBoolean("autoFill"));

                JSONArray gridData = levelData.getJSONArray("grid");
                for (int row = 0; row < GRID_HEIGHT && row < gridData.length(); row++) {
                    JSONArray rowData = gridData.getJSONArray(row);
                    for (int col = 0; col < GRID_WIDTH && col < rowData.length(); col++) {
                        grid[row][col] = rowData.getInt(col);
                        updateCellAppearance(row, col);
                    }
                }
            } catch (IOException e) {
                showError("Erreur de chargement", "Impossible de charger le niveau: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onClear() {
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                grid[row][col] = 0;
                updateCellAppearance(row, col);
            }
        }
    }

    private void updateCellAppearance(int row, int col) {
        Pane cellContainer = (Pane) gridPane.getChildren().get(row * GRID_WIDTH + col);
        ImageView cellImage = (ImageView) cellContainer.getChildren().get(0);
        
        switch (grid[row][col]) {
            case 0: // Vide
                cellImage.setImage(groundImage);
                break;
            case 1: // Mur
                cellImage.setImage(wallImage);
                break;
            case 2: // Destructible
                cellImage.setImage(brickImage);
                break;
            case 3: // Void
                cellImage.setImage(voidImage);
                break;
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreurChargement() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Erreur de chargement");
        alert.setContentText("Impossible de charger les images nécessaires au jeu.");
        alert.showAndWait();
    }
}
