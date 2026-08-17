package com.donidoni.auth.metier.domain;

import com.donidoni.auth.metier.domain.enums.TypeNotification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Notification affichée dans le centre de notifications du mobile.
 *
 * <p>Les libellés sont stockés en français et en anglais : le mobile choisit la
 * variante selon la locale active. Un {@code utilisateur} nul désigne une
 * notification de diffusion générale, visible par tous.</p>
 */
@Entity
@Table(
        name = "notifications",
        indexes = @Index(name = "idx_notification_utilisateur", columnList = "utilisateur_id, lue")
)
@Getter
@Setter
public class Notification extends AbstractSoftDeletableEntity {

    /** Destinataire ; {@code null} pour une notification diffusée à tous. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeNotification type;

    @Column(name = "titre_fr", nullable = false, length = 200)
    private String titreFr;

    @Column(name = "titre_en", length = 200)
    private String titreEn;

    @Column(name = "corps_fr", nullable = false, length = 1000)
    private String corpsFr;

    @Column(name = "corps_en", length = 1000)
    private String corpsEn;

    @Column(nullable = false)
    private boolean lue = false;

    @Column(name = "date_lecture")
    private Instant dateLecture;

    /** Route interne ouverte au tap (ex. {@code /tontines/12}). */
    @Column(name = "lien_action", length = 255)
    private String lienAction;
}
