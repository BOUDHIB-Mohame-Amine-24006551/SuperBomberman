package fr.univ.bomberman.model;

public enum CellType {
    EMPTY,
    INDESTRUCTIBLE_WALL,
    DESTRUCTIBLE_BRICK,
    FLAG_PLAYER_1,    // Drapeau du joueur 1
    FLAG_PLAYER_2,    // Drapeau du joueur 2
    FLAG_PLAYER_3,    // Drapeau du joueur 3
    FLAG_PLAYER_4;    // Drapeau du joueur 4

    /**
     * Obtient le type de cellule drapeau pour un joueur donné
     * @param playerIndex index du joueur (0-3)
     * @return le type de cellule correspondant au drapeau de ce joueur
     */
    public static CellType getFlagTypeForPlayer(int playerIndex) {
        switch (playerIndex) {
            case 0: return FLAG_PLAYER_1;
            case 1: return FLAG_PLAYER_2;
            case 2: return FLAG_PLAYER_3;
            case 3: return FLAG_PLAYER_4;
            default: return FLAG_PLAYER_1; // Fallback
        }
    }

    /**
     * Vérifie si ce type de cellule représente un drapeau
     * @return true si c'est un drapeau
     */
    public boolean isFlag() {
        return this == FLAG_PLAYER_1 || this == FLAG_PLAYER_2 ||
                this == FLAG_PLAYER_3 || this == FLAG_PLAYER_4;
    }

    /**
     * Obtient l'index du joueur propriétaire de ce drapeau
     * @return l'index du joueur, ou -1 si ce n'est pas un drapeau
     */
    public int getFlagOwnerIndex() {
        switch (this) {
            case FLAG_PLAYER_1: return 0;
            case FLAG_PLAYER_2: return 1;
            case FLAG_PLAYER_3: return 2;
            case FLAG_PLAYER_4: return 3;
            default: return -1;
        }
    }
}