package com.donidoni.auth.metier.domain;

import com.donidoni.auth.metier.domain.enums.Langue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Miroir applicatif d'un utilisateur Keycloak.
 *
 * <p>Keycloak reste la source de vérité de l'identité (mot de passe, tokens, rôles).
 * Cette entité porte les données <em>métier</em> et les préférences affichées par
 * l'écran « Mon Profil » du mobile : ville, photo, langue, thème sombre,
 * sécurité biométrique et notifications.</p>
 */
@Entity
@Table(
        name = "utilisateurs",
        indexes = @Index(name = "idx_utilisateur_telephone", columnList = "telephone")
)
@Getter
@Setter
public class Utilisateur extends AbstractSoftDeletableEntity {

    /** Identifiant {@code sub} du JWT Keycloak. Clé de rapprochement unique. */
    @Column(name = "keycloak_id", nullable = false, length = 64, unique = true)
    private String keycloakId;

    @Column(name = "nom_complet", length = 150)
    private String nomComplet;

    @Column(length = 150)
    private String email;

    /** Numéro au format E.164 (ex. {@code +22670000000}). */
    @Column(length = 20)
    private String telephone;

    @Column(length = 100)
    private String ville;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Langue langue = Langue.FR;

    @Column(name = "theme_sombre", nullable = false)
    private boolean themeSombre = false;

    @Column(name = "biometrie_activee", nullable = false)
    private boolean biometrieActivee = false;

    @Column(name = "notifications_activees", nullable = false)
    private boolean notificationsActivees = true;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "derniere_connexion")
    private Instant derniereConnexion;
}
