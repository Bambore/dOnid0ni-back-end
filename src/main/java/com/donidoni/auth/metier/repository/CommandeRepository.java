package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Commande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommandeRepository extends BaseRepository<Commande> {

    Page<Commande> findByUtilisateurIdAndDeletedFalseOrderByDateCommandeDesc(Long utilisateurId, Pageable pageable);

    Optional<Commande> findByReference(String reference);

    long countByUtilisateurId(Long utilisateurId);
}
