package com.donidoni.auth.metier.dto;

/**
 * Compteurs de l'écran d'accueil : « Mes commandes », tontines, groupages,
 * cadeaux disponibles et notifications non lues.
 */
public record TableauDeBordDto(
        long nombreCommandes,
        long nombreTontines,
        long nombreGroupages,
        long nombreCadeauxDisponibles,
        long nombreNotificationsNonLues,
        long nombreFavoris
) {
}
