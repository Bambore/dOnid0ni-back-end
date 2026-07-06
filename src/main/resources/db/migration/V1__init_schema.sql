-- V1__init_schema.sql
-- Fichier de migration Flyway initial

-- Activation des extensions UUID si besoin
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Exemple de table si nécessaire (peut être laissé vide ou utilisé pour la première entité)
-- CREATE TABLE example_entity (
--     id BIGSERIAL PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     created_by VARCHAR(255),
--     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
--     updated_by VARCHAR(255),
--     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
-- );
