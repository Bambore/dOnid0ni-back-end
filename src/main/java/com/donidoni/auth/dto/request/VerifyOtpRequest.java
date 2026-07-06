package com.donidoni.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Requête de vérification d'un code OTP.
 *
 * @param phoneNumber numéro de téléphone au format E.164
 * @param otpCode     code OTP saisi par l'utilisateur
 */
public record VerifyOtpRequest(
        @NotBlank(message = "Le numéro de téléphone est requis")
        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "Le numéro doit être au format E.164 (ex: +22670000000)")
        String phoneNumber,

        @NotBlank(message = "Le code OTP est requis")
        @Size(min = 4, max = 8, message = "Le code OTP doit contenir entre 4 et 8 chiffres")
        String otpCode
) {
}
