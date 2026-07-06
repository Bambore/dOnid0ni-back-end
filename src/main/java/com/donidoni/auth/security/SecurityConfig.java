package com.donidoni.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration de sécurité Spring Security avec OAuth2 Resource Server.
 *
 * <p>Valide les JWT émis par Keycloak via le JWK endpoint.
 * Les endpoints d'authentification sont publics, le reste est protégé.</p>
 *
 * <p>Les rôles Keycloak ({@code realm_access.roles}) sont automatiquement
 * convertis en {@link GrantedAuthority} Spring Security.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Endpoints publics ne nécessitant pas d'authentification. */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/api/produits/**",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    /**
     * Configure la chaîne de filtres de sécurité.
     *
     * @param http le builder de configuration HTTP
     * @return la chaîne de filtres configurée
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http)
            throws Exception {

        http
                // API stateless : pas de session ni CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CORS (délègue à CorsConfigurationSource)
                .cors(cors -> {})

                // Autorisation des endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())

                // Validation JWT via Keycloak JWK
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Convertisseur JWT → Authentication Spring Security.
     *
     * <p>Extrait les rôles depuis le claim {@code realm_access.roles}
     * de Keycloak et les convertit en {@link GrantedAuthority}.</p>
     *
     * @return le convertisseur configuré
     */
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    /**
     * Convertisseur de rôles Keycloak.
     *
     * <p>Extrait les rôles depuis {@code realm_access.roles} dans le JWT
     * et les préfixe avec {@code ROLE_} pour la compatibilité Spring Security.</p>
     */
    static class KeycloakRealmRoleConverter
            implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(final Jwt jwt) {
            final Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return Collections.emptyList();
            }

            final List<String> roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(role -> role.startsWith("ROLE_")
                            ? role
                            : "ROLE_" + role.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());
        }
    }
}
