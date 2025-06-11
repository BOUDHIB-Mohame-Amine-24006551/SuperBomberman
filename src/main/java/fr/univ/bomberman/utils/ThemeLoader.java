// FILE: src/main/java/fr/univ/bomberman/utils/ThemeLoader.java
package fr.univ.bomberman.utils;

import fr.univ.bomberman.exceptions.BombermanException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utilitaire pour charger des fichiers JSON de thèmes depuis le répertoire resources/themes.
 * Fournit un contenu JSON brut pour traitement ultérieur.
 */
public class ThemeLoader {

    private static final String THEMES_PATH = "/themes/";

    /**
     * Charge le contenu JSON d'un thème donné depuis resources/themes/{themeName}.json
     *
     * @param themeName nom du thème (sans l'extension .json)
     * @return contenu JSON du fichier de thème sous forme de chaîne
     * @throws BombermanException si le fichier n'existe pas ou en cas d'erreur de lecture
     */
    public static String loadThemeJson(String themeName) throws BombermanException {
        String resourcePath = THEMES_PATH + themeName + ".json";
        InputStream is = ThemeLoader.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new BombermanException("Thème introuvable : " + themeName);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
            return sb.toString();
        } catch (IOException e) {
            throw new BombermanException("Erreur lors de la lecture du fichier de thème : " + themeName, e);
        }
    }

    // À l'avenir, on pourra convertir ce JSON en des objets Theme spécifiques.
}
