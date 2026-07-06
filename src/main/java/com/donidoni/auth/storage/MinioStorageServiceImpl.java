package com.donidoni.auth.storage;

import com.donidoni.auth.config.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Implémentation du service de stockage basée sur MinIO.
 */
@Slf4j
@Service
public class MinioStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioStorageServiceImpl(
            final MinioClient minioClient,
            final MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public String uploadFile(
            final String objectName,
            final InputStream stream,
            final long size,
            final String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(stream, size, -1)
                    .contentType(contentType)
                    .build());
            
            log.info("[MINIO] Fichier uploadé avec succès : {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("[MINIO] Erreur lors de l'upload du fichier {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Erreur lors de l'upload vers MinIO", e);
        }
    }

    @Override
    public void deleteFile(final String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
            log.info("[MINIO] Fichier supprimé avec succès : {}", objectName);
        } catch (Exception e) {
            log.error("[MINIO] Erreur lors de la suppression du fichier {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression depuis MinIO", e);
        }
    }

    @Override
    public String getFileUrl(final String objectName, final int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .expiry(expirySeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("[MINIO] Erreur lors de la génération de l'URL pour {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Erreur lors de la génération de l'URL MinIO", e);
        }
    }
}
