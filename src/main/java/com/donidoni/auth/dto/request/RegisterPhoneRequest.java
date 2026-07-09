package com.donidoni.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Requête d'inscription avec un numéro de téléphone.
 */
public record RegisterPhoneRequest(
        @NotBlank(message = "Le numéro de téléphone est requis")
        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "Le numéro doit être au format E.164 (ex: +22670000000)")
        String phoneNumber,

        @NotBlank(message = "Le prénom est requis")
        String firstName,

        @NotBlank(message = "Le nom est requis")
        String lastName
) {
}
