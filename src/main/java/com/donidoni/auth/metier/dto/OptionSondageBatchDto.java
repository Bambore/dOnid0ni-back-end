package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Lot de produits à ajouter à la grille de vote d'un sondage.
 *
 * <p>Enveloppe la liste dans un objet afin que la validation en cascade
 * s'applique bien à chaque option — Spring ne descend pas dans les éléments
 * d'une liste reçue directement comme corps de requête.</p>
 */
public record OptionSondageBatchDto(

        @NotEmpty(groups = OnCreate.class, message = "Au moins une option est requise")
        @Valid
        List<OptionSondageCreateDto> options
) {
}
