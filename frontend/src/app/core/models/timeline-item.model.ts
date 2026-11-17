/** Élément générique d'une timeline (grades, formations, études ou mutations). */
export interface TimelineItem {
  date: string;
  titre: string;
  sousTitre?: string;
  badge?: string;
  badgeClass?: string;
  icon?: string;
  color?: string;
  enCours?: boolean;
  details?: { label: string; value: string }[];
  fichiers?: { label: string; nom: string }[];
  liste?: { titre: string; items: string[] };
  audit?: string;
  ref?: unknown;
}
