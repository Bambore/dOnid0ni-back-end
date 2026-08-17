package com.donidoni.auth.metier.dto;

import java.time.Instant;

/** Article mis en favori par l'utilisateur courant. */
public record FavoriResponseDto(
        Long id,
        ArticleResponseDto article,
        Instant createdAt
) {
}
