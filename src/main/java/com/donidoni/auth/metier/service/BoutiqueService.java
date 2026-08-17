package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.metier.domain.Boutique;
import com.donidoni.auth.metier.dto.BoutiqueCreateDto;
import com.donidoni.auth.metier.dto.BoutiqueResponseDto;
import com.donidoni.auth.metier.dto.BoutiqueUpdateDto;
import com.donidoni.auth.metier.mapper.BoutiqueMapper;
import com.donidoni.auth.metier.repository.BoutiqueRepository;
import com.donidoni.auth.metier.repository.CategorieRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** Gestion de l'annuaire des boutiques partenaires. */
@Service
public class BoutiqueService extends AbstractCrudService<Boutique, BoutiqueCreateDto, BoutiqueUpdateDto, BoutiqueResponseDto> {

    private final BoutiqueRepository boutiqueRepository;
    private final BoutiqueMapper boutiqueMapper;
    private final CategorieRepository categorieRepository;

    public BoutiqueService(final BoutiqueRepository repository,
                           final BoutiqueMapper mapper,
                           final CategorieRepository categorieRepository) {
        super(repository, mapper);
        this.boutiqueRepository = repository;
        this.boutiqueMapper = mapper;
        this.categorieRepository = categorieRepository;
    }

    @Override
    protected String getResourceName() {
        return "Boutique";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("nom", "ville", "adresse", "telephone", "active", "categorie",
                "createdAt", "updatedAt", "deleted");
    }

    @Override
    protected void beforeCreate(final Boutique entity, final BoutiqueCreateDto createDto) {
        appliquerCategorie(entity, createDto.categorieId());
    }

    @Override
    protected void beforeUpdate(final Boutique entity, final BoutiqueUpdateDto updateDto) {
        appliquerCategorie(entity, updateDto.categorieId());
    }

    /**
     * Boutiques d'une catégorie donnée (filtre du carrousel mobile).
     *
     * @param categorieId l'identifiant de la catégorie
     * @param pageable    la pagination demandée
     * @return la page de boutiques actives de cette catégorie
     */
    @Transactional(readOnly = true)
    public PageResponse<BoutiqueResponseDto> listerParCategorie(final Long categorieId, final Pageable pageable) {
        return PageResponse.of(
                boutiqueRepository.findByCategorieIdAndActiveTrueAndDeletedFalse(categorieId, pageable)
                        .map(boutiqueMapper::toResponse));
    }

    private void appliquerCategorie(final Boutique entity, final Long categorieId) {
        if (categorieId == null) {
            return;
        }
        entity.setCategorie(categorieRepository.findById(categorieId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "id", categorieId)));
    }
}
