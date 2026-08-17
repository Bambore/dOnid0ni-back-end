package com.donidoni.auth.metier.service;

import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.dto.ProfilUpdateDto;
import com.donidoni.auth.metier.dto.TableauDeBordDto;
import com.donidoni.auth.metier.dto.UtilisateurResponseDto;
import com.donidoni.auth.metier.mapper.UtilisateurMapper;
import com.donidoni.auth.metier.repository.CadeauRepository;
import com.donidoni.auth.metier.repository.CommandeRepository;
import com.donidoni.auth.metier.repository.FavoriRepository;
import com.donidoni.auth.metier.repository.NotificationRepository;
import com.donidoni.auth.metier.repository.ParticipationGroupageRepository;
import com.donidoni.auth.metier.repository.ParticipationTontineRepository;
import com.donidoni.auth.metier.repository.UtilisateurRepository;
import com.donidoni.auth.metier.domain.enums.StatutCadeau;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Profil de l'utilisateur connecté et compteurs de l'écran d'accueil.
 *
 * <p>Couvre l'écran « Mon Profil » : informations personnelles, préférences
 * (langue, thème sombre, sécurité biométrique, notifications) et suppression
 * de compte.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final UtilisateurCourantService utilisateurCourantService;
    private final CommandeRepository commandeRepository;
    private final ParticipationTontineRepository participationTontineRepository;
    private final ParticipationGroupageRepository participationGroupageRepository;
    private final CadeauRepository cadeauRepository;
    private final NotificationRepository notificationRepository;
    private final FavoriRepository favoriRepository;

    /**
     * @return le profil de l'utilisateur connecté
     */
    @Transactional
    public UtilisateurResponseDto monProfil() {
        return utilisateurMapper.toResponse(utilisateurCourantService.getUtilisateurCourant());
    }

    /**
     * Met à jour le profil et les préférences de l'utilisateur connecté.
     *
     * @param dto les champs à modifier ; les champs nuls sont ignorés
     * @return le profil mis à jour
     */
    @Transactional
    public UtilisateurResponseDto mettreAJourMonProfil(final ProfilUpdateDto dto) {
        final Utilisateur utilisateur = utilisateurCourantService.getUtilisateurCourant();
        utilisateurMapper.updateProfil(dto, utilisateur);
        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    /**
     * Suppression de compte demandée depuis le profil.
     *
     * <p>Réalise un soft-delete et désactive le compte : l'historique de
     * commandes et de cotisations reste exploitable côté comptabilité.
     * La suppression du compte Keycloak associé relève de l'administration.</p>
     */
    @Transactional
    public void supprimerMonCompte() {
        final Utilisateur utilisateur = utilisateurCourantService.getUtilisateurCourant();
        utilisateur.setActif(false);
        utilisateur.setDeleted(true);
        utilisateur.setDeletedAt(Instant.now());
        utilisateurRepository.save(utilisateur);
        log.info("Compte #{} supprimé à la demande de son titulaire", utilisateur.getId());
    }

    /**
     * Compteurs affichés sur l'accueil (commandes, tontines, groupages,
     * cadeaux disponibles, notifications non lues et favoris).
     *
     * @return le tableau de bord de l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public TableauDeBordDto tableauDeBord() {
        final Long id = utilisateurCourantService.getIdUtilisateurCourant();
        return new TableauDeBordDto(
                commandeRepository.countByUtilisateurId(id),
                participationTontineRepository.countByUtilisateurId(id),
                participationGroupageRepository.countByUtilisateurId(id),
                cadeauRepository.countByUtilisateurIdAndStatutAndDeletedFalse(id, StatutCadeau.DISPONIBLE),
                notificationRepository.compterNonLues(id),
                favoriRepository.countByUtilisateurId(id));
    }
}
