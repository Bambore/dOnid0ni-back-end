package com.donidoni.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod;
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
     * Ressources consultables sans compte.
     *
     * <p>Le mobile propose un mode invité : le catalogue, les boutiques, les
     * groupages, les tontines et les sondages sont donc lisibles sans jeton.
     * Toute action (adhésion, vote, commande) reste authentifiée grâce aux
     * annotations {@code @PreAuthorize} portées par les méthodes concernées.</p>
     */
    private static final String[] CATALOGUE_PUBLIC = {
            "/api/pays/**",
            "/api/categories/**",
            "/api/boutiques/**",
            "/api/articles/**",
            "/api/groupages/**",
            "/api/tontines/**",
            "/api/sondages/**"
    };

    /**
     * Lectures nominatives : accessibles au porteur du jeton, pour ses propres
     * données uniquement — le filtrage par utilisateur est fait dans les services.
     */
    private static final String[] PERSONNEL = {
            "/api/profil/**",
            "/api/favoris/**",
            "/api/commandes/mes",
            "/api/commandes/mes/*",
            "/api/paiements/mes",
            "/api/notifications/mes",
            "/api/notifications/non-lues/compteur",
            "/api/cadeaux/mes",
            "/api/groupages/mes",
            "/api/tontines/mes",
            "/api/tontines/*/mes-cotisations",
            "/api/sondages/*/mon-vote"
    };

    /** Ressources dont la création et la modification sont réservées au back-office. */
    private static final String[] CATALOGUE_ADMIN = {
            "/api/pays/**",
            "/api/categories/**",
            "/api/boutiques/**",
            "/api/articles/**",
            "/api/groupages/**",
            "/api/tontines/**",
            "/api/sondages/**",
            "/api/notifications/**",
            "/api/cadeaux/**"
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

                        // Recherche avancée du catalogue : lecture exposée en POST.
                        // Volontairement limitée aux ressources publiques — les
                        // recherches sur les clients et les paiements restent protégées.
                        .requestMatchers(HttpMethod.POST,
                                "/api/pays/search",
                                "/api/categories/search",
                                "/api/boutiques/search",
                                "/api/articles/search",
                                "/api/groupages/search",
                                "/api/tontines/search",
                                "/api/sondages/search").permitAll()

                        // Actions et données personnelles de l'utilisateur connecté.
                        // Déclarées avant les règles d'administration, qui couvrent
                        // les mêmes préfixes d'URL.
                        .requestMatchers(HttpMethod.GET, PERSONNEL).authenticated()
                        .requestMatchers(HttpMethod.POST,
                                "/api/groupages/*/participer",
                                "/api/tontines/*/participer",
                                "/api/sondages/*/voter",
                                "/api/notifications/toutes-lues",
                                "/api/cadeaux/*/utiliser",
                                "/api/commandes/*/annuler").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/notifications/*/lue").authenticated()
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/groupages/*/participer",
                                "/api/notifications/mes",
                                "/api/notifications/mes/*").authenticated()

                        // Consultation du catalogue en mode invité
                        .requestMatchers(HttpMethod.GET, CATALOGUE_PUBLIC).permitAll()

                        // Administration du contenu depuis le back-office
                        .requestMatchers(HttpMethod.POST, CATALOGUE_ADMIN).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, CATALOGUE_ADMIN).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, CATALOGUE_ADMIN).hasRole("ADMIN")

                        // Les endpoints CRUD génériques des ressources nominatives
                        // exposeraient les données de tous les clients : réservés au
                        // back-office. Les clients passent par les routes « /mes ».
                        .requestMatchers(HttpMethod.GET,
                                "/api/commandes", "/api/commandes/*",
                                "/api/notifications", "/api/notifications/*",
                                "/api/cadeaux", "/api/cadeaux/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/commandes/search").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/commandes/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/commandes/*").hasRole("ADMIN")

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
