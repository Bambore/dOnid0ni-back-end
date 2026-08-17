package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.exception.ErrorCode;
import com.donidoni.auth.metier.domain.CotisationTontine;
import com.donidoni.auth.metier.domain.ParticipationTontine;
import com.donidoni.auth.metier.domain.Tontine;
import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.domain.enums.StatutTontine;
import com.donidoni.auth.metier.dto.CotisationResponseDto;
import com.donidoni.auth.metier.dto.ParticipantResumeDto;
import com.donidoni.auth.metier.dto.TontineCreateDto;
import com.donidoni.auth.metier.dto.TontineResponseDto;
import com.donidoni.auth.metier.dto.TontineUpdateDto;
import com.donidoni.auth.metier.exception.MetierException;
import com.donidoni.auth.metier.mapper.TontineMapper;
import com.donidoni.auth.metier.repository.ArticleRepository;
import com.donidoni.auth.metier.repository.CotisationTontineRepository;
import com.donidoni.auth.metier.repository.ParticipationTontineRepository;
import com.donidoni.auth.metier.repository.TontineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Gestion des tontines, des adhésions et de l'échéancier de cotisations.
 *
 * <p>Une adhésion n'est possible que tant que la tontine est {@code EN_ATTENTE} :
 * une fois le cycle démarré, l'ordre de tirage est figé. Quand la dernière place
 * est prise, la tontine passe {@code EN_COURS} et l'échéancier mensuel de chaque
 * participant est généré.</p>
 */
@Slf4j
@Service
public class TontineService extends AbstractCrudService<Tontine, TontineCreateDto, TontineUpdateDto, TontineResponseDto> {

    private final TontineRepository tontineRepository;
    private final TontineMapper tontineMapper;
    private final ParticipationTontineRepository participationRepository;
    private final CotisationTontineRepository cotisationRepository;
    private final ArticleRepository articleRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    public TontineService(final TontineRepository repository,
                          final TontineMapper mapper,
                          final ParticipationTontineRepository participationRepository,
                          final CotisationTontineRepository cotisationRepository,
                          final ArticleRepository articleRepository,
                          final UtilisateurCourantService utilisateurCourantService) {
        super(repository, mapper);
        this.tontineRepository = repository;
        this.tontineMapper = mapper;
        this.participationRepository = participationRepository;
        this.cotisationRepository = cotisationRepository;
        this.articleRepository = articleRepository;
        this.utilisateurCourantService = utilisateurCourantService;
    }

    @Override
    protected String getResourceName() {
        return "Tontine";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("titre", "description", "montantMensuel", "statut", "article",
                "nombreParticipantsCible", "dureeMois", "dateDebut", "dateFin",
                "createdAt", "updatedAt", "deleted");
    }

    @Override
    protected void beforeCreate(final Tontine entity, final TontineCreateDto createDto) {
        appliquerArticle(entity, createDto.articleId());
        if (entity.getDureeMois() == null) {
            entity.setDureeMois(entity.getNombreParticipantsCible());
        }
    }

    @Override
    protected void beforeUpdate(final Tontine entity, final TontineUpdateDto updateDto) {
        appliquerArticle(entity, updateDto.articleId());
    }

    /**
     * Tontines d'un statut donné — alimente les trois onglets du mobile.
     *
     * @param statut   le statut recherché
     * @param pageable la pagination demandée
     * @return la page de tontines correspondantes
     */
    @Transactional(readOnly = true)
    public PageResponse<TontineResponseDto> listerParStatut(final StatutTontine statut, final Pageable pageable) {
        return PageResponse.of(
                tontineRepository.findByStatutAndDeletedFalse(statut, pageable).map(tontineMapper::toResponse));
    }

    /**
     * Inscrit l'utilisateur courant à une tontine encore en attente.
     *
     * @param tontineId l'identifiant de la tontine rejointe
     * @return le participant nouvellement créé
     * @throws MetierException si la tontine est démarrée, complète ou déjà rejointe
     */
    @Transactional
    public ParticipantResumeDto participer(final Long tontineId) {
        final Tontine tontine = getEntityById(tontineId);
        final Utilisateur utilisateur = utilisateurCourantService.getUtilisateurCourant();

        if (tontine.getStatut() != StatutTontine.EN_ATTENTE) {
            throw new MetierException(ErrorCode.TONTINE_FERMEE);
        }
        if (participationRepository.existsByTontineIdAndUtilisateurId(tontineId, utilisateur.getId())) {
            throw new MetierException(ErrorCode.DEJA_PARTICIPANT, "Vous participez déjà à cette tontine");
        }

        final long inscrits = participationRepository.countByTontineId(tontineId);
        if (inscrits >= tontine.getNombreParticipantsCible()) {
            throw new MetierException(ErrorCode.TONTINE_COMPLETE);
        }

        final ParticipationTontine participation = new ParticipationTontine();
        participation.setTontine(tontine);
        participation.setUtilisateur(utilisateur);
        participation.setRangTirage((int) inscrits + 1);
        final ParticipationTontine enregistree = participationRepository.save(participation);

        if (inscrits + 1 >= tontine.getNombreParticipantsCible()) {
            demarrer(tontine);
        }

        return tontineMapper.toParticipant(enregistree);
    }

    /**
     * @param tontineId l'identifiant de la tontine
     * @return les participants dans leur ordre d'adhésion
     */
    @Transactional(readOnly = true)
    public List<ParticipantResumeDto> listerParticipants(final Long tontineId) {
        return tontineMapper.toParticipants(
                participationRepository.findByTontineIdOrderByDateAdhesionAsc(tontineId));
    }

    /**
     * Échéancier de cotisations de l'utilisateur courant pour une tontine.
     *
     * @param tontineId l'identifiant de la tontine
     * @return les cotisations, de la première à la dernière
     * @throws MetierException si l'utilisateur ne participe pas à cette tontine
     */
    @Transactional(readOnly = true)
    public List<CotisationResponseDto> mesCotisations(final Long tontineId) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        final ParticipationTontine participation = participationRepository
                .findByTontineIdAndUtilisateurId(tontineId, utilisateurId)
                .orElseThrow(() -> new MetierException(ErrorCode.PARTICIPATION_INTROUVABLE));

        return tontineMapper.toCotisations(
                cotisationRepository.findByParticipationIdOrderByNumeroEcheanceAsc(participation.getId()));
    }

    /**
     * Tontines auxquelles l'utilisateur courant participe.
     *
     * @param pageable la pagination demandée
     * @return la page de tontines rejointes
     */
    @Transactional(readOnly = true)
    public PageResponse<TontineResponseDto> mesTontines(final Pageable pageable) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        return PageResponse.of(participationRepository.findByUtilisateurId(utilisateurId, pageable)
                .map(participation -> tontineMapper.toResponse(participation.getTontine())));
    }

    /**
     * Démarre le cycle : la tontine passe {@code EN_COURS} et chaque participant
     * reçoit son échéancier mensuel.
     */
    private void demarrer(final Tontine tontine) {
        final LocalDate debut = tontine.getDateDebut() != null ? tontine.getDateDebut() : LocalDate.now();
        final int duree = tontine.getDureeMois() != null
                ? tontine.getDureeMois()
                : tontine.getNombreParticipantsCible();

        tontine.setStatut(StatutTontine.EN_COURS);
        tontine.setDateDebut(debut);
        tontine.setDateFin(debut.plusMonths(duree));
        tontineRepository.save(tontine);

        for (ParticipationTontine participation : participationRepository.findByTontineIdOrderByDateAdhesionAsc(tontine.getId())) {
            genererEcheancier(participation, tontine, debut, duree);
        }
        log.info("Tontine #{} démarrée sur {} mois", tontine.getId(), duree);
    }

    private void genererEcheancier(final ParticipationTontine participation,
                                   final Tontine tontine,
                                   final LocalDate debut,
                                   final int duree) {
        if (!cotisationRepository.findByParticipationIdOrderByNumeroEcheanceAsc(participation.getId()).isEmpty()) {
            return;
        }
        for (int mois = 1; mois <= duree; mois++) {
            final CotisationTontine cotisation = new CotisationTontine();
            cotisation.setParticipation(participation);
            cotisation.setNumeroEcheance(mois);
            cotisation.setMontant(tontine.getMontantMensuel());
            cotisation.setDateEcheance(debut.plusMonths(mois - 1L));
            cotisationRepository.save(cotisation);
        }
    }

    private void appliquerArticle(final Tontine entity, final Long articleId) {
        if (articleId != null) {
            entity.setArticle(articleRepository.findById(articleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId)));
        }
    }
}
