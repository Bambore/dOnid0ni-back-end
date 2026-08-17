package com.donidoni.auth.metier.exception;

import com.donidoni.auth.exception.ErrorCode;
import lombok.Getter;

/**
 * Exception des règles métier de l'application mobile.
 *
 * <p>Portée par un {@link ErrorCode} qui détermine à la fois le code applicatif
 * et le statut HTTP retourné par
 * {@link com.donidoni.auth.exception.GlobalExceptionHandler}.</p>
 */
@Getter
public class MetierException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Crée une exception avec le message par défaut du code d'erreur.
     *
     * @param errorCode le code d'erreur métier
     */
    public MetierException(final ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    /**
     * Crée une exception avec un message contextualisé.
     *
     * @param errorCode le code d'erreur métier
     * @param message   le message précisant la situation rencontrée
     */
    public MetierException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
