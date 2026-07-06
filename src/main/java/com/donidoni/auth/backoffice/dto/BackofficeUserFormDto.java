package com.donidoni.auth.backoffice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de création/modification d'un compte utilisateur backoffice.
 *
 * <p>Adapté du {@code UserFormDto} de sigatt-uaa-service,
 * sans les champs spécifiques à SIGATT (directionId, serviceId,
 * fonctionId, siteId, matricule, typeUtilisateur).</p>
 */
@Data
public class BackofficeUserFormDto {
    @NotBlank(message = "user.nom.not.blank")
    private String nom;
    @NotBlank(message = "user.prenom.not.blank")
    private String prenom;
    @NotBlank(message = "user.email.not.blank")
    @Email(message = "user.email.invalid")
    private String email;
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "user.telephone.invalid")
    private String telephone;
    @NotBlank(message = "user.profilId.not.blank")
    private String profilId;
    private List<String> roleIds = new ArrayList<>();
}
