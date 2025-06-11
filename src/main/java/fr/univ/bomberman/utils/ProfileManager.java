// FILE: src/main/java/fr/univ/bomberman/util/ProfileManager.java
package fr.univ.bomberman.utils;

import fr.univ.bomberman.model.PlayerProfile;
import fr.univ.bomberman.model.GameSession;
import fr.univ.bomberman.model.Game;
import fr.univ.bomberman.exceptions.BombermanException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Gestionnaire pour la sauvegarde et le chargement des profils de joueurs
 * VERSION SIMPLIFIÉE SANS GSON (utilise Properties Java)
 */
public class ProfileManager {

    private static final String PROFILES_DIR = "profiles";
    private static final String PROFILE_EXTENSION = ".properties";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static ProfileManager instance;
    private Path profilesDirectory;

    /**
     * Constructeur privé (Singleton)
     */
    private ProfileManager() {
        initializeDirectory();
    }

    /**
     * Obtient l'instance unique du ProfileManager
     */
    public static ProfileManager getInstance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    /**
     * Initialise le répertoire des profils
     */
    private void initializeDirectory() {
        try {
            this.profilesDirectory = Paths.get(PROFILES_DIR);
            if (!Files.exists(profilesDirectory)) {
                Files.createDirectories(profilesDirectory);
                System.out.println("📁 Répertoire des profils créé: " + profilesDirectory.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la création du répertoire des profils: " + e.getMessage());
            this.profilesDirectory = Paths.get(".");
        }
    }

    /**
     * Sauvegarde un profil de joueur (version simplifiée)
     */
    public void saveProfile(PlayerProfile profile) throws BombermanException {
        if (profile == null || profile.getPlayerName() == null || profile.getPlayerName().trim().isEmpty()) {
            throw new BombermanException("Profil ou nom de joueur invalide");
        }

        try {
            String fileName = sanitizeFileName(profile.getPlayerName()) + PROFILE_EXTENSION;
            Path filePath = profilesDirectory.resolve(fileName);

            Properties props = new Properties();

            // Informations de base
            props.setProperty("playerName", profile.getPlayerName());
            props.setProperty("creationDate", profile.getCreationDate().format(DATE_FORMATTER));
            props.setProperty("lastPlayDate", profile.getLastPlayDate().format(DATE_FORMATTER));

            // Statistiques générales
            props.setProperty("totalGamesPlayed", String.valueOf(profile.getTotalGamesPlayed()));
            props.setProperty("totalWins", String.valueOf(profile.getTotalWins()));
            props.setProperty("totalLosses", String.valueOf(profile.getTotalLosses()));
            props.setProperty("totalBombsPlaced", String.valueOf(profile.getTotalBombsPlaced()));
            props.setProperty("totalEliminatonsDealt", String.valueOf(profile.getTotalEliminatonsDealt()));
            props.setProperty("totalDeaths", String.valueOf(profile.getTotalDeaths()));
            props.setProperty("totalPlayTimeSeconds", String.valueOf(profile.getTotalPlayTimeSeconds()));

            // Préférences
            props.setProperty("preferredTheme", profile.getPreferredTheme());
            props.setProperty("soundEnabled", String.valueOf(profile.isSoundEnabled()));
            props.setProperty("preferredBotDifficulty", String.valueOf(profile.getPreferredBotDifficulty()));

            // Statistiques par mode (basique)
            props.setProperty("classicWins", String.valueOf(profile.getClassicModeStats().getWins()));
            props.setProperty("classicGames", String.valueOf(profile.getClassicModeStats().getGamesPlayed()));
            props.setProperty("botWins", String.valueOf(profile.getBotModeStats().getWins()));
            props.setProperty("botGames", String.valueOf(profile.getBotModeStats().getGamesPlayed()));

            // Sauvegarder le fichier
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                props.store(fos, "Profil Super Bomberman - " + profile.getPlayerName());
            }

            System.out.println("💾 Profil sauvegardé: " + profile.getPlayerName() + " -> " + filePath.getFileName());

        } catch (IOException e) {
            throw new BombermanException("Impossible de sauvegarder le profil: " + e.getMessage(), e);
        }
    }

    /**
     * Charge un profil de joueur (version simplifiée)
     */
    public PlayerProfile loadProfile(String playerName) throws BombermanException {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new BombermanException("Nom de joueur invalide");
        }

        try {
            String fileName = sanitizeFileName(playerName) + PROFILE_EXTENSION;
            Path filePath = profilesDirectory.resolve(fileName);

            if (!Files.exists(filePath)) {
                System.out.println("📂 Aucun profil existant pour: " + playerName);
                return new PlayerProfile(playerName); // Nouveau profil
            }

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                props.load(fis);
            }

            // Créer le profil
            PlayerProfile profile = new PlayerProfile(playerName);

            // Charger les données de base
            try {
                String creationDateStr = props.getProperty("creationDate");
                if (creationDateStr != null) {
                    profile.setCreationDate(LocalDateTime.parse(creationDateStr, DATE_FORMATTER));
                }

                String lastPlayDateStr = props.getProperty("lastPlayDate");
                if (lastPlayDateStr != null) {
                    profile.setLastPlayDate(LocalDateTime.parse(lastPlayDateStr, DATE_FORMATTER));
                }
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors du chargement des dates, utilisation des valeurs par défaut");
            }

            // Charger les statistiques
            profile.setTotalGamesPlayed(getIntProperty(props, "totalGamesPlayed", 0));
            profile.setTotalWins(getIntProperty(props, "totalWins", 0));
            profile.setTotalLosses(getIntProperty(props, "totalLosses", 0));
            profile.setTotalBombsPlaced(getIntProperty(props, "totalBombsPlaced", 0));
            profile.setTotalEliminatonsDealt(getIntProperty(props, "totalEliminatonsDealt", 0));
            profile.setTotalDeaths(getIntProperty(props, "totalDeaths", 0));
            profile.setTotalPlayTimeSeconds(getLongProperty(props, "totalPlayTimeSeconds", 0L));

            // Charger les préférences
            profile.setPreferredTheme(props.getProperty("preferredTheme", "default"));
            profile.setSoundEnabled(getBooleanProperty(props, "soundEnabled", true));
            profile.setPreferredBotDifficulty(getIntProperty(props, "preferredBotDifficulty", 2));

            // Charger les stats par mode (basique)
            profile.getClassicModeStats().setWins(getIntProperty(props, "classicWins", 0));
            profile.getClassicModeStats().setGamesPlayed(getIntProperty(props, "classicGames", 0));
            profile.getBotModeStats().setWins(getIntProperty(props, "botWins", 0));
            profile.getBotModeStats().setGamesPlayed(getIntProperty(props, "botGames", 0));

            System.out.println("📖 Profil chargé: " + playerName + " (" + profile.getTotalGamesPlayed() + " parties)");
            return profile;

        } catch (IOException e) {
            throw new BombermanException("Impossible de charger le profil: " + e.getMessage(), e);
        }
    }

    /**
     * Enregistre une session de jeu terminée (version simplifiée)
     */
    public void recordGameSession(String playerName, Game game) {
        try {
            PlayerProfile profile = loadProfile(playerName);

            // Incrémenter les statistiques basiques
            profile.setTotalGamesPlayed(profile.getTotalGamesPlayed() + 1);

            // Déterminer le résultat
            if (game.getWinner() != null && game.getWinner().getName().equals(playerName)) {
                profile.setTotalWins(profile.getTotalWins() + 1);

                // Stats par mode
                if (game.hasBots()) {
                    profile.getBotModeStats().setWins(profile.getBotModeStats().getWins() + 1);
                    profile.getBotModeStats().setGamesPlayed(profile.getBotModeStats().getGamesPlayed() + 1);
                } else {
                    profile.getClassicModeStats().setWins(profile.getClassicModeStats().getWins() + 1);
                    profile.getClassicModeStats().setGamesPlayed(profile.getClassicModeStats().getGamesPlayed() + 1);
                }
            } else {
                profile.setTotalLosses(profile.getTotalLosses() + 1);

                // Stats par mode (défaite)
                if (game.hasBots()) {
                    profile.getBotModeStats().setGamesPlayed(profile.getBotModeStats().getGamesPlayed() + 1);
                } else {
                    profile.getClassicModeStats().setGamesPlayed(profile.getClassicModeStats().getGamesPlayed() + 1);
                }
            }

            // Statistiques approximatives
            profile.setTotalBombsPlaced(profile.getTotalBombsPlaced() + (int)(Math.random() * 5) + 1);
            profile.setTotalPlayTimeSeconds(profile.getTotalPlayTimeSeconds() + 120); // 2 minutes par partie

            // Mettre à jour la date
            profile.setLastPlayDate(LocalDateTime.now());

            saveProfile(profile);

            System.out.println("📊 Session enregistrée pour " + playerName);

        } catch (BombermanException e) {
            System.err.println("❌ Impossible d'enregistrer la session: " + e.getMessage());
        }
    }

    /**
     * Vérifie si un profil existe
     */
    public boolean profileExists(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }

        String fileName = sanitizeFileName(playerName) + PROFILE_EXTENSION;
        Path filePath = profilesDirectory.resolve(fileName);
        return Files.exists(filePath);
    }

    /**
     * Liste tous les profils disponibles
     */
    public List<String> listProfiles() {
        List<String> profileNames = new ArrayList<>();

        try {
            if (Files.exists(profilesDirectory)) {
                Files.list(profilesDirectory)
                        .filter(path -> path.toString().endsWith(PROFILE_EXTENSION))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            String profileName = fileName.substring(0, fileName.length() - PROFILE_EXTENSION.length());
                            profileNames.add(unsanitizeFileName(profileName));
                        });
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la lecture des profils: " + e.getMessage());
        }

        return profileNames;
    }

    /**
     * Supprime un profil
     */
    public boolean deleteProfile(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }

        try {
            String fileName = sanitizeFileName(playerName) + PROFILE_EXTENSION;
            Path filePath = profilesDirectory.resolve(fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("🗑️ Profil supprimé: " + playerName);
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la suppression du profil: " + e.getMessage());
        }

        return false;
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .toLowerCase();
    }

    private String unsanitizeFileName(String sanitizedName) {
        return sanitizedName;
    }

    private int getIntProperty(Properties props, String key, int defaultValue) {
        try {
            String value = props.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long getLongProperty(Properties props, String key, long defaultValue) {
        try {
            String value = props.getProperty(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBooleanProperty(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Classe pour les statistiques globales
     */
    public static class ProfileStats {
        private int totalProfiles;
        private int totalGamesPlayed;
        private int totalWins;
        private long totalPlayTime;
        private String mostActivePlayer;
        private int mostGamesPlayed;

        public ProfileStats() {
            this.mostActivePlayer = "Aucun";
            this.mostGamesPlayed = 0;
        }

        public void addProfile(PlayerProfile profile) {
            totalProfiles++;
            totalGamesPlayed += profile.getTotalGamesPlayed();
            totalWins += profile.getTotalWins();
            totalPlayTime += profile.getTotalPlayTimeSeconds();

            if (profile.getTotalGamesPlayed() > mostGamesPlayed) {
                mostGamesPlayed = profile.getTotalGamesPlayed();
                mostActivePlayer = profile.getPlayerName();
            }
        }

        public double getGlobalWinRate() {
            if (totalGamesPlayed == 0) return 0.0;
            return (double) totalWins / totalGamesPlayed * 100.0;
        }

        public String getFormattedTotalPlayTime() {
            long hours = totalPlayTime / 3600;
            long minutes = (totalPlayTime % 3600) / 60;
            return String.format("%dh %dm", hours, minutes);
        }

        // Getters
        public int getTotalProfiles() { return totalProfiles; }
        public int getTotalGamesPlayed() { return totalGamesPlayed; }
        public int getTotalWins() { return totalWins; }
        public String getMostActivePlayer() { return mostActivePlayer; }
        public int getMostGamesPlayed() { return mostGamesPlayed; }
    }

    /**
     * Obtient les statistiques globales de tous les profils
     */
    public ProfileStats getGlobalStats() {
        ProfileStats stats = new ProfileStats();

        List<String> profileNames = listProfiles();
        for (String profileName : profileNames) {
            try {
                PlayerProfile profile = loadProfile(profileName);
                stats.addProfile(profile);
            } catch (BombermanException e) {
                System.err.println("❌ Erreur lors du chargement du profil " + profileName + ": " + e.getMessage());
            }
        }

        return stats;
    }

    /**
     * Exporte un profil vers un fichier spécifique (version simplifiée)
     */
    public void exportProfile(String playerName, String exportPath) throws BombermanException {
        PlayerProfile profile = loadProfile(playerName);

        try {
            Properties props = new Properties();

            // Informations de base
            props.setProperty("playerName", profile.getPlayerName());
            props.setProperty("creationDate", profile.getCreationDate().format(DATE_FORMATTER));
            props.setProperty("lastPlayDate", profile.getLastPlayDate().format(DATE_FORMATTER));

            // Statistiques générales
            props.setProperty("totalGamesPlayed", String.valueOf(profile.getTotalGamesPlayed()));
            props.setProperty("totalWins", String.valueOf(profile.getTotalWins()));
            props.setProperty("totalLosses", String.valueOf(profile.getTotalLosses()));
            props.setProperty("totalBombsPlaced", String.valueOf(profile.getTotalBombsPlaced()));
            props.setProperty("totalEliminatonsDealt", String.valueOf(profile.getTotalEliminatonsDealt()));
            props.setProperty("totalDeaths", String.valueOf(profile.getTotalDeaths()));
            props.setProperty("totalPlayTimeSeconds", String.valueOf(profile.getTotalPlayTimeSeconds()));

            // Préférences
            props.setProperty("preferredTheme", profile.getPreferredTheme());
            props.setProperty("soundEnabled", String.valueOf(profile.isSoundEnabled()));
            props.setProperty("preferredBotDifficulty", String.valueOf(profile.getPreferredBotDifficulty()));

            // Statistiques par mode
            props.setProperty("classicWins", String.valueOf(profile.getClassicModeStats().getWins()));
            props.setProperty("classicGames", String.valueOf(profile.getClassicModeStats().getGamesPlayed()));
            props.setProperty("botWins", String.valueOf(profile.getBotModeStats().getWins()));
            props.setProperty("botGames", String.valueOf(profile.getBotModeStats().getGamesPlayed()));

            // Sauvegarder le fichier d'export
            try (FileOutputStream fos = new FileOutputStream(exportPath)) {
                props.store(fos, "Export du profil Super Bomberman - " + profile.getPlayerName());
            }

            System.out.println("📤 Profil exporté: " + playerName + " -> " + exportPath);

        } catch (IOException e) {
            throw new BombermanException("Impossible d'exporter le profil: " + e.getMessage(), e);
        }
    }

    /**
     * Importe un profil depuis un fichier (version simplifiée)
     */
    public PlayerProfile importProfile(String importPath) throws BombermanException {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(importPath)) {
                props.load(fis);
            }

            // Créer le profil à partir du fichier
            String playerName = props.getProperty("playerName", "Joueur Importé");
            PlayerProfile profile = new PlayerProfile(playerName);

            // Charger les données
            try {
                String creationDateStr = props.getProperty("creationDate");
                if (creationDateStr != null) {
                    profile.setCreationDate(LocalDateTime.parse(creationDateStr, DATE_FORMATTER));
                }

                String lastPlayDateStr = props.getProperty("lastPlayDate");
                if (lastPlayDateStr != null) {
                    profile.setLastPlayDate(LocalDateTime.parse(lastPlayDateStr, DATE_FORMATTER));
                }
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors du chargement des dates, utilisation des valeurs par défaut");
            }

            // Charger les statistiques
            profile.setTotalGamesPlayed(getIntProperty(props, "totalGamesPlayed", 0));
            profile.setTotalWins(getIntProperty(props, "totalWins", 0));
            profile.setTotalLosses(getIntProperty(props, "totalLosses", 0));
            profile.setTotalBombsPlaced(getIntProperty(props, "totalBombsPlaced", 0));
            profile.setTotalEliminatonsDealt(getIntProperty(props, "totalEliminatonsDealt", 0));
            profile.setTotalDeaths(getIntProperty(props, "totalDeaths", 0));
            profile.setTotalPlayTimeSeconds(getLongProperty(props, "totalPlayTimeSeconds", 0L));

            // Charger les préférences
            profile.setPreferredTheme(props.getProperty("preferredTheme", "default"));
            profile.setSoundEnabled(getBooleanProperty(props, "soundEnabled", true));
            profile.setPreferredBotDifficulty(getIntProperty(props, "preferredBotDifficulty", 2));

            // Charger les stats par mode
            profile.getClassicModeStats().setWins(getIntProperty(props, "classicWins", 0));
            profile.getClassicModeStats().setGamesPlayed(getIntProperty(props, "classicGames", 0));
            profile.getBotModeStats().setWins(getIntProperty(props, "botWins", 0));
            profile.getBotModeStats().setGamesPlayed(getIntProperty(props, "botGames", 0));

            // Sauvegarder le profil importé
            saveProfile(profile);

            System.out.println("📥 Profil importé: " + profile.getPlayerName());
            return profile;

        } catch (IOException e) {
            throw new BombermanException("Impossible d'importer le profil: " + e.getMessage(), e);
        }
    }

    /**
     * Enregistre une session de jeu personnalisée (version simplifiée)
     */
    public void recordCustomGameSession(String playerName, GameSession session) {
        try {
            PlayerProfile profile = loadProfile(playerName);

            // Incrémenter les statistiques depuis la session
            profile.setTotalGamesPlayed(profile.getTotalGamesPlayed() + 1);

            if (session.isWon()) {
                profile.setTotalWins(profile.getTotalWins() + 1);
            } else {
                profile.setTotalLosses(profile.getTotalLosses() + 1);
            }

            // Ajouter les statistiques de la session
            profile.setTotalBombsPlaced(profile.getTotalBombsPlaced() + session.getBombsPlaced());
            profile.setTotalEliminatonsDealt(profile.getTotalEliminatonsDealt() + session.getEliminationsDealt());
            profile.setTotalDeaths(profile.getTotalDeaths() + session.getDeaths());
            profile.setTotalPlayTimeSeconds(profile.getTotalPlayTimeSeconds() + session.getDurationSeconds());

            // Mettre à jour la date
            profile.setLastPlayDate(LocalDateTime.now());

            saveProfile(profile);

            System.out.println("📊 Session personnalisée enregistrée pour " + playerName);

        } catch (BombermanException e) {
            System.err.println("❌ Impossible d'enregistrer la session personnalisée: " + e.getMessage());
        }
    }
}