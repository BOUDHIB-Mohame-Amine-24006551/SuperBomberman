// FILE: src/main/java/fr/univ/bomberman/model/Game.java
package fr.univ.bomberman.model;

import fr.univ.bomberman.exceptions.BombermanException;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Classe représentant une partie de Super Bomberman avec support des modes tour par tour, temps réel et CTF.
 */
public class Game {

    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private List<Bomb> activeBombs;
    private List<Explosion> activeExplosions;
    private boolean gameOver;
    private GameMode gameMode;
    private long gameStartTime;
    private boolean statsUpdated = false;
    private String levelPath; // Chemin du fichier de niveau

    // ============================================================================
    // ✅ NOUVELLES PROPRIÉTÉS POUR CTF
    // ============================================================================

    private List<Flag> flags;                    // Liste des drapeaux en mode CTF
    private boolean flagSetupPhase;              // Phase de placement des drapeaux
    private int currentPlayerSettingFlag;       // Joueur en train de placer son drapeau
    private Position[] proposedFlagPositions;   // Positions proposées pour les drapeaux



    /**
     * Constructeur avec fichier de niveau spécifié
     * Initialise une partie en mode temps réel avec deux joueurs.
     * @param levelPath chemin vers le fichier de niveau
     */
    public Game(String levelPath) {
        this.gameMode = GameMode.REAL_TIME;
        this.levelPath = levelPath;
        try {
            this.board = new Board(levelPath);
        } catch (BombermanException e) {
            System.err.println("Erreur lors du chargement du niveau, utilisation du niveau par défaut: " + e.getMessage());
            this.board = new Board(15, 13);
        }
        this.players = new ArrayList<>();

        // Créer 2 joueurs par défaut
        Player player1 = new Player("Joueur 1", new Position(1, 1));
        Player player2 = new Player("Joueur 2", new Position(board.getCols() - 2, board.getRows() - 2));
        players.add(player1);
        players.add(player2);

        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
        this.gameOver = false;

        // Initialiser les propriétés CTF
        this.flags = new ArrayList<>();
        this.flagSetupPhase = false;
        this.currentPlayerSettingFlag = -1;
        this.proposedFlagPositions = new Position[0];
        this.gameStartTime = System.currentTimeMillis();
    }











    /**
     * Constructeur avec noms personnalisés et fichier de niveau spécifiés
     * @param playerNames tableau des noms des joueurs
     * @param levelPath chemin vers le fichier de niveau
     */
    public Game(String[] playerNames, String levelPath) {
        this(levelPath);
        this.gameStartTime = System.currentTimeMillis();

        // Positions de départ pour 4 joueurs
        Position[] startPositions = {
                new Position(1, 1),                                    // Coin haut-gauche
                new Position(board.getCols() - 2, 1),                  // Coin haut-droite
                new Position(1, board.getRows() - 2),                  // Coin bas-gauche
                new Position(board.getCols() - 2, board.getRows() - 2) // Coin bas-droite
        };

        // Créer les joueurs avec les noms fournis
        this.players.clear();
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

        // Initialiser les propriétés CTF
        this.flags = new ArrayList<>();
        this.flagSetupPhase = false;
        this.currentPlayerSettingFlag = -1;
        this.proposedFlagPositions = new Position[0];

        System.out.println("🎮 Partie " + playerNames.length + " joueurs créée avec noms personnalisés !");
    }



    /**
     * Constructeur pour le mode Capture the Flag avec fichier de niveau spécifié
     * @param playerNames tableau des noms des joueurs
     * @param mode mode de jeu
     * @param levelPath chemin vers le fichier de niveau
     */
    public Game(String[] playerNames, GameMode mode, String levelPath) {
        this(levelPath);
        this.gameStartTime = System.currentTimeMillis();

        if (mode != GameMode.CAPTURE_THE_FLAG) {
            // Utiliser le constructeur normal pour les autres modes
            this.gameMode = mode;

            // Reconfigurer pour le mode demandé
            this.players.clear();
            Position[] startPositions = {
                    new Position(1, 1),
                    new Position(board.getCols() - 2, 1),
                    new Position(1, board.getRows() - 2),
                    new Position(board.getCols() - 2, board.getRows() - 2)
            };

            for (int i = 0; i < Math.min(playerNames.length, 4); i++) {
                String name = (playerNames[i] != null && !playerNames[i].trim().isEmpty())
                        ? playerNames[i].trim()
                        : "Joueur " + (i + 1);

                Player player = new Player(name, startPositions[i]);
                players.add(player);
            }

            System.out.println("🎮 Partie " + playerNames.length + " joueurs créée en mode " + mode.getDisplayName());
            return;
        }

        // Initialisation spéciale pour CTF
        this.gameMode = GameMode.CAPTURE_THE_FLAG;
        this.players.clear(); // Vider les joueurs par défaut
        this.flags = new ArrayList<>();
        this.flagSetupPhase = true;
        this.currentPlayerSettingFlag = 0;
        this.proposedFlagPositions = new Position[playerNames.length];

        // Positions de départ pour les joueurs (aux coins)
        Position[] startPositions = {
                new Position(1, 1),                                    // Coin haut-gauche
                new Position(board.getCols() - 2, 1),                  // Coin haut-droite
                new Position(1, board.getRows() - 2),                  // Coin bas-gauche
                new Position(board.getCols() - 2, board.getRows() - 2) // Coin bas-droite
        };

        // Créer les joueurs avec capacité CTF
        for (int i = 0; i < Math.min(playerNames.length, 4); i++) {
            String name = (playerNames[i] != null && !playerNames[i].trim().isEmpty())
                    ? playerNames[i].trim()
                    : "Joueur " + (i + 1);

            Player player = new Player(name, startPositions[i]);
            player.setCanPlaceBombWhenEliminated(true); // ✅ CTF: joueurs éliminés peuvent bombarder
            players.add(player);
            clearStartingArea(startPositions[i]);

            // Positions par défaut des drapeaux (près des spawns mais pas dessus)
            proposedFlagPositions[i] = getDefaultFlagPosition(i, startPositions[i]);
        }

        this.currentPlayerIndex = 0;
        this.activeBombs.clear();
        this.activeExplosions.clear();
        this.gameOver = false;

        System.out.println("🏁 Mode CAPTURE THE FLAG créé avec " + playerNames.length + " joueurs !");
        System.out.println("📍 Phase de placement des drapeaux commencée...");
    }



    /**
     * Constructeur pour le mode bot avec fichier de niveau spécifié
     * @param playerName nom du joueur humain
     * @param botDifficulty niveau de difficulté du bot (1-3)
     * @param levelPath chemin vers le fichier de niveau
     */
    public Game(String playerName, int botDifficulty, String levelPath) {
        this(levelPath);
        this.gameStartTime = System.currentTimeMillis();

        // Créer le joueur humain
        Player human = new Player(playerName, new Position(1, 1));
        players.clear();
        players.add(human);

        // Créer le bot
        String botName = "Bot " + getBotDifficultyName(botDifficulty);
        BotPlayer bot = new BotPlayer(botName, new Position(board.getCols() - 2, board.getRows() - 2), botDifficulty);
        players.add(bot);

        this.currentPlayerIndex = 0;
        this.activeBombs = new ArrayList<>();
        this.activeExplosions = new ArrayList<>();
        this.gameOver = false;

        // Initialiser les propriétés CTF
        this.flags = new ArrayList<>();
        this.flagSetupPhase = false;
        this.currentPlayerSettingFlag = -1;
        this.proposedFlagPositions = new Position[0];

        System.out.println("🤖 Partie contre bot créée !");
        System.out.println("👤 " + playerName + " VS 🤖 " + botName);
    }

    // ============================================================================
    // ✅ MÉTHODES CTF - GESTION DES DRAPEAUX
    // ============================================================================

    /**
     * Obtient la position par défaut du drapeau pour un joueur
     * @param playerIndex index du joueur
     * @param playerStart position de départ du joueur
     * @return la position par défaut du drapeau
     */
    private Position getDefaultFlagPosition(int playerIndex, Position playerStart) {
        // Positionner le drapeau à 2-3 cases du spawn du joueur
        switch (playerIndex) {
            case 0: return new Position(playerStart.getX() + 2, playerStart.getY() + 1); // Haut-gauche
            case 1: return new Position(playerStart.getX() - 2, playerStart.getY() + 1); // Haut-droite
            case 2: return new Position(playerStart.getX() + 2, playerStart.getY() - 1); // Bas-gauche
            case 3: return new Position(playerStart.getX() - 2, playerStart.getY() - 1); // Bas-droite
            default: return new Position(playerStart.getX() + 1, playerStart.getY() + 1);
        }
    }

    /**
     * Vérifie si on est en phase de placement des drapeaux
     * @return true si on est en phase de placement des drapeaux
     */
    public boolean isInFlagSetupPhase() {
        return flagSetupPhase;
    }

    /**
     * Obtient le joueur en train de placer son drapeau
     * @return le joueur actuel, ou null si aucun joueur ne place de drapeau
     */
    public Player getCurrentPlayerSettingFlag() {
        if (flagSetupPhase && currentPlayerSettingFlag < players.size()) {
            return players.get(currentPlayerSettingFlag);
        }
        return null;
    }

    /**
     * Place le drapeau du joueur actuel à la position spécifiée
     * @param position position où placer le drapeau
     * @return true si le drapeau a été placé avec succès
     * @throws BombermanException si le placement est invalide
     */
    public boolean placeFlagAt(Position position) throws BombermanException {
        if (!flagSetupPhase) {
            throw new BombermanException("Phase de placement des drapeaux terminée !");
        }

        if (currentPlayerSettingFlag >= players.size()) {
            throw new BombermanException("Tous les drapeaux ont été placés !");
        }

        // Vérifier que la position est valide
        if (!isValidFlagPosition(position)) {
            throw new BombermanException("Position invalide pour le drapeau !");
        }

        // Créer et placer le drapeau
        Player player = players.get(currentPlayerSettingFlag);
        Flag flag = new Flag(player, position);
        flags.add(flag);

        // Placer le drapeau sur le plateau
        CellType flagType = CellType.getFlagTypeForPlayer(currentPlayerSettingFlag);
        board.setCellType(position, flagType);

        System.out.println("🏁 " + player.getName() + " a placé son drapeau en " + position);

        // Passer au joueur suivant
        currentPlayerSettingFlag++;

        // Vérifier si tous les drapeaux sont placés
        if (currentPlayerSettingFlag >= players.size()) {
            finalizeFlagSetup();
        }

        return true;
    }

    /**
     * Valide qu'une position est appropriée pour un drapeau
     * @param position position à valider
     * @return true si la position est valide pour un drapeau
     */
    private boolean isValidFlagPosition(Position position) {
        try {
            // Vérifier les limites du plateau
            if (!board.isWithinBounds(position)) {
                return false;
            }

            // Vérifier que la case est vide
            Cell cell = board.getCell(position);
            if (cell.getType() != CellType.EMPTY) {
                return false;
            }

            // Vérifier qu'il n'y a pas de joueur sur cette position
            for (Player player : players) {
                if (player.getPosition().equals(position)) {
                    return false;
                }
            }

            // Vérifier qu'il n'y a pas déjà un drapeau à cette position
            for (Flag flag : flags) {
                if (flag.getCurrentPosition().equals(position)) {
                    return false;
                }
            }

            // Vérifier la distance minimale avec les spawns des joueurs
            for (Player player : players) {
                int distance = Math.abs(position.getX() - player.getPosition().getX()) +
                        Math.abs(position.getY() - player.getPosition().getY());
                if (distance < 2) { // Minimum 2 cases des spawns
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finalise la phase de placement des drapeaux et commence le jeu
     */
    private void finalizeFlagSetup() {
        flagSetupPhase = false;
        currentPlayerSettingFlag = -1;

        System.out.println("🎮 Phase de placement terminée ! Le jeu CTF commence !");
        System.out.println("🎯 Objectif : Capturez TOUS les drapeaux adverses pour gagner !");
        System.out.println("💀 Les joueurs éliminés peuvent encore poser des bombes !");

        // Afficher l'état des drapeaux
        for (int i = 0; i < flags.size(); i++) {
            Flag flag = flags.get(i);
            System.out.println("🏁 Drapeau de " + flag.getOwner().getName() + " : " + flag.getCurrentPosition());
        }
    }

    /**
     * Obtient tous les drapeaux
     */
    public List<Flag> getFlags() {
        return new ArrayList<>(flags);
    }

    /**
     * Obtient le drapeau d'un joueur spécifique
     */
    public Flag getFlagByOwner(Player owner) {
        return flags.stream()
                .filter(flag -> flag.getOwner().equals(owner))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtient le drapeau à une position donnée
     */
    public Flag getFlagAtPosition(Position position) {
        return flags.stream()
                .filter(flag -> flag.getCurrentPosition().equals(position))
                .findFirst()
                .orElse(null);
    }

    // ============================================================================
    // ✅ MÉTHODES CTF - LOGIQUE DE JEU
    // ============================================================================

    /**
     * Gère le ramassage automatique des drapeaux quand un joueur se déplace
     */
    public void checkFlagPickup(Player player) {
        if (gameMode != GameMode.CAPTURE_THE_FLAG || player.isEliminated()) {
            return;
        }

        Position playerPos = player.getPosition();

        // Chercher un drapeau à cette position
        Flag flagAtPosition = getFlagAtPosition(playerPos);

        if (flagAtPosition != null && flagAtPosition.canBePickedUpBy(player)) {
            // Ramasser le drapeau
            flagAtPosition.pickUpBy(player);

            // Retirer le drapeau du plateau (il suit maintenant le joueur)
            try {
                board.setCellType(playerPos, CellType.EMPTY);
            } catch (Exception e) {
                System.out.println("Erreur lors du retrait du drapeau du plateau : " + e.getMessage());
            }
        }
    }



    /**
     * Vérifie les conditions de victoire en mode CTF
     */
    public boolean checkCTFVictory() {
        if (gameMode != GameMode.CAPTURE_THE_FLAG || flagSetupPhase) {
            return false;
        }

        // Un joueur gagne s'il a capturé TOUS les drapeaux des autres joueurs
        for (Player player : players) {
            if (player.isEliminated()) continue;

            int flagsNeeded = players.size() - 1; // Tous sauf le sien
            if (player.getCapturedFlagsCount() >= flagsNeeded) {
                gameOver = true;
                System.out.println("🏆 " + player.getName() + " remporte la victoire CTF en capturant tous les drapeaux !");

                // ✅ NOUVEAU : Mettre à jour les stats pour CTF
                updateProfileStats();

                return true;
            }
        }

        return false;
    }

    /**
     * Met à jour les positions des drapeaux portés
     */
    private void updateCarriedFlags() {
        if (gameMode != GameMode.CAPTURE_THE_FLAG) {
            return;
        }

        for (Flag flag : flags) {
            if (flag.isBeingCarried()) {
                flag.setCurrentPosition(flag.getCarrier().getPosition());
            }
        }
    }

    public void update() throws BombermanException {
        // ✅ NOUVEAU: Faire jouer les bots avant la mise à jour normale
        for (Player player : players) {
            if (player instanceof BotPlayer && !player.isEliminated()) {
                BotPlayer bot = (BotPlayer) player;
                executeBotAction(bot);
            }
        }

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

        // Mise à jour des explosions avec gestion CTF
        Iterator<Explosion> explosionIterator = activeExplosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();

            if (explosion.isFinished()) {
                explosionIterator.remove();
                System.out.println("🟢 Explosion terminée (1.5s écoulées)");
            } else {
                // Vérifier les joueurs touchés par l'explosion
                for (Player player : players) {
                    if (!player.isEliminated()) {
                        if (explosion.affectsPosition(player.getPosition())) {
                            Player bombOwner = explosion.getBombOwner();

                            // ✅ NOUVEAU: Gérer la chute des drapeaux AVANT l'élimination
                            if (gameMode == GameMode.CAPTURE_THE_FLAG) {
                                handlePlayerEliminationWithFlags(player);
                            }

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

        // ✅ NOUVEAU: Logique spécifique au CTF
        if (gameMode == GameMode.CAPTURE_THE_FLAG && !flagSetupPhase) {
            updateCarriedFlags();
            if (checkCTFVictory()) {
                return; // Jeu terminé
            }
        }

        // Vérifier fin de partie (mode normal)
        if (gameMode != GameMode.CAPTURE_THE_FLAG) {
            long aliveCount = players.stream().filter(p -> !p.isEliminated()).count();
            if (aliveCount <= 1) {
                gameOver = true;

                if (aliveCount == 1) {
                    Player winner = players.stream().filter(p -> !p.isEliminated()).findFirst().orElse(null);
                }
            }
        }
        if (gameMode != GameMode.CAPTURE_THE_FLAG) {
            long aliveCount = players.stream().filter(p -> !p.isEliminated()).count();
            if (aliveCount <= 1) {
                gameOver = true;

                if (aliveCount == 1) {
                    Player winner = players.stream().filter(p -> !p.isEliminated()).findFirst().orElse(null);
                    // ✅ NOUVEAU : Mettre à jour les stats quand il y a un gagnant
                    updateProfileStats();
                }
            }
        }
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
     * Libère la zone de départ d'un joueur.
     * @param center position centrale de la zone à libérer
     */
    private void clearStartingArea(Position center) {
        try {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Position pos = new Position(center.getX() + dx, center.getY() + dy);
                    if (board.isWithinBounds(pos)) {
                        board.setCellType(pos, CellType.EMPTY);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la libération de la zone de départ: " + e.getMessage());
        }
    }

    /**
     * Retourne le nombre de joueurs dans la partie.
     * @return le nombre de joueurs
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Retourne le nombre de joueurs encore vivants.
     * @return le nombre de joueurs vivants
     */
    public int getAlivePlayerCount() {
        return (int) players.stream().filter(p -> !p.isEliminated()).count();
    }

    /**
     * Retourne le mode de jeu actuel.
     * @return le mode de jeu
     */
    public GameMode getGameMode() {
        return gameMode;
    }



    /**
     * Retourne le plateau de jeu.
     * @return le plateau de jeu
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Retourne le joueur dont c'est actuellement le tour.
     * @return le joueur actuel
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Retourne la liste des joueurs.
     * @return une copie de la liste des joueurs
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Retourne la liste des bombes actives.
     * @return une copie de la liste des bombes actives
     */
    public List<Bomb> getActiveBombs() {
        return new ArrayList<>(activeBombs);
    }

    /**
     * Retourne la liste des explosions actives.
     * @return une copie de la liste des explosions actives
     */
    public List<Explosion> getActiveExplosions() {
        return new ArrayList<>(activeExplosions);
    }



    /**
     * Déplace un joueur dans une direction donnée.
     * @param playerIndex index du joueur à déplacer
     * @param dx déplacement horizontal
     * @param dy déplacement vertical
     * @throws BombermanException si le déplacement est invalide
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

        // ✅ NOUVEAU: Gestion CTF après le déplacement
        if (gameMode == GameMode.CAPTURE_THE_FLAG && !flagSetupPhase) {
            checkFlagPickup(player);
        }
    }

    /**
     * Place une bombe pour un joueur spécifique
     * @param playerIndex index du joueur
     * @throws BombermanException si le placement est invalide
     */
    public void placeBombForPlayer(int playerIndex) throws BombermanException {
        if (isGameOver()) {
            throw new BombermanException("Le jeu est terminé !");
        }

        if (playerIndex < 0 || playerIndex >= players.size()) {
            throw new BombermanException("Index de joueur invalide : " + playerIndex);
        }

        Player player = players.get(playerIndex);

        // ✅ NOUVEAU: En mode CTF, les joueurs éliminés peuvent poser des bombes
        if (gameMode == GameMode.CAPTURE_THE_FLAG) {
            if (player.isEliminated() && !player.canPlaceBombWhenEliminated()) {
                throw new BombermanException(player.getName() + " est éliminé et ne peut pas poser de bombe !");
            }
        } else {
            // Mode normal : joueurs éliminés ne peuvent pas poser de bombes
            if (player.isEliminated()) {
                throw new BombermanException(player.getName() + " est éliminé et ne peut pas poser de bombe !");
            }
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



    public void resetAllCooldowns() {
        for (Player player : players) {
            player.resetBombCooldown(); // Utilise la méthode publique au lieu d'accès direct
        }
        System.out.println("🔄 Cooldowns de tous les joueurs réinitialisés");
    }


    /**
     * @return true si la partie est terminée
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * ✅ MÉTHODE MODIFIÉE: Gagnant avec support CTF
     */
    public Player getWinner() {
        if (!gameOver) {
            return null;
        }

        if (gameMode == GameMode.CAPTURE_THE_FLAG) {
            // En mode CTF, le gagnant est celui qui a capturé tous les drapeaux
            for (Player player : players) {
                if (!player.isEliminated()) {
                    int flagsNeeded = players.size() - 1;
                    if (player.getCapturedFlagsCount() >= flagsNeeded) {
                        return player;
                    }
                }
            }
            return null; // Pas de gagnant CTF encore
        } else {
            // Mode normal : dernier survivant
            return players.stream().filter(p -> !p.isEliminated()).findFirst().orElse(null);
        }
    }

    /**
     * Force la fin de partie
     */
    public void endGame() {
        this.gameOver = true;
        updateProfileStats();
    }



    // ============================================================================
    // ✅ MÉTHODES UTILITAIRES CTF
    // ============================================================================


    private void handlePlayerEliminationWithFlags(Player eliminatedPlayer) {
        if (gameMode != GameMode.CAPTURE_THE_FLAG) {
            return;
        }

        System.out.println("🏁 Gestion des drapeaux pour " + eliminatedPlayer.getName() + " éliminé...");

        // 1. Faire tomber tous les drapeaux portés par ce joueur
        for (Flag flag : flags) {
            if (flag.getCarrier() != null && flag.getCarrier().equals(eliminatedPlayer)) {
                Position dropPosition = eliminatedPlayer.getPosition();

                System.out.println("📉 " + eliminatedPlayer.getName() + " fait tomber le drapeau de " + flag.getOwner().getName() + " en " + dropPosition);

                // Faire tomber le drapeau
                flag.drop();

                // Placer le drapeau sur le plateau à la position de chute
                try {
                    // Trouver l'index du propriétaire du drapeau pour déterminer le type de cellule
                    int ownerIndex = players.indexOf(flag.getOwner());
                    CellType flagType = CellType.getFlagTypeForPlayer(ownerIndex);
                    board.setCellType(dropPosition, flagType);

                    System.out.println("🏁 Drapeau de " + flag.getOwner().getName() + " placé au sol en " + dropPosition);
                } catch (Exception e) {
                    System.out.println("❌ Erreur lors du placement du drapeau tombé : " + e.getMessage());
                }
            }
        }

        // 2. Faire retourner le drapeau du joueur éliminé à sa base
        Flag ownFlag = getFlagByOwner(eliminatedPlayer);
        if (ownFlag != null) {
            // Si le drapeau était porté par quelqu'un d'autre
            if (ownFlag.isBeingCarried()) {
                Player carrier = ownFlag.getCarrier();
                System.out.println("🏠 Le drapeau de " + eliminatedPlayer.getName() + " retourne automatiquement à sa base (était porté par " + carrier.getName() + ")");

                // Retirer ce drapeau de la liste des drapeaux capturés du porteur
                carrier.removeCapturedFlag(ownFlag.getFlagId());
            } else {
                System.out.println("🏠 Le drapeau de " + eliminatedPlayer.getName() + " retourne automatiquement à sa base");
            }

            // Remettre le drapeau à sa position d'origine
            Position homePosition = ownFlag.getHomePosition();
            ownFlag.returnHome();

            // Mettre à jour le plateau
            try {
                int ownerIndex = players.indexOf(eliminatedPlayer);
                CellType flagType = CellType.getFlagTypeForPlayer(ownerIndex);

                // Nettoyer l'ancienne position si le drapeau était au sol
                if (!ownFlag.getCurrentPosition().equals(homePosition)) {
                    board.setCellType(ownFlag.getCurrentPosition(), CellType.EMPTY);
                }

                // Placer le drapeau à sa position d'origine
                board.setCellType(homePosition, flagType);

            } catch (Exception e) {
                System.out.println("❌ Erreur lors du retour du drapeau à la base : " + e.getMessage());
            }
        }

        // 3. Retirer tous les drapeaux capturés de la liste du joueur éliminé
        int capturedCount = eliminatedPlayer.getCapturedFlagsCount();
        if (capturedCount > 0) {
            System.out.println("🗑️ " + eliminatedPlayer.getName() + " perd ses " + capturedCount + " drapeaux capturés");
            eliminatedPlayer.clearCapturedFlags();
        }

        System.out.println("✅ Gestion des drapeaux terminée pour " + eliminatedPlayer.getName());
    }
    public long getGameDurationSeconds() {
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }

    /**
     * Met à jour les statistiques du profil à la fin de la partie
     */
    private void updateProfileStats() {
        if (statsUpdated) {
            return; // Éviter les doubles mises à jour
        }

        statsUpdated = true;

        // Trouver le joueur humain (pas un bot)
        Player humanPlayer = getHumanPlayer();
        if (humanPlayer != null) {
            long duration = getGameDurationSeconds();
            GameEndHandler.handleGameEnd(this, humanPlayer.getName(), duration);
        }
    }

}