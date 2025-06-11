package fr.univ.bomberman.model;

import fr.univ.bomberman.exceptions.BombermanException;

/**
 * Représente une bombe placée par un joueur.
 */
public class Bomb {
    private Position position;
    private Player owner;
    private Board board;
    private int timer; // en "tours" (update)
    private boolean exploded;

    /**
     * Constructeur principal avec Position, Player et Board
     * @param position position de la bombe
     * @param owner propriétaire de la bombe
     * @param board plateau de jeu
     */
    public Bomb(Position position, Player owner, Board board) {
        this.position = position;
        this.owner = owner;
        this.board = board;
        this.timer = 3; // explose après 3 mises à jour
        this.exploded = false;
    }



    /**
     * Définit le plateau de jeu pour cette bombe
     * @param board plateau de jeu
     */
    public void setBoard(Board board) {
        this.board = board;
    }

    /**
     * Retourne la coordonnée X de la bombe
     * @return position X
     */
    public int getX() {
        return position.getX();
    }

    /**
     * Retourne la coordonnée Y de la bombe
     * @return position Y
     */
    public int getY() {
        return position.getY();
    }

    /**
     * @return la position de la bombe
     */
    public Position getPosition() {
        return position;
    }

    /**
     * @return le propriétaire de la bombe
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Décrémente le timer de la bombe et marque comme explosée si timer atteint 0.
     */
    public void updateTimer() {
        if (!exploded) {
            timer--;
            if (timer <= 0) {
                exploded = true;
            }
        }
    }

    /**
     * @return true si la bombe est prête à exploser
     */
    public boolean isExploded() {
        return exploded;
    }

    /**
     * @return le temps restant avant explosion
     */
    public int getTimer() {
        return timer;
    }

    /**
     * Crée une explosion au centre de la bombe et retourne l'objet Explosion.
     * Détruit les briques adjacentes.
     * MODIFIÉ: Passe le propriétaire à l'explosion pour la protection
     *
     * @return Explosion générée
     * @throws BombermanException en cas d'erreur de mise à jour de la grille
     */
    public Explosion explode() throws BombermanException {
        // Génère explosion centrée sur cette bombe avec référence au propriétaire
        Explosion explosion = new Explosion(position, board, owner);

        // Note: On ne restitue pas la bombe au joueur car les bombes sont maintenant illimitées
        return explosion;
    }
}