UPDATE personnel p
JOIN `role` r ON r.nom = 'ADMINISTRATEUR'
SET p.role_id = r.id
WHERE p.matricule = '0159.203-M'
  AND (p.role_id IS NULL OR p.role_id <> r.id);
