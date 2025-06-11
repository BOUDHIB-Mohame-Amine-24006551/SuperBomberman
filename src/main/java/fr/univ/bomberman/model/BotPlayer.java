package fr.univ.bomberman.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Représente un joueur contrôlé par l'IA (bot) - VERSION AGRESSIVE STYLE FANTÔME PAC-MAN
 * Le bot poursuit activement le joueur pour l'éliminer tout en évitant ses propres bombes
 */
public class BotPlayer extends Player {
    private Random random;
    private long lastMoveTime = 0;
    private long lastBombTime = 0;
    private long botMoveDelay; // Vitesse adaptée selon la difficulté
    private static final long BOT_BOMB_COOLDOWN = 1000; // Cooldown très réduit pour plus d'agressivité
    private int difficulty; // 1=Facile, 2=Moyen, 3=Difficile

    // NOUVEAU: Variables pour le comportement agressif
    private Position lastPlayerPosition;
    private int huntingMode = 0; // 0=normal, 1=poursuite active, 2=piégeage
    private long lastPlayerSeen = 0;
    private boolean playerInRange = false;

    public BotPlayer(String name, Position position, int difficulty) {
        super(name, position);
        this.random = new Random();
        this.difficulty = Math.max(1, Math.min(3, difficulty));

        // ✅ NOUVEAU: Vitesse adaptée selon la difficulté
        // Vitesse humaine de référence dans BombermanApp: 150ms (MOVE_DELAY)
        switch (this.difficulty) {
            case 1: // FACILE - Même vitesse que le joueur
                this.botMoveDelay = 150; // Identique au joueur humain
                System.out.println("🐢 Bot " + name + " - Vitesse FACILE (même que le joueur)");
                break;
            case 2: // MOYEN - 25% plus rapide
                this.botMoveDelay = 120; // 25% plus rapide
                System.out.println("🏃 Bot " + name + " - Vitesse MOYENNE (+25% plus rapide)");
                break;
            case 3: // DIFFICILE - 50% plus rapide
                this.botMoveDelay = 100; // 50% plus rapide
                System.out.println("⚡ Bot " + name + " - Vitesse DIFFICILE (+50% plus rapide)");
                break;
            default:
                this.botMoveDelay = 150;
        }
    }

    /**
     * ✅ MÉTHODE PRINCIPALE MODIFIÉE: Le bot devient un chasseur agressif
     */
    public BotAction decideAction(Game game) {
        if (isEliminated()) {
            return BotAction.NONE;
        }

        long currentTime = System.currentTimeMillis();

        // Limiter la fréquence selon la difficulté
        if (currentTime - lastMoveTime < botMoveDelay) {
            return BotAction.NONE;
        }

        // 1. PRIORITÉ ABSOLUE: Fuir TOUTES les explosions mortelles
        BotAction escapeAction = tryEscapeFromDanger(game);
        if (escapeAction != BotAction.NONE) {
            lastMoveTime = currentTime;
            System.out.println("🏃‍♂️ " + getName() + " fuit le danger !");
            return escapeAction;
        }

        // 2. NOUVEAU: Analyser la position du joueur et adapter le mode de chasse
        updateHuntingStrategy(game);

        // 3. NOUVEAU: Attaque agressive si le joueur est proche
        BotAction aggressiveAction = tryAggressiveAttack(game, currentTime);
        if (aggressiveAction != BotAction.NONE) {
            lastMoveTime = currentTime;
            if (aggressiveAction == BotAction.PLACE_BOMB) {
                lastBombTime = currentTime;
                System.out.println("💀 " + getName() + " attaque agressivement !");
            }
            return aggressiveAction;
        }

        // 4. NOUVEAU: Poursuite active du joueur
        BotAction huntAction = tryHuntPlayer(game);
        if (huntAction != BotAction.NONE) {
            lastMoveTime = currentTime;
            System.out.println("👻 " + getName() + " poursuit le joueur !");
            return huntAction;
        }

        // 5. Fallback: Comportement standard mais orienté destruction
        BotAction standardAction = findAggressiveMove(game);
        if (standardAction != BotAction.NONE) {
            lastMoveTime = currentTime;
        }

        return standardAction;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Analyse la situation et adapte la stratégie de chasse
     */
    private void updateHuntingStrategy(Game game) {
        Position playerPos = findNearestPlayer(game);
        if (playerPos == null) return;

        int distance = getDistanceTo(playerPos);

        // Mettre à jour la dernière position connue du joueur
        lastPlayerPosition = playerPos;
        lastPlayerSeen = System.currentTimeMillis();

        // Déterminer le mode de chasse selon la distance et la difficulté
        if (distance <= 2) {
            huntingMode = 2; // Mode piégeage - très agressif
            playerInRange = true;
            System.out.println("🎯 " + getName() + " en mode PIÉGEAGE !");
        } else if (distance <= 5) {
            huntingMode = 1; // Mode poursuite active
            playerInRange = true;
            System.out.println("👀 " + getName() + " en mode POURSUITE !");
        } else {
            huntingMode = 0; // Mode recherche
            playerInRange = false;
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Attaque agressive quand le joueur est proche
     */
    private BotAction tryAggressiveAttack(Game game, long currentTime) {
        Position playerPos = findNearestPlayer(game);
        if (playerPos == null) return BotAction.NONE;

        Position myPos = getPosition();
        int distance = getDistanceTo(playerPos);

        // STRATÉGIE 1: Poser une bombe pour détruire les obstacles qui bloquent le chemin vers le joueur
        if (canPlaceBomb() && currentTime - lastBombTime >= BOT_BOMB_COOLDOWN) {
            // Vérifier s'il y a des briques qui bloquent le chemin direct vers le joueur
            if (shouldBombToReachPlayer(myPos, playerPos, game)) {
                System.out.println("💥 " + getName() + " détruit les obstacles pour atteindre le joueur !");
                return BotAction.PLACE_BOMB;
            }

            // STRATÉGIE 2: Piéger le joueur s'il est à portée directe
            if (distance <= 2 && canEscapeFromOwnBomb(myPos, game) && willTrapPlayer(myPos, playerPos, game)) {
                System.out.println("💣 " + getName() + " piège le joueur à distance " + distance + " !");
                return BotAction.PLACE_BOMB;
            }

            // STRATÉGIE 3: Bombardement agressif si le joueur est proche (selon difficulté)
            if (distance <= 3 && shouldBombAggressively(distance)) {
                if (canEscapeFromOwnBomb(myPos, game)) {
                    System.out.println("🔥 " + getName() + " bombarde agressivement !");
                    return BotAction.PLACE_BOMB;
                }
            }
        }

        // STRATÉGIE 4: Se positionner pour un piège optimal
        BotAction trapMove = findOptimalTrapPosition(game, playerPos);
        if (trapMove != BotAction.NONE) {
            return trapMove;
        }

        return BotAction.NONE;
    }

    /**
     * ✅ MÉTHODE AMÉLIORÉE: Bombardement intelligent qui évite les culs-de-sac
     */
    private boolean shouldBombToReachPlayer(Position myPos, Position playerPos, Game game) {
        // ✅ PRIORITÉ 1: Vérifier qu'on peut survivre à notre propre bombe
        if (!canEscapeFromOwnBomb(myPos, game)) {
            System.out.println("🚫 " + getName() + " : cul-de-sac détecté, pas de bombe !");
            return false;
        }



        // Vérifier s'il y a des briques destructibles dans le chemin direct
        List<Position> bricksInPath = findBricksInDirectPath(myPos, playerPos, game);

        if (!bricksInPath.isEmpty()) {
            // Il y a des briques qui bloquent le chemin
            Position nearestBrick = bricksInPath.get(0); // La première brique dans le chemin

            // Vérifier si on peut détruire cette brique avec notre bombe
            if (isInExplosionRange(myPos, nearestBrick)) {
                System.out.println("🧱 " + getName() + " va détruire une brique qui bloque le chemin vers le joueur !");
                return true;
            }

            // Si on n'est pas assez proche, vérifier s'il y a des briques à proximité immédiate
            return hasBricksAroundPosition(myPos, game);
        }

        // Pas de briques dans le chemin direct, mais vérifier quand même les briques proches
        // pour créer des ouvertures stratégiques
        if (getDistanceTo(playerPos) <= 4) {
            return hasBricksAroundPosition(myPos, game);
        }

        return false;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Trouve les briques destructibles dans le chemin direct vers le joueur
     */
    private List<Position> findBricksInDirectPath(Position start, Position target, Game game) {
        List<Position> bricks = new ArrayList<>();

        int dx = target.getX() - start.getX();
        int dy = target.getY() - start.getY();

        // Chemin horizontal puis vertical (L-shaped path)
        // D'abord horizontal
        int stepX = Integer.compare(dx, 0);
        for (int i = 1; i <= Math.abs(dx); i++) {
            Position pos = new Position(start.getX() + (stepX * i), start.getY());
            if (isBrickAt(pos, game)) {
                bricks.add(pos);
            }
        }

        // Puis vertical
        int stepY = Integer.compare(dy, 0);
        for (int i = 1; i <= Math.abs(dy); i++) {
            Position pos = new Position(target.getX(), start.getY() + (stepY * i));
            if (isBrickAt(pos, game)) {
                bricks.add(pos);
            }
        }

        return bricks;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Vérifie s'il y a une brique à une position donnée
     */
    private boolean isBrickAt(Position pos, Game game) {
        try {
            if (!game.getBoard().isWithinBounds(pos)) {
                return false;
            }
            Cell cell = game.getBoard().getCell(pos);
            return cell.getType() == CellType.DESTRUCTIBLE_BRICK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Vérifie s'il y a des briques destructibles autour de notre position
     */
    private boolean hasBricksAroundPosition(Position pos, Game game) {
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            for (int range = 1; range <= 2; range++) { // Portée de la bombe
                Position checkPos = new Position(pos.getX() + (dir[0] * range), pos.getY() + (dir[1] * range));
                if (isBrickAt(checkPos, game)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Détermine si on doit bombarder agressivement selon la difficulté
     */
    private boolean shouldBombAggressively(int distanceToPlayer) {
        int aggressiveness = 0;

        switch (difficulty) {
            case 1: // Facile - 30% de chance
                aggressiveness = 30;
                break;
            case 2: // Moyen - 60% de chance
                aggressiveness = 60;
                break;
            case 3: // Difficile - 85% de chance
                aggressiveness = 85;
                break;
        }

        // Plus le joueur est proche, plus on veut bombarder
        if (distanceToPlayer <= 1) {
            aggressiveness += 20;
        } else if (distanceToPlayer <= 2) {
            aggressiveness += 10;
        }

        return random.nextInt(100) < aggressiveness;
    }
    private boolean willTrapPlayer(Position bombPos, Position playerPos, Game game) {
        // Simuler l'explosion de notre bombe
        List<Position> explosionZones = simulateExplosionZones(bombPos, game);

        // Vérifier si le joueur sera dans la zone d'explosion
        if (!explosionZones.contains(playerPos)) {
            return false;
        }

        // Vérifier si le joueur a des moyens de s'échapper facilement
        int playerEscapeRoutes = countEscapeRoutes(playerPos, explosionZones, game);

        // Plus la difficulté est élevée, plus on accepte des pièges risqués
        int maxEscapeRoutes = (difficulty == 1) ? 2 : (difficulty == 2) ? 1 : 0;
        return playerEscapeRoutes <= maxEscapeRoutes;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Compte les routes d'évasion disponibles pour une position
     */
    private int countEscapeRoutes(Position pos, List<Position> dangerZones, Game game) {
        BotAction[] moves = {BotAction.MOVE_UP, BotAction.MOVE_DOWN, BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};
        int escapeRoutes = 0;

        for (BotAction move : moves) {
            Position newPos = getNewPosition(pos, move);

            if (isSafePosition(newPos, game) && !dangerZones.contains(newPos)) {
                escapeRoutes++;
            }
        }

        return escapeRoutes;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Trouve la position optimale pour piéger le joueur
     */
    private BotAction findOptimalTrapPosition(Game game, Position playerPos) {
        Position myPos = getPosition();
        BotAction[] moves = {BotAction.MOVE_UP, BotAction.MOVE_DOWN, BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};

        List<BotAction> trapMoves = new ArrayList<>();

        for (BotAction move : moves) {
            Position newPos = getNewPosition(myPos, move);

            if (!isSafePosition(newPos, game)) continue;

            // Vérifier si cette position nous rapproche du joueur pour un piège
            int currentDistance = getDistanceTo(playerPos);
            int newDistance = getDistanceTo(newPos, playerPos);

            // Préférer les positions qui nous mettent à distance optimale (1-2 cases)
            if (newDistance >= 1 && newDistance <= 2 && newDistance <= currentDistance) {
                // Vérifier si on pourrait piéger depuis cette position
                if (wouldBeTrapPosition(newPos, playerPos, game)) {
                    trapMoves.add(move);
                }
            }
        }

        if (!trapMoves.isEmpty()) {
            return trapMoves.get(random.nextInt(trapMoves.size()));
        }

        return BotAction.NONE;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Vérifie si une position serait idéale pour piéger
     */
    private boolean wouldBeTrapPosition(Position trapPos, Position playerPos, Game game) {
        // Vérifier si on peut poser une bombe depuis cette position
        List<Position> explosionZones = simulateExplosionZones(trapPos, game);

        // Le joueur doit être dans la zone d'explosion potentielle
        if (!explosionZones.contains(playerPos)) {
            return false;
        }

        // On doit pouvoir s'échapper nous-mêmes
        return canEscapeFromOwnBomb(trapPos, game);
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Poursuite active du joueur comme un fantôme
     */
    private BotAction tryHuntPlayer(Game game) {
        Position playerPos = findNearestPlayer(game);
        if (playerPos == null) return BotAction.NONE;



        // ALGORITHME DE POURSUITE DIRECTE (style fantôme Pac-Man)
        BotAction directMove = findDirectPathToPlayer(game, playerPos);
        if (directMove != BotAction.NONE) {
            return directMove;
        }

        // ALGORITHME DE CONTOURNEMENT si le chemin direct est bloqué
        BotAction flankMove = findFlankingMove(game, playerPos);
        if (flankMove != BotAction.NONE) {
            return flankMove;
        }

        // PRÉDICTION: Essayer d'anticiper où va le joueur
        BotAction predictiveMove = findPredictiveMove(game, playerPos);
        if (predictiveMove != BotAction.NONE) {
            return predictiveMove;
        }

        return BotAction.NONE;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Chemin direct vers le joueur (priorité absolue)
     */
    private BotAction findDirectPathToPlayer(Game game, Position playerPos) {
        Position myPos = getPosition();

        int dx = playerPos.getX() - myPos.getX();
        int dy = playerPos.getY() - myPos.getY();

        List<BotAction> directMoves = new ArrayList<>();

        // Prioriser le mouvement qui réduit le plus la distance
        if (Math.abs(dx) > Math.abs(dy)) {
            // Distance horizontale plus grande
            if (dx > 0) directMoves.add(BotAction.MOVE_RIGHT);
            if (dx < 0) directMoves.add(BotAction.MOVE_LEFT);
            if (dy > 0) directMoves.add(BotAction.MOVE_DOWN);
            if (dy < 0) directMoves.add(BotAction.MOVE_UP);
        } else {
            // Distance verticale plus grande ou égale
            if (dy > 0) directMoves.add(BotAction.MOVE_DOWN);
            if (dy < 0) directMoves.add(BotAction.MOVE_UP);
            if (dx > 0) directMoves.add(BotAction.MOVE_RIGHT);
            if (dx < 0) directMoves.add(BotAction.MOVE_LEFT);
        }

        // Tester les mouvements directs par ordre de priorité
        for (BotAction move : directMoves) {
            Position newPos = getNewPosition(myPos, move);
            if (isSafePosition(newPos, game) && evaluateDangerLevel(newPos, game) <= 1) {
                return move;
            }
        }

        return BotAction.NONE;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Mouvement de contournement quand le chemin direct est bloqué
     */
    private BotAction findFlankingMove(Game game, Position playerPos) {
        Position myPos = getPosition();
        BotAction[] allMoves = {BotAction.MOVE_UP, BotAction.MOVE_DOWN, BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};

        List<BotAction> validMoves = new ArrayList<>();

        for (BotAction move : allMoves) {
            Position newPos = getNewPosition(myPos, move);

            if (isSafePosition(newPos, game) && evaluateDangerLevel(newPos, game) <= 2) {
                // Vérifier si ce mouvement nous rapproche globalement du joueur
                int currentDistance = getDistanceTo(playerPos);
                int newDistance = getDistanceTo(newPos, playerPos);

                if (newDistance <= currentDistance + 1) { // Accepter même si on ne se rapproche pas beaucoup
                    validMoves.add(move);
                }
            }
        }

        if (!validMoves.isEmpty()) {
            return validMoves.get(random.nextInt(validMoves.size()));
        }

        return BotAction.NONE;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Mouvement prédictif pour anticiper le joueur
     */
    private BotAction findPredictiveMove(Game game, Position playerPos) {
        if (lastPlayerPosition == null) {
            return BotAction.NONE;
        }

        // Calculer la direction probable du joueur
        int playerDx = playerPos.getX() - lastPlayerPosition.getX();
        int playerDy = playerPos.getY() - lastPlayerPosition.getY();

        // Prédire la prochaine position du joueur
        Position predictedPos = new Position(
                playerPos.getX() + playerDx,
                playerPos.getY() + playerDy
        );

        // Essayer de se diriger vers la position prédite
        Position myPos = getPosition();
        int dx = predictedPos.getX() - myPos.getX();
        int dy = predictedPos.getY() - myPos.getY();

        BotAction predictiveMove = null;

        if (Math.abs(dx) > Math.abs(dy)) {
            predictiveMove = (dx > 0) ? BotAction.MOVE_RIGHT : BotAction.MOVE_LEFT;
        } else if (dy != 0) {
            predictiveMove = (dy > 0) ? BotAction.MOVE_DOWN : BotAction.MOVE_UP;
        }

        if (predictiveMove != null) {
            Position newPos = getNewPosition(myPos, predictiveMove);
            if (isSafePosition(newPos, game) && evaluateDangerLevel(newPos, game) <= 2) {
                System.out.println("🔮 " + getName() + " anticipe le mouvement du joueur !");
                return predictiveMove;
            }
        }

        return BotAction.NONE;
    }

    /**
     * ✅ MÉTHODE MODIFIÉE: Mouvement agressif orienté destruction ET chasse
     */
    private BotAction findAggressiveMove(Game game) {
        Position myPos = getPosition();
        Position playerPos = findNearestPlayer(game);

        // NOUVELLE PRIORITÉ: Si on peut poser une bombe pour aider la progression, le faire !
        if (canPlaceBomb() && System.currentTimeMillis() - lastBombTime >= BOT_BOMB_COOLDOWN) {
            // Vérifier si on a des briques à proximité qui nous empêchent de progresser
            if (playerPos != null && shouldBombToReachPlayer(myPos, playerPos, game)) {
                if (canEscapeFromOwnBomb(myPos, game)) {
                    System.out.println("🧨 " + getName() + " pose une bombe pour progresser vers le joueur !");
                    return BotAction.PLACE_BOMB;
                }
            }

            // Sinon, bombarder s'il y a des briques autour selon la difficulté
            if (hasBricksAroundPosition(myPos, game) && random.nextInt(100) < (difficulty * 25)) {
                if (canEscapeFromOwnBomb(myPos, game)) {
                    System.out.println("💥 " + getName() + " détruit des obstacles !");
                    return BotAction.PLACE_BOMB;
                }
            }
        }

        // 80% du temps : chercher à se rapprocher du joueur
        if (random.nextInt(100) < 80) {
            if (playerPos != null) {
                BotAction directMove = findDirectPathToPlayer(game, playerPos);
                if (directMove != BotAction.NONE) {
                    return directMove;
                }
            }
        }

        // 20% du temps : détruire des briques pour créer des passages
        Position brickTarget = findNearestBrick(game);

        if (brickTarget != null) {
            int dx = brickTarget.getX() - myPos.getX();
            int dy = brickTarget.getY() - myPos.getY();

            List<BotAction> possibleMoves = new ArrayList<>();

            if (dx > 0) possibleMoves.add(BotAction.MOVE_RIGHT);
            if (dx < 0) possibleMoves.add(BotAction.MOVE_LEFT);
            if (dy > 0) possibleMoves.add(BotAction.MOVE_DOWN);
            if (dy < 0) possibleMoves.add(BotAction.MOVE_UP);

            // Filtrer les mouvements sûrs
            List<BotAction> safeMoves = new ArrayList<>();
            for (BotAction move : possibleMoves) {
                Position newPos = getNewPosition(myPos, move);
                if (isSafePosition(newPos, game) && evaluateDangerLevel(newPos, game) <= 1) {
                    safeMoves.add(move);
                }
            }

            if (!safeMoves.isEmpty()) {
                return safeMoves.get(random.nextInt(safeMoves.size()));
            }
        }

        return getRandomMove(game);
    }

    // ================================================================
    // MÉTHODES UTILITAIRES EXISTANTES (gardées identiques)
    // ================================================================

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

        // Vérifier TOUTES les bombes (y compris les siennes)
        for (Bomb bomb : game.getActiveBombs()) {
            if (isInExplosionRange(myPos, bomb.getPosition())) {
                inDanger = true;
                break;
            }
        }

        if (!inDanger) {
            return BotAction.NONE;
        }

        return findSafestMove(game);
    }

    private boolean canEscapeFromOwnBomb(Position bombPos, Game game) {
        List<Position> explosionZones = simulateExplosionZones(bombPos, game);
        BotAction[] moves = {BotAction.MOVE_UP, BotAction.MOVE_DOWN, BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};

        for (BotAction move : moves) {
            Position escapePos = getNewPosition(bombPos, move);
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

    private List<Position> simulateExplosionZones(Position bombPos, Game game) {
        List<Position> zones = new ArrayList<>();
        zones.add(bombPos);

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
                        break;
                    }
                    zones.add(pos);
                } catch (Exception e) {
                    break;
                }
            }
        }

        return zones;
    }

    private BotAction findSafestMove(Game game) {
        Position myPos = getPosition();
        BotAction[] moves = {BotAction.MOVE_UP, BotAction.MOVE_DOWN, BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};

        List<BotAction> safeMoves = new ArrayList<>();
        List<BotAction> lessUnsafeMoves = new ArrayList<>();

        for (BotAction move : moves) {
            Position newPos = getNewPosition(myPos, move);

            if (!isSafePosition(newPos, game)) {
                continue;
            }

            int dangerLevel = evaluateDangerLevel(newPos, game);

            if (dangerLevel == 0) {
                safeMoves.add(move);
            } else if (dangerLevel <= 2) {
                lessUnsafeMoves.add(move);
            }
        }

        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        if (!lessUnsafeMoves.isEmpty()) {
            return lessUnsafeMoves.get(random.nextInt(lessUnsafeMoves.size()));
        }

        for (BotAction move : moves) {
            Position newPos = getNewPosition(myPos, move);
            if (isSafePosition(newPos, game)) {
                return move;
            }
        }

        return BotAction.NONE;
    }

    private int evaluateDangerLevel(Position pos, Game game) {
        int danger = 0;

        for (Bomb bomb : game.getActiveBombs()) {
            if (isInExplosionRange(pos, bomb.getPosition())) {
                danger += 3;
            } else if (getDistanceTo(pos, bomb.getPosition()) <= 3) {
                danger += 1;
            }
        }

        for (Explosion explosion : game.getActiveExplosions()) {
            if (explosion.affectsPosition(pos)) {
                danger += 5;
            }
        }

        return danger;
    }

    private int getDistanceTo(Position pos1, Position pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY());
    }

    private int getDistanceTo(Position target) {
        return getDistanceTo(getPosition(), target);
    }

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




    public int getDifficulty() {
        return difficulty;
    }
}