// FILE: src/main/java/fr/univ/bomberman/utils/JsonUtils.java
package fr.univ.bomberman.utils;

import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.model.Cell;
import fr.univ.bomberman.model.CellType;
import fr.univ.bomberman.model.Position;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

/**
 * Utilitaire pour charger et sauvegarder des données JSON simples (ex. profil utilisateur).
 */
public class JsonUtils {

    private static final String PROFILE_FILENAME = "profile.json";
    private static final Random random = new Random();
    private static final double DESTRUCTIBLE_CHANCE = 0.3; // 30% de chance d'avoir un bloc destructible

    /**
     * Sauvegarde le nom du joueur dans un fichier JSON nommé profile.json
     *
     * @param name nom du joueur à sauvegarder
     * @throws BombermanException si une erreur d'entrée/sortie survient
     */
    public static void saveProfile(String name) throws BombermanException {
        File file = new File(PROFILE_FILENAME);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String json = "{\"name\":\"" + escapeJson(name) + "\"}";
            writer.write(json);
        } catch (IOException e) {
            throw new BombermanException("Impossible de sauvegarder le profil JSON", e);
        }
    }

    /**
     * Charge le nom du joueur depuis le fichier profile.json
     *
     * @return le nom du joueur si le fichier existe et est valide, null sinon
     * @throws BombermanException si une erreur d'entrée/sortie survient ou si le format est invalide
     */
    public static String loadProfile() throws BombermanException {
        File file = new File(PROFILE_FILENAME);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
            String content = sb.toString();
            // Format attendu : {"name":"playername"}
            if (content.startsWith("{") && content.endsWith("}")) {
                int nameIndex = content.indexOf("\"name\"");
                if (nameIndex >= 0) {
                    int colonIndex = content.indexOf(":", nameIndex);
                    int firstQuote = content.indexOf("\"", colonIndex + 1);
                    int secondQuote = content.indexOf("\"", firstQuote + 1);
                    if (firstQuote >= 0 && secondQuote > firstQuote) {
                        return unescapeJson(content.substring(firstQuote + 1, secondQuote));
                    }
                }
            }
            throw new BombermanException("Format du fichier profile.json invalide.");
        } catch (IOException e) {
            throw new BombermanException("Impossible de charger le profil JSON", e);
        }
    }

    /**
     * Échappe les guillemets et antislash pour le JSON.
     *
     * @param text texte à échapper
     * @return texte échappé
     */
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Déséchappe une chaîne JSON simple.
     *
     * @param text texte JSON à déséchapper
     * @return texte déséchappé
     */
    private static String unescapeJson(String text) {
        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }

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
