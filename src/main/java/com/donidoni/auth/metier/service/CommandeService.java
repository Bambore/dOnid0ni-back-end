package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.exception.ErrorCode;
import com.donidoni.auth.metier.domain.Article;
import com.donidoni.auth.metier.domain.Commande;
import com.donidoni.auth.metier.domain.Echeance;
import com.donidoni.auth.metier.domain.LigneCommande;
import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.domain.enums.ModePaiement;
import com.donidoni.auth.metier.domain.enums.StatutCommande;
import com.donidoni.auth.metier.dto.CommandeCreateDto;
import com.donidoni.auth.metier.dto.CommandeResponseDto;
import com.donidoni.auth.metier.dto.CommandeUpdateDto;
import com.donidoni.auth.metier.dto.LigneCommandeCreateDto;
import com.donidoni.auth.metier.exception.MetierException;
import com.donidoni.auth.metier.mapper.CommandeMapper;
import com.donidoni.auth.metier.repository.ArticleRepository;
import com.donidoni.auth.metier.repository.CommandeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * Gestion des commandes passées depuis le marché.
 *
 * <p>À la création, le service fige les libellés et prix unitaires, décrémente
 * le stock et, pour le mode « Petit à petit », génère l'échéancier mensuel.
 * Le prix appliqué est toujours celui du catalogue : le client ne transmet que
 * l'article et la quantité.</p>
 */
@Slf4j
@Service
public class CommandeService extends AbstractCrudService<Commande, CommandeCreateDto, CommandeUpdateDto, CommandeResponseDto> {

    private final CommandeRepository commandeRepository;
    private final CommandeMapper commandeMapper;
    private final ArticleRepository articleRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    public CommandeService(final CommandeRepository repository,
                           final CommandeMapper mapper,
                           final ArticleRepository articleRepository,
                           final UtilisateurCourantService utilisateurCourantService) {
        super(repository, mapper);
        this.commandeRepository = repository;
        this.commandeMapper = mapper;
        this.articleRepository = articleRepository;
        this.utilisateurCourantService = utilisateurCourantService;
    }

    @Override
    protected String getResourceName() {
        return "Commande";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("reference", "montantTotal", "statut", "modePaiement", "dateCommande",
                "boutique", "utilisateur", "createdAt", "updatedAt", "deleted");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Une commande n'est jamais construite par mapping direct : elle est
     * assemblée par {@link #commander(CommandeCreateDto)} au nom de
     * l'utilisateur authentifié.</p>
     */
    @Override
    @Transactional
    public CommandeResponseDto create(final CommandeCreateDto createDto) {
        return commander(createDto);
    }

    /**
     * Enregistre une commande pour l'utilisateur courant.
     *
     * @param dto les lignes commandées et les modalités de règlement
     * @return la commande créée, avec son échéancier le cas échéant
     * @throws MetierException si un article est indisponible ou n'autorise pas
     *                         le paiement échelonné
     */
    @Transactional
    public CommandeResponseDto commander(final CommandeCreateDto dto) {
        final Utilisateur utilisateur = utilisateurCourantService.getUtilisateurCourant();

        final Commande commande = new Commande();
        commande.setUtilisateur(utilisateur);
        commande.setModePaiement(dto.modePaiement());
        commande.setAdresseLivraison(dto.adresseLivraison());
        commande.setTelephoneLivraison(dto.telephoneLivraison() != null
                ? dto.telephoneLivraison()
                : utilisateur.getTelephone());
        commande.setReference("CMD-TEMP");

        BigDecimal total = BigDecimal.ZERO;
        for (LigneCommandeCreateDto ligneDto : dto.lignes()) {
            final Article article = articleRepository.findById(ligneDto.articleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Article", "id", ligneDto.articleId()));
            total = total.add(ajouterLigne(commande, article, ligneDto.quantite(), dto.modePaiement()));
        }
        commande.setMontantTotal(total);

        if (commande.getBoutique() == null && !commande.getLignes().isEmpty()) {
            commande.setBoutique(commande.getLignes().get(0).getArticle().getBoutique());
        }

        if (dto.modePaiement() == ModePaiement.ECHELONNE) {
            final int nombre = dto.nombreEcheances() != null ? dto.nombreEcheances() : 3;
            commande.setNombreEcheances(nombre);
            genererEcheancier(commande, nombre);
        }

        Commande enregistree = commandeRepository.save(commande);
        enregistree.setReference(genererReference(enregistree.getId()));
        enregistree = commandeRepository.save(enregistree);

        log.info("Commande {} créée pour l'utilisateur #{} — {} XOF en {}",
                enregistree.getReference(), utilisateur.getId(), total, dto.modePaiement());
        return commandeMapper.toResponse(enregistree);
    }

    /**
     * Commandes de l'utilisateur courant — écran « Mes commandes ».
     *
     * @param pageable la pagination demandée
     * @return la page de commandes, de la plus récente à la plus ancienne
     */
    @Transactional(readOnly = true)
    public PageResponse<CommandeResponseDto> mesCommandes(final Pageable pageable) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        return PageResponse.of(commandeRepository
                .findByUtilisateurIdAndDeletedFalseOrderByDateCommandeDesc(utilisateurId, pageable)
                .map(commandeMapper::toResponse));
    }

    /**
     * Récupère une commande après vérification qu'elle appartient bien à
     * l'utilisateur courant.
     *
     * @param id l'identifiant de la commande
     * @return la commande demandée
     */
    @Transactional(readOnly = true)
    public CommandeResponseDto consulterMienne(final Long id) {
        final Commande commande = getEntityById(id);
        utilisateurCourantService.verifierProprietaire(commande.getUtilisateur().getId());
        return commandeMapper.toResponse(commande);
    }

    /**
     * Annule une commande qui n'est pas encore en préparation.
     *
     * @param id l'identifiant de la commande
     * @return la commande annulée
     * @throws MetierException si la commande est déjà engagée
     */
    @Transactional
    public CommandeResponseDto annuler(final Long id) {
        final Commande commande = getEntityById(id);
        utilisateurCourantService.verifierProprietaire(commande.getUtilisateur().getId());

        if (commande.getStatut() != StatutCommande.EN_ATTENTE && commande.getStatut() != StatutCommande.CONFIRMEE) {
            throw new MetierException(ErrorCode.COMMANDE_NON_MODIFIABLE,
                    "Une commande en préparation ou livrée ne peut plus être annulée");
        }

        commande.setStatut(StatutCommande.ANNULEE);
        commande.getLignes().forEach(ligne -> {
            final Article article = ligne.getArticle();
            article.setStock(article.getStock() + ligne.getQuantite());
            articleRepository.save(article);
        });
        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    private BigDecimal ajouterLigne(final Commande commande,
                                    final Article article,
                                    final int quantite,
                                    final ModePaiement modePaiement) {
        if (!article.isDisponible() || article.getStock() < quantite) {
            throw new MetierException(ErrorCode.ARTICLE_INDISPONIBLE,
                    "Stock insuffisant pour l'article « " + article.getNom() + " »");
        }
        if (modePaiement == ModePaiement.ECHELONNE && !article.isPaiementEchelonneAutorise()) {
            throw new MetierException(ErrorCode.PAIEMENT_ECHELONNE_INTERDIT,
                    "L'article « " + article.getNom() + " » ne peut être réglé qu'en une fois");
        }

        final LigneCommande ligne = new LigneCommande();
        ligne.setArticle(article);
        ligne.setLibelleArticle(article.getNom());
        ligne.setQuantite(quantite);
        ligne.setPrixUnitaire(article.getPrix());
        ligne.setMontantLigne(article.getPrix().multiply(BigDecimal.valueOf(quantite)));
        commande.ajouterLigne(ligne);

        article.setStock(article.getStock() - quantite);
        articleRepository.save(article);

        return ligne.getMontantLigne();
    }

    /**
     * Répartit le montant total sur {@code nombre} mensualités ; le reliquat de
     * l'arrondi est porté par la dernière échéance.
     */
    private void genererEcheancier(final Commande commande, final int nombre) {
        final BigDecimal mensualite = commande.getMontantTotal()
                .divide(BigDecimal.valueOf(nombre), 2, java.math.RoundingMode.DOWN);
        final BigDecimal derniere = commande.getMontantTotal()
                .subtract(mensualite.multiply(BigDecimal.valueOf(nombre - 1L)));
        final LocalDate premiere = LocalDate.now();

        for (int i = 1; i <= nombre; i++) {
            final Echeance echeance = new Echeance();
            echeance.setNumeroEcheance(i);
            echeance.setMontant(i == nombre ? derniere : mensualite);
            echeance.setDateEcheance(premiere.plusMonths(i - 1L));
            commande.ajouterEcheance(echeance);
        }
    }

    private String genererReference(final Long id) {
        return String.format("CMD-%d-%06d", LocalDate.now(ZoneOffset.UTC).getYear(), id);
    }
}
