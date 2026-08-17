package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Paiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaiementRepository extends BaseRepository<Paiement> {

    Page<Paiement> findByUtilisateurIdOrderByDateTransactionDesc(Long utilisateurId, Pageable pageable);

    Optional<Paiement> findByReference(String reference);

    long countByUtilisateurId(Long utilisateurId);
}
