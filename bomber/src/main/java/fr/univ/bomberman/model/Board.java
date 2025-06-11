// FILE: src/main/java/fr/univ/bomberman/model/Board.java
package fr.univ.bomberman.model;

import fr.univ.bomberman.utils.JsonUtils;
import fr.univ.bomberman.exceptions.BombermanException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Représente le plateau de jeu composé de cellules (murs indestructibles, briques destructibles, vides).
 */
public class Board {
    private static final int EMPTY = 0;
    private static final int WALL = 1;
    private static final int DESTRUCTIBLE = 2;

    private int cols;
    private int rows;
    private Cell[][] cells;

    public Board(String levelFilePath) throws IOException {
        JSONObject levelData = JsonUtils.readLevelFile(levelFilePath);
        this.cols = levelData.getInt("width");
        this.rows = levelData.getInt("height");
        this.cells = JsonUtils.parseLevelGrid(levelData);
    }

    /**
     * Vérifie si une position est à l'intérieur des limites du plateau.
     *
     * @param pos position à vérifier
     * @return true si dans les limites, false sinon
     */
    public boolean isWithinBounds(Position pos) {
        return pos.getX() >= 0 && pos.getX() < cols && pos.getY() >= 0 && pos.getY() < rows;
    }

    /**
     * Renvoie la cellule à la position donnée.
     *
     * @param pos position de la cellule
     * @return la Cell correspondante
     * @throws BombermanException si la position est hors limites
     */
    public Cell getCell(Position pos) throws BombermanException {
        if (!isWithinBounds(pos)) {
            throw new BombermanException("Position hors plateau : " + pos);
        }
        return cells[pos.getY()][pos.getX()];
    }

    /**
     * Modifie le type de la cellule à la position spécifiée.
     *
     * @param pos  position de la cellule
     * @param type nouveau type à affecter
     * @throws BombermanException si la position est hors limites
     */
    public void setCellType(Position pos, CellType type) throws BombermanException {
        if (!isWithinBounds(pos)) {
            throw new BombermanException("Position hors plateau : " + pos);
        }
        cells[pos.getY()][pos.getX()].setType(type);
    }

    /**
     * @return le nombre de colonnes du plateau
     */
    public int getCols() {
        return cols;
    }

    /**
     * @return le nombre de lignes du plateau
     */
    public int getRows() {
        return rows;
    }
}
