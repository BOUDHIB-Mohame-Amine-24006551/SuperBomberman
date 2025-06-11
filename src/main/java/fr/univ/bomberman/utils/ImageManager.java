// FILE: src/main/java/fr/univ/bomberman/utils/ImageManager.java
package fr.univ.bomberman.utils;

import fr.univ.bomberman.exceptions.BombermanException;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire centralisé pour les images du jeu Bomberman
 */
public class ImageManager {

    private static ImageManager instance;
    private Map<String, Image> imageCache;
    private String currentTheme = "default";

    private ImageManager() {
        this.imageCache = new HashMap<>();
    }

    /**
     * Obtient l'instance singleton du gestionnaire d'images
     */
    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    /**
     * Charge toutes les images nécessaires pour le jeu
     */
    public void loadGameImages() throws BombermanException {
        loadGameImages(currentTheme);
    }

    /**
     * Charge les images pour un thème spécifique
     * @param theme nom du thème (correspond au dossier dans resources/images/)
     */
    public void loadGameImages(String theme) throws BombermanException {
        this.currentTheme = theme;

        try {
            // Images des joueurs
            loadImage("player1", "/images/" + theme + "/player1.png");
            loadImage("player2", "/images/" + theme + "/player2.png");
            loadImage("player3", "/images/" + theme + "/player3.png");
            loadImage("player4", "/images/" + theme + "/player4.png");

            // Images des éléments de jeu
            loadImage("bomb", "/images/" + theme + "/bomb.png");
            loadImage("explosion", "/images/" + theme + "/explosion.png");
            loadImage("wall", "/images/" + theme + "/brick.png");
            loadImage("brick", "/images/" + theme + "/wall.png");
            loadImage("ground", "/images/" + theme + "/ground.png");

            // Images optionnelles
            loadImageOptional("power_up_bomb", "/images/" + theme + "/power_up_bomb.png");
            loadImageOptional("power_up_range", "/images/" + theme + "/power_up_range.png");
            loadImageOptional("power_up_speed", "/images/" + theme + "/power_up_speed.png");

        } catch (Exception e) {
            throw new BombermanException("Erreur lors du chargement des images du thème: " + theme, e);
        }
    }

    /**
     * Charge une image obligatoire
     */
    private void loadImage(String key, String path) throws BombermanException {
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            if (image.isError()) {
                throw new BombermanException("Impossible de charger l'image: " + path);
            }
            imageCache.put(key, image);
        } catch (Exception e) {
            // Essayer avec le thème par défaut si ce n'est pas déjà le cas
            if (!currentTheme.equals("default")) {
                String defaultPath = path.replace("/" + currentTheme + "/", "/default/");
                try {
                    Image image = new Image(getClass().getResourceAsStream(defaultPath));
                    if (!image.isError()) {
                        imageCache.put(key, image);
                        return;
                    }
                } catch (Exception ignored) {}
            }
            throw new BombermanException("Image obligatoire manquante: " + path, e);
        }
    }

    /**
     * Charge une image optionnelle (ne lève pas d'exception si elle n'existe pas)
     */
    private void loadImageOptional(String key, String path) {
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            if (!image.isError()) {
                imageCache.put(key, image);
            }
        } catch (Exception e) {
            System.out.println("Image optionnelle non trouvée: " + path);
        }
    }

    /**
     * Obtient une image par sa clé
     */
    public Image getImage(String key) {
        return imageCache.get(key);
    }

    /**
     * Vérifie si une image est disponible
     */
    public boolean hasImage(String key) {
        return imageCache.containsKey(key) && imageCache.get(key) != null;
    }

    /**
     * Vide le cache des images
     */
    public void clearCache() {
        imageCache.clear();
    }

    /**
     * Change de thème et recharge les images
     */
    public void changeTheme(String newTheme) throws BombermanException {
        clearCache();
        loadGameImages(newTheme);
    }

    /**
     * Obtient le thème actuel
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Obtient la liste des images chargées
     */
    public String[] getLoadedImageKeys() {
        return imageCache.keySet().toArray(new String[0]);
    }

    /**
     * Crée des images par défaut si les fichiers ne sont pas trouvés
     */
    public void createDefaultImages() {
        // Cette méthode pourrait créer des images par programmation
        // si les fichiers d'images ne sont pas disponibles

        // Pour l'instant, on peut juste signaler que les images par défaut seront utilisées
        System.out.println("Utilisation du rendu par défaut (formes géométriques)");
    }
}