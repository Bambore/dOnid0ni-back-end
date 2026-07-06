package com.donidoni.auth.backoffice.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO de représentation d'un utilisateur backoffice.
 *
 * <p>Adapté du {@code UserWebDto} de sigatt-uaa-service,
 * sans les champs spécifiques à SIGATT (directionId, serviceId,
 * fonctionId, siteId, matricule, typeUtilisateur).</p>
 */
@Data
public class BackofficeUserWebDto {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String profilId;
    private String profileName;
    private Boolean enabled;
    private Boolean emailVerified;
    private List<RoleWebDto> roles;
    private List<String> roleIds;
    private List<String> requiredActions;
}
