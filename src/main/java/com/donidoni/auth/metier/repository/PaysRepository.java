package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Pays;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaysRepository extends BaseRepository<Pays> {

    Optional<Pays> findByCodeIsoIgnoreCase(String codeIso);

    List<Pays> findByActifTrueAndDeletedFalseOrderByNomAsc();
}
