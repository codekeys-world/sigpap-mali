CREATE TABLE arrete_integration (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    reference VARCHAR(150) NOT NULL,
    fichier VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_arrete_integration_slug (slug),
    UNIQUE KEY uk_arrete_integration_reference (reference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE personnel
    ADD COLUMN arrete_integration_id BIGINT NULL,
    ADD KEY idx_personnel_arrete_integration (arrete_integration_id),
    ADD CONSTRAINT fk_personnel_arrete_integration
        FOREIGN KEY (arrete_integration_id) REFERENCES arrete_integration (id);
