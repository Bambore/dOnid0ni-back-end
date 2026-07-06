package com.donidoni.auth.dto;

import com.donidoni.auth.crud.validation.OnCreate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProduitCreateDto(

        @NotBlank(groups = OnCreate.class, message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @NotNull(groups = OnCreate.class, message = "Le prix est obligatoire")
        @Min(value = 0, message = "Le prix ne peut pas être négatif")
        BigDecimal prix,

        @NotNull(groups = OnCreate.class, message = "Le stock est obligatoire")
        @Min(value = 0, message = "Le stock ne peut pas être négatif")
        Integer stock
) {
}
