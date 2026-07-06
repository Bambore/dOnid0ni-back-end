package com.donidoni.auth.backoffice.dto;

import lombok.Data;

/**
 * DTO de représentation d'un rôle Keycloak.
 */
@Data
public class RoleWebDto {
    private String id;
    private String name;
    private String description;
}
