package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import com.donidoni.auth.metier.domain.enums.ModePaiement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Commande passée depuis la fiche produit.
 *
 * <p>{@code modePaiement = ECHELONNE} correspond au bouton « Petit à petit » :
 * {@code nombreEcheances} pilote alors la génération de l'échéancier.</p>
 */
public record CommandeCreateDto(

        @NotEmpty(groups = OnCreate.class, message = "La commande doit contenir au moins un article")
        @Valid
        List<LigneCommandeCreateDto> lignes,

        @NotNull(groups = OnCreate.class, message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement,

        @Min(value = 2, groups = OnCreate.class, message = "Un paiement échelonné compte au moins 2 échéances")
        Integer nombreEcheances,

        @Size(max = 255, groups = OnCreate.class, message = "L'adresse ne peut pas dépasser 255 caractères")
        String adresseLivraison,

        @Size(max = 20, groups = OnCreate.class, message = "Le téléphone ne peut pas dépasser 20 caractères")
        String telephoneLivraison
) {
}
