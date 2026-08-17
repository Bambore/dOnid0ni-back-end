package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Tontine;
import com.donidoni.auth.metier.domain.enums.StatutTontine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface TontineRepository extends BaseRepository<Tontine> {

    Page<Tontine> findByStatutAndDeletedFalse(StatutTontine statut, Pageable pageable);
}
