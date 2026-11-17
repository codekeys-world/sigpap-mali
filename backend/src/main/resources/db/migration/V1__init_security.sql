CREATE TABLE IF NOT EXISTS `role` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    nom VARCHAR(60) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS autorisation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    nom VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_autorisation_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_autorisation (
    role_id BIGINT NOT NULL,
    autorisation_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, autorisation_id),
    CONSTRAINT fk_role_autorisation_role FOREIGN KEY (role_id) REFERENCES `role` (id),
    CONSTRAINT fk_role_autorisation_autorisation FOREIGN KEY (autorisation_id) REFERENCES autorisation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS personne (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    sexe VARCHAR(1) NOT NULL,
    date_naissance DATE,
    lieu_naissance VARCHAR(150),
    telephone VARCHAR(30),
    groupe_sanguin VARCHAR(5),
    email VARCHAR(150),
    adresse VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS personnel (
    id BIGINT NOT NULL,
    matricule VARCHAR(50) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role_id BIGINT,
    grade_actuel_id BIGINT,
    corps_actuel_id BIGINT,
    service_courant_id BIGINT,
    surveillant BIT NOT NULL DEFAULT b'1',
    en_formation BIT NOT NULL DEFAULT b'0',
    decede BIT NOT NULL DEFAULT b'0',
    date_deces DATE,
    date_recrutement DATE,
    date_titularisation DATE,
    actif BIT NOT NULL DEFAULT b'1',
    photo VARCHAR(255),
    situation VARCHAR(20) DEFAULT 'ACTIF',
    situation_depuis DATE,
    situation_motif VARCHAR(500),
    mot_de_passe_temporaire BIT NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_personnel_matricule (matricule),
    CONSTRAINT fk_personnel_personne FOREIGN KEY (id) REFERENCES personne (id),
    CONSTRAINT fk_personnel_role FOREIGN KEY (role_id) REFERENCES `role` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `role` (nom) VALUES
    ('ADMINISTRATEUR'),
    ('GESTIONNAIRE_RH'),
    ('DIRECTEUR_REGIONAL'),
    ('CHEF_ETABLISSEMENT'),
    ('UTILISATEUR');

INSERT IGNORE INTO autorisation (nom) VALUES
    ('LIRE_PERSONNEL'),
    ('GERER_PERSONNEL'),
    ('EXPORTER_DONNEES'),
    ('GERER_REFERENTIELS'),
    ('GERER_ROLES'),
    ('CONSULTER_JOURNAL');

INSERT IGNORE INTO role_autorisation (role_id, autorisation_id)
SELECT r.id, a.id
FROM `role` r
CROSS JOIN autorisation a
WHERE (r.nom = 'ADMINISTRATEUR')
   OR (r.nom = 'GESTIONNAIRE_RH' AND a.nom IN ('LIRE_PERSONNEL', 'GERER_PERSONNEL', 'EXPORTER_DONNEES', 'GERER_REFERENTIELS'))
   OR (r.nom IN ('DIRECTEUR_REGIONAL', 'CHEF_ETABLISSEMENT') AND a.nom IN ('LIRE_PERSONNEL', 'GERER_PERSONNEL', 'EXPORTER_DONNEES'))
   OR (r.nom = 'UTILISATEUR' AND a.nom IN ('LIRE_PERSONNEL', 'EXPORTER_DONNEES'));

INSERT INTO personne (nom, prenom, sexe)
SELECT 'Administrateur', 'Système', 'M'
WHERE NOT EXISTS (SELECT 1 FROM personnel WHERE matricule = '0159.203-M');

SET @admin_personne_id = COALESCE(
    (SELECT id FROM personnel WHERE matricule = '0159.203-M' LIMIT 1),
    LAST_INSERT_ID()
);

INSERT INTO personnel (
    id, matricule, mot_de_passe, role_id, surveillant, en_formation, decede, actif, situation, mot_de_passe_temporaire
)
SELECT @admin_personne_id, '0159.203-M', '$2a$10$wRnaKrNFRWKCLgHn/0DZA.dTfkt9gtdi2FD//vnRHzjDV/w1u8/sG', r.id,
       b'0', b'0', b'0', b'1', 'ACTIF', b'1'
FROM `role` r
WHERE r.nom = 'ADMINISTRATEUR'
  AND NOT EXISTS (SELECT 1 FROM personnel WHERE matricule = '0159.203-M');