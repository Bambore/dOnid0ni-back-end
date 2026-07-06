package com.donidoni.auth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProduitUpdateDto(

        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @Min(value = 0, message = "Le prix ne peut pas être négatif")
        BigDecimal prix,

        @Min(value = 0, message = "Le stock ne peut pas être négatif")
        Integer stock
) {
}
