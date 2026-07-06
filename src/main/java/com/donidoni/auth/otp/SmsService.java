package com.donidoni.auth.otp;

/**
 * Abstraction pour l'envoi de SMS.
 *
 * <p>Permet de substituer facilement l'implémentation
 * (Twilio en production, log en développement).</p>
 */
public interface SmsService {

    /**
     * Envoie un SMS au numéro spécifié.
     *
     * @param phoneNumber numéro de téléphone au format E.164
     * @param message     contenu du SMS
     */
    void sendSms(String phoneNumber, String message);
}
