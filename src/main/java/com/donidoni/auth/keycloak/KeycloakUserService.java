package com.donidoni.auth.keycloak;

import com.donidoni.auth.dto.response.UserInfo;
import com.donidoni.auth.exception.AuthException;
import com.donidoni.auth.exception.ErrorCode;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service d'intégration Keycloak Admin API.
 *
 * <p>Encapsule toutes les opérations CRUD sur les utilisateurs Keycloak :
 * recherche, création, assignation de rôles et liaison d'identités fédérées.</p>
 *
 * <p>Ce service est le <strong>seul</strong> point de contact avec l'Admin API.
 * Aucun autre composant ne doit interagir directement avec {@link RealmResource}.</p>
 */
@Slf4j
@Service
public class KeycloakUserService {

    /** Rôle par défaut assigné à tout nouvel utilisateur. */
    private static final String DEFAULT_ROLE = "ROLE_USER";

    /** Rôles système Keycloak à exclure des réponses. */
    private static final List<String> SYSTEM_ROLES = List.of(
            "default-roles-doni-doni",
            "offline_access",
            "uma_authorization");

    private final RealmResource realmResource;

    public KeycloakUserService(final RealmResource realmResource) {
        this.realmResource = realmResource;
    }

    // ═══════════════════════════════════════════════════════════
    //  RECHERCHE
    // ═══════════════════════════════════════════════════════════

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param email l'adresse email à chercher
     * @return un {@link Optional} contenant l'utilisateur, ou vide si introuvable
     */
    public Optional<UserRepresentation> findByEmail(final String email) {
        return realmResource.users()
                .searchByEmail(email, true)
                .stream()
                .findFirst();
    }

    /**
     * Recherche un utilisateur par son numéro de téléphone (attribut custom).
     *
     * @param phoneNumber le numéro de téléphone au format E.164
     * @return un {@link Optional} contenant l'utilisateur, ou vide si introuvable
     */
    public Optional<UserRepresentation> findByPhone(final String phoneNumber) {
        return realmResource.users()
                .searchByAttributes("phone:" + phoneNumber)
                .stream()
                .findFirst();
    }

    /**
     * Récupère un utilisateur par son identifiant Keycloak.
     *
     * @param userId l'identifiant Keycloak (UUID)
     * @return la représentation de l'utilisateur
     * @throws AuthException si l'utilisateur n'existe pas
     */
    public UserRepresentation findById(final String userId) {
        try {
            return realmResource.users().get(userId).toRepresentation();
        } catch (NotFoundException e) {
            throw new AuthException(ErrorCode.USER_NOT_FOUND,
                    "Utilisateur introuvable : " + userId);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  CRÉATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Crée un utilisateur Keycloak pour une connexion Google.
     *
     * @param email     adresse email Google
     * @param firstName prénom
     * @param lastName  nom de famille
     * @param googleId  identifiant Google (sub claim)
     * @return l'identifiant Keycloak du nouvel utilisateur
     */
    public String createGoogleUser(
            final String email,
            final String firstName,
            final String lastName,
            final String googleId) {

        final UserRepresentation user = new UserRepresentation();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(email);
        user.setEnabled(true);
        user.setEmailVerified(true);

        final String userId = createUserInKeycloak(user);

        // Lier l'identité Google
        linkGoogleIdentity(userId, googleId, email);

        // Assigner le rôle par défaut
        assignRole(userId, DEFAULT_ROLE);

        log.info("[KC] Utilisateur Google créé : {} ({}) → {}",
                email, googleId, userId);
        return userId;
    }

    /**
     * Crée un utilisateur Keycloak pour une connexion par téléphone.
     *
     * @param phoneNumber numéro de téléphone au format E.164
     * @return l'identifiant Keycloak du nouvel utilisateur
     */
    public String createPhoneUser(final String phoneNumber) {
        final UserRepresentation user = new UserRepresentation();
        user.setUsername(phoneNumber);
        user.setEnabled(true);
        user.setAttributes(Map.of("phone", List.of(phoneNumber)));

        final String userId = createUserInKeycloak(user);

        // Assigner le rôle par défaut
        assignRole(userId, DEFAULT_ROLE);

        log.info("[KC] Utilisateur téléphone créé : {} → {}", phoneNumber, userId);
        return userId;
    }

    // ═══════════════════════════════════════════════════════════
    //  RÔLES
    // ═══════════════════════════════════════════════════════════

    /**
     * Assigne un rôle realm à un utilisateur.
     *
     * @param userId   l'identifiant de l'utilisateur
     * @param roleName le nom du rôle à assigner
     */
    public void assignRole(final String userId, final String roleName) {
        try {
            final RoleRepresentation role = realmResource.roles()
                    .get(roleName)
                    .toRepresentation();

            realmResource.users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(List.of(role));

            log.debug("[KC] Rôle '{}' assigné à l'utilisateur {}", roleName, userId);
        } catch (NotFoundException e) {
            log.warn("[KC] Rôle '{}' introuvable — création automatique", roleName);
            createRole(roleName);
            assignRole(userId, roleName);
        }
    }

    /**
     * Récupère les rôles d'un utilisateur (hors rôles système).
     *
     * @param userId l'identifiant de l'utilisateur
     * @return la liste des noms de rôles
     */
    public List<String> getUserRoles(final String userId) {
        try {
            return realmResource.users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .listAll()
                    .stream()
                    .map(RoleRepresentation::getName)
                    .filter(name -> !SYSTEM_ROLES.contains(name))
                    .toList();
        } catch (NotFoundException e) {
            return Collections.emptyList();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  IDENTITÉ FÉDÉRÉE
    // ═══════════════════════════════════════════════════════════

    /**
     * Lie une identité Google à un utilisateur Keycloak existant.
     *
     * @param userId   l'identifiant Keycloak de l'utilisateur
     * @param googleId l'identifiant Google (claim {@code sub})
     * @param email    l'adresse email Google
     */
    public void linkGoogleIdentity(
            final String userId,
            final String googleId,
            final String email) {

        final FederatedIdentityRepresentation fedIdentity =
                new FederatedIdentityRepresentation();
        fedIdentity.setIdentityProvider("google");
        fedIdentity.setUserId(googleId);
        fedIdentity.setUserName(email);

        try {
            realmResource.users()
                    .get(userId)
                    .addFederatedIdentity("google", fedIdentity);
            log.debug("[KC] Identité Google liée pour utilisateur {}", userId);
        } catch (Exception e) {
            log.warn("[KC] Impossible de lier l'identité Google pour {} : {}",
                    userId, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  MAPPING → DTO
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertit un {@link UserRepresentation} Keycloak en {@link UserInfo} DTO.
     *
     * @param user la représentation Keycloak
     * @return le DTO {@link UserInfo}
     */
    public UserInfo toUserInfo(final UserRepresentation user) {
        final String phone = extractAttribute(user, "phone");
        final List<String> roles = getUserRoles(user.getId());

        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(phone)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    /**
     * Crée un utilisateur dans Keycloak et retourne son ID.
     */
    private String createUserInKeycloak(final UserRepresentation user) {
        try (Response response = realmResource.users().create(user)) {
            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                throw new AuthException(ErrorCode.USER_CREATION_FAILED,
                        "Un utilisateur avec ces informations existe déjà");
            }
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                throw new AuthException(ErrorCode.USER_CREATION_FAILED,
                        "Erreur Keycloak lors de la création : HTTP " + response.getStatus());
            }
            return extractCreatedId(response);
        }
    }

    /**
     * Extrait l'identifiant depuis le header Location de la réponse de création.
     */
    private String extractCreatedId(final Response response) {
        final String location = response.getHeaderString("Location");
        if (location == null || !location.contains("/")) {
            throw new AuthException(ErrorCode.USER_CREATION_FAILED,
                    "Header Location manquant dans la réponse Keycloak");
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }

    /**
     * Crée un rôle realm s'il n'existe pas.
     */
    private void createRole(final String roleName) {
        final RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription("Rôle auto-créé : " + roleName);
        realmResource.roles().create(role);
        log.info("[KC] Rôle '{}' créé automatiquement", roleName);
    }

    /**
     * Extrait un attribut custom d'un utilisateur Keycloak.
     */
    private static String extractAttribute(
            final UserRepresentation user,
            final String attributeName) {
        if (user.getAttributes() == null) {
            return null;
        }
        final List<String> values = user.getAttributes().get(attributeName);
        return (values != null && !values.isEmpty()) ? values.getFirst() : null;
    }
}
