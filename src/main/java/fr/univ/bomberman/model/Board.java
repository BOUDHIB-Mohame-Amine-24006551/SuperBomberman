// FILE: src/main/java/fr/univ/bomberman/model/Board.java
package fr.univ.bomberman.model;

import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.utils.JsonUtils;

import java.io.IOException;
import java.util.Random;

import org.json.JSONObject;

/**
 * Représente le plateau de jeu composé de cellules (murs indestructibles, briques destructibles, vides).
 */
public class Board {

    private int cols;
    private int rows;
    private Cell[][] cells;
    private String levelPath; // Chemin du fichier de niveau

    /**
     * Crée un plateau de dimensions spécifiées et génère aléatoirement les briques destructibles.
     *
     * @param cols nombre de colonnes
     * @param rows nombre de lignes
     */
    public Board(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.cells = new Cell[rows][cols];
        this.levelPath = "src/main/resources/fr/univ/bomberman/level/default/level.json";
        initializeBoard();
    }

    /**
     * Crée un plateau à partir d'un fichier de niveau spécifique.
     *
     * @param levelPath chemin vers le fichier de niveau
     * @throws BombermanException si le chargement du niveau échoue
     */
    public Board(String levelPath) throws BombermanException {
        this.levelPath = levelPath;
        try {
            JSONObject levelData = JsonUtils.readLevelFile(levelPath);
            this.cols = levelData.getInt("width");
            this.rows = levelData.getInt("height");
            this.cells = new Cell[rows][cols];
            initializeBoard();
        } catch (IOException e) {
            throw new BombermanException("Impossible de charger le niveau: " + e.getMessage());
        }
    }

    /**
     * Initialise le plateau en chargeant le niveau depuis le fichier JSON.
     */
    private void initializeBoard() {
        try {
            JSONObject levelData = JsonUtils.readLevelFile(levelPath);
            this.cells = JsonUtils.parseLevelGrid(levelData);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du niveau, utilisation du niveau par défaut: " + e.getMessage());
            initializeDefaultBoard();
        }
    }

    /**
     * Initialise un plateau par défaut si le chargement du niveau échoue.
     */
    private void initializeDefaultBoard() {
        // Exemple simple : bordures indestructibles, motifs de murs, et briques aléatoires à l'intérieur
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (y == 0 || y == rows - 1 || x == 0 || x == cols - 1) {
                    cells[y][x] = new Cell(new Position(x, y), CellType.INDESTRUCTIBLE_WALL);
                } else if (y % 2 == 0 && x % 2 == 0) {
                    cells[y][x] = new Cell(new Position(x, y), CellType.INDESTRUCTIBLE_WALL);
                } else {
                    // Génération aléatoire de briques détruisibles (50% de chance)
                    if (new Random().nextBoolean()) {
                        cells[y][x] = new Cell(new Position(x, y), CellType.DESTRUCTIBLE_BRICK);
                    } else {
                        cells[y][x] = new Cell(new Position(x, y), CellType.EMPTY);
                    }
                }
            }
        }
        // S'assurer que les positions de départ des joueurs sont vides
        cells[1][1].setType(CellType.EMPTY);
        cells[1][2].setType(CellType.EMPTY);
        cells[2][1].setType(CellType.EMPTY);

        int px = cols - 2;
        int py = rows - 2;
        cells[py][px].setType(CellType.EMPTY);
        cells[py][px - 1].setType(CellType.EMPTY);
        cells[py - 1][px].setType(CellType.EMPTY);
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
