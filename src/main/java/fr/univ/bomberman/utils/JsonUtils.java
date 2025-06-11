// FILE: src/main/java/fr/univ/bomberman/utils/JsonUtils.java
package fr.univ.bomberman.utils;

import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.model.PlayerProfile;
import fr.univ.bomberman.model.Cell;
import fr.univ.bomberman.model.Position;
import fr.univ.bomberman.model.CellType;
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
import java.util.List;
import java.util.ArrayList;

/**
 * Utilitaire pour charger et sauvegarder des données JSON simples (ex. profil utilisateur).
 */
public class JsonUtils {

    private static final String PROFILE_FILENAME = "profile.json";
    private static final Random random = new Random();
    private static final double DESTRUCTIBLE_CHANCE = 0.3; // 30% de chance d'avoir un bloc destructible

    /**
     * Sauvegarde le profil complet du joueur dans un fichier JSON
     *
     * @param profile profil du joueur à sauvegarder
     * @throws BombermanException si une erreur d'entrée/sortie survient
     */
    public static void savePlayerProfile(PlayerProfile profile) throws BombermanException {
        File file = new File(PROFILE_FILENAME);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"firstName\":\"").append(escapeJson(profile.getFirstName())).append("\",");
            json.append("\"lastName\":\"").append(escapeJson(profile.getLastName())).append("\",");
            json.append("\"avatar\":\"").append(escapeJson(profile.getAvatar())).append("\",");
            json.append("\"gamesPlayed\":").append(profile.getGamesPlayed()).append(",");
            json.append("\"gamesWon\":").append(profile.getGamesWon());
            json.append("}");
            writer.write(json.toString());
        } catch (IOException e) {
            throw new BombermanException("Impossible de sauvegarder le profil JSON", e);
        }
    }

    /**
     * Charge le profil complet du joueur depuis le fichier profile.json
     *
     * @return le profil du joueur si le fichier existe et est valide, un nouveau profil sinon
     * @throws BombermanException si une erreur d'entrée/sortie survient ou si le format est invalide
     */
    public static PlayerProfile loadPlayerProfile() throws BombermanException {
        File file = new File(PROFILE_FILENAME);
        if (!file.exists()) {
            return new PlayerProfile();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
            String content = sb.toString();
            
            // Vérifier que le contenu est un objet JSON
            if (!content.startsWith("{") || !content.endsWith("}")) {
                throw new BombermanException("Format du fichier profile.json invalide.");
            }
            
            // Extraire les valeurs
            PlayerProfile profile = new PlayerProfile();
            
            // Prénom
            String firstName = extractStringValue(content, "firstName");
            if (firstName != null) {
                profile.setFirstName(firstName);
            }
            
            // Nom
            String lastName = extractStringValue(content, "lastName");
            if (lastName != null) {
                profile.setLastName(lastName);
            }
            
            // Avatar
            String avatar = extractStringValue(content, "avatar");
            if (avatar != null) {
                profile.setAvatar(avatar);
            }
            
            // Parties jouées
            Integer gamesPlayed = extractIntValue(content, "gamesPlayed");
            if (gamesPlayed != null) {
                profile.setGamesPlayed(gamesPlayed);
            }
            
            // Parties gagnées
            Integer gamesWon = extractIntValue(content, "gamesWon");
            if (gamesWon != null) {
                profile.setGamesWon(gamesWon);
            }
            
            return profile;
        } catch (IOException e) {
            throw new BombermanException("Impossible de charger le profil JSON", e);
        }
    }

    /**
     * Extrait une valeur chaîne du contenu JSON
     */
    private static String extractStringValue(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex < 0) return null;
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) return null;
        
        int firstQuote = json.indexOf("\"", colonIndex);
        if (firstQuote < 0) return null;
        
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote < 0) return null;
        
        return unescapeJson(json.substring(firstQuote + 1, secondQuote));
    }
    
    /**
     * Extrait une valeur entière du contenu JSON
     */
    private static Integer extractIntValue(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex < 0) return null;
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) return null;
        
        // Chercher la fin de la valeur (virgule ou accolade fermante)
        int commaIndex = json.indexOf(",", colonIndex);
        int braceIndex = json.indexOf("}", colonIndex);
        
        int endIndex = (commaIndex >= 0 && commaIndex < braceIndex) ? commaIndex : braceIndex;
        if (endIndex < 0) return null;
        
        try {
            String valueStr = json.substring(colonIndex + 1, endIndex).trim();
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Échappe les guillemets et antislash pour le JSON.
     *
     * @param text texte à échapper
     * @return texte échappé
     */
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Déséchappe une chaîne JSON simple.
     *
     * @param text texte JSON à déséchapper
     * @return texte déséchappé
     */
    private static String unescapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }
    
    /**
     * Méthodes de compatibilité pour l'ancien format
     */
    
    /**
     * Sauvegarde le nom du joueur dans un fichier JSON nommé profile.json
     *
     * @param name nom du joueur à sauvegarder
     * @throws BombermanException si une erreur d'entrée/sortie survient
     * @deprecated Utiliser savePlayerProfile à la place
     */
    @Deprecated
    public static void saveProfile(String name) throws BombermanException {
        PlayerProfile profile = new PlayerProfile();
        String[] parts = name.split(" ", 2);
        if (parts.length > 0) {
            profile.setFirstName(parts[0]);
        }
        if (parts.length > 1) {
            profile.setLastName(parts[1]);
        }
        savePlayerProfile(profile);
    }

    /**
     * Charge le nom du joueur depuis le fichier profile.json
     *
     * @return le nom du joueur si le fichier existe et est valide, null sinon
     * @throws BombermanException si une erreur d'entrée/sortie survient ou si le format est invalide
     * @deprecated Utiliser loadPlayerProfile à la place
     */
    @Deprecated
    public static String loadProfile() throws BombermanException {
        PlayerProfile profile = loadPlayerProfile();
        return profile.getFullName().trim();
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

    /**
     * Liste tous les fichiers de niveau disponibles dans le dossier des niveaux.
     * @return Liste des noms de fichiers de niveau (sans l'extension .json)
     */
    public static List<String> listAvailableLevels() {
        List<String> levels = new ArrayList<>();
        File levelDir = new File("bomber/src/main/resources/fr/univ/bomberman/level");
        
        if (levelDir.exists() && levelDir.isDirectory()) {
            File[] files = levelDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    levels.add(file.getName().replace(".json", ""));
                }
            }
        }
        
        return levels;
    }

    /**
     * Obtient le chemin complet vers un fichier de niveau.
     * @param levelName nom du niveau (sans l'extension .json)
     * @return chemin complet vers le fichier de niveau
     */
    public static String getLevelPath(String levelName) {
        return "bomber/src/main/resources/fr/univ/bomberman/level/" + levelName + ".json";
    }
}
