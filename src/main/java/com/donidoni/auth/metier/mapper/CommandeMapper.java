package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.Commande;
import com.donidoni.auth.metier.domain.Echeance;
import com.donidoni.auth.metier.domain.LigneCommande;
import com.donidoni.auth.metier.domain.enums.ModePaiement;
import com.donidoni.auth.metier.domain.enums.StatutCommande;
import com.donidoni.auth.metier.domain.enums.StatutEcheance;
import com.donidoni.auth.metier.dto.CommandeCreateDto;
import com.donidoni.auth.metier.dto.CommandeResponseDto;
import com.donidoni.auth.metier.dto.CommandeUpdateDto;
import com.donidoni.auth.metier.dto.EcheanceResponseDto;
import com.donidoni.auth.metier.dto.LigneCommandeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper des commandes.
 *
 * <p>La construction de l'entité (lignes, montants, échéancier) relève du
 * service : le mapper ne produit que la représentation sortante et applique
 * les mises à jour de suivi.</p>
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommandeMapper extends EntityMapper<Commande, CommandeCreateDto, CommandeUpdateDto, CommandeResponseDto> {

    /**
     * {@inheritDoc}
     *
     * <p>Non utilisé : une commande est assemblée par
     * {@code CommandeService#creerPourUtilisateurCourant}, qui résout les
     * articles et calcule les montants.</p>
     */
    @Override
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "boutique", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    @Mapping(target = "echeances", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "montantTotal", ignore = true)
    Commande toEntity(CommandeCreateDto createDto);

    @Override
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "boutique", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    @Mapping(target = "echeances", ignore = true)
    void updateEntity(CommandeUpdateDto updateDto, @MappingTarget Commande entity);

    @Override
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    @Mapping(target = "boutiqueId", source = "boutique.id")
    @Mapping(target = "boutiqueNom", source = "boutique.nom")
    @Mapping(target = "montantRegle", expression = "java(calculerMontantRegle(entity))")
    @Mapping(target = "resteAPayer", expression = "java(calculerResteAPayer(entity))")
    CommandeResponseDto toResponse(Commande entity);

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "imageArticle", source = "article.imagePrincipale")
    LigneCommandeResponseDto toLigneResponse(LigneCommande ligne);

    List<LigneCommandeResponseDto> toLigneResponses(List<LigneCommande> lignes);

    EcheanceResponseDto toEcheanceResponse(Echeance echeance);

    List<EcheanceResponseDto> toEcheanceResponses(List<Echeance> echeances);

    /**
     * Somme des montants effectivement encaissés.
     *
     * <p>Au comptant, la commande est soldée dès qu'elle quitte l'état
     * {@code EN_ATTENTE} sans avoir été annulée ; en échelonné, seules les
     * échéances marquées {@code PAYEE} sont comptées.</p>
     *
     * @param commande la commande évaluée
     * @return le montant réglé, en XOF
     */
    default BigDecimal calculerMontantRegle(final Commande commande) {
        if (commande.getModePaiement() == ModePaiement.COMPTANT) {
            final boolean solde = commande.getStatut() != StatutCommande.EN_ATTENTE
                    && commande.getStatut() != StatutCommande.ANNULEE;
            return solde ? commande.getMontantTotal() : BigDecimal.ZERO;
        }
        return commande.getEcheances().stream()
                .filter(e -> e.getStatut() == StatutEcheance.PAYEE)
                .map(Echeance::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * @param commande la commande évaluée
     * @return le solde restant dû, jamais négatif
     */
    default BigDecimal calculerResteAPayer(final Commande commande) {
        final BigDecimal reste = commande.getMontantTotal().subtract(calculerMontantRegle(commande));
        return reste.signum() < 0 ? BigDecimal.ZERO : reste;
    }
}
