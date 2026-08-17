package com.donidoni.auth.metier.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Article et quantité demandés dans une commande.
 *
 * <p>Les contraintes portent le groupe {@code OnCreate} : il est propagé depuis
 * {@link CommandeCreateDto} lors de la validation en cascade.</p>
 */
public record LigneCommandeCreateDto(

        @NotNull(groups = OnCreate.class, message = "L'article est obligatoire")
        Long articleId,

        @NotNull(groups = OnCreate.class, message = "La quantité est obligatoire")
        @Min(value = 1, groups = OnCreate.class, message = "La quantité doit être d'au moins 1")
        Integer quantite
) {
}
