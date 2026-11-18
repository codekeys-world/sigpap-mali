-- Tables previously managed by Hibernate. Existing tables and their data are kept.
CREATE TABLE IF NOT EXISTS formation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    titre VARCHAR(200) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_formation_slug (slug),
    UNIQUE KEY uk_formation_titre (titre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS etude (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    intitule VARCHAR(200) NOT NULL,
    niveau_etude VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_etude_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS niveau_etude (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    nom VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_niveau_etude_slug (slug),
    UNIQUE KEY uk_niveau_etude_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS poste (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    nom VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_poste_slug (slug),
    UNIQUE KEY uk_poste_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS affectation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    type VARCHAR(30) NOT NULL,
    decision VARCHAR(255),
    fichier_mutation VARCHAR(255),
    nom_mutation VARCHAR(200) NOT NULL,
    fichier_decision VARCHAR(255),
    service_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_affectation_slug (slug),
    CONSTRAINT fk_affectation_service FOREIGN KEY (service_id) REFERENCES service_ap (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS affectation_personnel (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    date_debut DATE,
    date_fin DATE,
    annee_debut INT,
    annee_fin INT,
    personnel_id BIGINT NOT NULL,
    affectation_id BIGINT NOT NULL,
    service_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_affectation_personnel_slug (slug),
    CONSTRAINT fk_affectation_personnel_personnel FOREIGN KEY (personnel_id) REFERENCES personnel (id),
    CONSTRAINT fk_affectation_personnel_affectation FOREIGN KEY (affectation_id) REFERENCES affectation (id),
    CONSTRAINT fk_affectation_personnel_service FOREIGN KEY (service_id) REFERENCES service_ap (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS poste_affectation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    date_debut DATE,
    date_fin DATE,
    annee_debut INT,
    annee_fin INT,
    description VARCHAR(1000),
    fichier VARCHAR(255),
    affectation_personnel_id BIGINT NOT NULL,
    poste_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_poste_affectation_slug (slug),
    CONSTRAINT fk_poste_affectation_affectation_personnel FOREIGN KEY (affectation_personnel_id) REFERENCES affectation_personnel (id),
    CONSTRAINT fk_poste_affectation_poste FOREIGN KEY (poste_id) REFERENCES poste (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS historique_grade (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    date_debut DATE,
    date_fin DATE,
    annee_debut INT,
    annee_fin INT,
    fichier_avancement VARCHAR(255),
    personnel_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_historique_grade_slug (slug),
    CONSTRAINT fk_historique_grade_personnel FOREIGN KEY (personnel_id) REFERENCES personnel (id),
    CONSTRAINT fk_historique_grade_grade FOREIGN KEY (grade_id) REFERENCES grade (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS faire_formation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    date_debut DATE,
    date_fin DATE,
    annee_debut INT,
    annee_fin INT,
    lieu VARCHAR(200),
    description VARCHAR(1000),
    fichier_attestation VARCHAR(255),
    personnel_id BIGINT NOT NULL,
    formation_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_faire_formation_slug (slug),
    CONSTRAINT fk_faire_formation_personnel FOREIGN KEY (personnel_id) REFERENCES personnel (id),
    CONSTRAINT fk_faire_formation_formation FOREIGN KEY (formation_id) REFERENCES formation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS faire_etude (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    date_debut DATE,
    date_fin DATE,
    annee_debut INT,
    annee_fin INT,
    lieu VARCHAR(200),
    option_etude VARCHAR(200),
    description VARCHAR(1000),
    fichier_diplome VARCHAR(255),
    personnel_id BIGINT NOT NULL,
    etude_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_faire_etude_slug (slug),
    CONSTRAINT fk_faire_etude_personnel FOREIGN KEY (personnel_id) REFERENCES personnel (id),
    CONSTRAINT fk_faire_etude_etude FOREIGN KEY (etude_id) REFERENCES etude (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS changement_situation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    situation VARCHAR(20) NOT NULL,
    date_debut DATE,
    date_fin DATE,
    motif VARCHAR(500),
    reference VARCHAR(200),
    fichier VARCHAR(255),
    personnel_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_changement_situation_slug (slug),
    CONSTRAINT fk_changement_situation_personnel FOREIGN KEY (personnel_id) REFERENCES personnel (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS piece_justificative (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(36) NOT NULL,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    categorie VARCHAR(60) NOT NULL,
    intitule VARCHAR(200) NOT NULL,
    date_document DATE,
    fichier VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    personnel_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_piece_justificative_slug (slug),
    CONSTRAINT fk_piece_justificative_personnel FOREIGN KEY (personnel_id) REFERENCES personnel (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS journal_activite (
    id BIGINT NOT NULL AUTO_INCREMENT,
    utilisateur VARCHAR(100),
    `role` VARCHAR(40),
    action VARCHAR(40),
    methode VARCHAR(10),
    uri VARCHAR(400),
    parametres VARCHAR(400),
    ressource VARCHAR(200),
    ressource_id VARCHAR(40),
    statut INT NOT NULL,
    duree_ms BIGINT,
    date_action DATETIME(6),
    ip VARCHAR(64),
    client VARCHAR(500),
    navigateur VARCHAR(60),
    systeme VARCHAR(60),
    appareil VARCHAR(20),
    origine VARCHAR(200),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used BIT NOT NULL,
    personnel_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_password_reset_hash (token_hash),
    CONSTRAINT fk_password_reset_token_personnel FOREIGN KEY (personnel_id) REFERENCES personnel (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
