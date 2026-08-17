package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.metier.domain.Article;
import com.donidoni.auth.metier.dto.ArticleCreateDto;
import com.donidoni.auth.metier.dto.ArticleResponseDto;
import com.donidoni.auth.metier.dto.ArticleUpdateDto;
import com.donidoni.auth.metier.mapper.ArticleMapper;
import com.donidoni.auth.metier.repository.ArticleRepository;
import com.donidoni.auth.metier.repository.BoutiqueRepository;
import com.donidoni.auth.metier.repository.CategorieRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** Gestion du catalogue d'articles du marché. */
@Service
public class ArticleService extends AbstractCrudService<Article, ArticleCreateDto, ArticleUpdateDto, ArticleResponseDto> {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final CategorieRepository categorieRepository;
    private final BoutiqueRepository boutiqueRepository;

    public ArticleService(final ArticleRepository repository,
                          final ArticleMapper mapper,
                          final CategorieRepository categorieRepository,
                          final BoutiqueRepository boutiqueRepository) {
        super(repository, mapper);
        this.articleRepository = repository;
        this.articleMapper = mapper;
        this.categorieRepository = categorieRepository;
        this.boutiqueRepository = boutiqueRepository;
    }

    @Override
    protected String getResourceName() {
        return "Article";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("nom", "description", "prix", "stock", "disponible", "categorie", "boutique",
                "paiementEchelonneAutorise", "createdAt", "updatedAt", "deleted");
    }

    @Override
    protected void beforeCreate(final Article entity, final ArticleCreateDto createDto) {
        appliquerRelations(entity, createDto.categorieId(), createDto.boutiqueId());
    }

    @Override
    protected void beforeUpdate(final Article entity, final ArticleUpdateDto updateDto) {
        appliquerRelations(entity, updateDto.categorieId(), updateDto.boutiqueId());
    }

    /**
     * Recherche plein texte de l'écran « Rechercher un produit ».
     *
     * @param terme    le terme saisi
     * @param pageable la pagination demandée
     * @return la page d'articles correspondants
     */
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponseDto> rechercher(final String terme, final Pageable pageable) {
        return PageResponse.of(articleRepository.rechercher(terme, pageable).map(articleMapper::toResponse));
    }

    /**
     * Articles d'une catégorie (filtre du carrousel du marché).
     *
     * @param categorieId l'identifiant de la catégorie
     * @param pageable    la pagination demandée
     * @return la page d'articles de cette catégorie
     */
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponseDto> listerParCategorie(final Long categorieId, final Pageable pageable) {
        return PageResponse.of(
                articleRepository.findByCategorieIdAndDeletedFalse(categorieId, pageable).map(articleMapper::toResponse));
    }

    /**
     * Articles proposés par une boutique partenaire.
     *
     * @param boutiqueId l'identifiant de la boutique
     * @param pageable   la pagination demandée
     * @return la page d'articles de cette boutique
     */
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponseDto> listerParBoutique(final Long boutiqueId, final Pageable pageable) {
        return PageResponse.of(
                articleRepository.findByBoutiqueIdAndDeletedFalse(boutiqueId, pageable).map(articleMapper::toResponse));
    }

    /**
     * Récupère un article et incrémente son compteur de consultations.
     *
     * @param id l'identifiant de l'article
     * @return la fiche article
     */
    @Transactional
    public ArticleResponseDto consulter(final Long id) {
        final Article article = getEntityById(id);
        article.setNombreVues(article.getNombreVues() + 1);
        return articleMapper.toResponse(repository.save(article));
    }

    private void appliquerRelations(final Article entity, final Long categorieId, final Long boutiqueId) {
        if (categorieId != null) {
            entity.setCategorie(categorieRepository.findById(categorieId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "id", categorieId)));
        }
        if (boutiqueId != null) {
            entity.setBoutique(boutiqueRepository.findById(boutiqueId)
                    .orElseThrow(() -> new ResourceNotFoundException("Boutique", "id", boutiqueId)));
        }
    }
}
