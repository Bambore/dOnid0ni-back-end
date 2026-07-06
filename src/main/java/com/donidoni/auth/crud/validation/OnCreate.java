package com.donidoni.auth.crud.validation;

/**
 * Groupe de validation pour les opérations de <strong>création</strong>.
 *
 * <p>Utilisé avec {@code @Validated(OnCreate.class)} pour activer
 * des contraintes spécifiques à la création (ex: {@code @NotBlank})
 * qui ne s'appliquent pas en mise à jour.</p>
 *
 * <p>Exemple :</p>
 * <pre>{@code
 * public record CategoryCreateDto(
 *     @NotBlank(groups = OnCreate.class) String name
 * ) {}
 * }</pre>
 */
public interface OnCreate {
}
