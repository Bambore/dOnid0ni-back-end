package com.donidoni.auth.metier.domain;

import com.donidoni.auth.domain.AbstractAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Produit proposé au vote dans un sondage (grille de vignettes du mobile).
 */
@Entity
@Table(name = "options_sondage")
@Getter
@Setter
public class OptionSondage extends AbstractAuditingEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sondage_id", nullable = false)
    private Sondage sondage;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Référence catalogue si l'option correspond à un article existant. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(name = "ordre_affichage", nullable = false)
    private Integer ordreAffichage = 0;
}
