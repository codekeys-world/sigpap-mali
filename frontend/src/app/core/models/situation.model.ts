export type SituationAgent = 'ACTIF' | 'MALADE' | 'ABANDON_POSTE' | 'DECEDE' | 'RETRAITE';

export interface SituationSource {
  situation?: SituationAgent | null;
  decede?: boolean;
  situationDepuis?: string | null;
  dateDeces?: string | null;
  situationMotif?: string | null;
}

export interface SituationInfo {
  label: string;
  cls: string;
  icon: string;
  color: string;
  remarque: string;
}

export const SITUATIONS: Record<SituationAgent, SituationInfo> = {
  ACTIF: {
    label: 'En activité',
    cls: 'green',
    icon: 'fa-user-check',
    color: '#009245',
    remarque: 'En activité',
  },
  MALADE: {
    label: 'Malade',
    cls: 'yellow',
    icon: 'fa-bed-pulse',
    color: '#b38f00',
    remarque: 'Malade depuis le',
  },
  ABANDON_POSTE: {
    label: 'Abandon de poste',
    cls: 'red',
    icon: 'fa-person-walking-arrow-right',
    color: '#E30613',
    remarque: 'Abandon de poste depuis le',
  },
  DECEDE: {
    label: 'Décédé(e)',
    cls: 'dark',
    icon: 'fa-dove',
    color: '#4B0D0D',
    remarque: 'Décédé(e) le',
  },
  RETRAITE: {
    label: 'Retraité(e)',
    cls: 'gray',
    icon: 'fa-umbrella-beach',
    color: '#6c757d',
    remarque: 'Retraité(e) depuis le',
  },
};
