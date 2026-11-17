
-- ============================================================
-- FLYWAY MIGRATION
-- CARTOGRAPHIE DES ETABLISSEMENTS PENITENTIAIRES DU MALI
-- SGBD : MYSQL
-- ============================================================

CREATE TABLE IF NOT EXISTS service_ap (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by VARCHAR(100),
    created_date DATETIME(6),
    last_modified_by VARCHAR(100),
    last_modified_date DATETIME(6),
    nom VARCHAR(200) NOT NULL,
    `type` VARCHAR(30) NOT NULL,
    parent_id BIGINT,
    PRIMARY KEY (id),
    KEY idx_service_ap_parent (parent_id),
    CONSTRAINT fk_service_ap_parent FOREIGN KEY (parent_id) REFERENCES service_ap (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 1. DIRECTIONS ADMINISTRATIVES
-- IDs : 1 à 20
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(1, 'Direction Générale de l''Administration Pénitentiaire', 'DIRECTION_GENERALE', NULL),
(2, 'Direction Régionale de l''Administration Pénitentiaire de Kayes', 'DIRECTION_REGIONALE', 1),
(3, 'Direction Régionale de l''Administration Pénitentiaire de Kita', 'DIRECTION_REGIONALE', 1),
(4, 'Direction Régionale de l''Administration Pénitentiaire de Nioro', 'DIRECTION_REGIONALE', 1),
(5, 'Direction Régionale de l''Administration Pénitentiaire de Koulikoro', 'DIRECTION_REGIONALE', 1),
(6, 'Direction Régionale de l''Administration Pénitentiaire de Nara', 'DIRECTION_REGIONALE', 1),
(7, 'Direction Régionale de l''Administration Pénitentiaire de Dioïla', 'DIRECTION_REGIONALE', 1),
(8, 'Direction Régionale de l''Administration Pénitentiaire de Sikasso', 'DIRECTION_REGIONALE', 1),
(9, 'Direction Régionale de l''Administration Pénitentiaire de Koutiala', 'DIRECTION_REGIONALE', 1),
(10, 'Direction Régionale de l''Administration Pénitentiaire de Bougouni', 'DIRECTION_REGIONALE', 1),
(11, 'Direction Régionale de l''Administration Pénitentiaire de Ségou', 'DIRECTION_REGIONALE', 1),
(12, 'Direction Régionale de l''Administration Pénitentiaire de San', 'DIRECTION_REGIONALE', 1),
(13, 'Direction Régionale de l''Administration Pénitentiaire de Mopti', 'DIRECTION_REGIONALE', 1),
(14, 'Direction Régionale de l''Administration Pénitentiaire de Badiangara', 'DIRECTION_REGIONALE', 1),
(15, 'Direction Régionale de l''Administration Pénitentiaire de Douentza', 'DIRECTION_REGIONALE', 1),
(16, 'Direction Régionale de l''Administration Pénitentiaire de Tombouctou', 'DIRECTION_REGIONALE', 1),
(17, 'Direction Régionale de l''Administration Pénitentiaire de Gao', 'DIRECTION_REGIONALE', 1),
(18, 'Direction Régionale de l''Administration Pénitentiaire de Kidal', 'DIRECTION_REGIONALE', 1),
(19, 'Direction Régionale de l''Administration Pénitentiaire de Ménaka', 'DIRECTION_REGIONALE', 1),
(20, 'Direction Régionale de l''Administration Pénitentiaire de Taoudenit', 'DIRECTION_REGIONALE', 1);


-- ============================================================
-- 2. SERVICES RATTACHES DIRECTEMENT A LA DIRECTION GENERALE
-- Parent : 1
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(21, 'Maison Centrale d''Arrêt de Bamako (MCA)', 'SERVICES_RATTACHE', 1),
(22, 'Centre Spécialisé de Détention, de Rééducation et de Réinsertion pour Femmes de Bollé (Bollé Femmes)', 'SERVICES_RATTACHE', 1),
(23, 'Centre Spécialisé de Détention, de Rééducation et de Réinsertion pour Mineurs de Bollé (Bollé Mineurs)', 'SERVICES_RATTACHE', 1);


-- ============================================================
-- 3. REGION DE KAYES
-- Parent : 2
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(24, 'Maison d''Arrêt et de Correction de Kayes', 'MAISON_ARRET_CORRECTION', 2),
(25, 'Maison d''Arrêt et de Correction de Bafoulabé', 'MAISON_ARRET_CORRECTION', 2),
(26, 'Maison d''Arrêt et de Correction de Kéniéba', 'MAISON_ARRET_CORRECTION', 2),
(27, 'Maison d''Arrêt et de Correction de Yélimané', 'MAISON_ARRET_CORRECTION', 2);


-- ============================================================
-- 4. REGION DE KITA
-- Parent : 3
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(28, 'Maison d''Arrêt et de Correction de Kita', 'MAISON_ARRET_CORRECTION', 3),
(29, 'Maison d''Arrêt et de Correction de Toukoto', 'MAISON_ARRET_CORRECTION', 3);


-- ============================================================
-- 5. REGION DE NIORO
-- Parent : 4
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(30, 'Maison d''Arrêt et de Correction de Nioro', 'MAISON_ARRET_CORRECTION', 4),
(31, 'Maison d''Arrêt et de Correction de Diéma', 'MAISON_ARRET_CORRECTION', 4);


-- ============================================================
-- 6. REGION DE KOULIKORO
-- Parent : 5
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(32, 'Maison d''Arrêt et de Correction de Koulikoro', 'MAISON_ARRET_CORRECTION', 5),
(33, 'Maison d''Arrêt et de Correction de Kangaba', 'MAISON_ARRET_CORRECTION', 5),
(34, 'Maison d''Arrêt et de Correction de Kati', 'MAISON_ARRET_CORRECTION', 5),
(35, 'Maison d''Arrêt et de Correction de Kéniéroba', 'MAISON_ARRET_CORRECTION', 5),
(36, 'Maison d''Arrêt et de Correction de Kolokani', 'MAISON_ARRET_CORRECTION', 5),
(37, 'Maison d''Arrêt et de Correction de Banamba', 'MAISON_ARRET_CORRECTION', 5),
(38, 'Centre Pénitentiaire Agricole de Kéniéroba', 'CENTRE_PENITENCIER_AGRICOLE', 5),
(39, 'Centre Pénitentiaire Agricole de Baguinéda', 'CENTRE_PENITENCIER_AGRICOLE', 5);


-- ============================================================
-- 7. REGION DE NARA
-- Parent : 6
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(40, 'Maison d''Arrêt de Nara', 'MAISON_ARRET_CORRECTION', 6);


-- ============================================================
-- 8. REGION DE DIOÏLA
-- Parent : 7
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(41, 'Maison d''Arrêt et de Correction de Dioïla (Dioïla I)', 'MAISON_ARRET_CORRECTION', 7),
(42, 'Maison d''Arrêt et de Correction de Fana', 'MAISON_ARRET_CORRECTION', 7),
(43, 'Quartier à Sécurité Renforcée de Dioïla (Dioïla II)', 'MAISON_ARRET_CORRECTION', 7);


-- ============================================================
-- 9. REGION DE SIKASSO
-- Parent : 8
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(44, 'Maison d''Arrêt et de Correction de Sikasso', 'MAISON_ARRET_CORRECTION', 8),
(45, 'Maison d''Arrêt et de Correction de Kadiolo', 'MAISON_ARRET_CORRECTION', 8),
(46, 'Maison d''Arrêt et de Correction de Kignan', 'MAISON_ARRET_CORRECTION', 8);


-- ============================================================
-- 10. REGION DE KOUTIALA
-- Parent : 9
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(47, 'Maison d''Arrêt et de Correction de Koutiala', 'MAISON_ARRET_CORRECTION', 9),
(48, 'Maison d''Arrêt et de Correction de Yorosso', 'MAISON_ARRET_CORRECTION', 9),
(49, 'Centre Pénitentiaire Agricole de Konseguela', 'CENTRE_PENITENCIER_AGRICOLE', 9);


-- ============================================================
-- 11. REGION DE BOUGOUNI
-- Parent : 10
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(50, 'Maison d''Arrêt et de Correction de Bougouni', 'MAISON_ARRET_CORRECTION', 10),
(51, 'Maison d''Arrêt et de Correction de Kolondieba', 'MAISON_ARRET_CORRECTION', 10),
(52, 'Maison d''Arrêt et de Correction de Ouélessébougou', 'MAISON_ARRET_CORRECTION', 10),
(53, 'Maison d''Arrêt et de Correction de Yanfolila', 'MAISON_ARRET_CORRECTION', 10);


-- ============================================================
-- 12. REGION DE SEGOU
-- Parent : 11
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(54, 'Maison d''Arrêt et de Correction de Ségou', 'MAISON_ARRET_CORRECTION', 11),
(55, 'Maison d''Arrêt et de Correction de Niono', 'MAISON_ARRET_CORRECTION', 11),
(56, 'Maison d''Arrêt et de Correction de Markala', 'MAISON_ARRET_CORRECTION', 11),
(57, 'Maison d''Arrêt et de Correction de Bla', 'MAISON_ARRET_CORRECTION', 11),
(58, 'Maison d''Arrêt et de Correction de Barouéli', 'MAISON_ARRET_CORRECTION', 11),
(59, 'Maison d''Arrêt et de Correction de Macina', 'MAISON_ARRET_CORRECTION', 11);


-- ============================================================
-- 13. REGION DE SAN
-- Parent : 12
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(60, 'Maison d''Arrêt et de Correction de San', 'MAISON_ARRET_CORRECTION', 12),
(61, 'Maison d''Arrêt et de Correction de Tominian', 'MAISON_ARRET_CORRECTION', 12),
(62, 'Maison d''Arrêt et de Correction de Kimparana', 'MAISON_ARRET_CORRECTION', 12),
(63, 'Centre Pénitentiaire Agricole de Tana', 'CENTRE_PENITENCIER_AGRICOLE', 12);


-- ============================================================
-- 14. REGION DE MOPTI
-- Parent : 13
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(64, 'Maison d''Arrêt et de Correction de Mopti', 'MAISON_ARRET_CORRECTION', 13),
(65, 'Maison d''Arrêt et de Correction de Djenné', 'MAISON_ARRET_CORRECTION', 13),
(66, 'Maison d''Arrêt et de Correction de Ténenkou', 'MAISON_ARRET_CORRECTION', 13),
(67, 'Maison d''Arrêt et de Correction de Youwarou', 'MAISON_ARRET_CORRECTION', 13);


-- ============================================================
-- 15. REGION DE BADIANGARA
-- Parent : 14
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(68, 'Maison d''Arrêt et de Correction de Badiangara', 'MAISON_ARRET_CORRECTION', 14),
(69, 'Maison d''Arrêt et de Correction de Bankass', 'MAISON_ARRET_CORRECTION', 14),
(70, 'Maison d''Arrêt et de Correction de Koro', 'MAISON_ARRET_CORRECTION', 14);


-- ============================================================
-- 16. REGION DE DOUENTZA
-- Parent : 15
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(71, 'Maison d''Arrêt et de Correction de Douentza', 'MAISON_ARRET_CORRECTION', 15);


-- ============================================================
-- 17. REGION DE TOMBOUCTOU
-- Parent : 16
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(72, 'Maison d''Arrêt et de Correction de Tombouctou', 'MAISON_ARRET_CORRECTION', 16),
(73, 'Maison d''Arrêt et de Correction de Niafunké', 'MAISON_ARRET_CORRECTION', 16),
(74, 'Maison d''Arrêt et de Correction de Diré', 'MAISON_ARRET_CORRECTION', 16),
(75, 'Maison d''Arrêt et de Correction de Goundam', 'MAISON_ARRET_CORRECTION', 16),
(76, 'Maison d''Arrêt et de Correction de Gourma-Rharous', 'MAISON_ARRET_CORRECTION', 16);


-- ============================================================
-- 18. REGION DE GAO
-- Parent : 17
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(77, 'Maison d''Arrêt et de Correction de Gao', 'MAISON_ARRET_CORRECTION', 17),
(78, 'Maison d''Arrêt et de Correction d''Ansongo', 'MAISON_ARRET_CORRECTION', 17),
(79, 'Maison d''Arrêt et de Correction de Bourem', 'MAISON_ARRET_CORRECTION', 17);


-- ============================================================
-- 19. REGION DE KIDAL
-- Parent : 18
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(80, 'Maison d''Arrêt et de Correction de Kidal', 'MAISON_ARRET_CORRECTION', 18);


-- ============================================================
-- 20. REGION DE MENAKA
-- Parent : 19
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUE
(81, 'Maison d''Arrêt et de Correction de Ménaka', 'MAISON_ARRET_CORRECTION', 19);


-- ============================================================
-- 21. REGION DE TAOUDENIT
-- Parent : 20
-- ============================================================

INSERT IGNORE INTO service_ap (id, nom, `type`, parent_id) VALUES
(82, 'Maison d''Arrêt et de Correction de Taoudenit', 'MAISON_ARRET_CORRECTION', 20);

-- ============================================================
-- FIN DE LA MIGRATION
-- TOTAL : 82 ENREGISTREMENTS
-- 20 DIRECTIONS
-- 3 SERVICES RATTACHES A LA DIRECTION GENERALE
-- 59 ETABLISSEMENTS REGIONAUX
-- ============================================================
