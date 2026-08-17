package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnUpdate;
import com.donidoni.auth.metier.domain.enums.StatutCommande;
import jakarta.validation.constraints.Size;

/**
 * Mise à jour d'une commande : suivi logistique et coordonnées de livraison.
 *
 * <p>Ni les lignes ni le montant total ne sont modifiables après création.</p>
 */
public record CommandeUpdateDto(

        StatutCommande statut,

        @Size(max = 255, groups = OnUpdate.class, message = "L'adresse ne peut pas dépasser 255 caractères")
        String adresseLivraison,

        @Size(max = 20, groups = OnUpdate.class, message = "Le téléphone ne peut pas dépasser 20 caractères")
        String telephoneLivraison
) {
}
