export interface Session {
  slug: string;
  matricule: string;
  nom: string;
  prenom: string;
  photo?: string | null;
  role: string;
  autorisations: string[];
  motDePasseTemporaire?: boolean;
}

export type AutorisationSession = string | { nom: string };

/** La connexion renvoie le nom du rôle, tandis que le profil renvoie l'objet rôle. */
export interface ProfilAuthentifie extends Omit<Session, 'role' | 'autorisations'> {
  role?: string | { nom: string; autorisations?: AutorisationSession[] } | null;
  autorisations?: AutorisationSession[];
}
