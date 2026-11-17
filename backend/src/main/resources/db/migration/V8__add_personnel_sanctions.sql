CREATE TABLE sanction_personnel (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    personnel_id BIGINT NOT NULL,
    nature VARCHAR(120) NOT NULL,
    date_sanction DATE NOT NULL,
    motif VARCHAR(1000) NOT NULL,
    reference VARCHAR(200),
    fichier VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sanction_personnel_slug (slug),
    KEY idx_sanction_personnel_personnel_date (personnel_id, date_sanction),
    CONSTRAINT fk_sanction_personnel_personnel FOREIGN KEY (personnel_id) REFERENCES personnel (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
