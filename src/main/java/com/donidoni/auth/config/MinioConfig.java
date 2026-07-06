package com.donidoni.auth.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client MinIO pour le stockage S3.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    private final MinioProperties props;

    public MinioConfig(final MinioProperties props) {
        this.props = props;
    }

    /**
     * Crée le client MinIO et initialise le bucket par défaut s'il n'existe pas.
     *
     * @return instance {@link MinioClient} configurée
     */
    @Bean
    public MinioClient minioClient() {
        log.info("[MINIO] Connexion → {}", props.getUrl());

        try {
            final MinioClient client = MinioClient.builder()
                    .endpoint(props.getUrl())
                    .credentials(props.getAccessKey(), props.getSecretKey())
                    .build();

            // Création automatique du bucket s'il n'existe pas
            final boolean bucketExists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(props.getBucketName()).build());
            if (!bucketExists) {
                log.info("[MINIO] Création du bucket : {}", props.getBucketName());
                client.makeBucket(
                        MakeBucketArgs.builder().bucket(props.getBucketName()).build());
            }

            return client;
        } catch (Exception e) {
            log.error("[MINIO] Erreur d'initialisation : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'initialisation de MinIO", e);
        }
    }
}
