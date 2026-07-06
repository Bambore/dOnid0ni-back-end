package com.donidoni.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration JPA.
 *
 * <p>Active l'audit JPA pour remplir automatiquement les champs
 * de création et de modification (ex: createdAt, updatedAt)
 * des entités héritant de AbstractAuditingEntity.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
