package com.donidoni.auth.util;

import java.util.regex.Pattern;

/**
 * Validateur de numéros de téléphone au format international E.164.
 *
 * <p>Format attendu : {@code +} suivi de 8 à 15 chiffres.
 * Exemples valides : {@code +22670000000}, {@code +33612345678}.</p>
 */
public final class PhoneNumberValidator {

    /** Pattern E.164 : + suivi de 8 à 15 chiffres. */
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private PhoneNumberValidator() {
        // Utility class
    }

    /**
     * Valide un numéro de téléphone au format E.164.
     *
     * @param phoneNumber le numéro à valider
     * @return {@code true} si le numéro est valide
     */
    public static boolean isValid(final String phoneNumber) {
        return phoneNumber != null && E164_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Normalise un numéro de téléphone en supprimant les espaces et tirets.
     *
     * @param phoneNumber le numéro brut
     * @return le numéro nettoyé
     */
    public static String normalize(final String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.replaceAll("[\\s\\-()]", "");
    }
}
