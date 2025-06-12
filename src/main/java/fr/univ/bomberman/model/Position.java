// FILE: src/main/java/fr/univ/bomberman/model/Position.java
package fr.univ.bomberman.model;

import java.util.Objects;

/**
 * Représente une position (coordonnées x,y) sur le plateau.
 * Cette classe est utilisée pour localiser les éléments du jeu sur le plateau.
 */
public class Position {
    private int x;
    private int y;

    /**
     * Constructeur d'une position avec des coordonnées spécifiques.
     * @param x l'abscisse (coordonnée horizontale)
     * @param y l'ordonnée (coordonnée verticale)
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return l'abscisse (x)
     */
    public int getX() {
        return x;
    }

    /**
     * @return l'ordonnée (y)
     */
    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position{" + "x=" + x + ", y=" + y + '}';
    }
}
