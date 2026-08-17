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

import java.math.BigDecimal;

/**
 * Ligne d'une commande : un article, une quantité et le prix figé au moment de l'achat.
 */
@Entity
@Table(name = "lignes_commande")
@Getter
@Setter
public class LigneCommande extends AbstractAuditingEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /** Libellé figé : la commande reste lisible même si l'article est renommé. */
    @Column(name = "libelle_article", nullable = false, length = 150)
    private String libelleArticle;

    @Column(nullable = false)
    private Integer quantite = 1;

    /** Prix unitaire au moment de la commande, en XOF. */
    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "montant_ligne", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantLigne;
}
