package com.donidoni.auth.metier.domain;

import com.donidoni.auth.metier.domain.enums.StatutTontine;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tontine d'acquisition : un groupe cotise mensuellement pour financer un bien.
 *
 * <p>Le statut correspond aux trois onglets du mobile : {@code EN_ATTENTE}
 * (« En attente »), {@code EN_COURS} (« En cours ») et {@code FERMEE} (« Fermé »).
 * Le mobile affiche {@code montantMensuel} sous la forme « 85 000 CFA / mois ».</p>
 */
@Entity
@Table(name = "tontines")
@Getter
@Setter
public class Tontine extends AbstractSoftDeletableEntity {

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(length = 2000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Cotisation due chaque mois par participant, en XOF. */
    @Column(name = "montant_mensuel", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantMensuel;

    @Column(name = "nombre_participants_cible", nullable = false)
    private Integer nombreParticipantsCible;

    /** Durée du cycle en mois ; par défaut, un tour par participant. */
    @Column(name = "duree_mois")
    private Integer dureeMois;

    /** Bien financé par la tontine, s'il correspond à un article du catalogue. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTontine statut = StatutTontine.EN_ATTENTE;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @OneToMany(mappedBy = "tontine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipationTontine> participations = new ArrayList<>();

    /**
     * @return le nombre de places encore disponibles (jamais négatif)
     */
    public int getPlacesRestantes() {
        final int cible = nombreParticipantsCible == null ? 0 : nombreParticipantsCible;
        return Math.max(0, cible - participations.size());
    }

    /**
     * @return {@code true} si le nombre de participants cible est atteint
     */
    public boolean estComplete() {
        return getPlacesRestantes() == 0;
    }
}
