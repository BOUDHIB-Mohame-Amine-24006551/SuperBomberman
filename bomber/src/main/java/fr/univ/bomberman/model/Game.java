// FILE: src/main/java/fr/univ/bomberman/model/Game.java
package fr.univ.bomberman.model;

import fr.univ.bomberman.controller.ProfileController;
import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

/**
 * Classe représentant une partie de Super Bomberman avec support des modes tour par tour et temps réel.
 */
public class Game {

    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private List<Bomb> activeBombs;
    private List<Explosion> activeExplosions;
    private boolean gameOver;
    private GameMode gameMode;
    private boolean statsUpdated = false; // Indique si les statistiques ont déjà été mises à jour
    private static final String DEFAULT_LEVEL = "bomber/src/main/resources/fr/univ/bomberman/level/default/level.json";
    private String currentLevel;
    
    /**
     * Constructeur par défaut : Crée une partie 2 joueurs classique
     */
    public Game() {
        this.gameMode = GameMode.REAL_TIME;
        this.currentLevel = DEFAULT_LEVEL;
        try {
            this.board = new Board(currentLevel);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger le niveau par défaut", e);
        }
        this.players = new ArrayList<>();

        // Créer 2 joueurs par défaut
        Player player1 = new Player("Joueur 1", new Position(1, 1));
        Player player2 = new Player("Joueur 2", new Position(board.getCols() - 2, board.getRows() - 2));
        players.add(player1);
        players.add(player2);

        this.currentPlayerIndex = 0;
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
        this.gameOver = false;
    }

    /**
     * Constructeur avec mode de jeu spécifié
     */
    public Game(GameMode mode) {
        this(); // Appelle le constructeur par défaut
        this.gameMode = mode;
    }

    /**
     * Constructeur avec le nombre de joueurs spécifié
     */
    public Game(int numberOfPlayers) {
        this.gameMode = GameMode.REAL_TIME;
        this.currentLevel = DEFAULT_LEVEL;
        try {
            this.board = new Board(currentLevel);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger le niveau par défaut", e);
        }
        this.players = new ArrayList<>();

        // Positions de départ pour 4 joueurs (aux 4 coins)
        Position[] startPositions = {
                new Position(1, 1),                                    // Joueur 1: Coin haut-gauche
                new Position(board.getCols() - 2, 1),                  // Joueur 2: Coin haut-droite
                new Position(1, board.getRows() - 2),                  // Joueur 3: Coin bas-gauche
                new Position(board.getCols() - 2, board.getRows() - 2) // Joueur 4: Coin bas-droite
        };

        // Noms par défaut des joueurs
        String[] defaultNames = {"Joueur 1", "Joueur 2", "Joueur 3", "Joueur 4"};

        // Créer les joueurs
        for (int i = 0; i < Math.min(numberOfPlayers, 4); i++) {
            Player player = new Player(defaultNames[i], startPositions[i]);
            players.add(player);
        }

        this.currentPlayerIndex = 0;
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
        this.gameOver = false;

        System.out.println("🎮 Partie " + numberOfPlayers + " joueurs créée !");
    }

    /**
     * Constructeur avec noms personnalisés pour 4 joueurs
     */
    public Game(String[] playerNames) {
        this.gameMode = GameMode.REAL_TIME;
        this.currentLevel = DEFAULT_LEVEL;
        try {
            this.board = new Board(currentLevel);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger le niveau par défaut", e);
        }
        this.players = new ArrayList<>();

        // Positions de départ pour 4 joueurs
        Position[] startPositions = {
                new Position(1, 1),                                    // Coin haut-gauche
                new Position(board.getCols() - 2, 1),                  // Coin haut-droite
                new Position(1, board.getRows() - 2),                  // Coin bas-gauche
                new Position(board.getCols() - 2, board.getRows() - 2) // Coin bas-droite
        };

        // Créer les joueurs avec les noms fournis
        for (int i = 0; i < Math.min(playerNames.length, 4); i++) {
            String name = (playerNames[i] != null && !playerNames[i].trim().isEmpty())
                    ? playerNames[i].trim()
                    : "Joueur " + (i + 1);

            Player player = new Player(name, startPositions[i]);
            players.add(player);
        }

        this.currentPlayerIndex = 0;
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
        this.gameOver = false;

        System.out.println("🎮 Partie " + playerNames.length + " joueurs créée avec noms personnalisés !");
    }

    public Game(String playerName, int botDifficulty) {
        this.gameMode = GameMode.REAL_TIME;
        this.currentLevel = DEFAULT_LEVEL;
        try {
            this.board = new Board(currentLevel);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger le niveau par défaut", e);
        }
        this.players = new ArrayList<>();

        // Créer le joueur humain
        Player human = new Player(playerName, new Position(1, 1));
        players.add(human);

        // Créer le bot
        String botName = "Bot " + getBotDifficultyName(botDifficulty);
        BotPlayer bot = new BotPlayer(botName, new Position(board.getCols() - 2, board.getRows() - 2), botDifficulty);
        players.add(bot);

        this.currentPlayerIndex = 0;
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
        this.gameOver = false;

        System.out.println("🤖 Partie contre bot créée !");
        System.out.println("👤 " + playerName + " VS 🤖 " + botName);
    }


    public void update() throws BombermanException {
        // ✅ NOUVEAU: Faire jouer les bots avant la mise à jour normale
        for (Player player : players) {
            if (player instanceof BotPlayer && !player.isEliminated()) {
                BotPlayer bot = (BotPlayer) player;
                executeBotAction(bot);
            }
        }

        // ✅ CORRIGÉ: Mise à jour normale du jeu (au lieu de super.update())
        // Mise à jour des bombes
        Iterator<Bomb> bombIterator = activeBombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.updateTimer();
            if (bomb.isExploded()) {
                Explosion explosion = bomb.explode();
                activeExplosions.add(explosion);
                bombIterator.remove();
                System.out.println("💥 EXPLOSION ! Disparaît dans 1.5 seconde !");
            }
        }

        // Mise à jour des explosions
        Iterator<Explosion> explosionIterator = activeExplosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();

            // Supprimer les explosions qui ont dépassé 1.5s
            if (explosion.isFinished()) {
                explosionIterator.remove();
                System.out.println("🟢 Explosion terminée (1.5s écoulées)");
            } else {
                // Vérifier les joueurs (explosion mortelle pendant toute sa durée)
                for (Player player : players) {
                    if (!player.isEliminated()) {
                        if (explosion.affectsPosition(player.getPosition())) {
                            Player bombOwner = explosion.getBombOwner();

                            // Éliminer le joueur
                            player.setEliminated(true);

                            System.out.println("💀 " + player.getName() + " a été éliminé par une explosion !");

                            if (bombOwner != null) {
                                if (bombOwner.equals(player)) {
                                    System.out.println("🤦 " + player.getName() + " s'est éliminé avec sa propre bombe !");
                                } else {
                                    System.out.println("🎯 " + bombOwner.getName() + " a éliminé " + player.getName() + " !");
                                }
                            }
                        }
                    }
                }
            }
        }

        // Vérifier fin de partie
        checkGameOver();
    }

    private void executeBotAction(BotPlayer bot) {
        try {
            BotAction action = bot.decideAction(this);
            int botIndex = players.indexOf(bot);

            switch (action) {
                case MOVE_UP:
                    movePlayer(botIndex, 0, -1);
                    break;
                case MOVE_DOWN:
                    movePlayer(botIndex, 0, 1);
                    break;
                case MOVE_LEFT:
                    movePlayer(botIndex, -1, 0);
                    break;
                case MOVE_RIGHT:
                    movePlayer(botIndex, 1, 0);
                    break;
                case PLACE_BOMB:
                    placeBombForPlayer(botIndex);
                    System.out.println("🤖 " + bot.getName() + " pose une bombe tactique !");
                    break;
                case NONE:
                default:
                    // Aucune action
                    break;
            }
        } catch (BombermanException e) {
            // Le bot a tenté une action invalide, on l'ignore silencieusement
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

    public boolean hasBots() {
        return players.stream().anyMatch(player -> player instanceof BotPlayer);
    }

    public BotPlayer getBot() {
        return players.stream()
                .filter(player -> player instanceof BotPlayer)
                .map(player -> (BotPlayer) player)
                .findFirst()
                .orElse(null);
    }

    public Player getHumanPlayer() {
        return players.stream()
                .filter(player -> !(player instanceof BotPlayer))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return le nombre de joueurs dans la partie
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * @return le nombre de joueurs encore vivants
     */
    public int getAlivePlayerCount() {
        return (int) players.stream().filter(p -> !p.isEliminated()).count();
    }

    /**
     * @return le mode de jeu actuel
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Change le mode de jeu
     */
    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
        System.out.println("Mode de jeu changé vers: " + mode.getDisplayName());
    }

    /**
     * @return le plateau de jeu (Board)
     */
    public Board getBoard() {
        return board;
    }

    /**
     * @return le joueur dont c'est actuellement le tour
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * @return la liste des joueurs
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * @return la liste des bombes actives
     */
    public List<Bomb> getActiveBombs() {
        return new ArrayList<>(activeBombs);
    }

    /**
     * @return la liste des explosions actives
     */
    public List<Explosion> getActiveExplosions() {
        return new ArrayList<>(activeExplosions);
    }

    /**
     * Trouve un joueur par son nom
     */
    public Player getPlayerByName(String name) {
        return players.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Déplace un joueur spécifique par son index
     */
    public void movePlayer(int playerIndex, int dx, int dy) throws BombermanException {
        if (gameOver) {
            throw new BombermanException("La partie est terminée.");
        }

        if (playerIndex < 0 || playerIndex >= players.size()) {
            throw new BombermanException("Index de joueur invalide: " + playerIndex);
        }

        Player player = players.get(playerIndex);

        if (player.isEliminated()) {
            throw new BombermanException("Le joueur " + player.getName() + " est éliminé.");
        }

        Position newPos = new Position(player.getPosition().getX() + dx, player.getPosition().getY() + dy);

        if (!board.isWithinBounds(newPos)) {
            throw new BombermanException("Déplacement hors du plateau pour " + player.getName());
        }

        Cell targetCell = board.getCell(newPos);
        if (targetCell.getType() == CellType.INDESTRUCTIBLE_WALL || targetCell.getType() == CellType.DESTRUCTIBLE_BRICK) {
            throw new BombermanException("Déplacement invalide pour " + player.getName() + " : obstacle.");
        }

        for (Bomb bomb : activeBombs) {
            if (bomb.getPosition().equals(newPos)) {
                throw new BombermanException("Impossible pour " + player.getName() + " de se déplacer sur une bombe.");
            }
        }

        for (Player otherPlayer : players) {
            if (otherPlayer != player && !otherPlayer.isEliminated() && otherPlayer.getPosition().equals(newPos)) {
                throw new BombermanException("Collision entre joueurs sur la position " + newPos);
            }
        }

        player.setPosition(newPos);
    }

    public void placeBombForPlayer(int playerIndex) throws BombermanException {
        if (isGameOver()) {
            throw new BombermanException("Le jeu est terminé !");
        }

        if (playerIndex < 0 || playerIndex >= players.size()) {
            throw new BombermanException("Index de joueur invalide : " + playerIndex);
        }

        Player player = players.get(playerIndex);

        if (player.isEliminated()) {
            throw new BombermanException(player.getName() + " est éliminé et ne peut pas poser de bombe !");
        }

        // ✅ NOUVEAU: Vérifier le cooldown
        if (!player.canPlaceBomb()) {
            long remainingMs = player.getRemainingCooldown();
            throw new BombermanException(player.getName() + " doit attendre encore " +
                    String.format("%.1f", remainingMs / 1000.0) + "s avant de poser une bombe !");
        }

        // Obtenir la position du joueur
        Position playerPosition = player.getPosition();

        // ✅ CORRIGÉ: Vérifier s'il n'y a pas déjà une bombe à cette position
        for (Bomb bomb : activeBombs) {
            if (bomb.getPosition().equals(playerPosition)) {
                throw new BombermanException("Il y a déjà une bombe à cette position !");
            }
        }

        // ✅ CORRIGÉ: Créer et ajouter la nouvelle bombe
        Bomb newBomb = new Bomb(playerPosition, player, board);
        activeBombs.add(newBomb);

        // ✅ NOUVEAU: Marquer que le joueur vient de poser une bombe
        player.bombPlaced();

        System.out.println(player.getName() + " a posé une bombe en " + playerPosition + " - Cooldown activé !");
    }

    public String getCooldownStatus() {
        StringBuilder status = new StringBuilder();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.isEliminated()) continue;

            if (i > 0) status.append(" | ");

            if (player.isOnBombCooldown()) {
                long remaining = player.getRemainingCooldown();
                status.append(player.getName()).append(": ⏱️").append(remaining / 100 / 10.0).append("s");
            } else {
                status.append(player.getName()).append(": ✅Prêt");
            }
        }

        return status.toString();
    }

    public void resetAllCooldowns() {
        for (Player player : players) {
            player.resetBombCooldown(); // Utilise la méthode publique au lieu d'accès direct
        }
        System.out.println("🔄 Cooldowns de tous les joueurs réinitialisés");
    }

    /**
     * Obtient un joueur spécifique par son index
     */
    public Player getPlayer(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            return null;
        }
        return players.get(playerIndex);
    }

    /**
     * Vérifie si un joueur spécifique peut se déplacer vers une position
     */
    public boolean canPlayerMove(int playerIndex, int dx, int dy) {
        try {
            if (playerIndex < 0 || playerIndex >= players.size()) {
                return false;
            }

            Player player = players.get(playerIndex);
            if (player.isEliminated()) {
                return false;
            }

            Position newPos = new Position(player.getPosition().getX() + dx, player.getPosition().getY() + dy);

            if (!board.isWithinBounds(newPos)) {
                return false;
            }

            Cell targetCell = board.getCell(newPos);
            if (targetCell.getType() == CellType.INDESTRUCTIBLE_WALL || targetCell.getType() == CellType.DESTRUCTIBLE_BRICK) {
                return false;
            }

            for (Bomb bomb : activeBombs) {
                if (bomb.getPosition().equals(newPos)) {
                    return false;
                }
            }

            for (Player otherPlayer : players) {
                if (otherPlayer != player && !otherPlayer.isEliminated() && otherPlayer.getPosition().equals(newPos)) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Déplace le joueur courant (pour compatibilité)
     */
    public void moveCurrentPlayer(int dx, int dy) throws BombermanException {
        movePlayer(currentPlayerIndex, dx, dy);
    }

    /**
     * Place une bombe pour un joueur spécifique
     */
    public void placeBomb(Player player) throws BombermanException {
        if (gameOver) {
            throw new BombermanException("La partie est terminée.");
        }

        if (player.isEliminated()) {
            throw new BombermanException("Le joueur " + player.getName() + " est éliminé.");
        }

        if (gameMode == GameMode.TURN_BASED && !player.equals(getCurrentPlayer())) {
            throw new BombermanException("Ce n'est pas le tour de " + player.getName());
        }

        Position pos = player.getPosition();

        for (Bomb b : activeBombs) {
            if (b.getPosition().equals(pos)) {
                throw new BombermanException("Une bombe est déjà présente sur cette case.");
            }
        }

        Bomb bomb = new Bomb(pos, player, board);
        activeBombs.add(bomb);
    }

    /**
     * Place une bombe pour le joueur courant
     */
    public void placeBomb() throws BombermanException {
        placeBomb(getCurrentPlayer());
    }

    /**
     * Passe au tour du joueur suivant
     */
    private void nextTurn() {
        if (gameMode != GameMode.TURN_BASED) {
            return;
        }

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (players.get(currentPlayerIndex).isEliminated() && !gameOver);
    }

    /**
     * Permet de changer de joueur manuellement
     */
    public void switchPlayer() {
        if (gameMode == GameMode.TURN_BASED) {
            nextTurn();
        }
    }

    /**
     * @return true si la partie est terminée
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * @return le joueur gagnant
     */
    public Player getWinner() {
        if (!gameOver) {
            return null;
        }
        return players.stream().filter(p -> !p.isEliminated()).findFirst().orElse(null);
    }

    /**
     * Force la fin de partie
     */
    public void endGame() {
        this.gameOver = true;
    }

    /**
     * Redémarre le jeu avec le même mode
     */
    public void restart() {
        GameMode currentMode = this.gameMode;
        try {
            this.board = new Board(JsonUtils.getLevelPath(DEFAULT_LEVEL));
        } catch (Exception e) {
            System.out.println("❌ Erreur critique : impossible de charger le niveau par défaut");
            throw new RuntimeException("Impossible de charger le niveau par défaut", e);
        }
        this.players.clear();
        Player player1 = new Player("Joueur 1", new Position(1, 1));
        Player player2 = new Player("Joueur 2", new Position(board.getCols() - 2, board.getRows() - 2));
        players.add(player1);
        players.add(player2);
        this.currentPlayerIndex = 0;
        this.activeBombs.clear();
        this.activeExplosions.clear();
        this.gameOver = false;
        this.gameMode = currentMode;
        this.statsUpdated = false; // Réinitialiser le drapeau de mise à jour des statistiques
    }

    /**
     * Vérifie et gère la fin de partie
     */
    private void checkGameOver() {
        int aliveCount = (int) players.stream().filter(p -> !p.isEliminated()).count();
        
        if (aliveCount <= 1) {
            gameOver = true;

            if (aliveCount == 1) {
                Player winner = players.stream().filter(p -> !p.isEliminated()).findFirst().orElse(null);
                if (winner != null) {
                    System.out.println("🏆 " + winner.getName() + " REMPORTE LA BATAILLE !");
                    
                    // Mettre à jour les statistiques si ce n'est pas déjà fait
                    if (!statsUpdated) {
                        updatePlayerStats(winner);
                        statsUpdated = true;
                    }
                }
            } else {
                System.out.println("💥 TOUS ÉLIMINÉS - ÉGALITÉ !");
                
                // Mettre à jour les statistiques pour une partie jouée (sans gagnant)
                if (!statsUpdated) {
                    PlayerProfileManager.incrementGamesPlayed();
                    statsUpdated = true;
                }
            }
        }
    }
    
    /**
     * Met à jour les statistiques du profil joueur
     * 
     * @param winner le joueur gagnant
     */
    private void updatePlayerStats(Player winner) {
        // Si le gagnant est un joueur humain (non bot), on incrémente ses victoires
        if (!(winner instanceof BotPlayer)) {
            PlayerProfileManager.incrementGamesWon();
        } else {
            // Si c'est un bot qui a gagné, on incrémente juste les parties jouées
            PlayerProfileManager.incrementGamesPlayed();
        }
    }

    /**
     * Change le niveau actuel
     */
    public void setLevel(String levelPath) {
        this.currentLevel = levelPath;
        try {
            this.board = new Board(currentLevel);
            System.out.println("Niveau changé vers: " + levelPath);
        } catch (IOException e) {
            System.out.println("❌ Erreur lors du changement de niveau: " + e.getMessage());
            throw new RuntimeException("Impossible de charger le niveau", e);
        }
    }

    /**
     * Obtient le niveau actuel
     */
    public String getCurrentLevel() {
        return currentLevel;
    }
}