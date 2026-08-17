package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.search.SearchRequest;
import com.donidoni.auth.crud.search.SpecificationBuilder;
import com.donidoni.auth.exception.ErrorCode;
import com.donidoni.auth.metier.domain.Commande;
import com.donidoni.auth.metier.domain.CotisationTontine;
import com.donidoni.auth.metier.domain.Echeance;
import com.donidoni.auth.metier.domain.Paiement;
import com.donidoni.auth.metier.domain.ParticipationGroupage;
import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.domain.enums.StatutCommande;
import com.donidoni.auth.metier.domain.enums.StatutEcheance;
import com.donidoni.auth.metier.domain.enums.StatutPaiement;
import com.donidoni.auth.metier.domain.enums.StatutParticipation;
import com.donidoni.auth.metier.dto.PaiementCreateDto;
import com.donidoni.auth.metier.dto.PaiementResponseDto;
import com.donidoni.auth.metier.exception.MetierException;
import com.donidoni.auth.metier.mapper.PaiementMapper;
import com.donidoni.auth.metier.repository.CommandeRepository;
import com.donidoni.auth.metier.repository.CotisationTontineRepository;
import com.donidoni.auth.metier.repository.EcheanceRepository;
import com.donidoni.auth.metier.repository.PaiementRepository;
import com.donidoni.auth.metier.repository.ParticipationGroupageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * Encaissement des paiements et report du règlement sur la cible concernée.
 *
 * <p>Un paiement vise exactement une cible : une commande au comptant, une
 * échéance « Petit à petit », une cotisation de tontine ou une quote-part de
 * groupage. La confirmation met à jour l'objet réglé — c'est elle que
 * l'agrégateur mobile money appelle en retour.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaiementService {

    /** Champs autorisés au filtrage depuis le back-office. */
    private static final Set<String> CHAMPS_FILTRABLES = Set.of(
            "reference", "montant", "moyen", "statut", "referenceExterne",
            "dateTransaction", "createdAt", "updatedAt");

    private final PaiementRepository paiementRepository;
    private final PaiementMapper paiementMapper;
    private final CommandeRepository commandeRepository;
    private final EcheanceRepository echeanceRepository;
    private final CotisationTontineRepository cotisationRepository;
    private final ParticipationGroupageRepository participationGroupageRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    /**
     * Initie un paiement pour l'utilisateur courant.
     *
     * @param dto le montant, le moyen et la cible du règlement
     * @return le paiement à l'état {@code INITIE}
     * @throws MetierException si la cible n'est pas unique ou est déjà réglée
     */
    @Transactional
    public PaiementResponseDto initier(final PaiementCreateDto dto) {
        final Utilisateur utilisateur = utilisateurCourantService.getUtilisateurCourant();

        final long ciblesRenseignees = compterCibles(dto);
        if (ciblesRenseignees != 1) {
            throw new MetierException(ErrorCode.CIBLE_PAIEMENT_INVALIDE);
        }

        final Paiement paiement = new Paiement();
        paiement.setUtilisateur(utilisateur);
        paiement.setMontant(dto.montant());
        paiement.setMoyen(dto.moyen());
        paiement.setReference("PAY-TEMP");
        rattacherCible(paiement, dto);

        Paiement enregistre = paiementRepository.save(paiement);
        enregistre.setReference(String.format("PAY-%d-%06d",
                LocalDate.now(ZoneOffset.UTC).getYear(), enregistre.getId()));
        enregistre = paiementRepository.save(enregistre);

        log.info("Paiement {} initié — {} XOF via {}",
                enregistre.getReference(), dto.montant(), dto.moyen());
        return paiementMapper.toResponse(enregistre);
    }

    /**
     * Confirme un paiement encaissé et solde la cible correspondante.
     *
     * @param paiementId        l'identifiant du paiement
     * @param referenceExterne  la référence renvoyée par l'agrégateur
     * @return le paiement à l'état {@code REUSSI}
     */
    @Transactional
    public PaiementResponseDto confirmer(final Long paiementId, final String referenceExterne) {
        final Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", paiementId));

        paiement.setStatut(StatutPaiement.REUSSI);
        paiement.setReferenceExterne(referenceExterne);
        appliquerSurCible(paiement);

        log.info("Paiement {} confirmé", paiement.getReference());
        return paiementMapper.toResponse(paiementRepository.save(paiement));
    }

    /**
     * Marque un paiement comme échoué.
     *
     * @param paiementId l'identifiant du paiement
     * @param motif      le motif transmis par l'agrégateur
     * @return le paiement à l'état {@code ECHOUE}
     */
    @Transactional
    public PaiementResponseDto echouer(final Long paiementId, final String motif) {
        final Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", paiementId));

        paiement.setStatut(StatutPaiement.ECHOUE);
        paiement.setMessage(motif);
        return paiementMapper.toResponse(paiementRepository.save(paiement));
    }

    /**
     * Historique des paiements de l'utilisateur courant.
     *
     * @param pageable la pagination demandée
     * @return la page de paiements, du plus récent au plus ancien
     */
    @Transactional(readOnly = true)
    public PageResponse<PaiementResponseDto> mesPaiements(final Pageable pageable) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        return PageResponse.of(paiementRepository
                .findByUtilisateurIdOrderByDateTransactionDesc(utilisateurId, pageable)
                .map(paiementMapper::toResponse));
    }

    /**
     * Journal complet des paiements — réservé au back-office.
     *
     * @param pageable la pagination demandée
     * @return la page de paiements
     */
    @Transactional(readOnly = true)
    public PageResponse<PaiementResponseDto> findAll(final Pageable pageable) {
        return PageResponse.of(paiementRepository.findAll(pageable).map(paiementMapper::toResponse));
    }

    /**
     * Recherche avancée sur le journal des paiements.
     *
     * @param request les critères, la pagination et le tri
     * @return la page de paiements correspondants
     */
    @Transactional(readOnly = true)
    public PageResponse<PaiementResponseDto> search(final SearchRequest request) {
        final Specification<Paiement> spec =
                SpecificationBuilder.fromSearchRequest(request, CHAMPS_FILTRABLES);

        final Sort sort = request.isDescending()
                ? Sort.by(request.sortBy()).descending()
                : Sort.by(request.sortBy()).ascending();
        final Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        return PageResponse.of((spec != null
                ? paiementRepository.findAll(spec, pageable)
                : paiementRepository.findAll(pageable))
                .map(paiementMapper::toResponse));
    }

    private long compterCibles(final PaiementCreateDto dto) {
        return java.util.stream.Stream
                .of(dto.commandeId(), dto.echeanceId(), dto.cotisationId(), dto.participationGroupageId())
                .filter(java.util.Objects::nonNull)
                .count();
    }

    private void rattacherCible(final Paiement paiement, final PaiementCreateDto dto) {
        if (dto.commandeId() != null) {
            final Commande commande = commandeRepository.findById(dto.commandeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Commande", "id", dto.commandeId()));
            utilisateurCourantService.verifierProprietaire(commande.getUtilisateur().getId());
            paiement.setCommande(commande);
        }
        if (dto.echeanceId() != null) {
            final Echeance echeance = echeanceRepository.findById(dto.echeanceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Échéance", "id", dto.echeanceId()));
            utilisateurCourantService.verifierProprietaire(echeance.getCommande().getUtilisateur().getId());
            if (echeance.getStatut() == StatutEcheance.PAYEE) {
                throw new MetierException(ErrorCode.ECHEANCE_DEJA_REGLEE);
            }
            paiement.setEcheance(echeance);
        }
        if (dto.cotisationId() != null) {
            final CotisationTontine cotisation = cotisationRepository.findById(dto.cotisationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cotisation", "id", dto.cotisationId()));
            utilisateurCourantService.verifierProprietaire(cotisation.getParticipation().getUtilisateur().getId());
            if (cotisation.getStatut() == StatutEcheance.PAYEE) {
                throw new MetierException(ErrorCode.ECHEANCE_DEJA_REGLEE);
            }
            paiement.setCotisation(cotisation);
        }
        if (dto.participationGroupageId() != null) {
            final ParticipationGroupage participation = participationGroupageRepository
                    .findById(dto.participationGroupageId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Participation au groupage", "id", dto.participationGroupageId()));
            utilisateurCourantService.verifierProprietaire(participation.getUtilisateur().getId());
            paiement.setParticipationGroupage(participation);
        }
    }

    private void appliquerSurCible(final Paiement paiement) {
        final Instant maintenant = Instant.now();

        if (paiement.getCommande() != null) {
            final Commande commande = paiement.getCommande();
            commande.setStatut(StatutCommande.CONFIRMEE);
            commandeRepository.save(commande);
        }
        if (paiement.getEcheance() != null) {
            final Echeance echeance = paiement.getEcheance();
            echeance.setStatut(StatutEcheance.PAYEE);
            echeance.setDatePaiement(maintenant);
            echeanceRepository.save(echeance);
            confirmerCommandeSiPremiereEcheance(echeance);
        }
        if (paiement.getCotisation() != null) {
            final CotisationTontine cotisation = paiement.getCotisation();
            cotisation.setStatut(StatutEcheance.PAYEE);
            cotisation.setDatePaiement(maintenant);
            cotisationRepository.save(cotisation);

            final var participation = cotisation.getParticipation();
            participation.setStatut(StatutParticipation.CONFIRMEE);
            participation.setMontantTotalVerse(
                    participation.getMontantTotalVerse().add(paiement.getMontant()));
        }
        if (paiement.getParticipationGroupage() != null) {
            final ParticipationGroupage participation = paiement.getParticipationGroupage();
            participation.setStatut(StatutParticipation.CONFIRMEE);
            participation.setMontantVerse(participation.getMontantVerse().add(paiement.getMontant()));
            participationGroupageRepository.save(participation);
        }
    }

    private void confirmerCommandeSiPremiereEcheance(final Echeance echeance) {
        final Commande commande = echeance.getCommande();
        if (commande.getStatut() == StatutCommande.EN_ATTENTE) {
            commande.setStatut(StatutCommande.CONFIRMEE);
            commandeRepository.save(commande);
        }
    }
}
