package com.donidoni.auth.metier.domain;

import com.donidoni.auth.metier.domain.enums.ModePaiement;
import com.donidoni.auth.metier.domain.enums.StatutCommande;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Commande passée depuis le marché (« Mes commandes » sur l'accueil).
 *
 * <p>Le {@link ModePaiement} reflète le choix de la fiche produit :
 * {@code COMPTANT} pour « Tout payer », {@code ECHELONNE} pour « Petit à petit ».
 * Dans le second cas, un {@link Echeance échéancier} est généré à la création.</p>
 */
@Entity
@Table(name = "commandes")
@Getter
@Setter
public class Commande extends AbstractSoftDeletableEntity {

    /** Référence lisible communiquée au client (ex. {@code CMD-2026-000123}). */
    @Column(nullable = false, length = 30, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boutique_id")
    private Boutique boutique;

    @Column(name = "montant_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 20)
    private ModePaiement modePaiement = ModePaiement.COMPTANT;

    /** Nombre d'échéances quand le mode de paiement est échelonné. */
    @Column(name = "nombre_echeances")
    private Integer nombreEcheances;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @Column(name = "adresse_livraison", length = 255)
    private String adresseLivraison;

    @Column(name = "telephone_livraison", length = 20)
    private String telephoneLivraison;

    @Column(name = "date_commande", nullable = false)
    private Instant dateCommande = Instant.now();

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Echeance> echeances = new ArrayList<>();

    /**
     * Ajoute une ligne à la commande en maintenant les deux côtés de l'association.
     *
     * @param ligne la ligne à rattacher
     */
    public void ajouterLigne(final LigneCommande ligne) {
        ligne.setCommande(this);
        lignes.add(ligne);
    }

    /**
     * Ajoute une échéance en maintenant les deux côtés de l'association.
     *
     * @param echeance l'échéance à rattacher
     */
    public void ajouterEcheance(final Echeance echeance) {
        echeance.setCommande(this);
        echeances.add(echeance);
    }
}
