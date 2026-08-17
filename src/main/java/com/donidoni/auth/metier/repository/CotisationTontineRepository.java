package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.CotisationTontine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CotisationTontineRepository extends BaseRepository<CotisationTontine> {

    List<CotisationTontine> findByParticipationIdOrderByNumeroEcheanceAsc(Long participationId);
}
