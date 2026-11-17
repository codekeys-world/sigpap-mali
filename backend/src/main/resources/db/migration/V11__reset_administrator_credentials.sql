-- Reinitialisation demandee du mot de passe du compte administrateur.
-- Le mot de passe est conserve uniquement sous forme d'empreinte BCrypt.
UPDATE personnel
SET mot_de_passe = '$2a$10$Puq8UEyojG5vpRNTfu2EvODVJ5V4HMfIOCWmMoOsGw5WG27d4yMNe',
    mot_de_passe_temporaire = b'0'
WHERE matricule = '0159.203-M';
