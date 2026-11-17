ALTER TABLE personne ADD COLUMN slug VARCHAR(36) NULL;
UPDATE personne SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE personne MODIFY COLUMN slug VARCHAR(36) NOT NULL;
ALTER TABLE personne ADD UNIQUE KEY uk_personne_slug (slug);

ALTER TABLE corps ADD COLUMN slug VARCHAR(36) NULL;
UPDATE corps SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE corps MODIFY COLUMN slug VARCHAR(36) NOT NULL;
ALTER TABLE corps ADD UNIQUE KEY uk_corps_slug (slug);

ALTER TABLE grade ADD COLUMN slug VARCHAR(36) NULL;
UPDATE grade SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE grade MODIFY COLUMN slug VARCHAR(36) NOT NULL;
ALTER TABLE grade ADD UNIQUE KEY uk_grade_slug (slug);

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'formation'),
    'ALTER TABLE formation ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_formation_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'formation'),
    'UPDATE formation SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'etude'),
    'ALTER TABLE etude ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_etude_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'etude'),
    'UPDATE etude SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'niveau_etude'),
    'ALTER TABLE niveau_etude ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_niveau_etude_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'niveau_etude'),
    'UPDATE niveau_etude SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'affectation'),
    'ALTER TABLE affectation ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_affectation_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'affectation'),
    'UPDATE affectation SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'poste'),
    'ALTER TABLE poste ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_poste_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'poste'),
    'UPDATE poste SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

ALTER TABLE service_ap ADD COLUMN slug VARCHAR(36) NULL;
UPDATE service_ap SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE service_ap MODIFY COLUMN slug VARCHAR(36) NOT NULL;
ALTER TABLE service_ap ADD UNIQUE KEY uk_service_ap_slug (slug);

ALTER TABLE `role` ADD COLUMN slug VARCHAR(36) NULL;
UPDATE `role` SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE `role` MODIFY COLUMN slug VARCHAR(36) NOT NULL;
ALTER TABLE `role` ADD UNIQUE KEY uk_role_slug (slug);

ALTER TABLE autorisation ADD COLUMN slug VARCHAR(36) NULL;
UPDATE autorisation SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE autorisation MODIFY COLUMN slug VARCHAR(36) NOT NULL;
ALTER TABLE autorisation ADD UNIQUE KEY uk_autorisation_slug (slug);

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'historique_grade'),
    'ALTER TABLE historique_grade ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_historique_grade_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'historique_grade'),
    'UPDATE historique_grade SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'faire_formation'),
    'ALTER TABLE faire_formation ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_faire_formation_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'faire_formation'),
    'UPDATE faire_formation SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'faire_etude'),
    'ALTER TABLE faire_etude ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_faire_etude_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'faire_etude'),
    'UPDATE faire_etude SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'affectation_personnel'),
    'ALTER TABLE affectation_personnel ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_affectation_personnel_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'affectation_personnel'),
    'UPDATE affectation_personnel SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'poste_affectation'),
    'ALTER TABLE poste_affectation ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_poste_affectation_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'poste_affectation'),
    'UPDATE poste_affectation SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'piece_justificative'),
    'ALTER TABLE piece_justificative ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_piece_justificative_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'piece_justificative'),
    'UPDATE piece_justificative SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'changement_situation'),
    'ALTER TABLE changement_situation ADD COLUMN slug VARCHAR(36) NULL, ADD UNIQUE KEY uk_changement_situation_slug (slug)', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
SET @migration_sql = IF(EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'changement_situation'),
    'UPDATE changement_situation SET slug = UUID() WHERE slug IS NULL', 'SELECT 1');
PREPARE migration_statement FROM @migration_sql; EXECUTE migration_statement; DEALLOCATE PREPARE migration_statement;
