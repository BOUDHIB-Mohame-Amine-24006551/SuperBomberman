package fr.univ.bomberman.view;

import fr.univ.bomberman.model.PlayerProfile;
import fr.univ.bomberman.model.PlayerProfileManager;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Classe pour afficher un tableau des scores des profils dans le jeu.
 */
public class ScoreboardRenderer {
    
    private VBox container;
    private Canvas canvas;
    private GraphicsContext gc;
    private int width = 200;
    private int height = 400;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");
    
    /**
     * Constructeur pour le tableau des scores
     * 
     * @param container le conteneur VBox où placer le tableau
     */
    public ScoreboardRenderer(VBox container) {
        this.container = container;
        setupScoreboard();
    }
    
    /**
     * Configure le tableau des scores
     */
    private void setupScoreboard() {
        // Configurer le conteneur
        container.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.7), 
                new CornerRadii(5), 
                Insets.EMPTY)));
        container.setPadding(new Insets(10));
        container.setSpacing(10);
        
        // Titre du tableau
        Text title = new Text("CLASSEMENT");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setFill(Color.WHITE);
        
        // Canvas pour les scores
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        
        // Ajouter au conteneur
        container.getChildren().clear();
        container.getChildren().addAll(title, canvas);
        
        // Mettre à jour le tableau
        updateScoreboard();
    }
    
    /**
     * Met à jour le tableau des scores
     */
    public void updateScoreboard() {
        // Effacer le canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Fond semi-transparent
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Obtenir les profils triés
        List<PlayerProfile> profiles = PlayerProfileManager.getProfiles();
        
        // Paramètres d'affichage
        int rowHeight = 30;
        int padding = 5;
        
        // Dessiner l'en-tête
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("Joueur", padding, 20);
        gc.fillText("V", width - 60, 20);
        gc.fillText("J", width - 40, 20);
        gc.fillText("%", width - 20, 20);
        
        // Ligne de séparation
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeLine(padding, 25, width - padding, 25);
        
        // Dessiner les profils
        int maxProfilesToShow = 10;
        for (int i = 0; i < Math.min(profiles.size(), maxProfilesToShow); i++) {
            PlayerProfile profile = profiles.get(i);
            
            // Position Y de cette ligne
            int y = 25 + (i + 1) * rowHeight;
            
            // Alterner les couleurs de fond pour les lignes
            if (i % 2 == 0) {
                gc.setFill(Color.rgb(40, 40, 40, 0.3));
                gc.fillRect(0, y - rowHeight + 5, width, rowHeight);
            }
            
            // Dessiner le numéro de rang (1-10)
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText((i + 1) + ".", padding, y);
            
            // Dessiner le nom du joueur
            gc.setFill(getPlayerColor(i));
            String displayName = formatPlayerName(profile);
            gc.fillText(displayName, padding + 20, y);
            
            // Dessiner les statistiques
            gc.setFill(Color.WHITE);
            gc.fillText(String.valueOf(profile.getGamesWon()), width - 60, y);
            gc.fillText(String.valueOf(profile.getGamesPlayed()), width - 40, y);
            gc.fillText(DECIMAL_FORMAT.format(profile.getWinRate()), width - 20, y);
        }
        
        // Message si aucun profil
        if (profiles.isEmpty()) {
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            gc.fillText("Aucun profil enregistré", padding, 50);
            gc.fillText("Créez un profil dans", padding, 70);
            gc.fillText("le menu \"Profil\"", padding, 90);
        }
    }
    
    /**
     * Formate le nom du joueur pour l'affichage
     */
    private String formatPlayerName(PlayerProfile profile) {
        String fullName = profile.getFullName().trim();
        if (fullName.length() > 15) {
            return fullName.substring(0, 12) + "...";
        }
        return fullName;
    }
    
    /**
     * Retourne une couleur basée sur la position du joueur
     */
    private Color getPlayerColor(int position) {
        switch (position) {
            case 0: return Color.GOLD;
            case 1: return Color.SILVER;
            case 2: return Color.rgb(205, 127, 50); // Bronze
            default: return Color.LIGHTGRAY;
        }
    }
    
    /**
     * Dessine un badge spécial pour le profil actuel
     */
    public void highlightCurrentProfile() {
        PlayerProfile currentProfile = PlayerProfileManager.getCurrentProfile();
        if (currentProfile == null) return;
        
        List<PlayerProfile> profiles = PlayerProfileManager.getProfiles();
        int currentProfileIndex = -1;
        
        // Trouver l'index du profil courant
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getFullName().equals(currentProfile.getFullName())) {
                currentProfileIndex = i;
                break;
            }
        }
        
        // Si le profil courant est dans la liste et visible
        if (currentProfileIndex >= 0 && currentProfileIndex < 10) {
            int y = 25 + (currentProfileIndex + 1) * 30;
            
            // Dessiner un marqueur à côté du nom
            gc.setFill(Color.GREEN);
            gc.fillOval(5, y - 8, 8, 8);
        }
    }
} 