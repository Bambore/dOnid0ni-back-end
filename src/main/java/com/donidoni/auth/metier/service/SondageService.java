package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.exception.ErrorCode;
import com.donidoni.auth.metier.domain.OptionSondage;
import com.donidoni.auth.metier.domain.Pays;
import com.donidoni.auth.metier.domain.Sondage;
import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.domain.VoteSondage;
import com.donidoni.auth.metier.domain.enums.StatutSondage;
import com.donidoni.auth.metier.dto.OptionSondageCreateDto;
import com.donidoni.auth.metier.dto.OptionSondageResponseDto;
import com.donidoni.auth.metier.dto.ResultatSondageDto;
import com.donidoni.auth.metier.dto.SondageCreateDto;
import com.donidoni.auth.metier.dto.SondageResponseDto;
import com.donidoni.auth.metier.dto.SondageUpdateDto;
import com.donidoni.auth.metier.dto.VoteSondageRequestDto;
import com.donidoni.auth.metier.dto.VoteSondageResponseDto;
import com.donidoni.auth.metier.exception.MetierException;
import com.donidoni.auth.metier.mapper.SondageMapper;
import com.donidoni.auth.metier.repository.ArticleRepository;
import com.donidoni.auth.metier.repository.OptionSondageRepository;
import com.donidoni.auth.metier.repository.PaysRepository;
import com.donidoni.auth.metier.repository.SondageRepository;
import com.donidoni.auth.metier.repository.VoteSondageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Gestion des sondages « Quel groupage lancer ? ».
 *
 * <p>Chaque utilisateur soumet une proposition unique par sondage : un nouvel
 * envoi met à jour son vote au lieu d'en créer un second.</p>
 */
@Service
public class SondageService extends AbstractCrudService<Sondage, SondageCreateDto, SondageUpdateDto, SondageResponseDto> {

    private final SondageRepository sondageRepository;
    private final SondageMapper sondageMapper;
    private final OptionSondageRepository optionRepository;
    private final VoteSondageRepository voteRepository;
    private final PaysRepository paysRepository;
    private final ArticleRepository articleRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    public SondageService(final SondageRepository repository,
                          final SondageMapper mapper,
                          final OptionSondageRepository optionRepository,
                          final VoteSondageRepository voteRepository,
                          final PaysRepository paysRepository,
                          final ArticleRepository articleRepository,
                          final UtilisateurCourantService utilisateurCourantService) {
        super(repository, mapper);
        this.sondageRepository = repository;
        this.sondageMapper = mapper;
        this.optionRepository = optionRepository;
        this.voteRepository = voteRepository;
        this.paysRepository = paysRepository;
        this.articleRepository = articleRepository;
        this.utilisateurCourantService = utilisateurCourantService;
    }

    @Override
    protected String getResourceName() {
        return "Sondage";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("titre", "statut", "dateDebut", "dateFin", "createdAt", "updatedAt", "deleted");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Crée aussi, dans la même transaction, les produits proposés au vote
     * transmis avec le sondage.</p>
     */
    @Override
    @Transactional
    public SondageResponseDto create(final SondageCreateDto createDto) {
        final SondageResponseDto cree = super.create(createDto);
        if (createDto.options() != null && !createDto.options().isEmpty()) {
            ajouterOptions(cree.id(), createDto.options());
        }
        return findById(cree.id());
    }

    /**
     * Sondages d'un statut donné.
     *
     * @param statut   le statut recherché
     * @param pageable la pagination demandée
     * @return la page de sondages correspondants
     */
    @Transactional(readOnly = true)
    public PageResponse<SondageResponseDto> listerParStatut(final StatutSondage statut, final Pageable pageable) {
        return PageResponse.of(
                sondageRepository.findByStatutAndDeletedFalse(statut, pageable).map(sondageMapper::toResponse));
    }

    /**
     * Ajoute des produits à la grille de vote d'un sondage.
     *
     * @param sondageId l'identifiant du sondage
     * @param options   les produits proposés
     * @return les options du sondage après ajout
     */
    @Transactional
    public List<OptionSondageResponseDto> ajouterOptions(final Long sondageId,
                                                         final List<OptionSondageCreateDto> options) {
        final Sondage sondage = getEntityById(sondageId);

        for (OptionSondageCreateDto dto : options) {
            final OptionSondage option = sondageMapper.toOptionEntity(dto);
            option.setSondage(sondage);
            if (dto.articleId() != null) {
                option.setArticle(articleRepository.findById(dto.articleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Article", "id", dto.articleId())));
            }
            optionRepository.save(option);
        }

        return sondageMapper.toOptionResponses(optionRepository.findBySondageIdOrderByOrdreAffichageAsc(sondageId));
    }

    /**
     * @param sondageId l'identifiant du sondage
     * @return les produits proposés au vote, dans l'ordre d'affichage
     */
    @Transactional(readOnly = true)
    public List<OptionSondageResponseDto> listerOptions(final Long sondageId) {
        return sondageMapper.toOptionResponses(optionRepository.findBySondageIdOrderByOrdreAffichageAsc(sondageId));
    }

    /**
     * Enregistre la proposition de l'utilisateur courant (produit + destination).
     *
     * @param sondageId l'identifiant du sondage
     * @param requete   le produit et la destination choisis
     * @return le vote enregistré
     * @throws MetierException si le sondage est clos ou si l'option n'en fait pas partie
     */
    @Transactional
    public VoteSondageResponseDto voter(final Long sondageId, final VoteSondageRequestDto requete) {
        final Sondage sondage = getEntityById(sondageId);
        if (!sondage.estOuvert()) {
            throw new MetierException(ErrorCode.SONDAGE_CLOS);
        }

        final OptionSondage option = optionRepository.findById(requete.optionId())
                .orElseThrow(() -> new ResourceNotFoundException("Option de sondage", "id", requete.optionId()));
        if (!option.getSondage().getId().equals(sondageId)) {
            throw new MetierException(ErrorCode.OPTION_SONDAGE_INVALIDE);
        }

        final Pays pays = paysRepository.findById(requete.paysId())
                .orElseThrow(() -> new ResourceNotFoundException("Pays", "id", requete.paysId()));
        final Utilisateur utilisateur = utilisateurCourantService.getUtilisateurCourant();

        final VoteSondage vote = voteRepository
                .findBySondageIdAndUtilisateurId(sondageId, utilisateur.getId())
                .orElseGet(() -> {
                    final VoteSondage nouveau = new VoteSondage();
                    nouveau.setSondage(sondage);
                    nouveau.setUtilisateur(utilisateur);
                    return nouveau;
                });
        vote.setOption(option);
        vote.setPays(pays);

        return sondageMapper.toVoteResponse(voteRepository.save(vote));
    }

    /**
     * @param sondageId l'identifiant du sondage
     * @return le vote de l'utilisateur courant, s'il en a soumis un
     */
    @Transactional(readOnly = true)
    public Optional<VoteSondageResponseDto> monVote(final Long sondageId) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        return voteRepository.findBySondageIdAndUtilisateurId(sondageId, utilisateurId)
                .map(sondageMapper::toVoteResponse);
    }

    /**
     * Dépouillement d'un sondage, par couple produit/destination.
     *
     * @param sondageId l'identifiant du sondage
     * @return les résultats triés par nombre de votes décroissant
     */
    @Transactional(readOnly = true)
    public List<ResultatSondageDto> resultats(final Long sondageId) {
        final long total = voteRepository.countBySondageId(sondageId);
        return voteRepository.agregerResultats(sondageId).stream()
                .map(ligne -> {
                    final long votes = ((Number) ligne[4]).longValue();
                    final double pourcentage = total == 0 ? 0d : (votes * 100d) / total;
                    return new ResultatSondageDto(
                            (Long) ligne[0],
                            (String) ligne[1],
                            (Long) ligne[2],
                            (String) ligne[3],
                            votes,
                            Math.round(pourcentage * 100d) / 100d);
                })
                .toList();
    }
}
