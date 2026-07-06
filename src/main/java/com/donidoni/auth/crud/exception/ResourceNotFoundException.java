package com.donidoni.auth.crud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée lorsqu'une ressource demandée est introuvable.
 *
 * <p>Mappée automatiquement sur un HTTP 404 NOT_FOUND par le
 * {@link com.donidoni.auth.exception.GlobalExceptionHandler}.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Crée une exception pour une ressource introuvable.
     *
     * @param resourceName le nom de la ressource (ex: "Category")
     * @param fieldName    le nom du champ de recherche (ex: "id")
     * @param fieldValue   la valeur recherchée (ex: 42)
     */
    public ResourceNotFoundException(
            final String resourceName,
            final String fieldName,
            final Object fieldValue) {
        super(String.format("%s introuvable avec %s = '%s'",
                resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Crée une exception simple avec un message.
     *
     * @param message le message d'erreur
     */
    public ResourceNotFoundException(final String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
