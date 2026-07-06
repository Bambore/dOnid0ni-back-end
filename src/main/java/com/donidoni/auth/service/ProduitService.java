package com.donidoni.auth.service;

import com.donidoni.auth.crud.mapper.ProduitMapper;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.domain.Produit;
import com.donidoni.auth.dto.ProduitCreateDto;
import com.donidoni.auth.dto.ProduitResponseDto;
import com.donidoni.auth.dto.ProduitUpdateDto;
import com.donidoni.auth.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProduitService extends AbstractCrudService<Produit, ProduitCreateDto, ProduitUpdateDto, ProduitResponseDto> {

    public ProduitService(final ProduitRepository repository, final ProduitMapper mapper) {
        super(repository, mapper);
    }

    @Override
    protected String getResourceName() {
        return "Produit";
    }

    @Override
    protected Set<String> getSearchableFields() {
        // Définir ici les champs autorisés pour la recherche Criteria
        return Set.of("nom", "description", "prix", "stock", "createdAt", "updatedAt", "deleted");
    }
}
