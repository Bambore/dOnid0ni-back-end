package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.search.SearchRequest;
import com.donidoni.auth.crud.search.SpecificationBuilder;
import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.dto.UtilisateurResponseDto;
import com.donidoni.auth.metier.mapper.UtilisateurMapper;
import com.donidoni.auth.metier.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

/**
 * Consultation des comptes clients depuis le back-office.
 *
 * <p>Lecture seule au sens métier : les profils sont créés par l'application
 * mobile via Keycloak. L'administration peut seulement suspendre un compte ou
 * le désactiver.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUtilisateurService {

    /** Champs autorisés au filtrage — protège des accès à des colonnes arbitraires. */
    private static final Set<String> CHAMPS_FILTRABLES = Set.of(
            "nomComplet", "email", "telephone", "ville", "langue",
            "actif", "deleted", "derniereConnexion", "createdAt", "updatedAt");

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;

    /**
     * @param pageable la pagination demandée
     * @return la page de comptes clients
     */
    @Transactional(readOnly = true)
    public PageResponse<UtilisateurResponseDto> findAll(final Pageable pageable) {
        return PageResponse.of(utilisateurRepository.findAll(pageable).map(utilisateurMapper::toResponse));
    }

    /**
     * Recherche avancée sur les comptes clients.
     *
     * @param request les critères, la pagination et le tri
     * @return la page de comptes correspondants
     */
    @Transactional(readOnly = true)
    public PageResponse<UtilisateurResponseDto> search(final SearchRequest request) {
        final Specification<Utilisateur> spec =
                SpecificationBuilder.fromSearchRequest(request, CHAMPS_FILTRABLES);

        final Sort sort = request.isDescending()
                ? Sort.by(request.sortBy()).descending()
                : Sort.by(request.sortBy()).ascending();
        final Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        return PageResponse.of((spec != null
                ? utilisateurRepository.findAll(spec, pageable)
                : utilisateurRepository.findAll(pageable))
                .map(utilisateurMapper::toResponse));
    }

    /**
     * @param id l'identifiant du compte
     * @return le compte demandé
     */
    @Transactional(readOnly = true)
    public UtilisateurResponseDto findById(final Long id) {
        return utilisateurMapper.toResponse(getEntity(id));
    }

    /**
     * Suspend ou réactive un compte client.
     *
     * @param id l'identifiant du compte
     * @return le compte après bascule
     */
    @Transactional
    public UtilisateurResponseDto basculerActivation(final Long id) {
        final Utilisateur utilisateur = getEntity(id);
        utilisateur.setActif(!utilisateur.isActif());
        log.info("Compte client #{} {}", id, utilisateur.isActif() ? "réactivé" : "suspendu");
        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    /**
     * Désactive un compte client (soft-delete) : l'historique reste consultable.
     *
     * @param id l'identifiant du compte
     */
    @Transactional
    public void supprimer(final Long id) {
        final Utilisateur utilisateur = getEntity(id);
        utilisateur.setActif(false);
        utilisateur.setDeleted(true);
        utilisateur.setDeletedAt(Instant.now());
        utilisateurRepository.save(utilisateur);
    }

    private Utilisateur getEntity(final Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
    }
}
