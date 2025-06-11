// FILE: src/main/java/fr/univ/bomberman/model/Explosion.java
package fr.univ.bomberman.model;

import fr.univ.bomberman.exceptions.BombermanException;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une explosion générée par une bombe.
 * MODIFIÉ: Les explosions ne traversent plus les murs indestructibles
 * NOUVEAU: Explosions mortelles limitées à 2.5 secondes
 */
public class Explosion {
    private List<Position> affectedPositions;
    private Player bombOwner; // référence vers le propriétaire de la bombe

    // ✅ NOUVEAU: Gestion du temps de mort en millisecondes
    private long creationTime;
    private static final long EXPLOSION_DURATION_MS = 1500;


    /**
     * CONSTRUCTEUR MODIFIÉ: Crée une explosion avec référence au propriétaire
     * Les explosions ne traversent plus les murs indestructibles
     *
     * @param center position de la bombe
     * @param board  plateau de jeu
     * @param bombOwner  propriétaire de la bombe
     * @throws BombermanException si modification du plateau échoue
     */
    public Explosion(Position center, Board board, Player bombOwner) throws BombermanException {
        this.affectedPositions = new ArrayList<>();
        this.bombOwner = bombOwner;
        this.creationTime = System.currentTimeMillis(); // ✅ Enregistrer quand l'explosion commence

        int x = center.getX();
        int y = center.getY();

        // Toujours exploser au centre
        addExplosionPosition(center, board);

        // Expansion dans les 4 directions avec portée de 2 cases
        expandInDirection(board, x, y, 1, 0, 2);   // droite
        expandInDirection(board, x, y, -1, 0, 2);  // gauche
        expandInDirection(board, x, y, 0, 1, 2);   // bas
        expandInDirection(board, x, y, 0, -1, 2);  // haut
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Vérifie si l'explosion est encore mortelle
     */
    public boolean isDeadly() {
        long elapsed = System.currentTimeMillis() - creationTime;
        return elapsed <= EXPLOSION_DURATION_MS;
    }


    /**
     * NOUVELLE MÉTHODE: Étend l'explosion dans une direction donnée
     * S'arrête si elle rencontre un mur indestructible
     *
     * @param board plateau de jeu
     * @param startX position X de départ
     * @param startY position Y de départ
     * @param dx direction X (-1, 0, 1)
     * @param dy direction Y (-1, 0, 1)
     * @param range portée de l'explosion dans cette direction
     */
    private void expandInDirection(Board board, int startX, int startY, int dx, int dy, int range) throws BombermanException {
        for (int i = 1; i <= range; i++) {
            int newX = startX + (dx * i);
            int newY = startY + (dy * i);
            Position pos = new Position(newX, newY);

            // Vérifier si la position est dans les limites
            if (!board.isWithinBounds(pos)) {
                break; // Sortie du plateau, arrêter l'expansion
            }

            Cell cell = board.getCell(pos);

            // Si c'est un mur indestructible, ARRÊTER l'expansion dans cette direction
            if (cell.getType() == CellType.INDESTRUCTIBLE_WALL) {
                break; // L'explosion ne peut pas traverser les murs indestructibles
            }

            // Ajouter la position à l'explosion
            addExplosionPosition(pos, board);

            // Si c'est une brique destructible, la détruire mais continuer l'expansion
            if (cell.getType() == CellType.DESTRUCTIBLE_BRICK) {
                board.setCellType(pos, CellType.EMPTY);
                // On peut continuer l'expansion après avoir détruit une brique
            }
        }
    }

    /**
     * NOUVELLE MÉTHODE: Ajoute une position à l'explosion en vérifiant les conditions
     *
     * @param pos position à ajouter
     * @param board plateau de jeu
     */
    private void addExplosionPosition(Position pos, Board board) {
        if (board.isWithinBounds(pos)) {
            affectedPositions.add(pos);
        }
    }

    /**
     * @return la liste des positions affectées par l'explosion
     */
    public List<Position> getAffectedPositions() {
        return new ArrayList<>(affectedPositions);
    }

    /**
     * @return le propriétaire de la bombe qui a causé cette explosion
     */
    public Player getBombOwner() {
        return bombOwner;
    }



    /**
     * MÉTHODE MODIFIÉE: L'explosion reste visible plus longtemps mais n'est mortelle que 2.5s
     */
    public boolean isFinished() {
        long elapsed = System.currentTimeMillis() - creationTime;
        return elapsed > EXPLOSION_DURATION_MS;
    }

    /**
     * Indique si une position est affectée par cette explosion.
     *
     * @param pos position à tester
     * @return true si affecté, false sinon
     */
    public boolean affectsPosition(Position pos) {
        return affectedPositions.contains(pos);
    }




}