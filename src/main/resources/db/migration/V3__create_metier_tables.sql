-- V3__create_metier_tables.sql
-- Schéma métier de l'application mobile Bass Yam :
-- profils, catalogue, boutiques partenaires, groupages, sondages, tontines,
-- commandes, paiements, notifications et cadeaux.
-- Tous les montants sont exprimés en francs CFA (XOF).

-- ════════════════════════════════════════════════════════════
--  UTILISATEURS
-- ════════════════════════════════════════════════════════════

CREATE TABLE utilisateurs (
    id                     BIGSERIAL PRIMARY KEY,
    keycloak_id            VARCHAR(64)  NOT NULL,
    nom_complet            VARCHAR(150),
    email                  VARCHAR(150),
    telephone              VARCHAR(20),
    ville                  VARCHAR(100),
    photo_url              VARCHAR(500),
    langue                 VARCHAR(2)   NOT NULL DEFAULT 'FR',
    theme_sombre           BOOLEAN      NOT NULL DEFAULT FALSE,
    biometrie_activee      BOOLEAN      NOT NULL DEFAULT FALSE,
    notifications_activees BOOLEAN      NOT NULL DEFAULT TRUE,
    actif                  BOOLEAN      NOT NULL DEFAULT TRUE,
    derniere_connexion     TIMESTAMP,
    deleted                BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at             TIMESTAMP,
    created_by             VARCHAR(255),
    created_at             TIMESTAMP    NOT NULL,
    updated_by             VARCHAR(255),
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT uk_utilisateur_keycloak UNIQUE (keycloak_id)
);

CREATE INDEX idx_utilisateur_telephone ON utilisateurs (telephone);

-- ════════════════════════════════════════════════════════════
--  RÉFÉRENTIELS : PAYS ET CATÉGORIES
-- ════════════════════════════════════════════════════════════

CREATE TABLE pays (
    id            BIGSERIAL PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    code_iso      VARCHAR(2)   NOT NULL,
    emoji_drapeau VARCHAR(16),
    actif         BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,
    created_by    VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL,
    updated_by    VARCHAR(255),
    updated_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uk_pays_code_iso UNIQUE (code_iso)
);

CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    image_url       VARCHAR(500),
    type            VARCHAR(20)  NOT NULL,
    ordre_affichage INTEGER      NOT NULL DEFAULT 0,
    actif           BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_categorie_type ON categories (type, actif);

-- ════════════════════════════════════════════════════════════
--  BOUTIQUES PARTENAIRES ET CATALOGUE
-- ════════════════════════════════════════════════════════════

CREATE TABLE boutiques (
    id           BIGSERIAL PRIMARY KEY,
    nom          VARCHAR(150) NOT NULL,
    categorie_id BIGINT,
    image_url    VARCHAR(500),
    description  VARCHAR(500),
    telephone    VARCHAR(20),
    adresse      VARCHAR(255),
    ville        VARCHAR(100),
    horaires     VARCHAR(100),
    latitude     NUMERIC(10, 7),
    longitude    NUMERIC(10, 7),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at   TIMESTAMP,
    created_by   VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL,
    updated_by   VARCHAR(255),
    updated_at   TIMESTAMP    NOT NULL,
    CONSTRAINT fk_boutique_categorie FOREIGN KEY (categorie_id) REFERENCES categories (id)
);

CREATE INDEX idx_boutique_categorie ON boutiques (categorie_id);

CREATE TABLE articles (
    id                          BIGSERIAL PRIMARY KEY,
    nom                         VARCHAR(150)  NOT NULL,
    description                 VARCHAR(2000),
    prix                        NUMERIC(12, 2) NOT NULL,
    categorie_id                BIGINT,
    boutique_id                 BIGINT,
    image_principale            VARCHAR(500),
    stock                       INTEGER        NOT NULL DEFAULT 0,
    disponible                  BOOLEAN        NOT NULL DEFAULT TRUE,
    paiement_echelonne_autorise BOOLEAN        NOT NULL DEFAULT TRUE,
    nombre_vues                 BIGINT         NOT NULL DEFAULT 0,
    deleted                     BOOLEAN        NOT NULL DEFAULT FALSE,
    deleted_at                  TIMESTAMP,
    created_by                  VARCHAR(255),
    created_at                  TIMESTAMP      NOT NULL,
    updated_by                  VARCHAR(255),
    updated_at                  TIMESTAMP      NOT NULL,
    CONSTRAINT fk_article_categorie FOREIGN KEY (categorie_id) REFERENCES categories (id),
    CONSTRAINT fk_article_boutique FOREIGN KEY (boutique_id) REFERENCES boutiques (id)
);

CREATE INDEX idx_article_categorie ON articles (categorie_id);
CREATE INDEX idx_article_boutique ON articles (boutique_id);

CREATE TABLE article_images (
    article_id BIGINT       NOT NULL,
    url        VARCHAR(500) NOT NULL,
    CONSTRAINT fk_article_image_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);

CREATE INDEX idx_article_image_article ON article_images (article_id);

CREATE TABLE favoris (
    id             BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT    NOT NULL,
    article_id     BIGINT    NOT NULL,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP NOT NULL,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP NOT NULL,
    CONSTRAINT fk_favori_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT fk_favori_article FOREIGN KEY (article_id) REFERENCES articles (id),
    CONSTRAINT uk_favori_utilisateur_article UNIQUE (utilisateur_id, article_id)
);

-- ════════════════════════════════════════════════════════════
--  GROUPAGES
-- ════════════════════════════════════════════════════════════

CREATE TABLE groupages (
    id                        BIGSERIAL PRIMARY KEY,
    titre                     VARCHAR(200)   NOT NULL,
    description               VARCHAR(2000),
    image_principale          VARCHAR(500),
    montant                   NUMERIC(12, 2) NOT NULL,
    pays_id                   BIGINT,
    article_id                BIGINT,
    nombre_participants_cible INTEGER        NOT NULL,
    statut                    VARCHAR(20)    NOT NULL DEFAULT 'OUVERT',
    date_ouverture            TIMESTAMP      NOT NULL,
    date_cloture_prevue       DATE,
    deleted                   BOOLEAN        NOT NULL DEFAULT FALSE,
    deleted_at                TIMESTAMP,
    created_by                VARCHAR(255),
    created_at                TIMESTAMP      NOT NULL,
    updated_by                VARCHAR(255),
    updated_at                TIMESTAMP      NOT NULL,
    CONSTRAINT fk_groupage_pays FOREIGN KEY (pays_id) REFERENCES pays (id),
    CONSTRAINT fk_groupage_article FOREIGN KEY (article_id) REFERENCES articles (id)
);

CREATE INDEX idx_groupage_statut ON groupages (statut, deleted);

CREATE TABLE groupage_images (
    groupage_id BIGINT       NOT NULL,
    url         VARCHAR(500) NOT NULL,
    CONSTRAINT fk_groupage_image_groupage FOREIGN KEY (groupage_id) REFERENCES groupages (id) ON DELETE CASCADE
);

CREATE INDEX idx_groupage_image_groupage ON groupage_images (groupage_id);

CREATE TABLE participations_groupage (
    id             BIGSERIAL PRIMARY KEY,
    groupage_id    BIGINT         NOT NULL,
    utilisateur_id BIGINT         NOT NULL,
    date_adhesion  TIMESTAMP      NOT NULL,
    statut         VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE',
    montant_verse  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP      NOT NULL,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP      NOT NULL,
    CONSTRAINT fk_participation_groupage_groupage FOREIGN KEY (groupage_id) REFERENCES groupages (id),
    CONSTRAINT fk_participation_groupage_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT uk_participation_groupage UNIQUE (groupage_id, utilisateur_id)
);

-- ════════════════════════════════════════════════════════════
--  SONDAGES
-- ════════════════════════════════════════════════════════════

CREATE TABLE sondages (
    id          BIGSERIAL PRIMARY KEY,
    titre       VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    statut      VARCHAR(20)  NOT NULL DEFAULT 'OUVERT',
    date_debut  TIMESTAMP    NOT NULL,
    date_fin    TIMESTAMP,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    created_by  VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL,
    updated_by  VARCHAR(255),
    updated_at  TIMESTAMP    NOT NULL
);

CREATE TABLE options_sondage (
    id              BIGSERIAL PRIMARY KEY,
    sondage_id      BIGINT       NOT NULL,
    libelle         VARCHAR(200) NOT NULL,
    image_url       VARCHAR(500),
    article_id      BIGINT,
    ordre_affichage INTEGER      NOT NULL DEFAULT 0,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP    NOT NULL,
    CONSTRAINT fk_option_sondage_sondage FOREIGN KEY (sondage_id) REFERENCES sondages (id),
    CONSTRAINT fk_option_sondage_article FOREIGN KEY (article_id) REFERENCES articles (id)
);

CREATE INDEX idx_option_sondage_sondage ON options_sondage (sondage_id);

CREATE TABLE votes_sondage (
    id             BIGSERIAL PRIMARY KEY,
    sondage_id     BIGINT    NOT NULL,
    utilisateur_id BIGINT    NOT NULL,
    option_id      BIGINT    NOT NULL,
    pays_id        BIGINT    NOT NULL,
    date_vote      TIMESTAMP NOT NULL,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP NOT NULL,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP NOT NULL,
    CONSTRAINT fk_vote_sondage_sondage FOREIGN KEY (sondage_id) REFERENCES sondages (id),
    CONSTRAINT fk_vote_sondage_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT fk_vote_sondage_option FOREIGN KEY (option_id) REFERENCES options_sondage (id),
    CONSTRAINT fk_vote_sondage_pays FOREIGN KEY (pays_id) REFERENCES pays (id),
    CONSTRAINT uk_vote_sondage_utilisateur UNIQUE (sondage_id, utilisateur_id)
);

-- ════════════════════════════════════════════════════════════
--  TONTINES
-- ════════════════════════════════════════════════════════════

CREATE TABLE tontines (
    id                        BIGSERIAL PRIMARY KEY,
    titre                     VARCHAR(200)   NOT NULL,
    description               VARCHAR(2000),
    image_url                 VARCHAR(500),
    montant_mensuel           NUMERIC(12, 2) NOT NULL,
    nombre_participants_cible INTEGER        NOT NULL,
    duree_mois                INTEGER,
    article_id                BIGINT,
    statut                    VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE',
    date_debut                DATE,
    date_fin                  DATE,
    deleted                   BOOLEAN        NOT NULL DEFAULT FALSE,
    deleted_at                TIMESTAMP,
    created_by                VARCHAR(255),
    created_at                TIMESTAMP      NOT NULL,
    updated_by                VARCHAR(255),
    updated_at                TIMESTAMP      NOT NULL,
    CONSTRAINT fk_tontine_article FOREIGN KEY (article_id) REFERENCES articles (id)
);

CREATE INDEX idx_tontine_statut ON tontines (statut, deleted);

CREATE TABLE participations_tontine (
    id                  BIGSERIAL PRIMARY KEY,
    tontine_id          BIGINT         NOT NULL,
    utilisateur_id      BIGINT         NOT NULL,
    date_adhesion       TIMESTAMP      NOT NULL,
    rang_tirage         INTEGER,
    statut              VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE',
    montant_total_verse NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP      NOT NULL,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP      NOT NULL,
    CONSTRAINT fk_participation_tontine_tontine FOREIGN KEY (tontine_id) REFERENCES tontines (id),
    CONSTRAINT fk_participation_tontine_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT uk_participation_tontine UNIQUE (tontine_id, utilisateur_id)
);

CREATE TABLE cotisations_tontine (
    id               BIGSERIAL PRIMARY KEY,
    participation_id BIGINT         NOT NULL,
    numero_echeance  INTEGER        NOT NULL,
    montant          NUMERIC(12, 2) NOT NULL,
    date_echeance    DATE           NOT NULL,
    statut           VARCHAR(20)    NOT NULL DEFAULT 'A_PAYER',
    date_paiement    TIMESTAMP,
    created_by       VARCHAR(255),
    created_at       TIMESTAMP      NOT NULL,
    updated_by       VARCHAR(255),
    updated_at       TIMESTAMP      NOT NULL,
    CONSTRAINT fk_cotisation_participation FOREIGN KEY (participation_id) REFERENCES participations_tontine (id)
);

CREATE INDEX idx_cotisation_participation ON cotisations_tontine (participation_id);

-- ════════════════════════════════════════════════════════════
--  COMMANDES ET ÉCHÉANCIERS
-- ════════════════════════════════════════════════════════════

CREATE TABLE commandes (
    id                  BIGSERIAL PRIMARY KEY,
    reference           VARCHAR(30)    NOT NULL,
    utilisateur_id      BIGINT         NOT NULL,
    boutique_id         BIGINT,
    montant_total       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    mode_paiement       VARCHAR(20)    NOT NULL DEFAULT 'COMPTANT',
    nombre_echeances    INTEGER,
    statut              VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE',
    adresse_livraison   VARCHAR(255),
    telephone_livraison VARCHAR(20),
    date_commande       TIMESTAMP      NOT NULL,
    deleted             BOOLEAN        NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP      NOT NULL,
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP      NOT NULL,
    CONSTRAINT fk_commande_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT fk_commande_boutique FOREIGN KEY (boutique_id) REFERENCES boutiques (id),
    CONSTRAINT uk_commande_reference UNIQUE (reference)
);

CREATE INDEX idx_commande_utilisateur ON commandes (utilisateur_id, deleted);

CREATE TABLE lignes_commande (
    id              BIGSERIAL PRIMARY KEY,
    commande_id     BIGINT         NOT NULL,
    article_id      BIGINT         NOT NULL,
    libelle_article VARCHAR(150)   NOT NULL,
    quantite        INTEGER        NOT NULL DEFAULT 1,
    prix_unitaire   NUMERIC(12, 2) NOT NULL,
    montant_ligne   NUMERIC(12, 2) NOT NULL,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP      NOT NULL,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP      NOT NULL,
    CONSTRAINT fk_ligne_commande_commande FOREIGN KEY (commande_id) REFERENCES commandes (id),
    CONSTRAINT fk_ligne_commande_article FOREIGN KEY (article_id) REFERENCES articles (id)
);

CREATE INDEX idx_ligne_commande_commande ON lignes_commande (commande_id);

CREATE TABLE echeances_commande (
    id              BIGSERIAL PRIMARY KEY,
    commande_id     BIGINT         NOT NULL,
    numero_echeance INTEGER        NOT NULL,
    montant         NUMERIC(12, 2) NOT NULL,
    date_echeance   DATE           NOT NULL,
    statut          VARCHAR(20)    NOT NULL DEFAULT 'A_PAYER',
    date_paiement   TIMESTAMP,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP      NOT NULL,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP      NOT NULL,
    CONSTRAINT fk_echeance_commande FOREIGN KEY (commande_id) REFERENCES commandes (id)
);

CREATE INDEX idx_echeance_commande ON echeances_commande (commande_id);

-- ════════════════════════════════════════════════════════════
--  PAIEMENTS
-- ════════════════════════════════════════════════════════════

CREATE TABLE paiements (
    id                        BIGSERIAL PRIMARY KEY,
    reference                 VARCHAR(30)    NOT NULL,
    utilisateur_id            BIGINT         NOT NULL,
    montant                   NUMERIC(12, 2) NOT NULL,
    moyen                     VARCHAR(25)    NOT NULL,
    statut                    VARCHAR(20)    NOT NULL DEFAULT 'INITIE',
    reference_externe         VARCHAR(100),
    date_transaction          TIMESTAMP      NOT NULL,
    message                   VARCHAR(500),
    commande_id               BIGINT,
    echeance_id               BIGINT,
    cotisation_id             BIGINT,
    participation_groupage_id BIGINT,
    created_by                VARCHAR(255),
    created_at                TIMESTAMP      NOT NULL,
    updated_by                VARCHAR(255),
    updated_at                TIMESTAMP      NOT NULL,
    CONSTRAINT fk_paiement_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT fk_paiement_commande FOREIGN KEY (commande_id) REFERENCES commandes (id),
    CONSTRAINT fk_paiement_echeance FOREIGN KEY (echeance_id) REFERENCES echeances_commande (id),
    CONSTRAINT fk_paiement_cotisation FOREIGN KEY (cotisation_id) REFERENCES cotisations_tontine (id),
    CONSTRAINT fk_paiement_participation_groupage FOREIGN KEY (participation_groupage_id) REFERENCES participations_groupage (id),
    CONSTRAINT uk_paiement_reference UNIQUE (reference),
    -- Un paiement vise exactement une cible.
    CONSTRAINT ck_paiement_cible_unique CHECK (
        (CASE WHEN commande_id IS NULL THEN 0 ELSE 1 END)
      + (CASE WHEN echeance_id IS NULL THEN 0 ELSE 1 END)
      + (CASE WHEN cotisation_id IS NULL THEN 0 ELSE 1 END)
      + (CASE WHEN participation_groupage_id IS NULL THEN 0 ELSE 1 END) = 1
    )
);

CREATE INDEX idx_paiement_utilisateur ON paiements (utilisateur_id);

-- ════════════════════════════════════════════════════════════
--  NOTIFICATIONS ET CADEAUX
-- ════════════════════════════════════════════════════════════

CREATE TABLE notifications (
    id             BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT,
    type           VARCHAR(20)   NOT NULL,
    titre_fr       VARCHAR(200)  NOT NULL,
    titre_en       VARCHAR(200),
    corps_fr       VARCHAR(1000) NOT NULL,
    corps_en       VARCHAR(1000),
    lue            BOOLEAN       NOT NULL DEFAULT FALSE,
    date_lecture   TIMESTAMP,
    lien_action    VARCHAR(255),
    deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMP,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP     NOT NULL,
    updated_by     VARCHAR(255),
    updated_at     TIMESTAMP     NOT NULL,
    CONSTRAINT fk_notification_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id)
);

CREATE INDEX idx_notification_utilisateur ON notifications (utilisateur_id, lue);

CREATE TABLE cadeaux (
    id              BIGSERIAL PRIMARY KEY,
    utilisateur_id  BIGINT       NOT NULL,
    libelle         VARCHAR(150) NOT NULL,
    description     VARCHAR(500),
    image_url       VARCHAR(500),
    type            VARCHAR(20)  NOT NULL,
    valeur          NUMERIC(12, 2),
    code            VARCHAR(40),
    statut          VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',
    date_expiration DATE,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL,
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP    NOT NULL,
    CONSTRAINT fk_cadeau_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id),
    CONSTRAINT uk_cadeau_code UNIQUE (code)
);

CREATE INDEX idx_cadeau_utilisateur ON cadeaux (utilisateur_id, statut);
