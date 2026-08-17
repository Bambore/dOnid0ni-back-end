package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.exception.ErrorCode;
import com.donidoni.auth.metier.domain.Groupage;
import com.donidoni.auth.metier.domain.ParticipationGroupage;
import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.domain.enums.StatutGroupage;
import com.donidoni.auth.metier.dto.GroupageCreateDto;
import com.donidoni.auth.metier.dto.GroupageResponseDto;
import com.donidoni.auth.metier.dto.GroupageUpdateDto;
import com.donidoni.auth.metier.dto.ParticipantResumeDto;
import com.donidoni.auth.metier.exception.MetierException;
import com.donidoni.auth.metier.mapper.GroupageMapper;
import com.donidoni.auth.metier.repository.ArticleRepository;
import com.donidoni.auth.metier.repository.GroupageRepository;
import com.donidoni.auth.metier.repository.ParticipationGroupageRepository;
import com.donidoni.auth.metier.repository.PaysRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Gestion des groupages et des adhésions associées.
 *
 * <p>Règles appliquées : un groupage n'accepte des adhésions que tant qu'il est
 * {@code OUVERT} et qu'il reste des places ; un même utilisateur ne peut adhérer
 * qu'une fois. Le passage automatique à {@code COMPLET} évite la surréservation
 * signalée côté mobile par le libellé « Groupage Complet ».</p>
 */
@Slf4j
@Service
public class GroupageService extends AbstractCrudService<Groupage, GroupageCreateDto, GroupageUpdateDto, GroupageResponseDto> {

    private final GroupageRepository groupageRepository;
    private final GroupageMapper groupageMapper;
    private final ParticipationGroupageRepository participationRepository;
    private final PaysRepository paysRepository;
    private final ArticleRepository articleRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    public GroupageService(final GroupageRepository repository,
                           final GroupageMapper mapper,
                           final ParticipationGroupageRepository participationRepository,
                           final PaysRepository paysRepository,
                           final ArticleRepository articleRepository,
                           final UtilisateurCourantService utilisateurCourantService) {
        super(repository, mapper);
        this.groupageRepository = repository;
        this.groupageMapper = mapper;
        this.participationRepository = participationRepository;
        this.paysRepository = paysRepository;
        this.articleRepository = articleRepository;
        this.utilisateurCourantService = utilisateurCourantService;
    }

    @Override
    protected String getResourceName() {
        return "Groupage";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("titre", "description", "montant", "statut", "pays", "article",
                "nombreParticipantsCible", "dateOuverture", "dateCloturePrevue",
                "createdAt", "updatedAt", "deleted");
    }

    @Override
    protected void beforeCreate(final Groupage entity, final GroupageCreateDto createDto) {
        appliquerRelations(entity, createDto.paysId(), createDto.articleId());
    }

    @Override
    protected void beforeUpdate(final Groupage entity, final GroupageUpdateDto updateDto) {
        appliquerRelations(entity, updateDto.paysId(), updateDto.articleId());
    }

    /**
     * Groupages filtrés par statut (onglet « Groupages disponibles » = {@code OUVERT}).
     *
     * @param statut   le statut recherché
     * @param pageable la pagination demandée
     * @return la page de groupages correspondants
     */
    @Transactional(readOnly = true)
    public PageResponse<GroupageResponseDto> listerParStatut(final StatutGroupage statut, final Pageable pageable) {
        return PageResponse.of(
                groupageRepository.findByStatutAndDeletedFalse(statut, pageable).map(groupageMapper::toResponse));
    }

    /**
     * Inscrit l'utilisateur courant à un groupage.
     *
     * @param groupageId l'identifiant du groupage rejoint
     * @return le participant nouvellement créé
     * @throws MetierException si le groupage est fermé, complet, ou déjà rejoint
     */
    @Transactional
    public ParticipantResumeDto participer(final Long groupageId) {
        final Groupage groupage = getEntityById(groupageId);
        final Utilisateur utilisateur = utilisateurCourantService.getUtilisateurCourant();

        if (groupage.getStatut() != StatutGroupage.OUVERT) {
            throw new MetierException(ErrorCode.GROUPAGE_FERME);
        }
        if (participationRepository.existsByGroupageIdAndUtilisateurId(groupageId, utilisateur.getId())) {
            throw new MetierException(ErrorCode.DEJA_PARTICIPANT, "Vous participez déjà à ce groupage");
        }

        final long inscrits = participationRepository.countByGroupageId(groupageId);
        if (inscrits >= groupage.getNombreParticipantsCible()) {
            groupage.setStatut(StatutGroupage.COMPLET);
            groupageRepository.save(groupage);
            throw new MetierException(ErrorCode.GROUPAGE_COMPLET);
        }

        final ParticipationGroupage participation = new ParticipationGroupage();
        participation.setGroupage(groupage);
        participation.setUtilisateur(utilisateur);
        final ParticipationGroupage enregistree = participationRepository.save(participation);

        if (inscrits + 1 >= groupage.getNombreParticipantsCible()) {
            groupage.setStatut(StatutGroupage.COMPLET);
            groupageRepository.save(groupage);
            log.info("Groupage #{} complet ({} participants)", groupageId, inscrits + 1);
        }

        return groupageMapper.toParticipant(enregistree);
    }

    /**
     * Retire l'utilisateur courant d'un groupage encore ouvert.
     *
     * @param groupageId l'identifiant du groupage quitté
     * @throws MetierException si aucune participation n'existe
     */
    @Transactional
    public void quitter(final Long groupageId) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        final ParticipationGroupage participation = participationRepository
                .findByGroupageIdAndUtilisateurId(groupageId, utilisateurId)
                .orElseThrow(() -> new MetierException(ErrorCode.PARTICIPATION_INTROUVABLE));

        final Groupage groupage = participation.getGroupage();
        if (groupage.getStatut() == StatutGroupage.EN_ACHEMINEMENT || groupage.getStatut() == StatutGroupage.CLOTURE) {
            throw new MetierException(ErrorCode.GROUPAGE_FERME,
                    "Le groupage est déjà engagé, la participation ne peut plus être retirée");
        }

        participationRepository.delete(participation);
        if (groupage.getStatut() == StatutGroupage.COMPLET) {
            groupage.setStatut(StatutGroupage.OUVERT);
            groupageRepository.save(groupage);
        }
    }

    /**
     * @param groupageId l'identifiant du groupage
     * @return les participants dans leur ordre d'adhésion
     */
    @Transactional(readOnly = true)
    public List<ParticipantResumeDto> listerParticipants(final Long groupageId) {
        return groupageMapper.toParticipants(
                participationRepository.findByGroupageIdOrderByDateAdhesionAsc(groupageId));
    }

    /**
     * Groupages auxquels l'utilisateur courant participe.
     *
     * @param pageable la pagination demandée
     * @return la page de groupages rejoints
     */
    @Transactional(readOnly = true)
    public PageResponse<GroupageResponseDto> mesGroupages(final Pageable pageable) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        return PageResponse.of(participationRepository.findByUtilisateurId(utilisateurId, pageable)
                .map(participation -> groupageMapper.toResponse(participation.getGroupage())));
    }

    private void appliquerRelations(final Groupage entity, final Long paysId, final Long articleId) {
        if (paysId != null) {
            entity.setPays(paysRepository.findById(paysId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pays", "id", paysId)));
        }
        if (articleId != null) {
            entity.setArticle(articleRepository.findById(articleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId)));
        }
    }
}
