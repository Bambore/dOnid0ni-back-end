package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.VoteSondage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteSondageRepository extends BaseRepository<VoteSondage> {

    Optional<VoteSondage> findBySondageIdAndUtilisateurId(Long sondageId, Long utilisateurId);

    long countBySondageId(Long sondageId);

    /**
     * Agrège les votes d'un sondage par couple (option, pays).
     *
     * @param sondageId l'identifiant du sondage
     * @return des lignes {@code [optionId, libelle, paysId, nomPays, nombreVotes]}
     *         triées par nombre de votes décroissant
     */
    @Query("""
            SELECT v.option.id, v.option.libelle, v.pays.id, v.pays.nom, COUNT(v)
            FROM VoteSondage v
            WHERE v.sondage.id = :sondageId
            GROUP BY v.option.id, v.option.libelle, v.pays.id, v.pays.nom
            ORDER BY COUNT(v) DESC
            """)
    List<Object[]> agregerResultats(@Param("sondageId") Long sondageId);
}
