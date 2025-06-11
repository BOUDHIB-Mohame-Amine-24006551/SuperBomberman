package fr.univ.bomberman.view;

import fr.univ.bomberman.model.*;
import fr.univ.bomberman.exceptions.BombermanException;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Classe responsable du rendu graphique du jeu Bomberman avec support d'images et thèmes
 */
public class GameRenderer {

    private static final int CELL_SIZE = 40; // Taille d'une cellule en pixels
    private Canvas canvas;
    private GraphicsContext gc;

    // Cache pour les images
    private Map<String, Image> imageCache;
    private boolean imagesLoaded = false;
    private String currentTheme = "default";
    private Game game;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.imageCache = new HashMap<>();
        loadImages();
    }

    /**
     * Charge les images du thème actuel
     */
    private void loadImages() {
        loadImagesForTheme(currentTheme);
    }

    /**
     * Charge les images pour un thème spécifique
     */
    private void loadImagesForTheme(String theme) {
        try {
            System.out.println("Tentative de chargement des images pour le thème: " + theme);

            // Chemin de base selon le thème
            String basePath = "/fr/univ/bomberman/image/" + theme + "/";

            // Essayer de charger les images
            imageCache.put("player1", loadImageSafely(basePath + "player1.png"));
            imageCache.put("player2", loadImageSafely(basePath + "player2.png"));
            imageCache.put("player3", loadImageSafely(basePath + "player3.png"));
            imageCache.put("player4", loadImageSafely(basePath + "player4.png"));

            imageCache.put("bomb", loadImageSafely(basePath + "bomb.png"));
            imageCache.put("explosion", loadImageSafely(basePath + "explosion.png"));
            imageCache.put("wall", loadImageSafely(basePath + "wall.png"));
            imageCache.put("brick", loadImageSafely(basePath + "brick.png"));
            imageCache.put("ground", loadImageSafely(basePath + "ground.png"));

            // Vérifier qu'au moins quelques images ont été chargées
            long loadedCount = imageCache.values().stream().filter(img -> img != null).count();
            if (loadedCount >= 4) { // Au moins les 4 joueurs
                imagesLoaded = true;
                System.out.println("Images du thème '" + theme + "' chargées avec succès: " + loadedCount + "/9");
            } else {
                System.out.println("Pas assez d'images chargées pour le thème '" + theme + "' (" + loadedCount + "/9)");

                // Si ce n'est pas le thème par défaut, essayer le thème par défaut
                if (!theme.equals("default")) {
                    System.out.println("Tentative de fallback vers le thème par défaut...");
                    imageCache.clear();
                    loadImagesForTheme("default");
                    return;
                }

                imagesLoaded = false;
            }

        } catch (Exception e) {
            System.out.println("Erreur lors du chargement des images du thème '" + theme + "': " + e.getMessage());
            e.printStackTrace();

            // Fallback vers le thème par défaut si possible
            if (!theme.equals("default")) {
                System.out.println("Tentative de fallback vers le thème par défaut...");
                imageCache.clear();
                loadImagesForTheme("default");
            } else {
                imagesLoaded = false;
            }
        }
    }

    /**
     * Charge une image de manière sécurisée
     */
    private Image loadImageSafely(String path) {
        try {
            System.out.println("Tentative de chargement: " + path);

            // Vérifier que la ressource existe
            if (getClass().getResourceAsStream(path) == null) {
                System.out.println("Ressource non trouvée: " + path);
                return null;
            }

            Image image = new Image(getClass().getResourceAsStream(path));

            // Vérifier que l'image n'est pas en erreur
            if (image.isError()) {
                System.out.println("Erreur lors du chargement de l'image: " + path);
                if (image.getException() != null) {
                    image.getException().printStackTrace();
                }
                return null;
            }

            System.out.println("Image chargée avec succès: " + path + " (" + image.getWidth() + "x" + image.getHeight() + ")");
            return image;

        } catch (Exception e) {
            System.out.println("Exception lors du chargement de " + path + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Change le thème et recharge les images
     */
    public void changeTheme(String newTheme) {
        System.out.println("Changement de thème vers: " + newTheme);
        this.currentTheme = newTheme;
        imageCache.clear();
        loadImagesForTheme(newTheme);
    }

    /**
     * Obtient le thème actuel
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    public void render(Game game) {
        this.game = game;

        clearCanvas();

        // Dessiner le plateau
        Board board = game.getBoard();
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getCols(); x++) {
                try {
                    Position pos = new Position(x, y);
                    Cell cell = board.getCell(pos);
                    drawCell(cell, x, y);
                } catch (Exception e) {
                    // Ignorer les erreurs de position
                }
            }
        }

        // ✅ NOUVEAU: Dessiner l'avertissement des zones dangereuses en arrière-plan
        drawExplosionWarning(game);

        // Dessiner les bombes
        for (Bomb bomb : game.getActiveBombs()) {
            drawBomb(bomb);
        }

        // Dessiner les explosions (avec indication mortelle/sûre)
        for (Explosion explosion : game.getActiveExplosions()) {
            drawExplosion(explosion);
        }

        // Dessiner les joueurs
        for (Player player : game.getPlayers()) {
            if (!player.isEliminated()) {
                drawPlayer(player);
            }
        }

        // ✅ AJOUTÉ: Dessiner les indicateurs de bot si présent
        if (game.hasBots()) {
            drawBotIndicators(game);
        }

        // Dessiner les indicateurs de cooldown
        drawCooldownIndicators(game);

        // Dessiner les informations de contrôles pour 4 joueurs
        if (game.getPlayerCount() > 2) {
            drawControlsInfo(game);
        }

        // Dessiner l'écran de victoire si le jeu est terminé
        if (game.isGameOver()) {
            // ✅ MODIFIÉ: Choisir le bon écran de victoire selon le type de jeu
            if (game.hasBots()) {
                drawBotVictoryScreen(game);
            } else {
                drawVictoryScreen(game);
            }
        }
    }

    /**
     * Dessine le plateau de jeu (murs, briques, cases vides)
     */
    private void drawBoard(Board board) {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getCols(); x++) {
                try {
                    Position pos = new Position(x, y);
                    Cell cell = board.getCell(pos);
                    drawCell(cell, x, y);
                } catch (BombermanException e) {
                    // Ignorer les erreurs de position
                }
            }
        }
    }

    /**
     * Dessine une cellule individuelle avec images si disponibles
     */
    private void drawCell(Cell cell, int x, int y) {
        double pixelX = x * CELL_SIZE;
        double pixelY = y * CELL_SIZE;

        switch (cell.getType()) {
            case EMPTY:
                Image groundImg = imageCache.get("ground");
                if (imagesLoaded && groundImg != null) {
                    gc.drawImage(groundImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                } else {
                    // Rendu par défaut
                    gc.setFill(Color.LIGHTGRAY);
                    gc.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                    gc.setStroke(Color.GRAY);
                    gc.strokeRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                }
                break;

            case INDESTRUCTIBLE_WALL:
                Image wallImg = imageCache.get("wall");
                if (imagesLoaded && wallImg != null) {
                    gc.drawImage(wallImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                } else {
                    // Rendu par défaut
                    gc.setFill(Color.DARKGRAY);
                    gc.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                }
                break;

            case DESTRUCTIBLE_BRICK:
                Image brickImg = imageCache.get("brick");
                if (imagesLoaded && brickImg != null) {
                    gc.drawImage(brickImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                } else {
                    // Rendu par défaut
                    gc.setFill(Color.BROWN);
                    gc.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                    gc.setStroke(Color.DARKRED);
                    gc.strokeRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                    // Motif de brique
                    gc.setStroke(Color.SADDLEBROWN);
                    gc.strokeLine(pixelX + 5, pixelY + CELL_SIZE/2, pixelX + CELL_SIZE - 5, pixelY + CELL_SIZE/2);
                    gc.strokeLine(pixelX + CELL_SIZE/2, pixelY + 5, pixelX + CELL_SIZE/2, pixelY + CELL_SIZE - 5);
                }
                break;

            // ✅ NOUVEAU: Rendu des drapeaux
            case FLAG_PLAYER_1:
                drawFlag(pixelX, pixelY, Color.RED, "🔴");
                break;
            case FLAG_PLAYER_2:
                drawFlag(pixelX, pixelY, Color.BLUE, "🔵");
                break;
            case FLAG_PLAYER_3:
                drawFlag(pixelX, pixelY, Color.YELLOW, "🟡");
                break;
            case FLAG_PLAYER_4:
                drawFlag(pixelX, pixelY, Color.GREEN, "🟢");
                break;
        }
    }

    private void drawFlag(double pixelX, double pixelY, Color playerColor, String emoji) {
        // Dessiner d'abord le sol
        Image groundImg = imageCache.get("ground");
        if (imagesLoaded && groundImg != null) {
            gc.drawImage(groundImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
        }

        // Essayer de charger une image de drapeau spécifique
        Image flagImg = imageCache.get("flag_" + playerColor.toString().toLowerCase());
        if (imagesLoaded && flagImg != null) {
            gc.drawImage(flagImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
        } else {
            // Rendu par défaut du drapeau

            // Mât du drapeau
            gc.setStroke(Color.BROWN);
            gc.setLineWidth(3);
            gc.strokeLine(pixelX + 5, pixelY + 5, pixelX + 5, pixelY + CELL_SIZE - 5);

            // Drapeau triangulaire
            gc.setFill(playerColor);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);

            double[] xPoints = {pixelX + 8, pixelX + CELL_SIZE - 5, pixelX + 8};
            double[] yPoints = {pixelY + 8, pixelY + 15, pixelY + 22};

            gc.fillPolygon(xPoints, yPoints, 3);
            gc.strokePolygon(xPoints, yPoints, 3);

            // Emoji ou symbole pour identifier le joueur
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 8));
            gc.fillText(emoji, pixelX + CELL_SIZE - 15, pixelY + CELL_SIZE - 5);
        }
    }

    /**
     * Dessine les bombes actives avec images si disponibles
     */
    private void drawBombs(Game game) {
        List<Bomb> bombs = getActiveBombs(game);

        for (Bomb bomb : bombs) {
            Position pos = bomb.getPosition();
            double pixelX = pos.getX() * CELL_SIZE;
            double pixelY = pos.getY() * CELL_SIZE;

            Image bombImg = imageCache.get("bomb");
            if (imagesLoaded && bombImg != null) {
                gc.drawImage(bombImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
            } else {
                // Rendu par défaut
                gc.setFill(Color.BLACK);
                gc.fillOval(pixelX + 5, pixelY + 5, CELL_SIZE - 10, CELL_SIZE - 10);

                // Mèche
                gc.setStroke(Color.RED);
                gc.setLineWidth(3);
                gc.strokeLine(pixelX + CELL_SIZE/2, pixelY + 5, pixelX + CELL_SIZE/2 + 5, pixelY);
                gc.setLineWidth(1);
            }
        }
    }

    /**
     * Dessine les explosions actives avec images si disponibles
     */
    private void drawExplosion(Explosion explosion) {
        java.util.List<Position> positions = explosion.getAffectedPositions();

        for (Position pos : positions) {
            double pixelX = pos.getX() * CELL_SIZE;
            double pixelY = pos.getY() * CELL_SIZE;

            Image explosionImg = imageCache.get("explosion");
            if (imagesLoaded && explosionImg != null) {
                gc.drawImage(explosionImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
            } else {
                // ✅ SIMPLIFIÉ: Rendu par défaut - toujours rouge/orange (mortelle)
                gc.setFill(Color.RED);
                gc.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                gc.setFill(Color.ORANGE);
                gc.fillOval(pixelX + 3, pixelY + 3, CELL_SIZE - 6, CELL_SIZE - 6);
                gc.setFill(Color.YELLOW);
                gc.fillOval(pixelX + 8, pixelY + 8, CELL_SIZE - 16, CELL_SIZE - 16);
            }
        }
    }


    private void drawExplosionWarning(Game game) {
        for (Explosion explosion : game.getActiveExplosions()) {
            if (explosion.isDeadly()) {
                java.util.List<Position> positions = explosion.getAffectedPositions();

                // Effet de clignotement pour les zones dangereuses
                double alpha = 0.3 + 0.2 * Math.sin(System.currentTimeMillis() / 200.0);

                for (Position pos : positions) {
                    double pixelX = pos.getX() * CELL_SIZE;
                    double pixelY = pos.getY() * CELL_SIZE;

                    gc.setFill(Color.color(1, 0, 0, alpha));
                    gc.fillRect(pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    private void drawBotIndicators(Game game) {
        if (!game.hasBots()) return;

        BotPlayer bot = game.getBot();
        if (bot == null || bot.isEliminated()) return;

        double pixelX = bot.getX() * CELL_SIZE;
        double pixelY = bot.getY() * CELL_SIZE;

        // ✅ NOUVEAU: Indicateur de vitesse du bot en temps réel
        gc.setFill(Color.YELLOW);
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(1);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 8));

        // Afficher la vitesse selon la difficulté
        String speedIndicator = getSpeedIndicator(bot.getDifficulty());
        gc.setFill(Color.YELLOW);
        gc.fillText(speedIndicator, pixelX + CELL_SIZE - 15, pixelY + 8);

        // ✅ NOUVEAU: Indicateur d'état du bot (poursuite, attaque, etc.)
        if (isNearPlayer(bot, game)) {
            // Indicateur de "chasse active"
            gc.setFill(Color.RED);
            gc.fillOval(pixelX - 3, pixelY - 3, 6, 6);

            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 6));
            gc.fillText("!", pixelX - 1, pixelY + 1);
        }
    }

    private String getSpeedIndicator(int difficulty) {
        switch (difficulty) {
            case 1: return "="; // Même vitesse
            case 2: return ">"; // Plus rapide
            case 3: return ">>"; // Très rapide
            default: return "=";
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Vérifie si le bot est proche d'un joueur
     */
    private boolean isNearPlayer(BotPlayer bot, Game game) {
        Position botPos = bot.getPosition();

        for (Player player : game.getPlayers()) {
            if (player != bot && !player.isEliminated()) {
                Position playerPos = player.getPosition();
                int distance = Math.abs(botPos.getX() - playerPos.getX()) +
                        Math.abs(botPos.getY() - playerPos.getY());
                if (distance <= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getDifficultyStars(int difficulty) {
        switch (difficulty) {
            case 1: return "⭐";      // Facile
            case 2: return "⭐⭐";    // Moyen
            case 3: return "⭐⭐⭐";  // Difficile
            default: return "⭐⭐";
        }
    }

    private void drawBotVictoryScreen(Game game) {
        if (!game.isGameOver() || !game.hasBots()) return;

        // Overlay semi-transparent
        gc.setFill(Color.BLACK);
        gc.setGlobalAlpha(0.8);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setGlobalAlpha(1.0);

        Player winner = game.getWinner();
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;

        if (winner != null) {
            if (winner instanceof BotPlayer) {
                // L'IA a gagné
                BotPlayer bot = (BotPlayer) winner;

                // Titre "DÉFAITE"
                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 44));
                gc.setFill(Color.RED);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(3);

                String defeatText = "🤖 L'IA VOUS A BATTU !";
                double titleWidth = getTextWidth(defeatText, 44);
                double titleX = centerX - titleWidth / 2;
                double titleY = centerY - 60;

                gc.strokeText(defeatText, titleX, titleY);
                gc.fillText(defeatText, titleX, titleY);

                // Message encourageant
                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 24));
                gc.setFill(Color.ORANGE);

                String encourageText = "Bot " + getBotDifficultyName(bot.getDifficulty()) + " " + getDifficultyStars(bot.getDifficulty());
                double encWidth = getTextWidth(encourageText, 24);
                double encX = centerX - encWidth / 2;
                double encY = centerY - 10;

                gc.fillText(encourageText, encX, encY);

                // Message de motivation
                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 18));
                gc.setFill(Color.LIGHTBLUE);

                String motivationText = getMotivationMessage(bot.getDifficulty());
                double motWidth = getTextWidth(motivationText, 18);
                double motX = centerX - motWidth / 2;
                double motY = centerY + 25;

                gc.fillText(motivationText, motX, motY);

            } else {
                // Le joueur humain a gagné
                // Titre "VICTOIRE"
                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 48));
                gc.setFill(Color.GOLD);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(3);

                String victoryText = "🏆 VICTOIRE HUMAINE !";
                double titleWidth = getTextWidth(victoryText, 48);
                double titleX = centerX - titleWidth / 2;
                double titleY = centerY - 60;

                gc.strokeText(victoryText, titleX, titleY);
                gc.fillText(victoryText, titleX, titleY);

                // Message de félicitations
                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 24));
                gc.setFill(Color.LIME);

                BotPlayer bot = game.getBot();
                String congratsText = "Vous avez vaincu le Bot " + (bot != null ? getBotDifficultyName(bot.getDifficulty()) : "");
                double congWidth = getTextWidth(congratsText, 24);
                double congX = centerX - congWidth / 2;
                double congY = centerY - 10;

                gc.fillText(congratsText, congX, congY);

                // Étoiles de victoire
                drawVictoryStars(centerX, centerY - 30);
            }
        } else {
            // Égalité (rare mais possible)
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 42));
            gc.setFill(Color.ORANGE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);

            String drawText = "🤝 ÉGALITÉ !";
            double drawWidth = getTextWidth(drawText, 42);
            double drawX = centerX - drawWidth / 2;
            double drawY = centerY;

            gc.strokeText(drawText, drawX, drawY);
            gc.fillText(drawText, drawX, drawY);
        }

        // Instructions
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 18));
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        String instructions = "R: Revanche contre l'IA | ESC: Retour au menu";
        double instrWidth = getTextWidth(instructions, 18);
        double instrX = centerX - instrWidth / 2;
        double instrY = centerY + 80;

        gc.strokeText(instructions, instrX, instrY);
        gc.fillText(instructions, instrX, instrY);
    }

    // Ajoutez ces méthodes à votre GameRenderer.java

    /**
     * ✅ MÉTHODE MANQUANTE: Dessine des étoiles de victoire
     */
    private void drawVictoryStars(double centerX, double centerY) {
        gc.setFill(Color.YELLOW);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(1);

        // 8 étoiles en cercle autour du centre
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI * 2) / 8;
            double starX = centerX + Math.cos(angle) * 80;
            double starY = centerY + Math.sin(angle) * 50;
            drawStar(starX, starY, 6);
        }
    }


    private String getMotivationMessage(int difficulty) {
        switch (difficulty) {
            case 1: return "Entraînez-vous encore !";
            case 2: return "Belle résistance ! Réessayez !";
            case 3: return "L'IA était redoutable ! Impressionnant !";
            default: return "Nouvelle tentative ?";
        }
    }

    private String getBotDifficultyName(int difficulty) {
        switch (difficulty) {
            case 1: return "Facile";
            case 2: return "Moyen";
            case 3: return "Difficile";
            default: return "Moyen";
        }
    }



    private void drawPlayers(Game game) {
        List<Player> players = getPlayers(game);
        String[] playerImageKeys = {"player1", "player2", "player3", "player4"};
        Color[] playerColors = {Color.BLUE, Color.GREEN, Color.PURPLE, Color.PINK};

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            if (!player.isEliminated()) {
                Position pos = player.getPosition();
                double pixelX = pos.getX() * CELL_SIZE;
                double pixelY = pos.getY() * CELL_SIZE;

                // Dessiner d'abord le sol sous le joueur
                Image groundImg = imageCache.get("ground");
                if (imagesLoaded && groundImg != null) {
                    gc.drawImage(groundImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                }

                // Dessiner le joueur avec son image
                String playerKey = playerImageKeys[i % playerImageKeys.length];
                Image playerImg = imageCache.get(playerKey);

                if (imagesLoaded && playerImg != null) {
                    gc.drawImage(playerImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
                } else {
                    // Rendu par défaut
                    gc.setFill(playerColors[i % playerColors.length]);
                    gc.fillOval(pixelX + 3, pixelY + 3, CELL_SIZE - 6, CELL_SIZE - 6);

                    // Contour
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(2);
                    gc.strokeOval(pixelX + 3, pixelY + 3, CELL_SIZE - 6, CELL_SIZE - 6);
                    gc.setLineWidth(1);
                }

                // Afficher le nom du joueur au-dessus
                gc.setFill(Color.WHITE);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeText(player.getName(), pixelX + 2, pixelY - 5);
                gc.fillText(player.getName(), pixelX + 2, pixelY - 5);
            }
        }
    }

    private void drawGameInfo(Game game) {
        double infoY = game.getBoard().getRows() * CELL_SIZE + 20;

        // Joueur actuel
        Player currentPlayer = game.getCurrentPlayer();
        gc.setFill(Color.BLACK);
        gc.fillText("Tour de: " + currentPlayer.getName(), 10, infoY);
        gc.fillText("Bombes restantes: " + currentPlayer.getRemainingBombs(), 10, infoY + 20);

        // Afficher le statut des images et le thème
        if (imagesLoaded) {
            gc.setFill(Color.GREEN);
            gc.fillText("Images: " + currentTheme.toUpperCase(), 200, infoY);
        } else {
            gc.setFill(Color.RED);
            gc.fillText("Images: Rendu par défaut", 200, infoY);
        }

        // État de la partie
        if (game.isGameOver()) {
            Player winner = game.getWinner();
            if (winner != null) {
                gc.setFill(Color.GREEN);
                gc.fillText("GAGNANT: " + winner.getName() + "!", 10, infoY + 40);
            } else {
                gc.setFill(Color.RED);
                gc.fillText("ÉGALITÉ!", 10, infoY + 40);
            }
        }
    }

    /**
     * Recharge les images
     */
    public void reloadImages() {
        imageCache.clear();
        loadImages();
    }

    /**
     * Vérifie si les images sont chargées
     */
    public boolean areImagesLoaded() {
        return imagesLoaded;
    }

    // Méthodes helper pour accéder aux données du jeu
    private List<Bomb> getActiveBombs(Game game) {
        try {
            java.lang.reflect.Field field = Game.class.getDeclaredField("activeBombs");
            field.setAccessible(true);
            return (List<Bomb>) field.get(game);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    private List<Explosion> getActiveExplosions(Game game) {
        try {
            java.lang.reflect.Field field = Game.class.getDeclaredField("activeExplosions");
            field.setAccessible(true);
            return (List<Explosion>) field.get(game);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    private List<Player> getPlayers(Game game) {
        try {
            java.lang.reflect.Field field = Game.class.getDeclaredField("players");
            field.setAccessible(true);
            return (List<Player>) field.get(game);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    private List<Position> getExplosionPositions(Explosion explosion) {
        try {
            java.lang.reflect.Field field = Explosion.class.getDeclaredField("affectedPositions");
            field.setAccessible(true);
            return (List<Position>) field.get(explosion);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    // Ajoutez cette méthode à votre GameRenderer.java :
    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Optionnel: remplir avec une couleur de fond
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawPlayer(Player player) {
        if (player.isEliminated()) return;

        double pixelX = player.getX() * CELL_SIZE;
        double pixelY = player.getY() * CELL_SIZE;

        // Dessiner d'abord le sol sous le joueur
        Image groundImg = imageCache.get("ground");
        if (imagesLoaded && groundImg != null) {
            gc.drawImage(groundImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
        }

        // Déterminer l'image du joueur selon son index
        java.util.List<Player> players = getPlayers(game);
        int playerIndex = players.indexOf(player);
        String[] playerImageKeys = {"player1", "player2", "player3", "player4"};
        Color[] playerColors = {Color.BLUE, Color.GREEN, Color.PURPLE, Color.PINK};

        // ✅ NOUVEAU: Couleur spéciale pour le bot
        if (player instanceof BotPlayer) {
            playerColors = new Color[]{Color.RED, Color.ORANGE, Color.DARKRED, Color.CRIMSON};
        }

        // Dessiner le joueur avec son image
        String playerKey = playerImageKeys[playerIndex % playerImageKeys.length];
        Image playerImg = imageCache.get(playerKey);

        if (imagesLoaded && playerImg != null) {
            gc.drawImage(playerImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
        } else {
            // Rendu par défaut
            Color playerColor = playerColors[playerIndex % playerColors.length];
            gc.setFill(playerColor);
            gc.fillOval(pixelX + 3, pixelY + 3, CELL_SIZE - 6, CELL_SIZE - 6);

            // Contour
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(pixelX + 3, pixelY + 3, CELL_SIZE - 6, CELL_SIZE - 6);
            gc.setLineWidth(1);
        }

        // ✅ MODIFIÉ: Affichage spécial pour le bot
        if (player instanceof BotPlayer) {
            BotPlayer bot = (BotPlayer) player;

            // Nom du bot avec indicateur spécial
            gc.setFill(Color.CYAN);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 10));
            gc.strokeText("🤖 " + player.getName(), pixelX + 2, pixelY - 5);
            gc.fillText("🤖 " + player.getName(), pixelX + 2, pixelY - 5);

            // Indicateur de difficulté
            String difficultyIndicator = getDifficultyStars(bot.getDifficulty());
            gc.setFill(Color.GOLD);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 8));
            gc.fillText(difficultyIndicator, pixelX + 2, pixelY + CELL_SIZE + 12);
        } else {
            // Affichage normal pour les joueurs humains
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 10));
            gc.strokeText(player.getName(), pixelX + 2, pixelY - 5);
            gc.fillText(player.getName(), pixelX + 2, pixelY - 5);
        }
    }

    private void drawBomb(Bomb bomb) {
        double pixelX = bomb.getX() * CELL_SIZE;
        double pixelY = bomb.getY() * CELL_SIZE;

        Image bombImg = imageCache.get("bomb");
        if (imagesLoaded && bombImg != null) {
            gc.drawImage(bombImg, pixelX, pixelY, CELL_SIZE, CELL_SIZE);
        } else {
            // Rendu par défaut
            gc.setFill(Color.BLACK);
            gc.fillOval(pixelX + 5, pixelY + 5, CELL_SIZE - 10, CELL_SIZE - 10);

            // Mèche
            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            gc.strokeLine(pixelX + CELL_SIZE/2, pixelY + 5, pixelX + CELL_SIZE/2 + 5, pixelY);
            gc.setLineWidth(1);
        }

        // Afficher le timer de la bombe
        gc.setFill(Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 12));
        String timerText = String.valueOf(bomb.getTimer());
        gc.fillText(timerText, pixelX + CELL_SIZE/2 - 3, pixelY + CELL_SIZE/2 + 4);
    }


    /**
     * NOUVELLE MÉTHODE: Affiche un écran de victoire impressionnant
     */
    private void drawVictoryScreen(Game game) {
        if (!game.isGameOver()) return;

        // Overlay semi-transparent
        gc.setFill(Color.BLACK);
        gc.setGlobalAlpha(0.7);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setGlobalAlpha(1.0);

        Player winner = game.getWinner();
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;

        if (winner != null) {
            // Couleur du gagnant
            Color winnerColor = getPlayerColor(winner);

            // Titre principal "VICTOIRE !"
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 48));
            gc.setFill(Color.GOLD);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);

            String victoryText = "🏆 VICTOIRE ! 🏆";
            double titleWidth = getTextWidth(victoryText, 48);
            double titleX = centerX - titleWidth / 2;
            double titleY = centerY - 60;

            gc.strokeText(victoryText, titleX, titleY);
            gc.fillText(victoryText, titleX, titleY);

            // Nom du gagnant
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 36));
            gc.setFill(winnerColor);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);

            String winnerText = winner.getName() + " gagne !";
            double winnerWidth = getTextWidth(winnerText, 36);
            double winnerX = centerX - winnerWidth / 2;
            double winnerY = centerY + 10;

            gc.strokeText(winnerText, winnerX, winnerY);
            gc.fillText(winnerText, winnerX, winnerY);

            // Étoiles autour du gagnant
            drawStars(centerX, centerY - 20);

        } else {
            // Égalité
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 42));
            gc.setFill(Color.ORANGE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);

            String drawText = "💥 ÉGALITÉ ! 💥";
            double drawWidth = getTextWidth(drawText, 42);
            double drawX = centerX - drawWidth / 2;
            double drawY = centerY;

            gc.strokeText(drawText, drawX, drawY);
            gc.fillText(drawText, drawX, drawY);
        }

        // Instructions
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 18));
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        String instructions = "Appuyez sur R pour rejouer ou ESC pour le menu";
        double instrWidth = getTextWidth(instructions, 18);
        double instrX = centerX - instrWidth / 2;
        double instrY = centerY + 80;

        gc.strokeText(instructions, instrX, instrY);
        gc.fillText(instructions, instrX, instrY);
    }

    private void drawStars(double centerX, double centerY) {
        gc.setFill(Color.YELLOW);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(1);

        // 8 étoiles en cercle autour du centre
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI * 2) / 8;
            double starX = centerX + Math.cos(angle) * 80;
            double starY = centerY + Math.sin(angle) * 50;
            drawStar(starX, starY, 6);
        }
    }

    /**
     * MÉTHODE CORRIGÉE: Dessine une étoile à 5 branches
     */
    private void drawStar(double centerX, double centerY, double radius) {
        double[] xPoints = new double[10];
        double[] yPoints = new double[10];

        for (int i = 0; i < 10; i++) {
            double angle = (i * Math.PI) / 5;
            double r = (i % 2 == 0) ? radius : radius / 2;
            xPoints[i] = centerX + r * Math.cos(angle - Math.PI / 2);
            yPoints[i] = centerY + r * Math.sin(angle - Math.PI / 2);
        }

        gc.fillPolygon(xPoints, yPoints, 10);
        gc.strokePolygon(xPoints, yPoints, 10);
    }

    /**
     * NOUVELLE MÉTHODE: Obtient la couleur associée à un joueur
     */
    private Color getPlayerColor(Player player) {
        java.util.List<Player> players = getPlayers(game);
        int playerIndex = players.indexOf(player);

        Color[] playerColors = {Color.BLUE, Color.GREEN, Color.PURPLE, Color.PINK};
        return playerColors[playerIndex % playerColors.length];
    }

    private void drawControlsInfo(Game game) {
        if (game.getPlayerCount() <= 2) return; // Seulement pour 4 joueurs

        // Position en haut à droite
        double startX = canvas.getWidth() - 220;
        double startY = 20;

        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 9));

        // Fond semi-transparent plus grand
        gc.setFill(Color.BLACK);
        gc.setGlobalAlpha(0.8);
        gc.fillRoundRect(startX - 10, startY - 5, 210, 100, 5, 5);
        gc.setGlobalAlpha(1.0);

        // Titre
        gc.setFill(Color.WHITE);
        gc.fillText("CONTRÔLES (1 touche = 1 mouvement):", startX, startY + 10);

        // Contrôles de chaque joueur avec plus de détails
        String[] controls = {
                "🔴 ZQSD + A (bombe)",
                "🔵 ↑↓←→ + ENTRÉE (bombe)",
                "🟡 IJKL + U (bombe)",
                "🟢 8456 + 7 (bombe)"
        };

        Color[] colors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};

        for (int i = 0; i < Math.min(game.getPlayerCount(), 4); i++) {
            gc.setFill(colors[i]);
            gc.fillText(controls[i], startX, startY + 25 + (i * 15));
        }

        // Instructions générales
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(javafx.scene.text.Font.font("Arial", 8));
        gc.fillText("T: Thème | R: Restart | ESC: Menu", startX, startY + 90);
    }

    /**
     * NOUVELLE MÉTHODE: Calcule approximativement la largeur d'un texte
     */
    private double getTextWidth(String text, double fontSize) {
        // Approximation simple : chaque caractère fait environ 0.6 * fontSize en largeur
        return text.length() * fontSize * 0.6;
    }

    private void drawCooldownIndicators(Game game) {
        List<Player> players = game.getPlayers();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.isEliminated()) continue;

            // Position du joueur
            double playerPixelX = player.getX() * CELL_SIZE;
            double playerPixelY = player.getY() * CELL_SIZE;

            // Si le joueur est en cooldown, dessiner un indicateur
            if (player.isOnBombCooldown()) {
                drawCooldownBar(playerPixelX, playerPixelY, player);
            } else {
                // Optionnel: indicateur "prêt"
                drawReadyIndicator(playerPixelX, playerPixelY, player);
            }
        }
    }
    private void drawCooldownBar(double x, double y, Player player) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Dimensions de la barre
        double barWidth = CELL_SIZE * 0.8;
        double barHeight = 4;
        double barX = x + (CELL_SIZE - barWidth) / 2;
        double barY = y - 8; // Au-dessus du joueur

        // Fond de la barre (gris)
        gc.setFill(Color.GRAY);
        gc.fillRect(barX, barY, barWidth, barHeight);

        // Barre de progression (rouge -> jaune -> vert)
        int cooldownPercent = player.getCooldownPercentage();
        double progressWidth = barWidth * (100 - cooldownPercent) / 100.0;

        // Couleur selon le pourcentage
        Color barColor;
        if (cooldownPercent > 66) {
            barColor = Color.RED;
        } else if (cooldownPercent > 33) {
            barColor = Color.ORANGE;
        } else {
            barColor = Color.YELLOW;
        }

        gc.setFill(barColor);
        gc.fillRect(barX, barY, progressWidth, barHeight);

        // Bordure
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(barX, barY, barWidth, barHeight);

        // Texte du temps restant
        long remainingMs = player.getRemainingCooldown();
        if (remainingMs > 0) {
            String timeText = String.format("%.1f", remainingMs / 1000.0) + "s";
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 10));

            // Centrer le texte
            javafx.scene.text.Text textNode = new javafx.scene.text.Text(timeText);
            textNode.setFont(gc.getFont());
            double textWidth = textNode.getBoundsInLocal().getWidth();

            gc.fillText(timeText, barX + (barWidth - textWidth) / 2, barY - 2);
        }
    }
    private void drawReadyIndicator(double x, double y, Player player) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Petit cercle vert avec checkmark
        double circleX = x + CELL_SIZE - 12;
        double circleY = y + 2;
        double radius = 5;

        // Cercle vert
        gc.setFill(Color.LIME);
        gc.fillOval(circleX, circleY, radius * 2, radius * 2);

        // Bordure
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(1);
        gc.strokeOval(circleX, circleY, radius * 2, radius * 2);

        // Checkmark (✓)
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(2);
        gc.strokeLine(circleX + 2, circleY + radius, circleX + radius - 1, circleY + radius + 2);
        gc.strokeLine(circleX + radius - 1, circleY + radius + 2, circleX + radius * 2 - 2, circleY + 2);
    }

    private void drawPlayerWithCooldownEffect(Player player) {
        // Position du joueur
        double x = player.getX() * CELL_SIZE;
        double y = player.getY() * CELL_SIZE;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        if (player.isOnBombCooldown()) {
            // Effet de pulsation rouge
            long remaining = player.getRemainingCooldown();
            double alpha = 0.3 + 0.2 * Math.sin(System.currentTimeMillis() / 200.0);

            gc.setFill(Color.color(1, 0, 0, alpha));
            gc.fillOval(x - 2, y - 2, CELL_SIZE + 4, CELL_SIZE + 4);
        }

        // Dessiner le joueur normalement par-dessus
        drawPlayer(player);
    }

}