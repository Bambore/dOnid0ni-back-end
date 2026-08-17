package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.metier.domain.Paiement;
import com.donidoni.auth.metier.dto.PaiementResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PaiementMapper {

    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    @Mapping(target = "commandeId", source = "commande.id")
    @Mapping(target = "echeanceId", source = "echeance.id")
    @Mapping(target = "cotisationId", source = "cotisation.id")
    @Mapping(target = "participationGroupageId", source = "participationGroupage.id")
    PaiementResponseDto toResponse(Paiement entity);
}
