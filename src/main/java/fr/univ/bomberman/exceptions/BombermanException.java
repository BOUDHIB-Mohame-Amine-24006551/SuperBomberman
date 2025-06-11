// FILE: src/main/java/fr/univ/bomberman/exceptions/BombermanException.java
package fr.univ.bomberman.exceptions;

/**
 * Exception spécifique aux erreurs du jeu Bomberman.
 */
public class BombermanException extends Exception {

    /**
     * Constructeur de l'exception avec message.
     *
     * @param message le message de l'exception
     */
    public BombermanException(String message) {
        super(message);
    }

    /**
     * Constructeur de l'exception avec message et cause.
     *
     * @param message le message de l'exception
     * @param cause   la cause de l'exception
     */
    public BombermanException(String message, Throwable cause) {
        super(message, cause);
    }
}
