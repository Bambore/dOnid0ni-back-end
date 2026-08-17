package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.metier.domain.Pays;
import com.donidoni.auth.metier.dto.PaysCreateDto;
import com.donidoni.auth.metier.dto.PaysResponseDto;
import com.donidoni.auth.metier.dto.PaysUpdateDto;
import com.donidoni.auth.metier.mapper.PaysMapper;
import com.donidoni.auth.metier.repository.PaysRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Gestion des pays d'approvisionnement proposés aux groupages et aux sondages. */
@Service
public class PaysService extends AbstractCrudService<Pays, PaysCreateDto, PaysUpdateDto, PaysResponseDto> {

    private final PaysRepository paysRepository;
    private final PaysMapper paysMapper;

    public PaysService(final PaysRepository repository, final PaysMapper mapper) {
        super(repository, mapper);
        this.paysRepository = repository;
        this.paysMapper = mapper;
    }

    @Override
    protected String getResourceName() {
        return "Pays";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("nom", "codeIso", "actif", "createdAt", "updatedAt", "deleted");
    }

    /**
     * Liste des destinations sélectionnables dans le formulaire de sondage.
     *
     * @return les pays actifs, triés par nom
     */
    @Transactional(readOnly = true)
    public List<PaysResponseDto> listerActifs() {
        return paysRepository.findByActifTrueAndDeletedFalseOrderByNomAsc()
                .stream()
                .map(paysMapper::toResponse)
                .toList();
    }
}
