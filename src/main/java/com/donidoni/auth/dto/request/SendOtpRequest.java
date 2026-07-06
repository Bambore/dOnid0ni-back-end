package com.donidoni.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Requête d'envoi d'un code OTP par SMS.
 *
 * @param phoneNumber numéro de téléphone au format E.164 (ex: +22670000000)
 */
public record SendOtpRequest(
        @NotBlank(message = "Le numéro de téléphone est requis")
        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "Le numéro doit être au format E.164 (ex: +22670000000)")
        String phoneNumber
) {
}
