// FILE: src/main/java/fr/univ/bomberman/model/Player.java
package fr.univ.bomberman.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un joueur dans la partie.
 */
public class Player {
    private String name;
    private Position position;
    private int remainingBombs;
    private boolean eliminated;
    private boolean hasFlag;
    private long lastBombTime = 0;
    private static final long BOMB_COOLDOWN = 2_000_000_000L; // 1 seconde en nanosecondes
    private static final long BOMB_COOLDOWN_MS = 2000;

    public Player(String name, Position position) {
        this.name = name;
        this.position = position;
        this.remainingBombs = 1;
        this.eliminated = false;
        this.hasFlag = false;
    }

    /**
     * @return le nom du joueur
     */
    public String getName() {
        return name;
    }

    /**
     * @return la position actuelle du joueur
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Définit une nouvelle position pour le joueur.
     *
     * @param position nouvelle position
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * @return le nombre de bombes restantes
     */
    public int getRemainingBombs() {
        return remainingBombs;
    }

    /**
     * Décrémente le nombre de bombes disponibles.
     */
    public void decrementBombCount() {
        if (remainingBombs > 0) {
            remainingBombs--;
        }
    }

    /**
     * @return true si le joueur est éliminé
     */
    public boolean isEliminated() {
        return eliminated;
    }

    /**
     * Marque le joueur comme éliminé ou non.
     *
     * @param eliminated état d'élimination
     */
    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    /**
     * @return true si le joueur a un drapeau (mode capture the flag)
     */
    public boolean hasFlag() {
        return hasFlag;
    }

    /**
     * Définit l'état de possession du drapeau.
     *
     * @param hasFlag true si le joueur porte le drapeau
     */
    public void setHasFlag(boolean hasFlag) {
        this.hasFlag = hasFlag;
    }
    /**
     * Définit le nom du joueur
     *
     * @param name nouveau nom du joueur
     */
    public void setName(String name) {
        this.name = name;
    }

    public boolean canPlaceBomb() {
        long currentTime = System.nanoTime();
        return (currentTime - lastBombTime) >= BOMB_COOLDOWN;
    }

    public void bombPlaced() {
        lastBombTime = System.nanoTime();
    }
    public long getRemainingCooldown() {
        long currentTime = System.nanoTime();
        long elapsed = currentTime - lastBombTime;
        long remaining = BOMB_COOLDOWN - elapsed;

        if (remaining <= 0) {
            return 0;
        }

        return remaining / 1_000_000; // Convertir en millisecondes
    }

    public int getCooldownPercentage() {
        long remaining = getRemainingCooldown();
        if (remaining <= 0) {
            return 0;
        }

        return (int) ((remaining * 100) / BOMB_COOLDOWN_MS);
    }

    public boolean isOnBombCooldown() {
        return getRemainingCooldown() > 0;
    }

    public int getX() {
        return position.getX();
    }

    /**
     * Retourne la coordonnée Y du joueur
     * @return position Y
     */
    public int getY() {
        return position.getY();
    }

    /**
     * Reset le cooldown des bombes (pour les redémarrages)
     */
    public void resetBombCooldown() {
        lastBombTime = 0;
    }
}
