package fr.univ.bomberman;

import fr.univ.bomberman.controller.MenuController;
import fr.univ.bomberman.model.BotPlayer;
import fr.univ.bomberman.model.Game;
import fr.univ.bomberman.model.Player;
import fr.univ.bomberman.model.PlayerProfileManager;
import fr.univ.bomberman.view.GameRenderer;
import fr.univ.bomberman.view.ScoreboardRenderer;
import fr.univ.bomberman.exceptions.BombermanException;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;


public class BombermanApp extends Application {

    private Stage primaryStage;
    private Game game;
    private GameRenderer renderer;
    private Canvas canvas;
    private Text statusText;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 1_000_000_000; // 1 seconde en nanosecondes
    private AnimationTimer gameTimer;
    private long lastMoveTime = 0;
    private static final long MOVE_DELAY = 150_000_000;
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private ScoreboardRenderer scoreboardRenderer;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        
        // Initialiser le gestionnaire de profils
        PlayerProfileManager.initialize();

        // Démarrer avec le menu principal
        showMenu();
    }

    /**
     * Affiche le menu principal FXML
     */
    public void showMenu() {
        try {
            // Arrêter le timer du jeu s'il est actif
            if (gameTimer != null) {
                gameTimer.stop();
            }

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/main/menu.fxml"));
            Parent root = loader.load();

            // Injecter cette instance dans le contrôleur du menu
            MenuController menuController = loader.getController();
            if (menuController != null) {
                menuController.setBombermanApp(this);
            }

            Scene scene = new Scene(root);

            // ✅ AJOUTER LE CSS ICI
            try {
                // Essayer de charger le CSS du menu principal
                scene.getStylesheets().add(getClass().getResource("/fr/univ/bomberman/css/default/theme.css").toExternalForm());
                System.out.println("CSS du menu principal chargé avec succès");
            } catch (Exception cssEx) {
                System.out.println("CSS du menu principal non trouvé : " + cssEx.getMessage());
                // Essayer un CSS alternatif
                try {
                    scene.getStylesheets().add(getClass().getResource("/fr/univ/bomberman/css/pokemon/default_theme.css").toExternalForm());
                    System.out.println("CSS alternatif chargé");
                } catch (Exception altCssEx) {
                    System.out.println("Aucun CSS trouvé, utilisation du style par défaut");
                }
            }

            primaryStage.setTitle("Super Bomberman - Menu");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger le menu principal");
        }
    }

    /**
     * Lance le jeu avec Canvas (version simultanée 2 joueurs)
     */
    public void startCanvasGame() {
        try {
            // Initialiser le jeu
            game = new Game();

            // Créer le canvas
            int canvasWidth = game.getBoard().getCols() * 40;
            int canvasHeight = game.getBoard().getRows() * 40;
            canvas = new Canvas(canvasWidth, canvasHeight);

            // Créer le renderer
            renderer = new GameRenderer(canvas);

            game.resetAllCooldowns();

            // Créer le texte de statut
            statusText = new Text("JEU SIMULTANÉ! Joueur 1: ZQSD + ESPACE | Joueur 2: Flèches + ENTRÉE | T: Thème, ESC: Menu");

            // Layout avec tableau des scores
            VBox scoreboardContainer = new VBox();
            scoreboardContainer.setPrefWidth(200);
            scoreboardRenderer = new ScoreboardRenderer(scoreboardContainer);
            
            VBox gameContainer = new VBox(10);
            gameContainer.getChildren().addAll(canvas, statusText);
            
            HBox root = new HBox(10);
            root.setPadding(new Insets(10));
            root.getChildren().addAll(gameContainer, scoreboardContainer);
            HBox.setHgrow(gameContainer, Priority.ALWAYS);
            root.setAlignment(Pos.CENTER);

            // Scène
            Scene scene = new Scene(root, canvasWidth + 220, canvasHeight + 40);

            // NOUVEAU: Gestion des touches pressées et relâchées
            scene.setOnKeyPressed(event -> {
                pressedKeys.add(event.getCode());
                handleKeyPress(event.getCode());
            });

            scene.setOnKeyReleased(event -> {
                pressedKeys.remove(event.getCode());
            });

            // Configuration de la fenêtre
            primaryStage.setTitle("Super Bomberman - Jeu Simultané");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            // Demander le focus pour les événements clavier
            canvas.requestFocus();

            // Timer pour les mises à jour automatiques
            gameTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Traiter les touches pressées en continu
                    processContinuousInput();

                    // Mettre à jour le jeu toutes les secondes
                    if (now - lastUpdateTime > UPDATE_INTERVAL) {
                        try {
                            game.update();
                            lastUpdateTime = now;
                        } catch (BombermanException e) {
                            showError("Erreur lors de la mise à jour", e.getMessage());
                        }
                    }

                    // Redessiner à chaque frame
                    renderer.render(game);
                    updateStatusText();
                    
                    // Mettre à jour le tableau des scores
                    if (scoreboardRenderer != null) {
                        scoreboardRenderer.updateScoreboard();
                        scoreboardRenderer.highlightCurrentProfile();
                    }
                }
            };
            gameTimer.start();

            // Rendu initial
            renderer.render(game);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu Canvas");
        }
    }

    /**
     * MODIFIÉE: Version avec limitation de vitesse pour un contrôle plus précis
     */
    private void processContinuousInput() {
        if (game.isGameOver()) return;

        long currentTime = System.nanoTime();
        if (currentTime - lastMoveTime < MOVE_DELAY) {
            return; // Pas assez de temps écoulé depuis le dernier mouvement
        }

        boolean hasMoved = false;

        try {
            // Mouvements du Joueur 1 (ZQSD)
            if (pressedKeys.contains(KeyCode.Z)) {
                game.movePlayer(0, 0, -1); // Joueur 1 vers le haut
                hasMoved = true;
            } else if (pressedKeys.contains(KeyCode.S)) {
                game.movePlayer(0, 0, 1);  // Joueur 1 vers le bas
                hasMoved = true;
            } else if (pressedKeys.contains(KeyCode.Q)) {
                game.movePlayer(0, -1, 0); // Joueur 1 vers la gauche
                hasMoved = true;
            } else if (pressedKeys.contains(KeyCode.D)) {
                game.movePlayer(0, 1, 0);  // Joueur 1 vers la droite
                hasMoved = true;
            }

            // Mouvements du Joueur 2 (Flèches) - seulement si le joueur 1 n'a pas bougé
            if (!hasMoved) {
                if (pressedKeys.contains(KeyCode.UP)) {
                    game.movePlayer(1, 0, -1); // Joueur 2 vers le haut
                    hasMoved = true;
                } else if (pressedKeys.contains(KeyCode.DOWN)) {
                    game.movePlayer(1, 0, 1);  // Joueur 2 vers le bas
                    hasMoved = true;
                } else if (pressedKeys.contains(KeyCode.LEFT)) {
                    game.movePlayer(1, -1, 0); // Joueur 2 vers la gauche
                    hasMoved = true;
                } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                    game.movePlayer(1, 1, 0);  // Joueur 2 vers la droite
                    hasMoved = true;
                }
            }

            // Mettre à jour le temps du dernier mouvement si un mouvement a eu lieu
            if (hasMoved) {
                lastMoveTime = currentTime;
            }

        } catch (BombermanException e) {
            // Ignorer les erreurs de mouvement (collision, etc.)
        }
    }


    private void changeTheme() {
        if (renderer == null) return;

        String currentTheme = renderer.getCurrentTheme();
        String newTheme;

        // Cycle entre les thèmes disponibles
        switch (currentTheme) {
            case "default":
                newTheme = "pokemon";
                break;
            case "pokemon":
                newTheme = "default";
                break;
            default:
                newTheme = "default";
        }

        System.out.println("Changement de thème: " + currentTheme + " -> " + newTheme);
        renderer.changeTheme(newTheme);

        // Redessiner immédiatement
        renderer.render(game);
    }

    /**
     * Lance le jeu avec FXML (votre version alternative)
     */
    public void startFXMLGame() {
        try {
            // Arrêter le timer du jeu s'il est actif
            if (gameTimer != null) {
                gameTimer.stop();
            }

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fr/univ/bomberman/fxml/game/view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // Chargement optionnel du CSS si disponible
            try {
                scene.getStylesheets().add(getClass().getResource("/fr/univ/bomberman/css/pokemon/default_theme.css").toExternalForm());
            } catch (Exception cssEx) {
                System.out.println("CSS non trouvé, utilisation du style par défaut");
            }

            primaryStage.setTitle("Super Bomberman - Jeu FXML");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu FXML");
        }
    }

    /**
     * MODIFIÉ: Met à jour le texte de statut pour le jeu simultané
     */
    private void updateStatusText() {
        if (statusText == null) return;

        if (game.isGameOver()) {
            Player winner = game.getWinner();
            if (winner != null) {
                statusText.setText("🏆 " + winner.getName() + " A GAGNÉ ! Appuyez sur R pour rejouer, ESC pour le menu.");
            } else {
                statusText.setText("💥 ÉGALITÉ ! Tous éliminés ! Appuyez sur R pour rejouer, ESC pour le menu.");
            }
        } else {
            int activeBombs = game.getActiveBombs().size();
            int activeExplosions = game.getActiveExplosions().size();

            // Afficher l'état des joueurs (vivant/éliminé)
            java.util.List<Player> players = game.getPlayers();
            String player1Status = "";
            String player2Status = "";

            if (players.size() > 0) {
                Player p1 = players.get(0);
                player1Status = p1.getName() + (p1.isEliminated() ? " ☠️" : " ❤️");
            }

            if (players.size() > 1) {
                Player p2 = players.get(1);
                player2Status = p2.getName() + (p2.isEliminated() ? " ☠️" : " ❤️");
            }

            statusText.setText("🎮 " + player1Status + " vs " + player2Status +
                    " | Bombes: " + activeBombs +
                    " | Explosions: " + activeExplosions +
                    " | Joueur 1: ZQSD+ESPACE | Joueur 2: ↑↓←→+ENTRÉE | T: Thème");
        }
    }
    // Ajoutez ces méthodes à votre BombermanApp.java :

    /**
     * NOUVELLE MÉTHODE: Lance le jeu Canvas avec des noms personnalisés
     */
    public void startCanvasGameWithNames(String player1Name, String player2Name) {
        try {
            // Initialiser le jeu avec des noms personnalisés
            game = new Game();

            // Modifier les noms des joueurs
            java.util.List<Player> players = game.getPlayers();
            if (players.size() >= 2) {
                players.get(0).setName(player1Name);
                players.get(1).setName(player2Name);
            }

            // Créer le canvas
            int canvasWidth = game.getBoard().getCols() * 40;
            int canvasHeight = game.getBoard().getRows() * 40;
            canvas = new Canvas(canvasWidth, canvasHeight);

            // Créer le renderer
            renderer = new GameRenderer(canvas);

            // Créer le texte de statut avec les noms personnalisés
            statusText = new Text("🎮 " + player1Name + " (ZQSD + ESPACE) VS " + player2Name + " (↑↓←→ + ENTRÉE) | T: Thème, ESC: Menu");

            // Layout avec tableau des scores
            VBox scoreboardContainer = new VBox();
            scoreboardContainer.setPrefWidth(200);
            scoreboardRenderer = new ScoreboardRenderer(scoreboardContainer);
            
            VBox gameContainer = new VBox(10);
            gameContainer.getChildren().addAll(canvas, statusText);
            
            HBox root = new HBox(10);
            root.setPadding(new Insets(10));
            root.getChildren().addAll(gameContainer, scoreboardContainer);
            HBox.setHgrow(gameContainer, Priority.ALWAYS);
            root.setAlignment(Pos.CENTER);

            // Scène
            Scene scene = new Scene(root, canvasWidth + 220, canvasHeight + 40);

            // Gestion des touches pressées et relâchées
            scene.setOnKeyPressed(event -> {
                pressedKeys.add(event.getCode());
                handleKeyPress(event.getCode());
            });

            scene.setOnKeyReleased(event -> {
                pressedKeys.remove(event.getCode());
            });

            // Configuration de la fenêtre
            primaryStage.setTitle("Super Bomberman - " + player1Name + " vs " + player2Name);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            // Demander le focus pour les événements clavier
            canvas.requestFocus();

            // Timer pour les mises à jour automatiques
            gameTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Traiter les touches pressées en continu
                    processContinuousInput();

                    // Mettre à jour le jeu toutes les secondes
                    if (now - lastUpdateTime > UPDATE_INTERVAL) {
                        try {
                            game.update();
                            lastUpdateTime = now;
                        } catch (BombermanException e) {
                            showError("Erreur lors de la mise à jour", e.getMessage());
                        }
                    }

                    // Redessiner à chaque frame
                    renderer.render(game);
                    updateStatusTextWithNames(player1Name, player2Name);
                    
                    // Mettre à jour le tableau des scores
                    if (scoreboardRenderer != null) {
                        scoreboardRenderer.updateScoreboard();
                        scoreboardRenderer.highlightCurrentProfile();
                    }
                }
            };
            gameTimer.start();

            // Rendu initial
            renderer.render(game);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu Canvas");
        }
    }

    /**
     * NOUVELLE MÉTHODE: Met à jour le texte de statut avec les noms personnalisés
     */
    private void updateStatusTextWithNames(String player1Name, String player2Name) {
        if (statusText == null) return;

        if (game.isGameOver()) {
            Player winner = game.getWinner();
            if (winner != null) {
                statusText.setText("🏆 " + winner.getName() + " A GAGNÉ ! Appuyez sur R pour rejouer, ESC pour le menu.");
            } else {
                statusText.setText("💥 ÉGALITÉ ! Tous éliminés ! Appuyez sur R pour rejouer, ESC pour le menu.");
            }
        } else {
            int activeBombs = game.getActiveBombs().size();
            int activeExplosions = game.getActiveExplosions().size();

            // Afficher les statuts des joueurs avec cooldowns
            java.util.List<Player> players = game.getPlayers();
            StringBuilder statusBuilder = new StringBuilder();

            if (players.size() > 0) {
                Player p1 = players.get(0);
                statusBuilder.append("🔵 ").append(p1.getName());
                if (p1.isOnBombCooldown()) {
                    long remaining = p1.getRemainingCooldown();
                    statusBuilder.append(" ⏱️").append(String.format("%.1f", remaining / 1000.0)).append("s");
                } else {
                    statusBuilder.append(" ✅");
                }
            }

            if (players.size() > 1) {
                Player p2 = players.get(1);
                statusBuilder.append(" VS 🟢 ").append(p2.getName());
                if (p2.isOnBombCooldown()) {
                    long remaining = p2.getRemainingCooldown();
                    statusBuilder.append(" ⏱️").append(String.format("%.1f", remaining / 1000.0)).append("s");
                } else {
                    statusBuilder.append(" ✅");
                }
            }

            statusText.setText(statusBuilder.toString() +
                    " | 💣:" + activeBombs +
                    " | 💥:" + activeExplosions +
                    "\n💨 EXPLOSIONS: 1.5s puis disparition ! VOS BOMBES VOUS TUENT !" +
                    "\nJ1: ZQSD+ESPACE | J2: ↑↓←→+ENTRÉE | T: Thème | R: Restart");
        }
    }

    /**
     * MÉTHODE MODIFIÉE: Gestion des touches avec support des noms personnalisés
     */
    /**
     * MÉTHODE COMPLÈTE ET CORRIGÉE: Gestion des touches avec support des noms personnalisés
     */
    private void handleKeyPress(KeyCode keyCode) {
        if (game.isGameOver()) {
            if (keyCode == KeyCode.R) {
                // Redémarrer avec les mêmes noms
                java.util.List<Player> currentPlayers = game.getPlayers();
                String name1 = currentPlayers.size() > 0 ? currentPlayers.get(0).getName() : "Joueur 1";
                String name2 = currentPlayers.size() > 1 ? currentPlayers.get(1).getName() : "Joueur 2";

                startCanvasGameWithNames(name1, name2);
                return;
            } else if (keyCode == KeyCode.ESCAPE) {
                showMenu();
                return;
            }
            return;
        }

        switch (keyCode) {
            case SPACE:
                try {
                    game.placeBombForPlayer(0);
                    System.out.println("💣 " + game.getPlayers().get(0).getName() + " pose une bombe !");
                } catch (BombermanException e) {
                    if (e.getMessage().contains("attendre")) {
                        System.out.println("⏱️ " + e.getMessage());
                        showCooldownFeedback(0);
                    } else {
                        System.out.println("Action: " + e.getMessage());
                    }
                }
                break;

            case ENTER:
                try {
                    game.placeBombForPlayer(1);
                    System.out.println("💣 " + game.getPlayers().get(1).getName() + " pose une bombe !");
                } catch (BombermanException e) {
                    if (e.getMessage().contains("attendre")) {
                        System.out.println("⏱️ " + e.getMessage());
                        showCooldownFeedback(1);
                    } else {
                        System.out.println("Action: " + e.getMessage());
                    }
                }
                break;

            case T:
                changeTheme();
                break;

            case R:
                // Redémarrer avec les mêmes noms
                java.util.List<Player> currentPlayers = game.getPlayers();
                String name1 = currentPlayers.size() > 0 ? currentPlayers.get(0).getName() : "Joueur 1";
                String name2 = currentPlayers.size() > 1 ? currentPlayers.get(1).getName() : "Joueur 2";

                startCanvasGameWithNames(name1, name2);
                break;

            case ESCAPE:
                showMenu();
                return;

            case E:
                game.endGame();
                break;

            default:
                // Aucune action pour les autres touches
                break;
        }

        renderer.render(game);
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showCooldownFeedback(int playerIndex) {
        if (game == null || playerIndex >= game.getPlayers().size()) return;

        Player player = game.getPlayers().get(playerIndex);
        long remaining = player.getRemainingCooldown();

        // Changer temporairement le titre de la fenêtre pour montrer le cooldown
        String originalTitle = primaryStage.getTitle();
        primaryStage.setTitle("⏱️ " + player.getName() + " - Cooldown: " +
                String.format("%.1f", remaining / 1000.0) + "s");

        // Remettre le titre original après 500ms
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(500),
                        e -> primaryStage.setTitle(originalTitle)
                )
        );
        timeline.play();
    }

    public void startFourPlayerGame(String[] playerNames) {
        try {
            // Initialiser le jeu avec 4 joueurs
            game = new Game(playerNames);

            // Créer le canvas
            int canvasWidth = game.getBoard().getCols() * 40;
            int canvasHeight = game.getBoard().getRows() * 40;
            canvas = new Canvas(canvasWidth, canvasHeight);

            // Créer le renderer
            renderer = new GameRenderer(canvas);

            // Créer le texte de statut pour 4 joueurs
            StringBuilder statusBuilder = new StringBuilder();
            statusBuilder.append("4 JOUEURS! ");
            statusBuilder.append("1:").append(playerNames[0]).append("(ZQSD+A) ");
            statusBuilder.append("2:").append(playerNames[1]).append("(↑↓←→+⏎) ");
            if (playerNames.length > 2) statusBuilder.append("3:").append(playerNames[2]).append("(IJKL+U) ");
            if (playerNames.length > 3) statusBuilder.append("4:").append(playerNames[3]).append("(8456+0) ");
            statusBuilder.append("| ESC:Menu");

            statusText = new Text(statusBuilder.toString());

            // Layout avec tableau des scores
            VBox scoreboardContainer = new VBox();
            scoreboardContainer.setPrefWidth(200);
            scoreboardRenderer = new ScoreboardRenderer(scoreboardContainer);
            
            VBox gameContainer = new VBox(10);
            gameContainer.getChildren().addAll(canvas, statusText);
            
            HBox root = new HBox(10);
            root.setPadding(new Insets(10));
            root.getChildren().addAll(gameContainer, scoreboardContainer);
            HBox.setHgrow(gameContainer, Priority.ALWAYS);
            root.setAlignment(Pos.CENTER);

            // Scène
            Scene scene = new Scene(root, canvasWidth + 220, canvasHeight + 40);

            // Gestion des touches pressées et relâchées
            scene.setOnKeyPressed(event -> {
                pressedKeys.add(event.getCode());
                handleKeyPressFourPlayers(event.getCode());
            });

            scene.setOnKeyReleased(event -> {
                pressedKeys.remove(event.getCode());
            });

            // Configuration de la fenêtre
            primaryStage.setTitle("Super Bomberman - 4 Joueurs");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            // Demander le focus pour les événements clavier
            canvas.requestFocus();

            // Timer pour les mises à jour automatiques
            gameTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Traiter les touches pressées en continu
                    processContinuousInputFourPlayers();

                    // Mettre à jour le jeu toutes les secondes
                    if (now - lastUpdateTime > UPDATE_INTERVAL) {
                        try {
                            game.update();
                            lastUpdateTime = now;
                        } catch (BombermanException e) {
                            showError("Erreur lors de la mise à jour", e.getMessage());
                        }
                    }

                    // Redessiner à chaque frame
                    renderer.render(game);
                    updateStatusTextFourPlayers();
                    
                    // Mettre à jour le tableau des scores
                    if (scoreboardRenderer != null) {
                        scoreboardRenderer.updateScoreboard();
                        scoreboardRenderer.highlightCurrentProfile();
                    }
                }
            };
            gameTimer.start();

            // Rendu initial
            renderer.render(game);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu à 4 joueurs");
        }
    }


    private void handleKeyPressFourPlayers(KeyCode keyCode) {
        if (game.isGameOver()) {
            if (keyCode == KeyCode.R) {
                restartFourPlayerGame();
                return;
            } else if (keyCode == KeyCode.ESCAPE) {
                showMenu();
                return;
            }
            return;
        }

        try {
            switch (keyCode) {
                // JOUEUR 1 - ZQSD (mouvement) + A (bombe)
                case Z:
                    game.movePlayer(0, 0, -1); // Haut
                    break;
                case S:
                    game.movePlayer(0, 0, 1);  // Bas
                    break;
                case Q:
                    game.movePlayer(0, -1, 0); // Gauche
                    break;
                case D:
                    game.movePlayer(0, 1, 0);  // Droite
                    break;
                case A:
                    game.placeBombForPlayer(0); // Bombe
                    break;

                // JOUEUR 2 - Flèches (mouvement) + ENTRÉE (bombe)
                case UP:
                    game.movePlayer(1, 0, -1); // Haut
                    break;
                case DOWN:
                    game.movePlayer(1, 0, 1);  // Bas
                    break;
                case LEFT:
                    game.movePlayer(1, -1, 0); // Gauche
                    break;
                case RIGHT:
                    game.movePlayer(1, 1, 0);  // Droite
                    break;
                case ENTER:
                    game.placeBombForPlayer(1); // Bombe
                    break;

                // JOUEUR 3 - IJKL (mouvement) + U (bombe)
                case I:
                    if (game.getPlayerCount() > 2) {
                        game.movePlayer(2, 0, -1); // Haut
                    }
                    break;
                case K:
                    if (game.getPlayerCount() > 2) {
                        game.movePlayer(2, 0, 1);  // Bas
                    }
                    break;
                case J:
                    if (game.getPlayerCount() > 2) {
                        game.movePlayer(2, -1, 0); // Gauche
                    }
                    break;
                case L:
                    if (game.getPlayerCount() > 2) {
                        game.movePlayer(2, 1, 0);  // Droite
                    }
                    break;
                case U:
                    if (game.getPlayerCount() > 2) {
                        game.placeBombForPlayer(2); // Bombe
                    }
                    break;

                // JOUEUR 4 - Pavé numérique 8456 (mouvement) + 7 (bombe)
                case DIGIT8:
                case NUMPAD8:
                    if (game.getPlayerCount() > 3) {
                        game.movePlayer(3, 0, -1); // Haut
                    }
                    break;
                case DIGIT5:
                case NUMPAD5:
                    if (game.getPlayerCount() > 3) {
                        game.movePlayer(3, 0, 1);  // Bas
                    }
                    break;
                case DIGIT4:
                case NUMPAD4:
                    if (game.getPlayerCount() > 3) {
                        game.movePlayer(3, -1, 0); // Gauche
                    }
                    break;
                case DIGIT6:
                case NUMPAD6:
                    if (game.getPlayerCount() > 3) {
                        game.movePlayer(3, 1, 0);  // Droite
                    }
                    break;
                case DIGIT7:
                case NUMPAD7:
                    if (game.getPlayerCount() > 3) {
                        game.placeBombForPlayer(3); // Bombe
                    }
                    break;

                // COMMANDES GÉNÉRALES
                case T:
                    changeTheme();
                    break;
                case R:
                    restartFourPlayerGame();
                    break;
                case ESCAPE:
                    showMenu();
                    return;
                case E:
                    game.endGame();
                    break;
            }
        } catch (BombermanException e) {
            // Gestion spécifique pour les cooldowns
            if (e.getMessage().contains("attendre")) {
                System.out.println("⏱️ " + e.getMessage());
            } else {
                System.out.println("Action: " + e.getMessage());
            }
        }

        renderer.render(game);
    }

    private void processContinuousInputFourPlayers() {
        if (game.isGameOver()) return;

        try {
            // Joueur 1 (ZQSD)
            if (pressedKeys.contains(KeyCode.Z)) {
                game.movePlayer(0, 0, -1);
            } else if (pressedKeys.contains(KeyCode.S)) {
                game.movePlayer(0, 0, 1);
            } else if (pressedKeys.contains(KeyCode.Q)) {
                game.movePlayer(0, -1, 0);
            } else if (pressedKeys.contains(KeyCode.D)) {
                game.movePlayer(0, 1, 0);
            }

            // Joueur 2 (Flèches)
            if (pressedKeys.contains(KeyCode.UP)) {
                game.movePlayer(1, 0, -1);
            } else if (pressedKeys.contains(KeyCode.DOWN)) {
                game.movePlayer(1, 0, 1);
            } else if (pressedKeys.contains(KeyCode.LEFT)) {
                game.movePlayer(1, -1, 0);
            } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                game.movePlayer(1, 1, 0);
            }

            // Joueur 3 (IJKL) - si présent
            if (game.getPlayerCount() > 2) {
                if (pressedKeys.contains(KeyCode.I)) {
                    game.movePlayer(2, 0, -1);
                } else if (pressedKeys.contains(KeyCode.K)) {
                    game.movePlayer(2, 0, 1);
                } else if (pressedKeys.contains(KeyCode.J)) {
                    game.movePlayer(2, -1, 0);
                } else if (pressedKeys.contains(KeyCode.L)) {
                    game.movePlayer(2, 1, 0);
                }
            }

            // Joueur 4 (Pavé numérique 8456) - si présent
            if (game.getPlayerCount() > 3) {
                if (pressedKeys.contains(KeyCode.DIGIT8) || pressedKeys.contains(KeyCode.NUMPAD8)) {
                    game.movePlayer(3, 0, -1);
                } else if (pressedKeys.contains(KeyCode.DIGIT5) || pressedKeys.contains(KeyCode.NUMPAD5)) {
                    game.movePlayer(3, 0, 1);
                } else if (pressedKeys.contains(KeyCode.DIGIT4) || pressedKeys.contains(KeyCode.NUMPAD4)) {
                    game.movePlayer(3, -1, 0);
                } else if (pressedKeys.contains(KeyCode.DIGIT6) || pressedKeys.contains(KeyCode.NUMPAD6)) {
                    game.movePlayer(3, 1, 0);
                }
            }

        } catch (BombermanException e) {
            // Ignorer les erreurs de mouvement
        }
    }

    private void updateStatusTextFourPlayers() {
        if (statusText == null) return;

        if (game.isGameOver()) {
            Player winner = game.getWinner();
            if (winner != null) {
                statusText.setText("👑 " + winner.getName() + " REMPORTE LA BATAILLE ROYALE ! 👑 | R: Nouvelle bataille | ESC: Menu");
            } else {
                statusText.setText("💥 TOUS ÉLIMINÉS - AUCUN SURVIVANT ! 💥 | R: Nouvelle bataille | ESC: Menu");
            }
        } else {
            int activeBombs = game.getActiveBombs().size();
            int aliveCount = game.getAlivePlayerCount();

            // Construire le statut avec cooldowns
            StringBuilder playersStatus = new StringBuilder();
            java.util.List<Player> players = game.getPlayers();
            String[] emojis = {"🔴", "🔵", "🟡", "🟢"};

            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                if (i > 0) playersStatus.append(" ");

                playersStatus.append(emojis[i]).append(p.getName());

                if (p.isEliminated()) {
                    playersStatus.append("💀");
                } else if (p.isOnBombCooldown()) {
                    long remaining = p.getRemainingCooldown();
                    playersStatus.append("⏱️").append(String.format("%.1f", remaining / 1000.0)).append("s");
                } else {
                    playersStatus.append("✅");
                }
            }

            statusText.setText("⚔️ BATAILLE EXPRESS: " + playersStatus.toString() +
                    " | 💣:" + activeBombs +
                    " | 👥:" + aliveCount + " survivants" +
                    "\n💨 Explosions: 1.5s total ! Cooldown: 10s" +
                    "\nR:Restart T:Thème");
        }
    }

    public void startBotGame(String playerName, int botDifficulty) {
        try {
            // Initialiser le jeu contre un bot
            game = new Game(playerName, botDifficulty);

            // Créer le canvas
            int canvasWidth = game.getBoard().getCols() * 40;
            int canvasHeight = game.getBoard().getRows() * 40;
            canvas = new Canvas(canvasWidth, canvasHeight);

            // Créer le renderer
            renderer = new GameRenderer(canvas);

            // Récupérer les noms
            Player human = game.getHumanPlayer();
            BotPlayer bot = game.getBot();
            String humanName = human != null ? human.getName() : playerName;
            String botName = bot != null ? bot.getName() : "Bot";

            // Créer le texte de statut
            statusText = new Text("🤖 " + humanName + " VS " + botName + " | ZQSD + ESPACE pour jouer | ESC: Menu");

            // Layout avec tableau des scores
            VBox scoreboardContainer = new VBox();
            scoreboardContainer.setPrefWidth(200);
            scoreboardRenderer = new ScoreboardRenderer(scoreboardContainer);
            
            VBox gameContainer = new VBox(10);
            gameContainer.getChildren().addAll(canvas, statusText);
            
            HBox root = new HBox(10);
            root.setPadding(new Insets(10));
            root.getChildren().addAll(gameContainer, scoreboardContainer);
            HBox.setHgrow(gameContainer, Priority.ALWAYS);
            root.setAlignment(Pos.CENTER);

            // Scène
            Scene scene = new Scene(root, canvasWidth + 220, canvasHeight + 40);

            // Gestion des touches (seulement pour le joueur humain)
            scene.setOnKeyPressed(event -> {
                pressedKeys.add(event.getCode());
                handleKeyPressBotMode(event.getCode());
            });

            scene.setOnKeyReleased(event -> {
                pressedKeys.remove(event.getCode());
            });

            // Configuration de la fenêtre
            primaryStage.setTitle("Super Bomberman - " + humanName + " vs " + botName);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            // Demander le focus pour les événements clavier
            canvas.requestFocus();

            // Timer pour les mises à jour automatiques
            gameTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Traiter les touches pressées en continu
                    processContinuousInputBotMode();

                    // Mettre à jour le jeu toutes les secondes
                    if (now - lastUpdateTime > UPDATE_INTERVAL) {
                        try {
                            game.update();
                            lastUpdateTime = now;
                        } catch (BombermanException e) {
                            showError("Erreur lors de la mise à jour", e.getMessage());
                        }
                    }

                    // Redessiner à chaque frame
                    renderer.render(game);
                    updateStatusTextBotMode();
                    
                    // Mettre à jour le tableau des scores
                    if (scoreboardRenderer != null) {
                        scoreboardRenderer.updateScoreboard();
                        scoreboardRenderer.highlightCurrentProfile();
                    }
                }
            };
            gameTimer.start();

            // Rendu initial
            renderer.render(game);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu contre le bot");
        }
    }

    private void handleKeyPressBotMode(KeyCode keyCode) {
        if (game.isGameOver()) {
            if (keyCode == KeyCode.R) {
                // Redémarrer avec le même bot
                Player human = game.getHumanPlayer();
                BotPlayer bot = game.getBot();
                if (human != null && bot != null) {
                    startBotGame(human.getName(), bot.getDifficulty());
                }
                return;
            } else if (keyCode == KeyCode.ESCAPE) {
                showMenu();
                return;
            }
            return;
        }

        switch (keyCode) {
            case SPACE:
                try {
                    game.placeBombForPlayer(0); // Le joueur humain est toujours l'index 0
                    Player human = game.getHumanPlayer();
                    if (human != null) {
                        System.out.println("💣 " + human.getName() + " pose une bombe !");
                    }
                } catch (BombermanException e) {
                    if (e.getMessage().contains("attendre")) {
                        System.out.println("⏱️ " + e.getMessage());
                        showCooldownFeedback(0);
                    }
                }
                break;

            case T:
                changeTheme();
                break;

            case R:
                // Redémarrer
                Player human = game.getHumanPlayer();
                BotPlayer bot = game.getBot();
                if (human != null && bot != null) {
                    startBotGame(human.getName(), bot.getDifficulty());
                }
                break;

            case ESCAPE:
                showMenu();
                return;

            case E:
                game.endGame();
                break;
        }

        renderer.render(game);
    }

    private void processContinuousInputBotMode() {
        if (game.isGameOver()) return;

        long currentTime = System.nanoTime();
        if (currentTime - lastMoveTime < MOVE_DELAY) {
            return;
        }

        boolean hasMoved = false;

        try {
            // Mouvements du joueur humain seulement (ZQSD)
            if (pressedKeys.contains(KeyCode.Z)) {
                game.movePlayer(0, 0, -1); // Joueur humain vers le haut
                hasMoved = true;
            } else if (pressedKeys.contains(KeyCode.S)) {
                game.movePlayer(0, 0, 1);  // Joueur humain vers le bas
                hasMoved = true;
            } else if (pressedKeys.contains(KeyCode.Q)) {
                game.movePlayer(0, -1, 0); // Joueur humain vers la gauche
                hasMoved = true;
            } else if (pressedKeys.contains(KeyCode.D)) {
                game.movePlayer(0, 1, 0);  // Joueur humain vers la droite
                hasMoved = true;
            }

            if (hasMoved) {
                lastMoveTime = currentTime;
            }

        } catch (BombermanException e) {
            // Ignorer les erreurs de mouvement
        }
    }

    private void updateStatusTextBotMode() {
        if (statusText == null) return;

        if (game.isGameOver()) {
            Player winner = game.getWinner();
            if (winner != null) {
                if (winner instanceof BotPlayer) {
                    statusText.setText("🤖 " + winner.getName() + " A GAGNÉ ! L'IA vous a battu ! R: Revanche | ESC: Menu");
                } else {
                    statusText.setText("🏆 " + winner.getName() + " A GAGNÉ ! Vous avez battu l'IA ! R: Rejouer | ESC: Menu");
                }
            } else {
                statusText.setText("💥 ÉGALITÉ ! R: Rejouer | ESC: Menu");
            }
        } else {
            int activeBombs = game.getActiveBombs().size();
            int activeExplosions = game.getActiveExplosions().size();

            Player human = game.getHumanPlayer();
            BotPlayer bot = game.getBot();

            StringBuilder statusBuilder = new StringBuilder();

            if (human != null) {
                statusBuilder.append("👤 ").append(human.getName());
                if (human.isOnBombCooldown()) {
                    long remaining = human.getRemainingCooldown();
                    statusBuilder.append(" ⏱️").append(String.format("%.1f", remaining / 1000.0)).append("s");
                } else {
                    statusBuilder.append(" ✅");
                }
            }

            if (bot != null) {
                statusBuilder.append(" VS 🤖 ").append(bot.getName());
                if (bot.isOnBombCooldown()) {
                    long remaining = bot.getRemainingCooldown();
                    statusBuilder.append(" ⏱️").append(String.format("%.1f", remaining / 1000.0)).append("s");
                } else {
                    statusBuilder.append(" ✅");
                }
            }

            statusText.setText(statusBuilder.toString() +
                    " | 💣:" + activeBombs +
                    " | 💥:" + activeExplosions +
                    "\n🤖 IA " + (bot != null ? getBotDifficultyText(bot.getDifficulty()) : "") +
                    " | Vous: ZQSD+ESPACE | T: Thème | R: Restart");
        }
    }

    private String getBotDifficultyText(int difficulty) {
        switch (difficulty) {
            case 1: return "Facile 😊";
            case 2: return "Moyen 😐";
            case 3: return "Difficile 😈";
            default: return "Moyen 😐";
        }
    }

    private void restartFourPlayerGame() {
        if (game == null) return;

        // Récupérer les noms actuels
        java.util.List<Player> currentPlayers = game.getPlayers();
        String[] names = new String[4];
        for (int i = 0; i < Math.min(currentPlayers.size(), 4); i++) {
            names[i] = currentPlayers.get(i).getName();
        }

        // Relancer avec les mêmes noms
        startFourPlayerGame(names);
    }
    


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

}