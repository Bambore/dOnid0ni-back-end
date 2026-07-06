package com.donidoni.auth.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriétés de configuration du client MinIO.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** URL de l'API MinIO (ex: http://localhost:9000). */
    @NotBlank
    private String url;

    /** Clé d'accès. */
    @NotBlank
    private String accessKey;

    /** Clé secrète. */
    @NotBlank
    private String secretKey;

    /** Nom du bucket par défaut pour l'application. */
    @NotBlank
    private String bucketName;
}
