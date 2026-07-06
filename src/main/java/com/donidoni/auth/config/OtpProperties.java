package com.donidoni.auth.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriétés de configuration OTP.
 *
 * <p>Permet de configurer la longueur du code, le TTL,
 * le nombre maximum de tentatives et le rate limiting.</p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

    /** Longueur du code OTP (défaut : 6 chiffres). */
    @Min(4)
    @Max(8)
    private int length = 6;

    /** Durée de validité de l'OTP en secondes (défaut : 300 = 5 minutes). */
    @Positive
    private long ttlSeconds = 300;

    /** Nombre maximum de tentatives de validation (défaut : 5). */
    @Min(3)
    @Max(10)
    private int maxAttempts = 5;

    /** Nombre maximum d'envois OTP par numéro dans la fenêtre (défaut : 3). */
    @Min(1)
    private int rateLimitMaxSend = 3;

    /** Fenêtre de rate limiting en secondes (défaut : 300 = 5 minutes). */
    @Positive
    private long rateLimitWindowSeconds = 300;
}
