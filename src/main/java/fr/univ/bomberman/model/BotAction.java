package fr.univ.bomberman.model;

/**
 * Actions possibles pour un bot
 */
public enum BotAction {
    NONE,           // Aucune action
    MOVE_UP,        // Se déplacer vers le haut
    MOVE_DOWN,      // Se déplacer vers le bas
    MOVE_LEFT,      // Se déplacer vers la gauche
    MOVE_RIGHT,     // Se déplacer vers la droite
    PLACE_BOMB      // Poser une bombe
}