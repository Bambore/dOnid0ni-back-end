package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.ParticipationTontine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationTontineRepository extends BaseRepository<ParticipationTontine> {

    List<ParticipationTontine> findByTontineIdOrderByDateAdhesionAsc(Long tontineId);

    Optional<ParticipationTontine> findByTontineIdAndUtilisateurId(Long tontineId, Long utilisateurId);

    boolean existsByTontineIdAndUtilisateurId(Long tontineId, Long utilisateurId);

    long countByTontineId(Long tontineId);

    Page<ParticipationTontine> findByUtilisateurId(Long utilisateurId, Pageable pageable);

    long countByUtilisateurId(Long utilisateurId);
}
