package com.donidoni.auth.metier.repository;

import com.donidoni.auth.crud.repository.BaseRepository;
import com.donidoni.auth.metier.domain.Utilisateur;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends BaseRepository<Utilisateur> {

    Optional<Utilisateur> findByKeycloakId(String keycloakId);

    Optional<Utilisateur> findByTelephone(String telephone);

    Optional<Utilisateur> findByEmailIgnoreCase(String email);
}
