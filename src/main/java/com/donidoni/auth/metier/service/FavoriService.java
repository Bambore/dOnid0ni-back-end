package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.metier.domain.Article;
import com.donidoni.auth.metier.domain.Favori;
import com.donidoni.auth.metier.domain.Utilisateur;
import com.donidoni.auth.metier.dto.FavoriResponseDto;
import com.donidoni.auth.metier.mapper.FavoriMapper;
import com.donidoni.auth.metier.repository.ArticleRepository;
import com.donidoni.auth.metier.repository.FavoriRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestion des articles mis en favori par l'utilisateur courant. */
@Service
@RequiredArgsConstructor
public class FavoriService {

    private final FavoriRepository favoriRepository;
    private final FavoriMapper favoriMapper;
    private final ArticleRepository articleRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    /**
     * Ajoute un article aux favoris ; l'appel est idempotent.
     *
     * @param articleId l'identifiant de l'article
     * @return le favori, existant ou nouvellement créé
     */
    @Transactional
    public FavoriResponseDto ajouter(final Long articleId) {
        final Utilisateur utilisateur = utilisateurCourantService.getUtilisateurCourant();
        final Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        final Favori favori = favoriRepository
                .findByUtilisateurIdAndArticleId(utilisateur.getId(), articleId)
                .orElseGet(() -> {
                    final Favori nouveau = new Favori();
                    nouveau.setUtilisateur(utilisateur);
                    nouveau.setArticle(article);
                    return favoriRepository.save(nouveau);
                });

        return favoriMapper.toResponse(favori);
    }

    /**
     * Retire un article des favoris ; sans effet s'il n'y figurait pas.
     *
     * @param articleId l'identifiant de l'article
     */
    @Transactional
    public void retirer(final Long articleId) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        favoriRepository.findByUtilisateurIdAndArticleId(utilisateurId, articleId)
                .ifPresent(favoriRepository::delete);
    }

    /**
     * @param pageable la pagination demandée
     * @return la page de favoris de l'utilisateur courant
     */
    @Transactional(readOnly = true)
    public PageResponse<FavoriResponseDto> mesFavoris(final Pageable pageable) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        return PageResponse.of(favoriRepository.findByUtilisateurId(utilisateurId, pageable)
                .map(favoriMapper::toResponse));
    }

    /**
     * @param articleId l'identifiant de l'article
     * @return {@code true} si l'article est dans les favoris de l'utilisateur courant
     */
    @Transactional(readOnly = true)
    public boolean estFavori(final Long articleId) {
        return favoriRepository.existsByUtilisateurIdAndArticleId(
                utilisateurCourantService.getIdUtilisateurCourant(), articleId);
    }
}
