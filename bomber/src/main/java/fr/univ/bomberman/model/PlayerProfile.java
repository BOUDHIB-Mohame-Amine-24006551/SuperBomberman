package fr.univ.bomberman.model;

/**
 * Représente le profil d'un joueur avec ses statistiques.
 */
public class PlayerProfile {
    private String firstName;
    private String lastName;
    private String avatar;
    private int gamesPlayed;
    private int gamesWon;

    /**
     * Constructeur par défaut
     */
    public PlayerProfile() {
        this.firstName = "";
        this.lastName = "";
        this.avatar = "default";
        this.gamesPlayed = 0;
        this.gamesWon = 0;
    }

    /**
     * Constructeur avec tous les attributs
     *
     * @param firstName   Prénom du joueur
     * @param lastName    Nom de famille du joueur
     * @param avatar      Identifiant de l'avatar du joueur
     * @param gamesPlayed Nombre de parties jouées
     * @param gamesWon    Nombre de parties gagnées
     */
    public PlayerProfile(String firstName, String lastName, String avatar, int gamesPlayed, int gamesWon) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
    }

    // Getters et setters

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    /**
     * Incrémente le nombre de parties jouées
     */
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    /**
     * Incrémente le nombre de parties gagnées
     */
    public void incrementGamesWon() {
        this.gamesWon++;
    }

    /**
     * Retourne le taux de victoire du joueur
     * 
     * @return pourcentage de victoire, ou 0 si aucune partie jouée
     */
    public double getWinRate() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) gamesWon / gamesPlayed * 100.0;
    }

    /**
     * Retourne le nom complet du joueur
     * 
     * @return prénom + nom
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
} 