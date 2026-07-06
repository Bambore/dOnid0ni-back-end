package com.donidoni.auth.backoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * DTO de création/modification d'un profil (groupe Keycloak).
 */
@Data
public class ProfileFormDto {
    @NotBlank(message = "profile.name.not.blank")
    private String name;
    private String description;
    @NotEmpty(message = "profile.roleIds.not.empty")
    private List<String> roleIds;
}
