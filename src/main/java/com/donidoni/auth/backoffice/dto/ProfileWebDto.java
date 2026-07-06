package com.donidoni.auth.backoffice.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO de représentation d'un profil (groupe Keycloak).
 */
@Data
public class ProfileWebDto {
    private String id;
    private String name;
    private String description;
    private List<RoleWebDto> roles;
}
