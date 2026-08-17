package com.donidoni.auth.sms;

import com.donidoni.auth.otp.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implémentation Twilio pour l'envoi de SMS en production.
 *
 * <p>Utilise l'API Twilio pour envoyer les codes OTP par SMS.
 * Activé uniquement avec le profil {@code prod}.</p>
 */
@Slf4j
@Service
@Profile("prod")
public class TwilioSmsService implements SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    /**
     * Initialise le client Twilio au démarrage.
     *
     * <p>L'absence d'identifiants interrompt le démarrage : mieux vaut un
     * échec explicite qu'une application en ligne dont aucun SMS ne part,
     * les utilisateurs se connectant uniquement par code OTP.</p>
     *
     * @throws IllegalStateException si une des variables Twilio est absente
     */
    @PostConstruct
    void init() {
        verifierPresence("TWILIO_ACCOUNT_SID", accountSid);
        verifierPresence("TWILIO_AUTH_TOKEN", authToken);
        verifierPresence("TWILIO_FROM_NUMBER", fromNumber);

        Twilio.init(accountSid, authToken);
        log.info("[TWILIO] Client initialisé (from: {})", fromNumber);
    }

    private void verifierPresence(final String variable, final String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalStateException(
                    "Le profil 'prod' exige la variable d'environnement " + variable
                            + " pour envoyer les codes OTP par SMS.");
        }
    }

    @Override
    public void sendSms(final String phoneNumber, final String message) {
        final Message twilioMessage = Message.creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber(fromNumber),
                        message)
                .create();

        log.info("[TWILIO] SMS envoyé → {} (SID: {})",
                phoneNumber, twilioMessage.getSid());
    }
}
