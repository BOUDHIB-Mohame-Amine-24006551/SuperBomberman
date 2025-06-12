// FILE: src/main/java/fr/univ/bomberman/model/Player.java
package fr.univ.bomberman.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un joueur dans la partie.
 * Un joueur peut se déplacer, poser des bombes et interagir avec les éléments du jeu.
 */
public class Player {
    private String name;
    private Position position;
    private int remainingBombs;
    private boolean eliminated;
    private boolean hasFlag;
    private long lastBombTime = 0;
    private static final long BOMB_COOLDOWN = 2_000_000_000L; // 2 secondes en nanosecondes
    private static final long BOMB_COOLDOWN_MS = 2000;

    // Nouvelles propriétés pour le mode CTF
    private List<String> capturedFlags; // Drapeaux capturés par ce joueur
    private boolean canPlaceBombWhenEliminated; // Peut poser des bombes même éliminé en mode CTF

    /**
     * Constructeur d'un joueur avec un nom et une position initiale.
     * @param name le nom du joueur
     * @param position la position initiale sur le plateau
     */
    public Player(String name, Position position) {
        this.name = name;
        this.position = position;
        this.remainingBombs = 1;
        this.eliminated = false;
        this.hasFlag = false;
        this.capturedFlags = new ArrayList<>();
        this.canPlaceBombWhenEliminated = false;
    }

    /**
     * Retourne le nom du joueur.
     * @return le nom du joueur
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne la position actuelle du joueur sur le plateau.
     * @return la position actuelle
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Définit une nouvelle position pour le joueur.
     * @param position nouvelle position sur le plateau
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Retourne le nombre de bombes que le joueur peut encore poser.
     * @return le nombre de bombes restantes
     */
    public int getRemainingBombs() {
        return remainingBombs;
    }

    /**
     * Vérifie si le joueur est éliminé de la partie.
     * @return true si le joueur est éliminé, false sinon
     */
    public boolean isEliminated() {
        return eliminated;
    }

    /**
     * Marque le joueur comme éliminé ou non.
     * @param eliminated true pour éliminer le joueur, false sinon
     */
    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
    
    /**
     * Définit le nom du joueur
     *
     * @param name nouveau nom du joueur
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Vérifie si le joueur peut poser une bombe en fonction du temps écoulé depuis la dernière.
     * @return true si le joueur peut poser une bombe, false sinon
     */
    public boolean canPlaceBomb() {
        long currentTime = System.nanoTime();
        boolean cooldownReady = (currentTime - lastBombTime) >= BOMB_COOLDOWN;

        // En mode CTF, les joueurs éliminés peuvent poser des bombes
        return cooldownReady && (!eliminated || canPlaceBombWhenEliminated);
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

    // === NOUVELLES MÉTHODES POUR LE MODE CTF ===

    /**
     * Ajoute un drapeau capturé à la liste
     */
    public void addCapturedFlag(String flagOwnerId) {
        if (!capturedFlags.contains(flagOwnerId)) {
            capturedFlags.add(flagOwnerId);
        }
    }

    /**
     * Retire un drapeau de la liste des drapeaux capturés
     */
    public void removeCapturedFlag(String flagOwnerId) {
        capturedFlags.remove(flagOwnerId);
    }

    /**
     * Vide la liste des drapeaux capturés
     */
    public void clearCapturedFlags() {
        capturedFlags.clear();
    }

    /**
     * @return le nombre de drapeaux capturés
     */
    public int getCapturedFlagsCount() {
        return capturedFlags.size();
    }

    /**
     * @return true si le joueur peut poser des bombes même quand éliminé (mode CTF)
     */
    public boolean canPlaceBombWhenEliminated() {
        return canPlaceBombWhenEliminated;
    }

    /**
     * Définit si le joueur peut poser des bombes même quand éliminé
     */
    public void setCanPlaceBombWhenEliminated(boolean canPlaceBombWhenEliminated) {
        this.canPlaceBombWhenEliminated = canPlaceBombWhenEliminated;
    }






}