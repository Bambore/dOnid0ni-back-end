package com.donidoni.auth.metier.service;

import com.donidoni.auth.crud.dto.PageResponse;
import com.donidoni.auth.crud.exception.ResourceNotFoundException;
import com.donidoni.auth.crud.service.AbstractCrudService;
import com.donidoni.auth.metier.domain.Notification;
import com.donidoni.auth.metier.domain.enums.TypeNotification;
import com.donidoni.auth.metier.dto.NotificationCreateDto;
import com.donidoni.auth.metier.dto.NotificationResponseDto;
import com.donidoni.auth.metier.dto.NotificationUpdateDto;
import com.donidoni.auth.metier.mapper.NotificationMapper;
import com.donidoni.auth.metier.repository.NotificationRepository;
import com.donidoni.auth.metier.repository.UtilisateurRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

/**
 * Centre de notifications.
 *
 * <p>Les lectures ne renvoient que les notifications de l'utilisateur courant
 * et celles diffusées à tous. Les onglets de filtrage du mobile s'appuient sur
 * {@link TypeNotification}.</p>
 */
@Service
public class NotificationService extends AbstractCrudService<Notification, NotificationCreateDto, NotificationUpdateDto, NotificationResponseDto> {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    public NotificationService(final NotificationRepository repository,
                               final NotificationMapper mapper,
                               final UtilisateurRepository utilisateurRepository,
                               final UtilisateurCourantService utilisateurCourantService) {
        super(repository, mapper);
        this.notificationRepository = repository;
        this.notificationMapper = mapper;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurCourantService = utilisateurCourantService;
    }

    @Override
    protected String getResourceName() {
        return "Notification";
    }

    @Override
    protected Set<String> getSearchableFields() {
        return Set.of("type", "lue", "titreFr", "titreEn", "corpsFr", "utilisateur",
                "createdAt", "updatedAt", "deleted");
    }

    @Override
    protected void beforeCreate(final Notification entity, final NotificationCreateDto createDto) {
        if (createDto.utilisateurId() != null) {
            entity.setUtilisateur(utilisateurRepository.findById(createDto.utilisateurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", createDto.utilisateurId())));
        }
    }

    /**
     * Notifications visibles par l'utilisateur courant.
     *
     * @param types    les types à conserver ; {@code null} ou vide pour tout afficher
     * @param pageable la pagination demandée
     * @return la page de notifications, de la plus récente à la plus ancienne
     */
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDto> mesNotifications(final Collection<TypeNotification> types,
                                                                  final Pageable pageable) {
        final Long utilisateurId = utilisateurCourantService.getIdUtilisateurCourant();
        final var page = (types == null || types.isEmpty())
                ? notificationRepository.findPourUtilisateur(utilisateurId, pageable)
                : notificationRepository.findPourUtilisateurParTypes(utilisateurId, types, pageable);
        return PageResponse.of(page.map(notificationMapper::toResponse));
    }

    /**
     * @return le nombre de notifications non lues (pastille de l'accueil)
     */
    @Transactional(readOnly = true)
    public long compterNonLues() {
        return notificationRepository.compterNonLues(utilisateurCourantService.getIdUtilisateurCourant());
    }

    /**
     * Marque une notification comme lue.
     *
     * @param id l'identifiant de la notification
     * @return la notification mise à jour
     */
    @Transactional
    public NotificationResponseDto marquerLue(final Long id) {
        final Notification notification = getEntityById(id);
        if (notification.getUtilisateur() != null) {
            utilisateurCourantService.verifierProprietaire(notification.getUtilisateur().getId());
        }
        if (!notification.isLue()) {
            notification.setLue(true);
            notification.setDateLecture(Instant.now());
        }
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    /**
     * Action « Tout marquer comme lu ».
     *
     * @return le nombre de notifications marquées
     */
    @Transactional
    public int marquerToutesLues() {
        return notificationRepository.marquerToutesLues(
                utilisateurCourantService.getIdUtilisateurCourant(), Instant.now());
    }

    /**
     * Action « Tout effacer » : soft-delete des notifications personnelles.
     *
     * @return le nombre de notifications supprimées
     */
    @Transactional
    public int supprimerToutes() {
        return notificationRepository.supprimerToutes(
                utilisateurCourantService.getIdUtilisateurCourant(), Instant.now());
    }

    /**
     * Supprime une notification de l'utilisateur courant (balayage sur la carte).
     *
     * @param id l'identifiant de la notification
     */
    @Transactional
    public void supprimerMienne(final Long id) {
        final Notification notification = getEntityById(id);
        if (notification.getUtilisateur() != null) {
            utilisateurCourantService.verifierProprietaire(notification.getUtilisateur().getId());
        }
        delete(id);
    }
}
