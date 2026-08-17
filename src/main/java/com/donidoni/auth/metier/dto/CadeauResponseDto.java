package com.donidoni.auth.metier.dto;

import com.donidoni.auth.metier.domain.enums.StatutCadeau;
import com.donidoni.auth.metier.domain.enums.TypeCadeau;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Cadeau affiché dans « Mes Cadeaux ». */
public record CadeauResponseDto(
        Long id,
        Long utilisateurId,
        String libelle,
        String description,
        String imageUrl,
        TypeCadeau type,
        BigDecimal valeur,
        String code,
        StatutCadeau statut,
        LocalDate dateExpiration,
        Instant createdAt
) {
}
