CREATE TABLE produits (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    prix NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL
);
