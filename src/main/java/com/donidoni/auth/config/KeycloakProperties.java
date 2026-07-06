package com.donidoni.auth.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriétés de configuration du client Keycloak Admin.
 *
 * <p>Liées au préfixe {@code keycloak} dans {@code application.yml}.
 * Toutes les propriétés sont obligatoires et validées au démarrage.</p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    /** URL du serveur Keycloak (ex: http://localhost:8080). */
    @NotBlank
    private String serverUrl;

    /** Nom du realm Keycloak (ex: doni-doni). */
    @NotBlank
    private String realm;

    /** Client ID du service account backend (ex: doni-doni-backend). */
    @NotBlank
    private String clientId;

    /** Client secret du service account backend. */
    @NotBlank
    private String clientSecret;
}
