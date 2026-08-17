package com.donidoni.auth.metier.dto;

import java.time.Instant;

/** Représentation d'un pays renvoyée au client. */
public record PaysResponseDto(
        Long id,
        String nom,
        String codeIso,
        String emojiDrapeau,
        boolean actif,
        Instant createdAt,
        Instant updatedAt
) {
}
