// FILE: src/main/java/fr/univ/bomberman/model/Board.java
package fr.univ.bomberman.model;

import fr.univ.bomberman.exceptions.BombermanException;
import fr.univ.bomberman.utils.JsonUtils;

import java.io.IOException;
import java.util.Random;

import org.json.JSONObject;

/**
 * Représente le plateau de jeu de Super Bomberman.
 * Gère la grille de cellules qui compose le niveau, avec différents types de cellules :
 * - Murs indestructibles (bordures et motifs)
 * - Briques destructibles (peuvent être détruites par les bombes)
 * - Cellules vides (zones de déplacement)
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
     * Si le chargement échoue, un plateau par défaut est généré.
     * Le plateau par défaut contient :
     * - Des murs indestructibles sur les bords
     * - Un motif de murs indestructibles
     * - Des briques destructibles placées aléatoirement
     * - Des zones vides pour les positions de départ des joueurs
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
     * @param pos La position à vérifier
     * @return true si la position est dans les limites du plateau, false sinon
     */
    public boolean isWithinBounds(Position pos) {
        return pos.getX() >= 0 && pos.getX() < cols && pos.getY() >= 0 && pos.getY() < rows;
    }

    /**
     * Récupère la cellule à une position donnée sur le plateau.
     * Vérifie d'abord que la position est valide.
     *
     * @param pos La position de la cellule à récupérer
     * @return La cellule à la position spécifiée
     * @throws BombermanException Si la position est hors des limites du plateau
     */
    public Cell getCell(Position pos) throws BombermanException {
        if (!isWithinBounds(pos)) {
            throw new BombermanException("Position hors plateau : " + pos);
        }
        return cells[pos.getY()][pos.getX()];
    }

    /**
     * Modifie le type d'une cellule à une position donnée.
     * Permet de changer une cellule en mur, brique destructible ou cellule vide.
     *
     * @param pos La position de la cellule à modifier
     * @param type Le nouveau type de cellule à appliquer
     * @throws BombermanException Si la position est hors des limites du plateau
     */
    public void setCellType(Position pos, CellType type) throws BombermanException {
        if (!isWithinBounds(pos)) {
            throw new BombermanException("Position hors plateau : " + pos);
        }
        cells[pos.getY()][pos.getX()].setType(type);
    }

    /**
     * Récupère le nombre de colonnes du plateau.
     *
     * @return Le nombre de colonnes
     */
    public int getCols() {
        return cols;
    }

    /**
     * Récupère le nombre de lignes du plateau.
     *
     * @return Le nombre de lignes
     */
    public int getRows() {
        return rows;
    }
}
