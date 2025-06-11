package fr.univ.bomberman.model;

/**
 * Représente un drapeau dans le mode Capture the Flag
 */
public class Flag {
    private Position homePosition;    // Position initiale du drapeau
    private Position currentPosition; // Position actuelle du drapeau
    private Player owner;            // Joueur propriétaire du drapeau
    private Player carrier;          // Joueur qui porte actuellement le drapeau (null si au sol)
    private boolean captured;        // True si le drapeau a été capturé par un adversaire
    private String flagId;          // Identifiant unique du drapeau

    public Flag(Player owner, Position homePosition) {
        this.owner = owner;
        this.homePosition = homePosition;
        this.currentPosition = homePosition;
        this.carrier = null;
        this.captured = false;
        this.flagId = "FLAG_" + owner.getName();
    }

    /**
     * @return la position initiale du drapeau
     */
    public Position getHomePosition() {
        return homePosition;
    }

    /**
     * @return la position actuelle du drapeau
     */
    public Position getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Définit la position actuelle du drapeau
     */
    public void setCurrentPosition(Position position) {
        this.currentPosition = position;
    }

    /**
     * @return le joueur propriétaire du drapeau
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * @return le joueur qui porte actuellement le drapeau (null si au sol)
     */
    public Player getCarrier() {
        return carrier;
    }

    /**
     * Définit le joueur qui porte le drapeau
     */
    public void setCarrier(Player carrier) {
        this.carrier = carrier;
        if (carrier != null) {
            this.currentPosition = carrier.getPosition();
            this.captured = true;
        }
    }

    /**
     * @return true si le drapeau est porté par un joueur
     */
    public boolean isBeingCarried() {
        return carrier != null;
    }

    /**
     * @return true si le drapeau a été capturé (n'est plus à sa position d'origine)
     */
    public boolean isCaptured() {
        return captured;
    }

    /**
     * @return true si le drapeau est à sa position d'origine
     */
    public boolean isAtHome() {
        return currentPosition.equals(homePosition) && !isBeingCarried();
    }

    /**
     * Remet le drapeau à sa position d'origine
     */
    public void returnHome() {
        this.currentPosition = homePosition;
        this.carrier = null;
        this.captured = false;
        System.out.println("🏠 Le drapeau de " + owner.getName() + " est retourné à sa base !");
    }

    /**
     * Fait tomber le drapeau à la position actuelle du porteur
     */
    public void drop() {
        if (carrier != null) {
            this.currentPosition = carrier.getPosition();
            System.out.println("📉 " + carrier.getName() + " a fait tomber le drapeau de " + owner.getName() + " !");
            this.carrier = null;
        }
    }

    /**
     * @return l'identifiant unique du drapeau
     */
    public String getFlagId() {
        return flagId;
    }

    /**
     * Vérifie si un joueur peut ramasser ce drapeau
     */
    public boolean canBePickedUpBy(Player player) {
        // Un joueur ne peut pas ramasser son propre drapeau
        // Le drapeau ne doit pas être déjà porté
        // Le joueur ne doit pas être éliminé
        return !player.equals(owner) &&
                !isBeingCarried() &&
                !player.isEliminated();
    }

    /**
     * Fait ramasser le drapeau par un joueur
     */
    public boolean pickUpBy(Player player) {
        if (canBePickedUpBy(player)) {
            setCarrier(player);
            player.addCapturedFlag(flagId);
            System.out.println("🏁 " + player.getName() + " a ramassé le drapeau de " + owner.getName() + " !");
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Flag{" +
                "owner=" + owner.getName() +
                ", homePos=" + homePosition +
                ", currentPos=" + currentPosition +
                ", carrier=" + (carrier != null ? carrier.getName() : "none") +
                ", captured=" + captured +
                '}';
    }
}