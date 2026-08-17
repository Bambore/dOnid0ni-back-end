package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Favori;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriRepository extends BaseRepository<Favori> {

    Page<Favori> findByUtilisateurId(Long utilisateurId, Pageable pageable);

    Optional<Favori> findByUtilisateurIdAndArticleId(Long utilisateurId, Long articleId);

    boolean existsByUtilisateurIdAndArticleId(Long utilisateurId, Long articleId);

    long countByUtilisateurId(Long utilisateurId);
}
