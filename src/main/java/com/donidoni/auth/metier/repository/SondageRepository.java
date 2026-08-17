package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Sondage;
import com.donidoni.auth.metier.domain.enums.StatutSondage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface SondageRepository extends BaseRepository<Sondage> {

    Page<Sondage> findByStatutAndDeletedFalse(StatutSondage statut, Pageable pageable);
}
