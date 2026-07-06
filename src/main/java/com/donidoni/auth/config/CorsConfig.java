package com.donidoni.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration CORS pour les clients mobiles Flutter.
 *
 * <p>En développement, autorise toutes les origines.
 * En production, restreindre aux domaines de l'application.</p>
 */
@Configuration(proxyBeanMethods = false)
public class CorsConfig {

    /**
     * Source de configuration CORS.
     * Les applications Flutter mobiles n'envoient pas d'origine HTTP classique,
     * mais cette config est nécessaire pour les tests et le web.
     *
     * @return {@link CorsConfigurationSource} configurée
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
