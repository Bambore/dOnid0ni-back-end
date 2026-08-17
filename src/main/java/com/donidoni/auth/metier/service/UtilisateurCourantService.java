package com.donidoni.auth.metier.service;

import com.donidoni.auth.exception.ErrorCode;
import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.exception.MetierException;
import com.donidoni.auth.metier.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Résout l'utilisateur métier correspondant au JWT Keycloak de la requête.
 *
 * <p>Keycloak reste la source de vérité de l'identité. Au premier appel
 * authentifié, un {@link Utilisateur} local est provisionné à partir des claims
 * du jeton : l'application mobile n'a donc aucune étape d'inscription
 * supplémentaire à effectuer après la connexion.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurCourantService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Retourne l'utilisateur métier du porteur du jeton, en le créant au besoin.
     *
     * @return l'utilisateur courant, jamais {@code null}
     * @throws MetierException si la requête n'est pas authentifiée
     */
    @Transactional
    public Utilisateur getUtilisateurCourant() {
        final Jwt jwt = extraireJwt();
        final String keycloakId = jwt.getSubject();

        return utilisateurRepository.findByKeycloakId(keycloakId)
                .map(existant -> mettreAJourDepuisJeton(existant, jwt))
                .orElseGet(() -> provisionner(jwt));
    }

    /**
     * @return l'identifiant technique de l'utilisateur courant
     */
    @Transactional
    public Long getIdUtilisateurCourant() {
        return getUtilisateurCourant().getId();
    }

    /**
     * Vérifie que l'utilisateur courant est bien le propriétaire de la ressource.
     *
     * @param idProprietaire l'identifiant du propriétaire de la ressource
     * @throws MetierException si la ressource appartient à quelqu'un d'autre
     */
    @Transactional(readOnly = true)
    public void verifierProprietaire(final Long idProprietaire) {
        if (idProprietaire == null || !idProprietaire.equals(getIdUtilisateurCourant())) {
            throw new MetierException(ErrorCode.ACCES_REFUSE);
        }
    }

    private Jwt extraireJwt() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new MetierException(ErrorCode.ACCES_REFUSE, "Requête non authentifiée");
        }
        return jwt;
    }

    private Utilisateur provisionner(final Jwt jwt) {
        final Utilisateur utilisateur = new Utilisateur();
        utilisateur.setKeycloakId(jwt.getSubject());
        utilisateur.setNomComplet(nomDepuisJeton(jwt));
        utilisateur.setEmail(jwt.getClaimAsString("email"));
        utilisateur.setTelephone(telephoneDepuisJeton(jwt));
        utilisateur.setDerniereConnexion(Instant.now());

        log.info("Provisionnement de l'utilisateur métier pour le sujet Keycloak {}", jwt.getSubject());
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Complète les champs encore vides à partir du jeton et horodate la connexion.
     * Les valeurs saisies par l'utilisateur dans l'application ne sont jamais écrasées.
     *
     * <p>Un profil précédemment supprimé est réactivé : le compte Keycloak
     * existant reste valide, et recréer une ligne violerait l'unicité de
     * {@code keycloak_id}.</p>
     */
    private Utilisateur mettreAJourDepuisJeton(final Utilisateur utilisateur, final Jwt jwt) {
        if (utilisateur.isDeleted()) {
            log.info("Réactivation du profil #{} après une suppression de compte", utilisateur.getId());
            utilisateur.setDeleted(false);
            utilisateur.setDeletedAt(null);
            utilisateur.setActif(true);
        }
        if (utilisateur.getNomComplet() == null) {
            utilisateur.setNomComplet(nomDepuisJeton(jwt));
        }
        if (utilisateur.getEmail() == null) {
            utilisateur.setEmail(jwt.getClaimAsString("email"));
        }
        if (utilisateur.getTelephone() == null) {
            utilisateur.setTelephone(telephoneDepuisJeton(jwt));
        }
        utilisateur.setDerniereConnexion(Instant.now());
        return utilisateurRepository.save(utilisateur);
    }

    private String nomDepuisJeton(final Jwt jwt) {
        final String nom = jwt.getClaimAsString("name");
        if (nom != null && !nom.isBlank()) {
            return nom;
        }
        final String prenom = jwt.getClaimAsString("given_name");
        final String famille = jwt.getClaimAsString("family_name");
        if (prenom != null || famille != null) {
            return String.join(" ", prenom == null ? "" : prenom, famille == null ? "" : famille).trim();
        }
        return jwt.getClaimAsString("preferred_username");
    }

    private String telephoneDepuisJeton(final Jwt jwt) {
        final String telephone = jwt.getClaimAsString("phone_number");
        return telephone != null ? telephone : jwt.getClaimAsString("preferred_username");
    }
}
