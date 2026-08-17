package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends BaseRepository<Article> {

    Page<Article> findByCategorieIdAndDeletedFalse(Long categorieId, Pageable pageable);

    Page<Article> findByBoutiqueIdAndDeletedFalse(Long boutiqueId, Pageable pageable);

    /**
     * Recherche plein texte simple sur le nom et la description
     * (écran « Rechercher un produit » du mobile).
     *
     * @param terme    le terme saisi par l'utilisateur
     * @param pageable la pagination demandée
     * @return la page d'articles correspondants
     */
    @Query("""
            SELECT a FROM Article a
            WHERE a.deleted = false
              AND (LOWER(a.nom) LIKE LOWER(CONCAT('%', :terme, '%'))
                   OR LOWER(a.description) LIKE LOWER(CONCAT('%', :terme, '%')))
            """)
    Page<Article> rechercher(@Param("terme") String terme, Pageable pageable);
}
