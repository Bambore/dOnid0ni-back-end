package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.metier.domain.Cadeau;
import com.donidoni.auth.metier.domain.enums.StatutCadeau;
import com.donidoni.auth.metier.dto.CadeauCreateDto;
import com.donidoni.auth.metier.dto.CadeauResponseDto;
import com.donidoni.auth.metier.dto.CadeauUpdateDto;
import com.donidoni.auth.metier.mapper.CadeauMapper;
import com.donidoni.auth.metier.repository.CadeauRepository;
import com.donidoni.auth.metier.repository.UtilisateurRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;

/** Gestion des avantages attribués aux utilisateurs (« Mes Cadeaux »). */
@Service
public class CadeauService extends AbstractCrudService<Cadeau, CadeauCreateDto, CadeauUpdateDto, CadeauResponseDto> {

    private static final String ALPHABET_CODE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGUEUR_CODE = 8;

    private final CadeauRepository cadeauRepository;
    private final CadeauMapper cadeauMapper;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurCourantService utilisateurCourantService;
    private final SecureRandom aleatoire = new SecureRandom();

    public CadeauService(final CadeauRepository repository,
                         final CadeauMapper mapper,
                         final UtilisateurRepository utilisateurRepository,
                         final UtilisateurCourantService utilisateurCourantService) {
        super(repository, mapper);
        this.cadeauRepository = repository;
        this.cadeauMapper = mapper;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurCourantService = utilisateurCourantService;
    }

    @Override
    protected String getResourceName() {
        return "Cadeau";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("libelle", "type", "statut", "valeur", "code", "dateExpiration",
                "utilisateur", "createdAt", "updatedAt", "deleted");
    }

    @Override
    protected void beforeCreate(final Cadeau entity, final CadeauCreateDto createDto) {
        entity.setUtilisateur(utilisateurRepository.findById(createDto.utilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", createDto.utilisateurId())));
        entity.setCode(genererCode());
    }

    /**
     * Cadeaux de l'utilisateur courant.
     *
     * @param statut   filtre optionnel sur l'état du cadeau
     * @param pageable la pagination demandée
     * @return la page de cadeaux
     */
    @Transactional(readOnly = true)
    public PageResponse<CadeauResponseDto> mesCadeaux(final StatutCadeau statut, final Pageable pageable) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        final var page = statut == null
                ? cadeauRepository.findByUtilisateurIdAndDeletedFalse(utilisateurId, pageable)
                : cadeauRepository.findByUtilisateurIdAndStatutAndDeletedFalse(utilisateurId, statut, pageable);
        return PageResponse.of(page.map(cadeauMapper::toResponse));
    }

    /**
     * Marque un cadeau comme utilisé.
     *
     * @param id l'identifiant du cadeau
     * @return le cadeau mis à jour
     */
    @Transactional
    public CadeauResponseDto utiliser(final Long id) {
        final Cadeau cadeau = getEntityById(id);
        utilisateurCourantService.verifierProprietaire(cadeau.getUtilisateur().getId());
        cadeau.setStatut(StatutCadeau.UTILISE);
        return cadeauMapper.toResponse(cadeauRepository.save(cadeau));
    }

    /** Code alphanumérique sans caractères ambigus (ni O/0 ni I/1). */
    private String genererCode() {
        final StringBuilder code = new StringBuilder(LONGUEUR_CODE);
        for (int i = 0; i < LONGUEUR_CODE; i++) {
            code.append(ALPHABET_CODE.charAt(aleatoire.nextInt(ALPHABET_CODE.length())));
        }
        return code.toString();
    }
}
