package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.ParticipationGroupage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationGroupageRepository extends BaseRepository<ParticipationGroupage> {

    List<ParticipationGroupage> findByGroupageIdOrderByDateAdhesionAsc(Long groupageId);

    Optional<ParticipationGroupage> findByGroupageIdAndUtilisateurId(Long groupageId, Long utilisateurId);

    boolean existsByGroupageIdAndUtilisateurId(Long groupageId, Long utilisateurId);

    long countByGroupageId(Long groupageId);

    Page<ParticipationGroupage> findByUtilisateurId(Long utilisateurId, Pageable pageable);

    long countByUtilisateurId(Long utilisateurId);
}
