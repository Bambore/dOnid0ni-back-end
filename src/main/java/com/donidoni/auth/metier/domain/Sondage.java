package com.donidoni.auth.metier.domain;

import com.donidoni.auth.metier.domain.enums.StatutSondage;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Sondage « Quel groupage lancer ? » (second onglet de l'écran Groupages).
 *
 * <p>Chaque utilisateur soumet une proposition composée d'un produit
 * ({@link OptionSondage}) et d'une destination ({@link Pays}).</p>
 */
@Entity
@Table(name = "sondages")
@Getter
@Setter
public class Sondage extends AbstractSoftDeletableEntity {

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSondage statut = StatutSondage.OUVERT;

    @Column(name = "date_debut", nullable = false)
    private Instant dateDebut = Instant.now();

    @Column(name = "date_fin")
    private Instant dateFin;

    @OneToMany(mappedBy = "sondage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionSondage> options = new ArrayList<>();

    @OneToMany(mappedBy = "sondage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteSondage> votes = new ArrayList<>();

    /**
     * @return {@code true} si le sondage accepte encore des votes
     */
    public boolean estOuvert() {
        return statut == StatutSondage.OUVERT
                && (dateFin == null || dateFin.isAfter(Instant.now()));
    }
}
