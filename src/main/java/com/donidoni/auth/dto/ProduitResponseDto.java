package com.donidoni.auth.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProduitResponseDto(
        Long id,
        String nom,
        String description,
        BigDecimal prix,
        Integer stock,
        Instant createdAt,
        Instant updatedAt,
        boolean deleted
) {
}
