import { SituationSource, SITUATIONS, SituationAgent } from './situation.model';

export const label = (list: { value: string; label: string }[], value: string | null | undefined) =>
  list.find((item) => item.value === value)?.label ?? value ?? '';

export const initiales = (nom?: string, prenom?: string) =>
  `${(prenom ?? '?').charAt(0)}${(nom ?? '?').charAt(0)}`.toUpperCase();

export const fmtDate = (date?: string | null) =>
  date ? date.split('T')[0].split('-').reverse().join('/') : '';

export const situationDe = (personnel: SituationSource): SituationAgent =>
  personnel.situation ?? (personnel.decede ? 'DECEDE' : 'ACTIF');

export const infoSituation = (personnel: SituationSource) => SITUATIONS[situationDe(personnel)];

/** Phrase de remarque, par exemple « Décédé(e) le 12/03/2026 : motif ». */
export const remarqueSituation = (personnel: SituationSource): string => {
  const situation = situationDe(personnel);
  if (situation === 'ACTIF') return '';
  const date = fmtDate(personnel.situationDepuis || personnel.dateDeces);
  return `${SITUATIONS[situation].remarque} ${date}${personnel.situationMotif ? ' : ' + personnel.situationMotif : ''}`;
};

/** Nom lisible d'un fichier à partir de son chemin, sans le préfixe UUID. */
export const nomFichier = (path?: string | null) =>
  path
    ? path
        .split('/')
        .pop()!
        .replace(/^[0-9a-fA-F-]{36}_/, '')
    : '';
