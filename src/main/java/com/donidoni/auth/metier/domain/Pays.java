package com.donidoni.auth.metier.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Pays d'approvisionnement d'un groupage (Chine, Dubaï, Turquie...).
 *
 * <p>Alimente le badge pays des cartes de groupage et la liste des destinations
 * proposées dans le sondage mobile.</p>
 */
@Entity
@Table(name = "pays")
@Getter
@Setter
public class Pays extends AbstractSoftDeletableEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    /** Code ISO 3166-1 alpha-2 (ex. {@code CN}, {@code AE}, {@code TR}). */
    @Column(name = "code_iso", nullable = false, length = 2, unique = true)
    private String codeIso;

    /** Emoji du drapeau affiché par le mobile à côté du nom. */
    @Column(name = "emoji_drapeau", length = 16)
    private String emojiDrapeau;

    @Column(nullable = false)
    private boolean actif = true;
}
