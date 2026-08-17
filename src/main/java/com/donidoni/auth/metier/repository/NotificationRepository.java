package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Notification;
import com.donidoni.auth.metier.domain.enums.TypeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;

@Repository
public interface NotificationRepository extends BaseRepository<Notification> {

    /** Notifications personnelles + notifications de diffusion générale. */
    @Query("""
            SELECT n FROM Notification n
            WHERE n.deleted = false
              AND (n.utilisateur.id = :utilisateurId OR n.utilisateur IS NULL)
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findPourUtilisateur(@Param("utilisateurId") Long utilisateurId, Pageable pageable);

    @Query("""
            SELECT n FROM Notification n
            WHERE n.deleted = false
              AND n.type IN :types
              AND (n.utilisateur.id = :utilisateurId OR n.utilisateur IS NULL)
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> findPourUtilisateurParTypes(@Param("utilisateurId") Long utilisateurId,
                                                   @Param("types") Collection<TypeNotification> types,
                                                   Pageable pageable);

    @Query("""
            SELECT COUNT(n) FROM Notification n
            WHERE n.deleted = false AND n.lue = false
              AND (n.utilisateur.id = :utilisateurId OR n.utilisateur IS NULL)
            """)
    long compterNonLues(@Param("utilisateurId") Long utilisateurId);

    @Modifying
    @Query("""
            UPDATE Notification n SET n.lue = true, n.dateLecture = :maintenant
            WHERE n.lue = false AND n.deleted = false AND n.utilisateur.id = :utilisateurId
            """)
    int marquerToutesLues(@Param("utilisateurId") Long utilisateurId, @Param("maintenant") Instant maintenant);

    @Modifying
    @Query("""
            UPDATE Notification n SET n.deleted = true, n.deletedAt = :maintenant
            WHERE n.deleted = false AND n.utilisateur.id = :utilisateurId
            """)
    int supprimerToutes(@Param("utilisateurId") Long utilisateurId, @Param("maintenant") Instant maintenant);
}
