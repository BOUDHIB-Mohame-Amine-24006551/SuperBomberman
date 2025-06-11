package fr.univ.bomberman;

import fr.univ.bomberman.controller.GameModeController;
import fr.univ.bomberman.controller.MenuController;
import fr.univ.bomberman.controller.ProfileStatsController;
import fr.univ.bomberman.model.*;
import fr.univ.bomberman.view.GameRenderer;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.utils.ProfileManager;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Optional;
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
    private ProfileManager profileManager = ProfileManager.getInstance();


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

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

            // Layout
            VBox root = new VBox(10);
            root.getChildren().addAll(canvas, statusText);

            // Scène
            Scene scene = new Scene(root, canvasWidth, canvasHeight + 120);

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

    public void startCanvasGameWithNames(String player1Name, String player2Name) {
        gameStartTime = System.currentTimeMillis(); // Enregistrer le début

        try {
            PlayerProfile currentProfile = GameModeController.getCurrentGameProfile();
            if (currentProfile != null && !currentProfile.getPlayerName().equals(player1Name)) {
                System.out.println("⚠️ Profil actuel: " + currentProfile.getPlayerName() + ", Joueur 1: " + player1Name);
            }

            // Si aucun profil n'est sélectionné mais on a un nom, essayer de charger/créer le profil
            if (currentProfile == null && player1Name != null && !player1Name.trim().isEmpty()) {
                try {
                    ProfileManager profileManager = ProfileManager.getInstance();
                    PlayerProfile profile = profileManager.loadProfile(player1Name);
                    GameModeController.setCurrentGameProfile(profile);
                    System.out.println("🎮 Profil automatiquement chargé pour: " + player1Name);
                } catch (Exception e) {
                    System.out.println("⚠️ Impossible de charger le profil pour " + player1Name);
                }
            }

            PlayerProfile profile1 = null;

            // Charger le profil si disponible
            if (currentProfile != null && currentProfile.getPlayerName().equals(player1Name)) {
                profile1 = currentProfile;
            } else {
                try {
                    ProfileManager profileManager = ProfileManager.getInstance(); // Add this line
                    profile1 = profileManager.loadProfile(player1Name);
                } catch (BombermanException e) {
                    // Profil non trouvé, continuer sans profil
                    System.out.println("Profil non trouvé pour " + player1Name);
                }
            }

            String preferredTheme = "default";
            if (profile1 != null) {
                preferredTheme = profile1.getPreferredTheme();
            }

            // Initialiser le jeu
            game = new Game();

            // Créer le canvas
            int canvasWidth = game.getBoard().getCols() * 40;
            int canvasHeight = game.getBoard().getRows() * 40;
            canvas = new Canvas(canvasWidth, canvasHeight);

            // Créer le renderer
            renderer = new GameRenderer(canvas);

            // Appliquer le thème préféré
            if (!"default".equals(preferredTheme)) {
                renderer.changeTheme(preferredTheme);
            }

            // Modifier les noms des joueurs
            java.util.List<Player> players = game.getPlayers();
            if (players.size() >= 2) {
                players.get(0).setName(player1Name);
                players.get(1).setName(player2Name);
            }

            // Créer le texte de statut avec les noms personnalisés
            statusText = new Text("🎮 " + player1Name + " (ZQSD + ESPACE) VS " + player2Name + " (↑↓←→ + ENTRÉE) | T: Thème, ESC: Menu");

            // Layout
            VBox root = new VBox(10);
            root.getChildren().addAll(canvas, statusText);

            // Scène
            Scene scene = new Scene(root, canvasWidth, canvasHeight + 120);

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
                    processContinuousInput();

                    if (now - lastUpdateTime > UPDATE_INTERVAL) {
                        try {
                            game.update();
                            lastUpdateTime = now;
                        } catch (BombermanException e) {
                            showError("Erreur lors de la mise à jour", e.getMessage());
                        }
                    }

                    renderer.render(game);
                    updateStatusTextWithNames(player1Name, player2Name);
                }
            };
            gameTimer.start();

            // Rendu initial
            renderer.render(game);

            // Message de bienvenue
            System.out.println("🎮 NOUVELLE PARTIE COMMENCÉE !");
            System.out.println("🔵 " + player1Name + " (ZQSD + ESPACE)");
            System.out.println("🟢 " + player2Name + " (↑↓←→ + ENTRÉE)");
            System.out.println("🔥 Que le meilleur gagne !");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu Canvas: " + e.getMessage());
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
     * MÉTHODE COMPLÈTE ET CORRIGÉE: Gestion des touches avec support des noms personnalisés
     */
    private void handleKeyPress(KeyCode keyCode) {
        if (game.isGameOver()) {
            if (keyCode == KeyCode.R) {
                // ✅ NOUVEAU: Enregistrer la partie avant de redémarrer
                recordGameSessionWithDuration();

                // Redémarrer avec les mêmes noms
                java.util.List<Player> currentPlayers = game.getPlayers();
                String name1 = currentPlayers.size() > 0 ? currentPlayers.get(0).getName() : "Joueur 1";
                String name2 = currentPlayers.size() > 1 ? currentPlayers.get(1).getName() : "Joueur 2";
                startCanvasGameWithNames(name1, name2);
                return;
            } else if (keyCode == KeyCode.ESCAPE) {
                // ✅ NOUVEAU: Enregistrer la partie avant de quitter
                recordGameSessionWithDuration();
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
                // Enregistrer avant de redémarrer
                recordGameSessionWithDuration();

                java.util.List<Player> currentPlayers = game.getPlayers();
                String name1 = currentPlayers.size() > 0 ? currentPlayers.get(0).getName() : "Joueur 1";
                String name2 = currentPlayers.size() > 1 ? currentPlayers.get(1).getName() : "Joueur 2";
                startCanvasGameWithNames(name1, name2);
                break;

            case ESCAPE:
                // Enregistrer avant de quitter
                recordGameSessionWithDuration();
                showMenu();
                return;

            case E:
                game.endGame();
                break;

            default:
                break;
        }

        renderer.render(game);
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue de confirmation
     * @return true si l'utilisateur a cliqué sur OK
     */
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
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
            if (playerNames.length > 0) {
                String firstPlayerName = playerNames[0];

                PlayerProfile currentProfile = GameModeController.getCurrentGameProfile();
                if (currentProfile == null || !currentProfile.getPlayerName().equals(firstPlayerName)) {
                    try {
                        ProfileManager profileManager = ProfileManager.getInstance();
                        PlayerProfile profile = profileManager.loadProfile(firstPlayerName);
                        GameModeController.setCurrentGameProfile(profile);
                        System.out.println("🎮 Profil défini pour la bataille royale: " + firstPlayerName);
                    } catch (Exception e) {
                        System.out.println("⚠️ Impossible de charger le profil pour " + firstPlayerName);
                    }
                }
            }
            // Initialiser le jeu avec 4 joueurs
            game = new Game(playerNames);

            // Créer le canvas
            int canvasWidth = game.getBoard().getCols() * 40;
            int canvasHeight = game.getBoard().getRows() * 40;
            canvas = new Canvas(canvasWidth, canvasHeight);

            // Créer le renderer
            renderer = new GameRenderer(canvas);

            // Texte de statut pour 4 joueurs
            statusText = new Text("🎮 BATAILLE ROYALE 4 JOUEURS - MOUVEMENT DISCRET ! 🎮");

            // Layout
            VBox root = new VBox(10);
            root.getChildren().addAll(canvas, statusText);

            // Scène
            Scene scene = new Scene(root, canvasWidth, canvasHeight + 140);

            // MODIFIÉ: Gestion simple des touches pressées (pas de mouvement continu)
            scene.setOnKeyPressed(event -> {
                handleKeyPressFourPlayers(event.getCode());
            });

            // Configuration de la fenêtre
            primaryStage.setTitle("Super Bomberman - Bataille Royale 4 Joueurs");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            canvas.requestFocus();

            // Timer simplifié pour les mises à jour automatiques seulement
            gameTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Seulement mise à jour du jeu (bombes et explosions)
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
                }
            };
            gameTimer.start();

            renderer.render(game);

            // Messages d'introduction
            System.out.println("🔥 BATAILLE ROYALE COMMENCÉE - MODE DISCRET !");
            System.out.println("🔴 " + playerNames[0] + " (ZQSD + A)");
            System.out.println("🔵 " + playerNames[1] + " (↑↓←→ + ENTRÉE)");
            System.out.println("🟡 " + playerNames[2] + " (IJKL + U)");
            System.out.println("🟢 " + playerNames[3] + " (8456 + 7)");
            System.out.println("⚔️ Une touche = un mouvement ! Dernier survivant gagne !");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu 4 joueurs");
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
            PlayerProfile currentProfile = GameModeController.getCurrentGameProfile();
            if (currentProfile == null || !currentProfile.getPlayerName().equals(playerName)) {
                try {
                    ProfileManager profileManager = ProfileManager.getInstance();
                    PlayerProfile profile = profileManager.loadProfile(playerName);
                    GameModeController.setCurrentGameProfile(profile);
                    System.out.println("🎮 Profil défini pour le jeu bot: " + playerName);
                } catch (Exception e) {
                    System.out.println("⚠️ Impossible de charger le profil pour " + playerName);
                }
            }
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

            // Layout
            VBox root = new VBox(10);
            root.getChildren().addAll(canvas, statusText);

            // Scène
            Scene scene = new Scene(root, canvasWidth, canvasHeight + 120);

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
                    // Traiter les mouvements du joueur humain
                    processContinuousInputBotMode();

                    // Mettre à jour le jeu (inclut les actions du bot)
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
                }
            };
            gameTimer.start();

            // Rendu initial
            renderer.render(game);

            // Message de bienvenue
            System.out.println("🎮 PARTIE CONTRE BOT COMMENCÉE !");
            System.out.println("👤 " + humanName + " (ZQSD + ESPACE)");
            System.out.println("🤖 " + botName + " (IA)");
            System.out.println("🔥 Bonne chance contre l'IA !");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu contre bot");
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

    public void startCTFGame(String[] playerNames) {
        try {
            if (playerNames.length > 0) {
                String firstPlayerName = playerNames[0];

                PlayerProfile currentProfile = GameModeController.getCurrentGameProfile();
                if (currentProfile == null || !currentProfile.getPlayerName().equals(firstPlayerName)) {
                    try {
                        ProfileManager profileManager = ProfileManager.getInstance();
                        PlayerProfile profile = profileManager.loadProfile(firstPlayerName);
                        GameModeController.setCurrentGameProfile(profile);
                        System.out.println("🎮 Profil défini pour CTF: " + firstPlayerName);
                    } catch (Exception e) {
                        System.out.println("⚠️ Impossible de charger le profil pour " + firstPlayerName);
                    }
                }
            }
            // ✅ CORRECTION: Utiliser GameMode du package model
            game = new Game(playerNames, GameMode.CAPTURE_THE_FLAG);

            // Créer le canvas
            int canvasWidth = game.getBoard().getCols() * 40;
            int canvasHeight = game.getBoard().getRows() * 40;
            canvas = new Canvas(canvasWidth, canvasHeight);

            // Créer le renderer
            renderer = new GameRenderer(canvas);

            // Texte de statut pour CTF
            statusText = new Text("🏁 CAPTURE THE FLAG - Phase de placement des drapeaux");

            // Layout
            VBox root = new VBox(10);
            root.getChildren().addAll(canvas, statusText);

            // Scène
            Scene scene = new Scene(root, canvasWidth, canvasHeight + 140);

            // Gestion des touches pour CTF
            scene.setOnKeyPressed(event -> {
                handleKeyPressCTF(event.getCode());
            });

            // Configuration de la fenêtre
            primaryStage.setTitle("Super Bomberman - Capture the Flag");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            canvas.requestFocus();

            // Timer pour les mises à jour CTF
            gameTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Mise à jour du jeu
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
                    updateStatusTextCTF();
                }
            };
            gameTimer.start();

            renderer.render(game);

            // Messages d'introduction
            System.out.println("🏁 MODE CAPTURE THE FLAG COMMENCÉ !");
            for (int i = 0; i < playerNames.length; i++) {
                String[] emojis = {"🔴", "🔵", "🟡", "🟢"};
                System.out.println(emojis[i] + " " + playerNames[i]);
            }
            System.out.println("📍 Placez vos drapeaux pour commencer !");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de lancement", "Impossible de démarrer le jeu CTF");
        }
    }

    private void handleKeyPressCTF(KeyCode keyCode) {
        // Si on est en phase de placement des drapeaux
        if (game.isInFlagSetupPhase()) {
            Player currentPlayerSettingFlag = game.getCurrentPlayerSettingFlag();
            if (currentPlayerSettingFlag == null) return;

            try {
                Position currentPos = currentPlayerSettingFlag.getPosition();
                Position newFlagPos = null;

                // Permettre au joueur de déplacer la position proposée du drapeau
                switch (keyCode) {
                    case Z:
                    case UP:
                        newFlagPos = new Position(currentPos.getX(), currentPos.getY() - 1);
                        break;
                    case S:
                    case DOWN:
                        newFlagPos = new Position(currentPos.getX(), currentPos.getY() + 1);
                        break;
                    case Q:
                    case LEFT:
                        newFlagPos = new Position(currentPos.getX() - 1, currentPos.getY());
                        break;
                    case D:
                    case RIGHT:
                        newFlagPos = new Position(currentPos.getX(), currentPos.getY() + 1);
                        break;
                    case SPACE:
                    case ENTER:
                        // Placer le drapeau à la position actuelle du joueur + décalage
                        Position flagPosition = new Position(
                                currentPos.getX() + 1,
                                currentPos.getY() + 1
                        );

                        try {
                            boolean placed = game.placeFlagAt(flagPosition);
                            if (placed) {
                                System.out.println("🏁 " + currentPlayerSettingFlag.getName() +
                                        " a placé son drapeau en " + flagPosition);

                                // Vérifier si tous les drapeaux sont placés
                                if (!game.isInFlagSetupPhase()) {
                                    System.out.println("🎮 Tous les drapeaux placés ! Le jeu CTF commence !");
                                    updateStatusTextCTF(); // Mettre à jour l'interface
                                }
                            }
                        } catch (BombermanException e) {
                            System.out.println("❌ Impossible de placer le drapeau : " + e.getMessage());
                            // Afficher un message à l'utilisateur
                            updateStatusTextCTF();
                        }
                        break;

                    case ESCAPE:
                        showMenu();
                        return;
                }

                // Si une nouvelle position a été proposée, mettre à jour la position du joueur temporairement
                // (pour visualiser où sera placé le drapeau)
                if (newFlagPos != null && game.getBoard().isWithinBounds(newFlagPos)) {
                    // Note: Ici vous pourriez ajouter une logique pour visualiser où sera placé le drapeau
                    // Par exemple, changer temporairement la position du joueur pour prévisualiser
                }

            } catch (Exception e) {
                System.out.println("Erreur lors du placement du drapeau : " + e.getMessage());
            }

            renderer.render(game);
            return;
        }

        // Si le jeu est terminé
        if (game.isGameOver()) {
            if (keyCode == KeyCode.R) {
                // Redémarrer CTF
                java.util.List<Player> currentPlayers = game.getPlayers();
                String[] names = new String[currentPlayers.size()];
                for (int i = 0; i < currentPlayers.size(); i++) {
                    names[i] = currentPlayers.get(i).getName();
                }
                startCTFGame(names);
                return;
            } else if (keyCode == KeyCode.ESCAPE) {
                showMenu();
                return;
            }
            return;
        }

        // Gestion normale du jeu CTF (après placement des drapeaux)
        try {
            switch (keyCode) {
                // JOUEUR 1 - ZQSD (mouvement) + A (bombe)
                case Z:
                    game.movePlayer(0, 0, -1);
                    break;
                case S:
                    game.movePlayer(0, 0, 1);
                    break;
                case Q:
                    game.movePlayer(0, -1, 0);
                    break;
                case D:
                    game.movePlayer(0, 1, 0);
                    break;
                case A:
                    game.placeBombForPlayer(0);
                    break;

                // JOUEUR 2 - Flèches (mouvement) + ENTRÉE (bombe)
                case UP:
                    game.movePlayer(1, 0, -1);
                    break;
                case DOWN:
                    game.movePlayer(1, 0, 1);
                    break;
                case LEFT:
                    game.movePlayer(1, -1, 0);
                    break;
                case RIGHT:
                    game.movePlayer(1, 1, 0);
                    break;
                case ENTER:
                    game.placeBombForPlayer(1);
                    break;

                // JOUEUR 3 - IJKL (mouvement) + U (bombe)
                case I:
                    if (game.getPlayerCount() > 2) {
                        game.movePlayer(2, 0, -1);
                    }
                    break;
                case K:
                    if (game.getPlayerCount() > 2) {
                        game.movePlayer(2, 0, 1);
                    }
                    break;
                case J:
                    if (game.getPlayerCount() > 2) {
                        game.movePlayer(2, -1, 0);
                    }
                    break;
                case L:
                    if (game.getPlayerCount() > 2) {
                        game.movePlayer(2, 1, 0);
                    }
                    break;
                case U:
                    if (game.getPlayerCount() > 2) {
                        game.placeBombForPlayer(2);
                    }
                    break;

                // JOUEUR 4 - Pavé numérique (mouvement) + 7 (bombe)
                case DIGIT8:
                case NUMPAD8:
                    if (game.getPlayerCount() > 3) {
                        game.movePlayer(3, 0, -1);
                    }
                    break;
                case DIGIT5:
                case NUMPAD5:
                    if (game.getPlayerCount() > 3) {
                        game.movePlayer(3, 0, 1);
                    }
                    break;
                case DIGIT4:
                case NUMPAD4:
                    if (game.getPlayerCount() > 3) {
                        game.movePlayer(3, -1, 0);
                    }
                    break;
                case DIGIT6:
                case NUMPAD6:
                    if (game.getPlayerCount() > 3) {
                        game.movePlayer(3, 1, 0);
                    }
                    break;
                case DIGIT7:
                case NUMPAD7:
                    if (game.getPlayerCount() > 3) {
                        game.placeBombForPlayer(3);
                    }
                    break;

                // COMMANDES GÉNÉRALES
                case T:
                    changeTheme();
                    break;
                case R:
                    // Redémarrer
                    java.util.List<Player> currentPlayers = game.getPlayers();
                    String[] names = new String[currentPlayers.size()];
                    for (int i = 0; i < currentPlayers.size(); i++) {
                        names[i] = currentPlayers.get(i).getName();
                    }
                    startCTFGame(names);
                    break;
                case ESCAPE:
                    showMenu();
                    return;
                case E:
                    game.endGame();
                    break;
            }
        } catch (BombermanException e) {
            // Gestion des erreurs (cooldowns, mouvements invalides, etc.)
            if (e.getMessage().contains("attendre")) {
                System.out.println("⏱️ " + e.getMessage());
            } else {
                System.out.println("Action: " + e.getMessage());
            }
        }

        renderer.render(game);
    }

    private void updateStatusTextCTF() {
        if (statusText == null) return;

        // Phase de placement des drapeaux
        if (game.isInFlagSetupPhase()) {
            Player currentPlayerSettingFlag = game.getCurrentPlayerSettingFlag();
            if (currentPlayerSettingFlag != null) {
                statusText.setText("🏁 PLACEMENT DES DRAPEAUX - " +
                        currentPlayerSettingFlag.getName() +
                        " : Déplacez-vous avec ZQSD/Flèches puis ESPACE/ENTRÉE pour placer votre drapeau");
            } else {
                statusText.setText("🏁 CAPTURE THE FLAG - Phase de placement des drapeaux");
            }
            return;
        }

        // Jeu terminé
        if (game.isGameOver()) {
            Player winner = game.getWinner();
            if (winner != null) {
                statusText.setText("🏆 " + winner.getName() + " REMPORTE LE CTF ! R: Nouvelle partie | ESC: Menu");
            } else {
                statusText.setText("💥 MATCH NUL CTF ! R: Nouvelle partie | ESC: Menu");
            }
            return;
        }

        // Jeu en cours
        int activeBombs = game.getActiveBombs().size();
        int activeFlags = 0;

        // Compter les drapeaux en jeu
        for (Flag flag : game.getFlags()) {
            if (flag.isBeingCarried()) {
                activeFlags++;
            }
        }

        // Afficher le statut des joueurs avec leurs drapeaux capturés
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

            // Afficher le nombre de drapeaux capturés
            if (p.getCapturedFlagsCount() > 0) {
                playersStatus.append("🏁").append(p.getCapturedFlagsCount());
            }
        }

        statusText.setText("🏁 CAPTURE THE FLAG: " + playersStatus.toString() +
                " | 💣:" + activeBombs +
                " | 🏁 portés:" + activeFlags +
                "\n🎯 Objectif: Capturez TOUS les drapeaux adverses pour gagner !" +
                "\n💀 Joueurs éliminés peuvent encore bombarder | R:Restart T:Thème");
    }

    //profil
    private void recordGameSession() {
        if (game == null || game.getPlayers().isEmpty()) return;

        try {
            // Enregistrer pour chaque joueur
            for (Player player : game.getPlayers()) {
                if (!(player instanceof BotPlayer)) { // Seulement les joueurs humains
                    profileManager.recordGameSession(player.getName(), game);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement des sessions: " + e.getMessage());
        }
    }

    private void loadPlayerPreferences(String playerName) {
        try {
            PlayerProfile profile = profileManager.loadProfile(playerName);

            // Appliquer le thème préféré
            if (renderer != null && profile.getPreferredTheme() != null) {
                renderer.changeTheme(profile.getPreferredTheme());
            }

            // Autres préférences peuvent être appliquées ici
            // (son, difficulté bot par défaut, etc.)

        } catch (Exception e) {
            System.err.println("Impossible de charger les préférences: " + e.getMessage());
        }
    }
    private void recordDetailedGameSession(long gameStartTime) {
        if (game == null || game.getPlayers().isEmpty()) return;

        try {
            for (Player player : game.getPlayers()) {
                if (!(player instanceof BotPlayer)) {
                    // Créer une session détaillée
                    GameSession session = new GameSession();
                    session.setStartTime(java.time.LocalDateTime.now().minusSeconds(
                            (System.currentTimeMillis() - gameStartTime) / 1000));
                    session.setGameMode(game.getGameMode());
                    session.setPlayersCount(game.getPlayerCount());
                    session.setBotGame(game.hasBots());

                    if (game.hasBots()) {
                        BotPlayer bot = game.getBot();
                        session.setBotDifficulty(bot != null ? bot.getDifficulty() : 2);
                    }

                    // Déterminer le résultat
                    Player winner = game.getWinner();
                    session.setWon(winner != null && winner.equals(player));
                    session.setWinnerName(winner != null ? winner.getName() : "Égalité");

                    // Statistiques estimées (pourraient être améliorées avec un tracking en temps réel)
                    session.setBombsPlaced(estimateBombsForPlayer(player));
                    session.setEliminationsDealt(estimateEliminationsForPlayer(player));
                    session.setDeaths(player.isEliminated() ? 1 : 0);

                    session.finalize();

                    // Enregistrer
                    profileManager.recordCustomGameSession(player.getName(), session);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement détaillé: " + e.getMessage());
        }
    }

    private int estimateBombsForPlayer(Player player) {
        // Logique d'estimation basée sur la durée de jeu et l'activité
        // Dans une version plus avancée, on pourrait tracker cela en temps réel
        return Math.max(1, (int) (Math.random() * 5) + 1);
    }

    private int estimateEliminationsForPlayer(Player player) {
        if (game.getWinner() != null && game.getWinner().equals(player)) {
            // Le gagnant a probablement fait des éliminations
            return Math.min(game.getPlayerCount() - 1, (int) (Math.random() * 2) + 1);
        }
        return 0;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private PlayerProfile currentProfile; // Profil actuel sélectionné
    private long gameStartTime; // Pour calculer la durée des parties

    /**
     * ✅ NOUVELLE MÉTHODE: Définit le profil actuel
     */
    public void setCurrentProfile(PlayerProfile profile) {
        this.currentProfile = profile;

        // Appliquer les préférences du profil
        if (profile != null) {
            loadPlayerPreferences(profile.getPlayerName());
        }
    }

    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }

    public void startCanvasGameWithProfile() {
        if (currentProfile != null) {
            // Utiliser le profil pour le joueur 1
            startCanvasGameWithNames(currentProfile.getPlayerName(), "Adversaire");
        } else {
            // Fallback vers la méthode normale
            startCanvasGame();
        }
    }

    private void recordGameSessionWithDuration() {
        if (game == null || game.getPlayers().isEmpty()) return;

        try {
            long gameDuration = System.currentTimeMillis() - gameStartTime;

            for (Player player : game.getPlayers()) {
                if (!(player instanceof BotPlayer)) { // Seulement les joueurs humains

                    // Charger ou créer le profil
                    PlayerProfile profile;
                    try {
                        profile = profileManager.loadProfile(player.getName());
                    } catch (BombermanException e) {
                        // Créer un nouveau profil si il n'existe pas
                        profile = new PlayerProfile(player.getName());
                    }

                    // Créer une session détaillée
                    GameSession session = new GameSession();
                    session.setStartTime(java.time.LocalDateTime.now().minusSeconds(gameDuration / 1000));
                    session.setDurationSeconds((int)(gameDuration / 1000));
                    session.setGameMode(game.getGameMode());
                    session.setPlayersCount(game.getPlayerCount());
                    session.setBotGame(game.hasBots());

                    if (game.hasBots()) {
                        BotPlayer bot = game.getBot();
                        session.setBotDifficulty(bot != null ? bot.getDifficulty() : 2);
                    }

                    // Déterminer le résultat
                    Player winner = game.getWinner();
                    session.setWon(winner != null && winner.equals(player));
                    session.setWinnerName(winner != null ? winner.getName() : "Égalité");

                    // Statistiques estimées
                    session.setBombsPlaced(estimateBombsForPlayer(player));
                    session.setEliminationsDealt(estimateEliminationsForPlayer(player));
                    session.setDeaths(player.isEliminated() ? 1 : 0);

                    session.finalize();

                    // Enregistrer la session dans le profil
                    profileManager.recordCustomGameSession(player.getName(), session);

                    // Mettre à jour le profil si c'est le profil actuel
                    if (currentProfile != null && currentProfile.getPlayerName().equals(player.getName())) {
                        currentProfile = profileManager.loadProfile(player.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement détaillé: " + e.getMessage());
        }
    }



    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}