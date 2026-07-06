package com.donidoni.auth.util;

import java.security.SecureRandom;

/**
 * Générateur d'OTP cryptographiquement sécurisé.
 *
 * <p>Utilise {@link SecureRandom} pour garantir l'imprévisibilité
 * des codes générés. Ne jamais utiliser {@code Math.random()} pour l'OTP.</p>
 */
public final class SecureOtpGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecureOtpGenerator() {
        // Utility class
    }

    /**
     * Génère un code OTP numérique de la longueur spécifiée.
     *
     * @param length longueur du code (entre 4 et 8)
     * @return le code OTP sous forme de String avec zéros de tête préservés
     * @throws IllegalArgumentException si la longueur est hors limites
     */
    public static String generate(final int length) {
        if (length < 4 || length > 8) {
            throw new IllegalArgumentException(
                    "La longueur de l'OTP doit être entre 4 et 8, reçu : " + length);
        }
        final int bound = (int) Math.pow(10, length);
        final int code = SECURE_RANDOM.nextInt(bound);
        return String.format("%0" + length + "d", code);
    }
}
