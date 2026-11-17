-- Reinitialisation ponctuelle du compte administrateur initial.
-- Applique aussi la correction aux bases deja initialisees.
UPDATE personnel p
JOIN `role` r ON r.nom = 'ADMINISTRATEUR'
SET p.mot_de_passe = '$2a$10$EswcNLZMAVNxSZi3J9BjMOlNzlu3631Xb1ud0p3DSz51ShL17WzD2',
    p.role_id = r.id,
    p.mot_de_passe_temporaire = b'0'
WHERE p.matricule = '0159.203-M';
