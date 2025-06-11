package fr.univ.bomberman.model;

/**
 * Représente une cellule du plateau avec une position et un type.
 */
public class Cell {
    private Position position;
    private CellType type;

    public Cell(Position position, CellType type) {
        this.position = position;
        this.type = type;
    }

    /**
     * @return la position de la cellule
     */
    public Position getPosition() {
        return position;
    }

    /**
     * @return le type actuel de la cellule
     */
    public CellType getType() {
        return type;
    }

    /**
     * Modifie le type de la cellule.
     *
     * @param type nouveau type à affecter
     */
    public void setType(CellType type) {
        this.type = type;
    }
}
