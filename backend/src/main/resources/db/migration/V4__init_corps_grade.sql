CREATE TABLE IF NOT EXISTS corps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    nom VARCHAR(150) NOT NULL,
    age_retraite INT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_corps_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS grade (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    nom VARCHAR(150) NOT NULL,
    corps_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_grade_nom_corps (nom, corps_id),
    CONSTRAINT fk_grade_corps FOREIGN KEY (corps_id) REFERENCES corps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO corps (nom) VALUES
    ('Inspecteur du Cadre de la Surveillance'),
    ('Contrôleur du Cadre de la Surveillance'),
    ('Agent Technique du Cadre de la Surveillance'),
    ('Administrateurs civils'),
    ('Attachés d''administration'),
    ('Secrétaires d''administration'),
    ('Adjoints administratifs'),
    ('Secrétaires dactylographes'),
    ('Inspecteurs des impôts'),
    ('Contrôleurs des impôt'),
    ('Inspecteurs du Trésor'),
    ('Contrôleurs du Trésor'),
    ('Inspecteurs des finances'),
    ('Contrôleurs des finances'),
    ('Inspecteurs des services économiques'),
    ('Contrôleurs des services économiques'),
    ('Inspecteurs des douanes'),
    ('Contrôleurs des douanes'),
    ('Médecins'),
    ('Pharmaciens'),
    ('Chirurgiens-dentistes'),
    ('Infirmiers'),
    ('Sages-femmes'),
    ('Assistants médicaux'),
    ('Ingénieurs sanitaires'),
    ('Techniciens de santé'),
    ('Travailleurs sociaux'),
    ('Enseignants du fondamental'),
    ('Professeurs de l''enseignement secondaire'),
    ('Conseillers pédagogiques'),
    ('Inspecteurs de l''enseignement'),
    ('Formateurs de la formation professionnelle'),
    ('Ingénieurs de l''agriculture et du génie rural'),
    ('Techniciens de l''agriculture'),
    ('Ingénieurs de l''élevage'),
    ('Vétérinaires'),
    ('Agents techniques de l''agriculture'),
    ('Ingénieurs des eaux et forêts'),
    ('Techniciens des eaux et forêts'),
    ('Agents techniques des eaux et forêts'),
    ('Ingénieurs des constructions civiles'),
    ('Techniciens des constructions civiles'),
    ('Ingénieurs de l''industrie et des mines'),
    ('Techniciens de l''industrie et des mines'),
    ('Ingénieurs informaticiens'),
    ('Techniciens informaticiens'),
    ('Ingénieurs de la statistique'),
    ('Techniciens de la statistique'),
    ('Journalistes-réalisateurs'),
    ('Instructeurs de la jeunesse et des sports'),
    ('Conservateurs de musée'),
    ('Bibliothécaires et archivistes'),
    ('Magistrats'),
    ('Greffiers'),
    ('Fonctionnaires de la Police nationale'),
    ('Fonctionnaires de la Protection civile'),
    ('Personnel des Affaires étrangères'),
    ('Officier des Forces Armées et de Sécurité'),
    ('Sous-Officier des Forces Armées et de Sécurité'),
    ('Homme de Rang des Forces Armées et de Sécurité');

INSERT IGNORE INTO grade (nom, corps_id)
SELECT grades.nom, corps.id
FROM (
    SELECT 'Inspecteur Général' AS nom, 'Inspecteur du Cadre de la Surveillance' AS corps
    UNION ALL SELECT 'Inspecteur Divisionnaire Major', 'Inspecteur du Cadre de la Surveillance'
    UNION ALL SELECT 'Inspecteur Divisionnaire', 'Inspecteur du Cadre de la Surveillance'
    UNION ALL SELECT 'Inspecteur Principal', 'Inspecteur du Cadre de la Surveillance'
    UNION ALL SELECT 'Inspecteur', 'Inspecteur du Cadre de la Surveillance'
    UNION ALL SELECT 'Contrôleur Divisionnaire', 'Contrôleur du Cadre de la Surveillance'
    UNION ALL SELECT 'Contrôleur Principal', 'Contrôleur du Cadre de la Surveillance'
    UNION ALL SELECT 'Contrôleur', 'Contrôleur du Cadre de la Surveillance'
    UNION ALL SELECT 'Adjudant-Chef Major', 'Agent Technique du Cadre de la Surveillance'
    UNION ALL SELECT 'Adjudant-Chef', 'Agent Technique du Cadre de la Surveillance'
    UNION ALL SELECT 'Adjudant', 'Agent Technique du Cadre de la Surveillance'
    UNION ALL SELECT 'Sergent-Chef', 'Agent Technique du Cadre de la Surveillance'
    UNION ALL SELECT 'Sergent', 'Agent Technique du Cadre de la Surveillance'
    UNION ALL SELECT 'Elève Surveillant', 'Agent Technique du Cadre de la Surveillance'
) grades
JOIN corps ON corps.nom = grades.corps;