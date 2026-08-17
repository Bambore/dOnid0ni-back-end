package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Cadeau;
import com.donidoni.auth.metier.domain.enums.StatutCadeau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface CadeauRepository extends BaseRepository<Cadeau> {

    Page<Cadeau> findByUtilisateurIdAndDeletedFalse(Long utilisateurId, Pageable pageable);

    Page<Cadeau> findByUtilisateurIdAndStatutAndDeletedFalse(Long utilisateurId, StatutCadeau statut, Pageable pageable);

    long countByUtilisateurIdAndStatutAndDeletedFalse(Long utilisateurId, StatutCadeau statut);
}
