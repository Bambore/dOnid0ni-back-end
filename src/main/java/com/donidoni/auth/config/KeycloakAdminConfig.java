package com.donidoni.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client Keycloak Admin basé sur un service account.
 *
 * <p>Utilise {@code client_credentials} pour obtenir un token admin
 * permettant les opérations CRUD utilisateur via l'Admin API.</p>
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakAdminConfig {

    private final KeycloakProperties props;

    public KeycloakAdminConfig(final KeycloakProperties props) {
        this.props = props;
    }

    /**
     * Crée le client Keycloak Admin en mode service account.
     *
     * @return instance {@link Keycloak} configurée
     */
    @Bean
    public Keycloak keycloak() {
        log.info("[KEYCLOAK] Connexion Admin API → {} (realm: {})",
                props.getServerUrl(), props.getRealm());
        return KeycloakBuilder.builder()
                .serverUrl(props.getServerUrl())
                .realm(props.getRealm())
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    /**
     * Expose la ressource du realm Keycloak cible.
     *
     * @param keycloak client Keycloak injecté
     * @return {@link RealmResource} du realm configuré
     */
    @Bean
    public RealmResource realmResource(final Keycloak keycloak) {
        return keycloak.realm(props.getRealm());
    }
}
