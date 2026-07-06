package com.donidoni.auth.service;

import com.donidoni.auth.config.GoogleAuthProperties;
import com.donidoni.auth.exception.AuthException;
import com.donidoni.auth.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service de validation des Google ID Tokens côté serveur.
 *
 * <p>Utilise la librairie officielle Google pour vérifier la signature,
 * l'expiration et l'audience du token.</p>
 */
@Slf4j
@Service
@EnableConfigurationProperties(GoogleAuthProperties.class)
public class GoogleTokenVerifier {

    private final GoogleAuthProperties googleProperties;
    private GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(final GoogleAuthProperties googleProperties) {
        this.googleProperties = googleProperties;
    }

    /**
     * Initialise le vérificateur Google ID Token.
     */
    @PostConstruct
    void init() {
        verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleProperties.getClientId()))
                .build();
        log.info("[GOOGLE] Vérificateur initialisé (clientId: {}...)",
                googleProperties.getClientId().substring(0,
                        Math.min(20, googleProperties.getClientId().length())));
    }

    /**
     * Vérifie un Google ID Token et retourne les informations utilisateur.
     *
     * @param idTokenString le Google ID Token JWT brut
     * @return les informations extraites du token
     * @throws AuthException si le token est invalide ou expiré
     */
    public GoogleUserInfo verify(final String idTokenString) {
        try {
            final GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new AuthException(ErrorCode.INVALID_GOOGLE_TOKEN,
                        "Le Google ID Token est invalide ou expiré");
            }

            final GoogleIdToken.Payload payload = idToken.getPayload();

            final GoogleUserInfo userInfo = new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("given_name"),
                    (String) payload.get("family_name"),
                    (String) payload.get("picture"),
                    payload.getEmailVerified());

            log.info("[GOOGLE] Token vérifié pour {} (sub: {})",
                    userInfo.email(), userInfo.sub());
            return userInfo;

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GOOGLE] Erreur de vérification du token : {}", e.getMessage());
            throw new AuthException(ErrorCode.INVALID_GOOGLE_TOKEN,
                    "Impossible de vérifier le Google ID Token", e);
        }
    }

    /**
     * Informations utilisateur extraites d'un Google ID Token vérifié.
     *
     * @param sub           identifiant unique Google (claim {@code sub})
     * @param email         adresse email
     * @param firstName     prénom (claim {@code given_name})
     * @param lastName      nom de famille (claim {@code family_name})
     * @param pictureUrl    URL de la photo de profil
     * @param emailVerified indique si l'email est vérifié
     */
    public record GoogleUserInfo(
            String sub,
            String email,
            String firstName,
            String lastName,
            String pictureUrl,
            boolean emailVerified
    ) {
    }
}
