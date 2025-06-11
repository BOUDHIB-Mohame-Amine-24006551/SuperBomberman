package fr.univ.bomberman.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Représente un joueur contrôlé par l'IA (bot) - VERSION INTELLIGENTE QUI ÉVITE SES PROPRES BOMBES
 */
public class BotPlayer extends Player {
    private Random random;
    private long lastMoveTime = 0;
    private long lastBombTime = 0;
    private static final long BOT_MOVE_DELAY = 300; // Plus rapide pour esquiver
    private static final long BOT_BOMB_COOLDOWN = 1500; // Cooldown réduit
    private int difficulty; // 1=Facile, 2=Moyen, 3=Difficile

    public BotPlayer(String name, Position position, int difficulty) {
        super(name, position);
        this.random = new Random();
        this.difficulty = Math.max(1, Math.min(3, difficulty));
    }

    /**
     * ✅ MÉTHODE PRINCIPALE: Le bot décide de son action avec priorité à la survie
     */
    public BotAction decideAction(Game game) {
        if (isEliminated()) {
            return BotAction.NONE;
        }

        long currentTime = System.currentTimeMillis();

        // Limiter la fréquence des actions mais plus rapide pour esquiver
        if (currentTime - lastMoveTime < BOT_MOVE_DELAY) {
            return BotAction.NONE;
        }

        // 1. PRIORITÉ ABSOLUE: Fuir TOUTES les explosions (y compris ses propres bombes)
        BotAction escapeAction = tryEscapeFromDanger(game);
        if (escapeAction != BotAction.NONE) {
            lastMoveTime = currentTime;
            System.out.println("🏃‍♂️ " + getName() + " fuit le danger !");
            return escapeAction;
        }

        // 2. ✅ NOUVEAU: Vérifier si on est dans une zone qui va devenir dangereuse
        BotAction preventiveEscape = tryPreventiveEscape(game);
        if (preventiveEscape != BotAction.NONE) {
            lastMoveTime = currentTime;
            System.out.println("🔮 " + getName() + " anticipe le danger !");
            return preventiveEscape;
        }

        // 3. ✅ CORRIGÉ: Poser une bombe SEULEMENT si c'est sûr
        if (shouldPlaceBombSafely(game, currentTime)) {
            lastBombTime = currentTime;
            return BotAction.PLACE_BOMB;
        }

        // 4. Se déplacer vers l'objectif
        BotAction moveAction = findBestMove(game);
        if (moveAction != BotAction.NONE) {
            lastMoveTime = currentTime;
        }

        return moveAction;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Fuite intelligente de tous les dangers
     */
    private BotAction tryEscapeFromDanger(Game game) {
        Position myPos = getPosition();
        boolean inDanger = false;

        // Vérifier les explosions actives
        for (Explosion explosion : game.getActiveExplosions()) {
            if (explosion.affectsPosition(myPos)) {
                inDanger = true;
                break;
            }
        }

        // ✅ IMPORTANT: Vérifier TOUTES les bombes (y compris les siennes)
        for (Bomb bomb : game.getActiveBombs()) {
            if (isInExplosionRange(myPos, bomb.getPosition())) {
                inDanger = true;
                String ownerName = bomb.getOwner() != null ? bomb.getOwner().getName() : "inconnue";
                break;
            }
        }

        if (!inDanger) {
            return BotAction.NONE;
        }

        // Trouver la meilleure direction pour fuir
        return findSafestMove(game);
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Évasion préventive des futures explosions
     */
    private BotAction tryPreventiveEscape(Game game) {
        Position myPos = getPosition();

        // Vérifier si on va être dans une zone dangereuse bientôt
        for (Bomb bomb : game.getActiveBombs()) {
            if (bomb.getTimer() <= 1 && isInExplosionRange(myPos, bomb.getPosition())) {
                return findSafestMove(game);
            }
        }

        return BotAction.NONE;
    }

    /**
     * ✅ MÉTHODE CORRIGÉE: Pose une bombe seulement si c'est sécurisé
     */
    private boolean shouldPlaceBombSafely(Game game, long currentTime) {
        // Vérifications de base
        if (!canPlaceBomb()) {
            return false;
        }

        if (currentTime - lastBombTime < BOT_BOMB_COOLDOWN) {
            return false;
        }

        Position myPos = getPosition();

        // ✅ CRUCIAL: Simuler la pose de bombe et vérifier qu'on peut s'échapper
        if (!canEscapeFromOwnBomb(myPos, game)) {
            return false;
        }

        // Vérifier s'il y a des blocs destructibles à proximité
        boolean hasBlocks = hasDestructibleBlocksInRange(myPos, game);

        if (!hasBlocks) {
            // Pas de blocs, ne pas gaspiller de bombes sauf pour attaquer
            if (difficulty >= 2) {
                Position playerPos = findNearestPlayer(game);
                if (playerPos != null && getDistanceTo(playerPos) <= 2) {
                    int attackChance = (difficulty == 2) ? 10 : 20; // Chances réduites
                    return random.nextInt(100) < attackChance;
                }
            }
            return false;
        }

        // Il y a des blocs et on peut s'échapper, poser selon la difficulté
        int chance = getBombChanceForDifficulty();
        return random.nextInt(100) < chance;
    }

    /**
     * ✅ NOUVELLE MÉTHODE CRUCIALE: Vérifie si le bot peut s'échapper de sa propre bombe
     */
    private boolean canEscapeFromOwnBomb(Position bombPos, Game game) {
        // Simuler les positions d'explosion de notre future bombe
        List<Position> explosionZones = simulateExplosionZones(bombPos, game);

        // Trouver les positions sûres accessibles
        BotAction[] moves = {BotAction.MOVE_UP, BotAction.MOVE_DOWN, BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};

        for (BotAction move : moves) {
            Position escapePos = getNewPosition(bombPos, move);

            // Vérifier si cette position est sûre et accessible
            if (isSafePosition(escapePos, game) && !explosionZones.contains(escapePos)) {
                return true;
            }
        }

        // Vérifier aussi les positions à 2 cases de distance
        for (BotAction move1 : moves) {
            Position step1 = getNewPosition(bombPos, move1);
            if (!isSafePosition(step1, game)) continue;

            for (BotAction move2 : moves) {
                Position step2 = getNewPosition(step1, move2);
                if (isSafePosition(step2, game) && !explosionZones.contains(step2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Simule les zones d'explosion d'une bombe
     */
    private List<Position> simulateExplosionZones(Position bombPos, Game game) {
        List<Position> zones = new ArrayList<>();
        zones.add(bombPos); // Le centre

        // Expansion dans les 4 directions avec portée de 2
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            for (int range = 1; range <= 2; range++) {
                int newX = bombPos.getX() + (dir[0] * range);
                int newY = bombPos.getY() + (dir[1] * range);
                Position pos = new Position(newX, newY);

                if (!game.getBoard().isWithinBounds(pos)) {
                    break;
                }

                try {
                    Cell cell = game.getBoard().getCell(pos);
                    if (cell.getType() == CellType.INDESTRUCTIBLE_WALL) {
                        break; // L'explosion s'arrête
                    }
                    zones.add(pos);
                } catch (Exception e) {
                    break;
                }
            }
        }

        return zones;
    }

    /**
     * ✅ MÉTHODE AMÉLIORÉE: Trouve le mouvement le plus sûr
     */
    private BotAction findSafestMove(Game game) {
        Position myPos = getPosition();
        BotAction[] moves = {BotAction.MOVE_UP, BotAction.MOVE_DOWN, BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};

        List<BotAction> safeMoves = new ArrayList<>();
        List<BotAction> lessUnsafeMoves = new ArrayList<>();

        for (BotAction move : moves) {
            Position newPos = getNewPosition(myPos, move);

            if (!isSafePosition(newPos, game)) {
                continue; // Position inaccessible
            }

            // Évaluer la sécurité de cette position
            int dangerLevel = evaluateDangerLevel(newPos, game);

            if (dangerLevel == 0) {
                safeMoves.add(move); // Complètement sûr
            } else if (dangerLevel <= 2) {
                lessUnsafeMoves.add(move); // Moins dangereux
            }
        }

        // Prioriser les mouvements complètement sûrs
        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        // Sinon, prendre le moins dangereux
        if (!lessUnsafeMoves.isEmpty()) {
            return lessUnsafeMoves.get(random.nextInt(lessUnsafeMoves.size()));
        }

        // En dernier recours, n'importe quel mouvement valide
        for (BotAction move : moves) {
            Position newPos = getNewPosition(myPos, move);
            if (isSafePosition(newPos, game)) {
                return move;
            }
        }

        return BotAction.NONE;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Évalue le niveau de danger d'une position
     */
    private int evaluateDangerLevel(Position pos, Game game) {
        int danger = 0;

        // Vérifier la proximité des bombes
        for (Bomb bomb : game.getActiveBombs()) {
            if (isInExplosionRange(pos, bomb.getPosition())) {
                danger += 3; // Très dangereux
            } else if (getDistanceTo(pos, bomb.getPosition()) <= 3) {
                danger += 1; // Assez proche
            }
        }

        // Vérifier la proximité des explosions
        for (Explosion explosion : game.getActiveExplosions()) {
            if (explosion.affectsPosition(pos)) {
                danger += 5; // Mortel
            }
        }

        return danger;
    }

    /**
     * ✅ HELPER: Distance entre deux positions
     */
    private int getDistanceTo(Position pos1, Position pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY());
    }

    private int getDistanceTo(Position target) {
        return getDistanceTo(getPosition(), target);
    }

    /**
     * ✅ HELPER: Retourne la chance de poser une bombe selon la difficulté
     */
    private int getBombChanceForDifficulty() {
        switch (difficulty) {
            case 1: return 50; // 50% pour facile
            case 2: return 75; // 75% pour moyen
            case 3: return 90; // 90% pour difficile
            default: return 60;
        }
    }

    /**
     * Vérifie s'il y a des blocs destructibles dans la portée d'explosion
     */
    private boolean hasDestructibleBlocksInRange(Position pos, Game game) {
        try {
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

            for (int[] dir : directions) {
                for (int range = 1; range <= 2; range++) {
                    int newX = pos.getX() + (dir[0] * range);
                    int newY = pos.getY() + (dir[1] * range);
                    Position checkPos = new Position(newX, newY);

                    if (!game.getBoard().isWithinBounds(checkPos)) {
                        break;
                    }

                    Cell cell = game.getBoard().getCell(checkPos);

                    if (cell.getType() == CellType.INDESTRUCTIBLE_WALL) {
                        break;
                    }

                    if (cell.getType() == CellType.DESTRUCTIBLE_BRICK) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * MOUVEMENT INTELLIGENT: Le bot trouve le meilleur mouvement
     */
    private BotAction findBestMove(Game game) {
        Position myPos = getPosition();
        Position targetPos = findTarget(game);

        if (targetPos == null) {
            return getRandomMove(game);
        }

        int dx = targetPos.getX() - myPos.getX();
        int dy = targetPos.getY() - myPos.getY();

        List<BotAction> possibleMoves = new ArrayList<>();

        if (dx > 0) possibleMoves.add(BotAction.MOVE_RIGHT);
        if (dx < 0) possibleMoves.add(BotAction.MOVE_LEFT);
        if (dy > 0) possibleMoves.add(BotAction.MOVE_DOWN);
        if (dy < 0) possibleMoves.add(BotAction.MOVE_UP);

        // Filtrer les mouvements sûrs
        List<BotAction> safeMoves = new ArrayList<>();
        for (BotAction move : possibleMoves) {
            Position newPos = getNewPosition(myPos, move);
            if (isSafePosition(newPos, game) && evaluateDangerLevel(newPos, game) == 0) {
                safeMoves.add(move);
            }
        }

        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        return getRandomMove(game);
    }

    private Position findTarget(Game game) {
        Position brickTarget = findNearestBrick(game);
        if (brickTarget != null) {
            return brickTarget;
        }

        switch (difficulty) {
            case 1:
                return getRandomEmptyPosition(game);
            case 2:
                if (random.nextInt(100) < 20) {
                    return findNearestPlayer(game);
                }
                return getRandomEmptyPosition(game);
            case 3:
                if (random.nextInt(100) < 40) {
                    Position playerTarget = findNearestPlayer(game);
                    if (playerTarget != null) {
                        return playerTarget;
                    }
                }
                return getRandomEmptyPosition(game);
            default:
                return getRandomEmptyPosition(game);
        }
    }

    // Méthodes utilitaires (identiques à la version précédente)
    private boolean isInExplosionRange(Position pos1, Position pos2) {
        return (pos1.getX() == pos2.getX() && Math.abs(pos1.getY() - pos2.getY()) <= 2) ||
                (pos1.getY() == pos2.getY() && Math.abs(pos1.getX() - pos2.getX()) <= 2);
    }

    private boolean isSafePosition(Position pos, Game game) {
        try {
            if (!game.getBoard().isWithinBounds(pos)) return false;

            Cell cell = game.getBoard().getCell(pos);
            if (cell.getType() != CellType.EMPTY) return false;

            for (Bomb bomb : game.getActiveBombs()) {
                if (bomb.getPosition().equals(pos)) {
                    return false;
                }
            }

            for (Player player : game.getPlayers()) {
                if (player != this && !player.isEliminated() && player.getPosition().equals(pos)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Position getNewPosition(Position current, BotAction action) {
        switch (action) {
            case MOVE_UP: return new Position(current.getX(), current.getY() - 1);
            case MOVE_DOWN: return new Position(current.getX(), current.getY() + 1);
            case MOVE_LEFT: return new Position(current.getX() - 1, current.getY());
            case MOVE_RIGHT: return new Position(current.getX() + 1, current.getY());
            default: return current;
        }
    }

    private BotAction getRandomMove(Game game) {
        BotAction[] moves = {BotAction.MOVE_UP, BotAction.MOVE_DOWN, BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};
        List<BotAction> validMoves = new ArrayList<>();

        for (BotAction move : moves) {
            Position newPos = getNewPosition(getPosition(), move);
            if (isSafePosition(newPos, game) && evaluateDangerLevel(newPos, game) <= 1) {
                validMoves.add(move);
            }
        }

        if (validMoves.isEmpty()) {
            // En dernier recours, n'importe quel mouvement valide
            for (BotAction move : moves) {
                Position newPos = getNewPosition(getPosition(), move);
                if (isSafePosition(newPos, game)) {
                    validMoves.add(move);
                }
            }
        }

        if (validMoves.isEmpty()) {
            return BotAction.NONE;
        }

        return validMoves.get(random.nextInt(validMoves.size()));
    }

    private Position findNearestBrick(Game game) {
        Position myPos = getPosition();
        Position nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (int y = 0; y < game.getBoard().getRows(); y++) {
            for (int x = 0; x < game.getBoard().getCols(); x++) {
                try {
                    Position pos = new Position(x, y);
                    Cell cell = game.getBoard().getCell(pos);
                    if (cell.getType() == CellType.DESTRUCTIBLE_BRICK) {
                        double distance = Math.abs(x - myPos.getX()) + Math.abs(y - myPos.getY());
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearest = pos;
                        }
                    }
                } catch (Exception e) {
                    // Ignorer
                }
            }
        }
        return nearest;
    }

    private Position findNearestPlayer(Game game) {
        Position myPos = getPosition();
        Position nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Player player : game.getPlayers()) {
            if (!player.isEliminated() && player != this) {
                Position playerPos = player.getPosition();
                double distance = Math.abs(playerPos.getX() - myPos.getX()) +
                        Math.abs(playerPos.getY() - myPos.getY());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = playerPos;
                }
            }
        }
        return nearest;
    }

    private Position getRandomEmptyPosition(Game game) {
        for (int attempts = 0; attempts < 10; attempts++) {
            int x = random.nextInt(game.getBoard().getCols());
            int y = random.nextInt(game.getBoard().getRows());
            Position pos = new Position(x, y);

            if (isSafePosition(pos, game)) {
                return pos;
            }
        }
        return null;
    }

    public int getDifficulty() {
        return difficulty;
    }
}