package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.metier.domain.Categorie;
import com.donidoni.auth.metier.domain.enums.TypeCategorie;
import com.donidoni.auth.metier.dto.CategorieCreateDto;
import com.donidoni.auth.metier.dto.CategorieResponseDto;
import com.donidoni.auth.metier.dto.CategorieUpdateDto;
import com.donidoni.auth.metier.mapper.CategorieMapper;
import com.donidoni.auth.metier.repository.CategorieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Gestion des catégories illustrées du marché et de l'annuaire des boutiques. */
@Service
public class CategorieService extends AbstractCrudService<Categorie, CategorieCreateDto, CategorieUpdateDto, CategorieResponseDto> {

    private final CategorieRepository categorieRepository;
    private final CategorieMapper categorieMapper;

    public CategorieService(final CategorieRepository repository, final CategorieMapper mapper) {
        super(repository, mapper);
        this.categorieRepository = repository;
        this.categorieMapper = mapper;
    }

    @Override
    protected String getResourceName() {
        return "Catégorie";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("nom", "type", "actif", "ordreAffichage", "createdAt", "updatedAt", "deleted");
    }

    /**
     * Catégories actives d'un domaine donné, dans l'ordre d'affichage du carrousel.
     *
     * @param type le domaine ciblé (articles ou boutiques)
     * @return la liste ordonnée des catégories actives
     */
    @Transactional(readOnly = true)
    public List<CategorieResponseDto> listerParType(final TypeCategorie type) {
        return categorieRepository.findByTypeAndActifTrueAndDeletedFalseOrderByOrdreAffichageAsc(type)
                .stream()
                .map(categorieMapper::toResponse)
                .toList();
    }
}
