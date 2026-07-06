package com.donidoni.auth.otp;

import com.donidoni.auth.config.OtpProperties;
import com.donidoni.auth.exception.ErrorCode;
import com.donidoni.auth.exception.OtpException;
import com.donidoni.auth.util.SecureOtpGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service de gestion des OTP : génération, stockage Redis, validation et rate limiting.
 *
 * <p>Chaque OTP est stocké dans Redis avec un TTL configurable.
 * Les tentatives échouées sont comptabilisées et l'OTP est invalidé
 * après un nombre maximum de tentatives.</p>
 *
 * <p>Un rate limiting par numéro empêche l'abus d'envoi de SMS.</p>
 */
@Slf4j
@Service
public class OtpService {

    /** Préfixe des clés Redis pour les OTP. */
    private static final String OTP_KEY_PREFIX = "otp:";

    /** Préfixe des clés Redis pour le rate limiting. */
    private static final String RATE_KEY_PREFIX = "otp-rate:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final OtpProperties otpProperties;
    private final SmsService smsService;

    public OtpService(
            final RedisTemplate<String, Object> redisTemplate,
            final OtpProperties otpProperties,
            final SmsService smsService) {
        this.redisTemplate = redisTemplate;
        this.otpProperties = otpProperties;
        this.smsService = smsService;
    }

    /**
     * Génère un OTP, le stocke dans Redis et envoie le SMS.
     *
     * @param phoneNumber numéro de téléphone au format E.164
     * @throws OtpException si le rate limit est atteint ou si l'envoi échoue
     */
    public void generateAndSend(final String phoneNumber) {
        checkRateLimit(phoneNumber);

        final String code = SecureOtpGenerator.generate(otpProperties.getLength());
        final OtpEntry entry = OtpEntry.create(code);

        // Stocker l'OTP avec TTL
        final String otpKey = OTP_KEY_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(
                otpKey,
                entry,
                Duration.ofSeconds(otpProperties.getTtlSeconds()));

        // Incrémenter le compteur de rate limiting
        incrementRateLimit(phoneNumber);

        // Envoyer le SMS
        final String message = String.format(
                "Votre code Doni-Doni : %s. Valable %d minutes.",
                code,
                otpProperties.getTtlSeconds() / 60);

        try {
            smsService.sendSms(phoneNumber, message);
            log.info("[OTP] Code envoyé vers {}", maskPhone(phoneNumber));
        } catch (Exception e) {
            // Supprimer l'OTP si l'envoi échoue
            redisTemplate.delete(otpKey);
            log.error("[OTP] Échec envoi SMS vers {} : {}",
                    maskPhone(phoneNumber), e.getMessage());
            throw new OtpException(ErrorCode.OTP_SEND_FAILED,
                    "Impossible d'envoyer le SMS, réessayez plus tard");
        }
    }

    /**
     * Valide un code OTP pour un numéro donné.
     *
     * @param phoneNumber numéro de téléphone
     * @param otpCode     code OTP saisi par l'utilisateur
     * @throws OtpException si l'OTP est invalide, expiré ou si le max de tentatives est atteint
     */
    public void verify(final String phoneNumber, final String otpCode) {
        final String otpKey = OTP_KEY_PREFIX + phoneNumber;
        final Object stored = redisTemplate.opsForValue().get(otpKey);

        if (stored == null) {
            log.warn("[OTP] Code expiré ou inexistant pour {}", maskPhone(phoneNumber));
            throw new OtpException(ErrorCode.OTP_EXPIRED,
                    "Le code OTP a expiré, veuillez en demander un nouveau");
        }

        final OtpEntry entry;
        if (stored instanceof OtpEntry otpEntry) {
            entry = otpEntry;
        } else {
            // Fallback si la désérialisation ne produit pas directement un OtpEntry
            log.error("[OTP] Type inattendu en cache : {}", stored.getClass().getName());
            redisTemplate.delete(otpKey);
            throw new OtpException(ErrorCode.OTP_EXPIRED);
        }

        // Vérifier le nombre de tentatives
        if (entry.attempts() >= otpProperties.getMaxAttempts()) {
            redisTemplate.delete(otpKey);
            log.warn("[OTP] Max tentatives atteint pour {}", maskPhone(phoneNumber));
            throw new OtpException(ErrorCode.OTP_MAX_ATTEMPTS);
        }

        // Vérifier le code (comparaison en temps constant)
        if (!constantTimeEquals(entry.code(), otpCode)) {
            // Incrémenter le compteur de tentatives
            final OtpEntry updated = entry.incrementAttempts();
            redisTemplate.opsForValue().set(otpKey, updated,
                    Duration.ofSeconds(otpProperties.getTtlSeconds()));

            log.warn("[OTP] Code invalide pour {} (tentative {}/{})",
                    maskPhone(phoneNumber),
                    updated.attempts(),
                    otpProperties.getMaxAttempts());

            if (updated.attempts() >= otpProperties.getMaxAttempts()) {
                redisTemplate.delete(otpKey);
                throw new OtpException(ErrorCode.OTP_MAX_ATTEMPTS);
            }

            throw new OtpException(ErrorCode.OTP_INVALID,
                    "Code OTP invalide, il vous reste "
                            + (otpProperties.getMaxAttempts() - updated.attempts())
                            + " tentative(s)");
        }

        // OTP valide — suppression immédiate (usage unique)
        redisTemplate.delete(otpKey);
        log.info("[OTP] Code validé avec succès pour {}", maskPhone(phoneNumber));
    }

    /**
     * Vérifie que le numéro n'a pas dépassé le rate limit d'envoi.
     */
    private void checkRateLimit(final String phoneNumber) {
        final String rateKey = RATE_KEY_PREFIX + phoneNumber;
        final Object count = redisTemplate.opsForValue().get(rateKey);

        if (count != null) {
            final int currentCount;
            if (count instanceof Integer intCount) {
                currentCount = intCount;
            } else if (count instanceof Number number) {
                currentCount = number.intValue();
            } else {
                currentCount = 0;
            }

            if (currentCount >= otpProperties.getRateLimitMaxSend()) {
                log.warn("[OTP] Rate limit atteint pour {}", maskPhone(phoneNumber));
                throw new OtpException(ErrorCode.OTP_RATE_LIMITED);
            }
        }
    }

    /**
     * Incrémente le compteur de rate limiting pour un numéro.
     */
    private void incrementRateLimit(final String phoneNumber) {
        final String rateKey = RATE_KEY_PREFIX + phoneNumber;
        final Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count != null && count == 1L) {
            // Première requête dans la fenêtre : initialiser le TTL
            redisTemplate.expire(rateKey,
                    Duration.ofSeconds(otpProperties.getRateLimitWindowSeconds()));
        }
    }

    /**
     * Comparaison en temps constant pour éviter les attaques timing.
     *
     * @param expected le code attendu
     * @param actual   le code fourni
     * @return {@code true} si les codes sont identiques
     */
    private static boolean constantTimeEquals(final String expected, final String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        if (expected.length() != actual.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }

    /**
     * Masque un numéro de téléphone pour le logging sécurisé.
     * Exemple : +22670123456 → +226****3456
     */
    private static String maskPhone(final String phone) {
        if (phone == null || phone.length() < 8) {
            return "****";
        }
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 4);
    }
}
