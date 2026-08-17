package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.OptionSondage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionSondageRepository extends BaseRepository<OptionSondage> {

    List<OptionSondage> findBySondageIdOrderByOrdreAffichageAsc(Long sondageId);
}
