package com.donidoni.auth.backoffice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de mise à jour de la description d'un rôle.
 */
@Data
public class RoleFormDto {
    @NotBlank(message = "role.description.not.blank")
    private String description;
}
