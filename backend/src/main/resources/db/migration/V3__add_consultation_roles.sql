INSERT IGNORE INTO `role` (nom) VALUES
    ('DIRECTEUR_GENERAL'),
    ('DIRECTEUR_GENERAL_ADJOINT'),
    ('CONSULTATION');

INSERT IGNORE INTO role_autorisation (role_id, autorisation_id)
SELECT r.id, a.id
FROM `role` r
CROSS JOIN autorisation a
WHERE r.nom IN ('DIRECTEUR_GENERAL', 'DIRECTEUR_GENERAL_ADJOINT', 'CONSULTATION')
  AND a.nom = 'LIRE_PERSONNEL';

INSERT IGNORE INTO role_autorisation (role_id, autorisation_id)
SELECT r.id, a.id
FROM `role` r
CROSS JOIN autorisation a
WHERE r.nom = 'ADMINISTRATEUR';

DELETE ra
FROM role_autorisation ra
JOIN `role` r ON r.id = ra.role_id
JOIN autorisation a ON a.id = ra.autorisation_id
WHERE r.nom = 'UTILISATEUR'
  AND a.nom = 'EXPORTER_DONNEES';

SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'affectation'
          AND column_name = 'service_id'
    ),
    'ALTER TABLE affectation MODIFY COLUMN service_id BIGINT NULL',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'affectation_personnel'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'affectation_personnel'
          AND column_name = 'service_id'
    ),
    'ALTER TABLE affectation_personnel ADD COLUMN service_id BIGINT NULL',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'affectation_personnel'
          AND column_name = 'service_id'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'affectation'
          AND column_name = 'service_id'
    ),
    'UPDATE affectation_personnel ap JOIN affectation a ON a.id = ap.affectation_id SET ap.service_id = a.service_id WHERE ap.service_id IS NULL',
    'SELECT 1'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;
