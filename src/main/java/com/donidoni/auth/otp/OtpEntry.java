package com.donidoni.auth.otp;

import java.io.Serializable;
import java.time.Instant;

/**
 * Entrée OTP stockée dans Redis.
 *
 * <p>Contient le code OTP, le compteur de tentatives de validation,
 * et l'horodatage de création.</p>
 *
 * @param code      code OTP (6 chiffres)
 * @param attempts  nombre de tentatives de validation échouées
 * @param createdAt horodatage de création
 */
public record OtpEntry(
        String code,
        int attempts,
        Instant createdAt
) implements Serializable {

    /**
     * Crée une nouvelle entrée OTP avec 0 tentatives.
     *
     * @param code le code OTP généré
     * @return une nouvelle {@link OtpEntry}
     */
    public static OtpEntry create(final String code) {
        return new OtpEntry(code, 0, Instant.now());
    }

    /**
     * Retourne une copie avec le compteur de tentatives incrémenté.
     *
     * @return une nouvelle {@link OtpEntry} avec {@code attempts + 1}
     */
    public OtpEntry incrementAttempts() {
        return new OtpEntry(code, attempts + 1, createdAt);
    }
}
