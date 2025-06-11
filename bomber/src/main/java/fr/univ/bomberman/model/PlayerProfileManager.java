package fr.univ.bomberman.model;

import fr.univ.bomberman.exceptions.BombermanException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Gestionnaire des profils de joueurs.
 * Permet de charger, sauvegarder et gérer plusieurs profils.
 */
public class PlayerProfileManager {
    
    private static final String PROFILES_DIRECTORY = "profiles";
    private static final String PROFILES_EXTENSION = ".profile";
    private static List<PlayerProfile> profiles = new ArrayList<>();
    private static PlayerProfile currentProfile = null;
    
    /**
     * Initialise le gestionnaire de profils
     */
    public static void initialize() {
        // Créer le répertoire des profils s'il n'existe pas
        File profilesDir = new File(PROFILES_DIRECTORY);
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }
        
        // Charger tous les profils existants
        loadAllProfiles();
        
        // Si aucun profil n'existe, créer un profil par défaut
        if (profiles.isEmpty()) {
            try {
                PlayerProfile defaultProfile = new PlayerProfile("Joueur", "1", "default", 0, 0);
                saveProfile(defaultProfile);
                currentProfile = defaultProfile;
            } catch (BombermanException e) {
                System.out.println("Erreur lors de la création du profil par défaut: " + e.getMessage());
            }
        } else if (currentProfile == null) {
            // Définir le premier profil comme profil courant
            currentProfile = profiles.get(0);
        }
    }
    
    /**
     * Charge tous les profils disponibles
     */
    public static void loadAllProfiles() {
        profiles.clear();
        
        File profilesDir = new File(PROFILES_DIRECTORY);
        if (!profilesDir.exists() || !profilesDir.isDirectory()) {
            return;
        }
        
        File[] profileFiles = profilesDir.listFiles((dir, name) -> name.endsWith(PROFILES_EXTENSION));
        if (profileFiles == null) {
            return;
        }
        
        for (File file : profileFiles) {
            try {
                PlayerProfile profile = loadProfileFromFile(file);
                if (profile != null) {
                    profiles.add(profile);
                }
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement du profil " + file.getName() + ": " + e.getMessage());
            }
        }
        
        // Trier les profils par nombre de victoires (décroissant)
        sortProfiles();
    }
    
    /**
     * Trie les profils selon le nombre de victoires décroissant
     */
    public static void sortProfiles() {
        Collections.sort(profiles, (p1, p2) -> Integer.compare(p2.getGamesWon(), p1.getGamesWon()));
    }
    
    /**
     * Charge un profil depuis un fichier
     */
    private static PlayerProfile loadProfileFromFile(File file) throws BombermanException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
            String content = sb.toString();
            
            // Vérifier que le contenu est un objet JSON
            if (!content.startsWith("{") || !content.endsWith("}")) {
                throw new BombermanException("Format du fichier profil invalide.");
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
            throw new BombermanException("Impossible de charger le profil", e);
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
        
        return json.substring(firstQuote + 1, secondQuote).replace("\\\"", "\"").replace("\\\\", "\\");
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
     * Sauvegarde un profil
     */
    public static void saveProfile(PlayerProfile profile) throws BombermanException {
        // Assurons-nous que le profil a au moins un nom
        if ((profile.getFirstName() == null || profile.getFirstName().trim().isEmpty()) && 
            (profile.getLastName() == null || profile.getLastName().trim().isEmpty())) {
            profile.setFirstName("Joueur");
        }
        
        // Assurons-nous que l'avatar est défini
        if (profile.getAvatar() == null || profile.getAvatar().trim().isEmpty()) {
            profile.setAvatar("default");
        }
        
        // Créer le répertoire des profils s'il n'existe pas
        File profilesDir = new File(PROFILES_DIRECTORY);
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }
        
        String fileName = getProfileFileName(profile);
        File file = new File(PROFILES_DIRECTORY, fileName);
        
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
            
            // S'assurer que le profil est dans la liste
            boolean found = false;
            for (int i = 0; i < profiles.size(); i++) {
                if (isSameProfile(profiles.get(i), profile)) {
                    profiles.set(i, profile);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                profiles.add(profile);
            }
            
            // Retrier les profils
            sortProfiles();
            
        } catch (IOException e) {
            throw new BombermanException("Impossible de sauvegarder le profil", e);
        }
    }
    
    /**
     * Compare deux profils pour savoir s'il s'agit de la même personne
     */
    private static boolean isSameProfile(PlayerProfile p1, PlayerProfile p2) {
        if (p1 == null || p2 == null) return false;
        
        String name1 = (p1.getFirstName() + " " + p1.getLastName()).trim();
        String name2 = (p2.getFirstName() + " " + p2.getLastName()).trim();
        
        return name1.equalsIgnoreCase(name2);
    }
    
    /**
     * Génère un nom de fichier pour un profil
     */
    private static String getProfileFileName(PlayerProfile profile) {
        String firstName = profile.getFirstName() != null ? profile.getFirstName().toLowerCase() : "";
        String lastName = profile.getLastName() != null ? profile.getLastName().toLowerCase() : "";
        
        String baseName = firstName + "_" + lastName;
        if (baseName.equals("_")) {
            baseName = "player_profile";
        }
        
        // Remplacer les caractères non autorisés dans les noms de fichiers
        baseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "_");
        return baseName + PROFILES_EXTENSION;
    }
    
    /**
     * Échappe les guillemets et antislash pour le JSON
     */
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
    /**
     * Retourne la liste des profils
     */
    public static List<PlayerProfile> getProfiles() {
        return new ArrayList<>(profiles);
    }
    
    /**
     * Définit le profil courant
     */
    public static void setCurrentProfile(PlayerProfile profile) {
        currentProfile = profile;
    }
    
    /**
     * Obtient le profil courant
     */
    public static PlayerProfile getCurrentProfile() {
        if (currentProfile == null && !profiles.isEmpty()) {
            currentProfile = profiles.get(0);
        }
        return currentProfile;
    }
    
    /**
     * Vérifie si un profil existe avec le nom spécifié
     */
    public static boolean profileExists(String firstName, String lastName) {
        for (PlayerProfile profile : profiles) {
            if ((profile.getFirstName().equalsIgnoreCase(firstName) || 
                (firstName == null && profile.getFirstName() == null)) && 
                (profile.getLastName().equalsIgnoreCase(lastName) || 
                (lastName == null && profile.getLastName() == null))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Trouve un profil par son nom
     */
    public static PlayerProfile findProfileByName(String firstName, String lastName) {
        for (PlayerProfile profile : profiles) {
            if ((profile.getFirstName().equalsIgnoreCase(firstName) || 
                (firstName == null && profile.getFirstName() == null)) && 
                (profile.getLastName().equalsIgnoreCase(lastName) || 
                (lastName == null && profile.getLastName() == null))) {
                return profile;
            }
        }
        return null;
    }
    
    /**
     * Incrémente le nombre de parties jouées pour le profil courant
     */
    public static void incrementGamesPlayed() {
        if (currentProfile != null) {
            currentProfile.incrementGamesPlayed();
            try {
                saveProfile(currentProfile);
            } catch (BombermanException e) {
                System.out.println("Erreur lors de la sauvegarde des statistiques: " + e.getMessage());
            }
        }
    }
    
    /**
     * Incrémente le nombre de parties gagnées pour le profil courant
     */
    public static void incrementGamesWon() {
        if (currentProfile != null) {
            currentProfile.incrementGamesPlayed();
            currentProfile.incrementGamesWon();
            try {
                saveProfile(currentProfile);
            } catch (BombermanException e) {
                System.out.println("Erreur lors de la sauvegarde des statistiques: " + e.getMessage());
            }
        }
    }
} 