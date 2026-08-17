package com.donidoni.auth.metier.domain;

import com.donidoni.auth.metier.domain.enums.StatutGroupage;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Achat groupé à l'international (onglet « Groupages disponibles » du mobile).
 *
 * <p>Un groupage cible un nombre de participants ({@code nombreParticipantsCible}) ;
 * la carte mobile affiche la progression {@code participants.size() / cible}.
 * Le montant est la quote-part en francs CFA due par participant.</p>
 */
@Entity
@Table(name = "groupages")
@Getter
@Setter
public class Groupage extends AbstractSoftDeletableEntity {

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(length = 2000)
    private String description;

    @Column(name = "image_principale", length = 500)
    private String imagePrincipale;

    /** Galerie affichée par la page de détail du groupage. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "groupage_images", joinColumns = @JoinColumn(name = "groupage_id"))
    @Column(name = "url", length = 500, nullable = false)
    private List<String> images = new ArrayList<>();

    /** Quote-part due par participant, en XOF. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    /** Pays d'approvisionnement affiché en badge sur la carte. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_id")
    private Pays pays;

    /** Article du catalogue concerné, quand le groupage porte sur une référence connue. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(name = "nombre_participants_cible", nullable = false)
    private Integer nombreParticipantsCible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutGroupage statut = StatutGroupage.OUVERT;

    @Column(name = "date_ouverture", nullable = false)
    private Instant dateOuverture = Instant.now();

    /** Date limite d'adhésion communiquée aux participants. */
    @Column(name = "date_cloture_prevue")
    private LocalDate dateCloturePrevue;

    @OneToMany(mappedBy = "groupage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipationGroupage> participations = new ArrayList<>();

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
    public boolean estComplet() {
        return getPlacesRestantes() == 0;
    }
}
