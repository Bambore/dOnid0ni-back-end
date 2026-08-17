package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Echeance;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcheanceRepository extends BaseRepository<Echeance> {

    List<Echeance> findByCommandeIdOrderByNumeroEcheanceAsc(Long commandeId);
}
