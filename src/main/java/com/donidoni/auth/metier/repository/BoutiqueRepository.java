package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Boutique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface BoutiqueRepository extends BaseRepository<Boutique> {

    Page<Boutique> findByCategorieIdAndActiveTrueAndDeletedFalse(Long categorieId, Pageable pageable);
}
