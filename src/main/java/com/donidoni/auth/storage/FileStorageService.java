package com.donidoni.auth.storage;

import java.io.InputStream;

/**
 * Interface d'abstraction pour le stockage de fichiers.
 */
public interface FileStorageService {

    /**
     * Uploade un fichier vers le stockage.
     *
     * @param objectName nom du fichier cible (avec chemin éventuel)
     * @param stream     flux de données du fichier
     * @param size       taille du fichier
     * @param contentType type MIME du fichier
     * @return le nom de l'objet sauvegardé (souvent identique à objectName)
     */
    String uploadFile(String objectName, InputStream stream, long size, String contentType);

    /**
     * Supprime un fichier du stockage.
     *
     * @param objectName nom du fichier cible
     */
    void deleteFile(String objectName);

    /**
     * Génère une URL présignée pour accéder au fichier (téléchargement).
     *
     * @param objectName nom du fichier cible
     * @param expirySeconds durée de validité de l'URL en secondes
     * @return l'URL d'accès
     */
    String getFileUrl(String objectName, int expirySeconds);
}
