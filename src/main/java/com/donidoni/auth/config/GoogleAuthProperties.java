package com.donidoni.auth.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriétés Google OAuth2 pour la validation des ID Tokens côté serveur.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "google")
public class GoogleAuthProperties {

    /**
     * Google OAuth2 Client ID (celui de l'app mobile Flutter).
     * Utilisé pour valider l'audience du Google ID Token.
     */
    @NotBlank
    private String clientId;
}
