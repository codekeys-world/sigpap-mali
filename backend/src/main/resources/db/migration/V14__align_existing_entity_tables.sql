-- Hibernate may already have added this column in development databases.
SET @migration_sql = IF(EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'personnel' AND column_name = 'fonction'
), 'SELECT 1', 'ALTER TABLE personnel ADD COLUMN fonction VARCHAR(255)');
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

-- V5 left slugs nullable when these tables already existed.
UPDATE formation SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE formation MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE etude SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE etude MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE niveau_etude SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE niveau_etude MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE poste SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE poste MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE affectation SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE affectation MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE affectation_personnel SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE affectation_personnel MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE poste_affectation SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE poste_affectation MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE historique_grade SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE historique_grade MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE faire_formation SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE faire_formation MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE faire_etude SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE faire_etude MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE changement_situation SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE changement_situation MODIFY COLUMN slug VARCHAR(36) NOT NULL;

UPDATE piece_justificative SET slug = UUID() WHERE slug IS NULL;
ALTER TABLE piece_justificative MODIFY COLUMN slug VARCHAR(36) NOT NULL;

-- Recognize equivalent foreign keys created by Hibernate under generated names.
SET @migration_sql = IF(EXISTS (
    SELECT 1 FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'personnel' AND column_name = 'grade_actuel_id'
      AND referenced_table_schema = DATABASE()
      AND referenced_table_name = 'grade' AND referenced_column_name = 'id'
), 'SELECT 1', 'ALTER TABLE personnel ADD CONSTRAINT fk_personnel_grade_actuel
    FOREIGN KEY (grade_actuel_id) REFERENCES grade (id)');
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (
    SELECT 1 FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'personnel' AND column_name = 'corps_actuel_id'
      AND referenced_table_schema = DATABASE()
      AND referenced_table_name = 'corps' AND referenced_column_name = 'id'
), 'SELECT 1', 'ALTER TABLE personnel ADD CONSTRAINT fk_personnel_corps_actuel
    FOREIGN KEY (corps_actuel_id) REFERENCES corps (id)');
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (
    SELECT 1 FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'personnel' AND column_name = 'service_courant_id'
      AND referenced_table_schema = DATABASE()
      AND referenced_table_name = 'service_ap' AND referenced_column_name = 'id'
), 'SELECT 1', 'ALTER TABLE personnel ADD CONSTRAINT fk_personnel_service_courant
    FOREIGN KEY (service_courant_id) REFERENCES service_ap (id)');
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(EXISTS (
    SELECT 1 FROM information_schema.key_column_usage
    WHERE table_schema = DATABASE()
      AND table_name = 'affectation_personnel' AND column_name = 'service_id'
      AND referenced_table_schema = DATABASE()
      AND referenced_table_name = 'service_ap' AND referenced_column_name = 'id'
), 'SELECT 1', 'ALTER TABLE affectation_personnel ADD CONSTRAINT fk_affectation_personnel_service
    FOREIGN KEY (service_id) REFERENCES service_ap (id)');
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
