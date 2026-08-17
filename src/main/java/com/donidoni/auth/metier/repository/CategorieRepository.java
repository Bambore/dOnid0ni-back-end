package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Categorie;
import com.donidoni.auth.metier.domain.enums.TypeCategorie;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategorieRepository extends BaseRepository<Categorie> {

    List<Categorie> findByTypeAndActifTrueAndDeletedFalseOrderByOrdreAffichageAsc(TypeCategorie type);
}
