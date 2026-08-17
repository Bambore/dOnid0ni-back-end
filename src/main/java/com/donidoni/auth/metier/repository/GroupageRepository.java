package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Groupage;
import com.donidoni.auth.metier.domain.enums.StatutGroupage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupageRepository extends BaseRepository<Groupage> {

    Page<Groupage> findByStatutAndDeletedFalse(StatutGroupage statut, Pageable pageable);

    Page<Groupage> findByPaysIdAndDeletedFalse(Long paysId, Pageable pageable);
}
