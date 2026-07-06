package com.donidoni.auth.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO portant les statistiques globales des comptes utilisateurs backoffice.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private long total;
    private long active;
    private long inactive;

    /**
     * Construit un {@code UserStatsDto} en déduisant le total depuis les deux compteurs.
     *
     * @param active   nombre d'utilisateurs actifs
     * @param inactive nombre d'utilisateurs inactifs
     * @return l'instance calculée
     */
    public static UserStatsDto of(final long active, final long inactive) {
        return UserStatsDto.builder()
            .active(active)
            .inactive(inactive)
            .total(active + inactive)
            .build();
    }
}
