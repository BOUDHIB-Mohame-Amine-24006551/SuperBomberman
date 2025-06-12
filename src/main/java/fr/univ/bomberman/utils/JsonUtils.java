// FILE: src/main/java/fr/univ/bomberman/utils/JsonUtils.java
package fr.univ.bomberman.utils;


import fr.univ.bomberman.model.Cell;
import fr.univ.bomberman.model.CellType;
import fr.univ.bomberman.model.Position;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

/**
 * Utilitaire pour charger et sauvegarder des données JSON simples.
 */
public class JsonUtils {

    private static final Random random = new Random();
    private static final double DESTRUCTIBLE_CHANCE = 0.3; // 30% de chance d'avoir un bloc destructible

    /**
     * Lit et parse un fichier de niveau au format JSON
     * @param filePath chemin vers le fichier de niveau
     * @return l'objet JSON contenant les données du niveau
     * @throws IOException si le fichier ne peut pas être lu
     */
    public static JSONObject readLevelFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(new File(filePath).toPath()));
        return new JSONObject(content);
    }

    public static Cell[][] parseLevelGrid(JSONObject levelData) {
        int width = levelData.getInt("width");
        int height = levelData.getInt("height");
        boolean autoFill = levelData.getBoolean("autoFill");
        
        JSONArray gridData = levelData.getJSONArray("grid");
        Cell[][] cells = new Cell[height][width];
        
        // Lire la grille de base
        for (int row = 0; row < height; row++) {
            JSONArray rowData = gridData.getJSONArray(row);
            for (int col = 0; col < width; col++) {
                int cellValue = rowData.getInt(col);
                Position pos = new Position(col, row);
                
                // Si l'auto-remplissage est activé et que c'est une case vide (0)
                if (autoFill && cellValue == 0 && random.nextDouble() < DESTRUCTIBLE_CHANCE) {
                    cells[row][col] = new Cell(pos, CellType.DESTRUCTIBLE_BRICK);
                } else {
                    // Convertir les valeurs en types de cellules
                    CellType type;
                    switch (cellValue) {
                        case 1:
                            type = CellType.INDESTRUCTIBLE_WALL;
                            break;
                        case 2:
                            type = CellType.DESTRUCTIBLE_BRICK;
                            break;
                        case 3:
                            type = CellType.EMPTY; // Les voids sont traités comme des cases vides
                            break;
                        default:
                            type = CellType.EMPTY;
                    }
                    cells[row][col] = new Cell(pos, type);
                }
            }
        }
        
        return cells;
    }
}
