export const ROLES = {
  ADMIN: 'ADMINISTRATEUR',
  RH: 'GESTIONNAIRE_RH',
  DR: 'DIRECTEUR_REGIONAL',
  CHEF: 'CHEF_ETABLISSEMENT',
  USER: 'UTILISATEUR',
  DG: 'DIRECTEUR_GENERAL',
  DGA: 'DIRECTEUR_GENERAL_ADJOINT',
  CONSULTATION: 'CONSULTATION',
} as const;

export const GESTIONNAIRES = [ROLES.ADMIN, ROLES.RH, ROLES.DR, ROLES.CHEF];
export const ADMIN_RH = [ROLES.ADMIN, ROLES.RH];

export const ROLE_LABELS: Record<string, string> = {
  ADMINISTRATEUR: 'Administrateur',
  GESTIONNAIRE_RH: 'Gestionnaire RH',
  DIRECTEUR_REGIONAL: 'Directeur régional',
  CHEF_ETABLISSEMENT: "Chef d'établissement",
  UTILISATEUR: 'Utilisateur',
  DIRECTEUR_GENERAL: 'Directeur général',
  DIRECTEUR_GENERAL_ADJOINT: 'Directeur général adjoint',
  CONSULTATION: 'Consultation',
};
