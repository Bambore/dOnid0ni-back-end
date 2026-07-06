package com.donidoni.auth.sms;

import com.donidoni.auth.otp.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implémentation de développement pour l'envoi SMS.
 *
 * <p>Au lieu d'envoyer un vrai SMS, le code OTP est loggé dans la console.
 * Activé uniquement avec le profil {@code dev}.</p>
 */
@Slf4j
@Service
@Profile("!prod")
public class LogSmsService implements SmsService {

    @Override
    public void sendSms(final String phoneNumber, final String message) {
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║  📱 SMS (DEV) → {}",  phoneNumber);
        log.info("║  📨 {}",              message);
        log.info("╚══════════════════════════════════════════════╝");
    }
}
