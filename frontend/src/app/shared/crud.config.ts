import {
  ADMIN_RH,
  GESTIONNAIRES,
  ROLES,
  TYPES_AFFECTATION,
  TYPES_SERVICE,
  TimelineItem,
  fmtDate,
  label,
} from '../core/models';

export type FieldType =
  | 'text'
  | 'textarea'
  | 'number'
  | 'date'
  | 'select'
  | 'catalogSelect'
  | 'ref'
  | 'multiref'
  | 'file'
  | 'checkbox';

export interface CrudField {
  key: string; // chemin pointé possible : "affectation.service"
  label: string;
  placeholder?: string;
  type?: FieldType;
  required?: boolean;
  options?: { value: any; label: string }[];
  optionsUrl?: string; // ref / multiref
  optionLabel?: string;
  suggest?: { url: string; label: string }; // liste de suggestions pour un champ texte
  table?: boolean; // afficher dans le tableau (défaut : oui)
  virtual?: boolean; // colonne calculée, absente du formulaire
  col?: number; // largeur (1-12) dans le formulaire
  hint?: string;
  format?: (row: any) => string;
  min?: number; // nombres
  max?: number;
  minLength?: number; // textes
  maxLength?: number; // 255 par défaut
  pattern?: string; // expression régulière
  patternMessage?: string;
}

export interface CrudAction {
  name: string;
  label: string;
  icon: string;
}

export interface CrudConfig {
  title: string;
  description?: string;
  section?: string;
  emptyMessage?: string;
  singular: string;
  addLabel?: string;
  fields: CrudField[];
  writeRoles: string[];
  actions?: CrudAction[];
  validate?: (model: any) => string | null; // règle globale (ex. cohérence des dates)
  paged?: boolean; // true = recherche + pagination côté serveur (référentiels)
  timeline?: (row: any) => TimelineItem; // si défini : vue timeline disponible (et par défaut)
}

export const periode = (r: any) => {
  const a = r.dateDebut ? fmtDate(r.dateDebut) : (r.anneeDebut ?? '');
  const b = r.dateFin ? fmtDate(r.dateFin) : (r.anneeFin ?? '');
  return a || b ? `${a || 'Début non renseigné'} au ${b || 'en cours'}` : 'Période non renseignée';
};
const audit = (r: any) =>
  `Saisi par ${r.createdBy || 'Utilisateur inconnu'}${r.createdDate ? ' le ' + fmtDate(r.createdDate) : ''}` +
  (r.lastModifiedBy && r.lastModifiedDate !== r.createdDate
    ? ` · modifié par ${r.lastModifiedBy} le ${fmtDate(r.lastModifiedDate)}`
    : '');
const enCours = (r: any) => !r.dateFin && !r.anneeFin;

/** Cohérence d'une période : début obligatoire (date ou année), fin postérieure au début. */
export const validerPeriode = (m: any): string | null => {
  if (!m.dateDebut && !m.anneeDebut)
    return "Renseignez la date de début ou, à défaut, l'année de début.";
  if (m.dateDebut && m.dateFin && m.dateFin < m.dateDebut)
    return 'La date de fin ne peut pas précéder la date de début.';
  if (m.anneeDebut && m.anneeFin && Number(m.anneeFin) < Number(m.anneeDebut))
    return "L'année de fin ne peut pas précéder l'année de début.";
  const debut = m.dateDebut ? Number(m.dateDebut.slice(0, 4)) : Number(m.anneeDebut);
  const fin = m.dateFin ? Number(m.dateFin.slice(0, 4)) : m.anneeFin ? Number(m.anneeFin) : null;
  if (fin != null && fin < debut) return 'La fin de la période ne peut pas précéder son début.';
  return null;
};

const PERIODE: CrudField[] = [
  { key: '_periode', label: 'Période', virtual: true, format: periode },
  { key: 'dateDebut', label: 'Date de début', type: 'date', table: false, col: 3 },
  { key: 'dateFin', label: 'Date de fin', type: 'date', table: false, col: 3 },
  {
    key: 'anneeDebut',
    label: 'Année de début',
    type: 'number',
    table: false,
    col: 3,
    min: 1950,
    max: 2100,
    hint: 'Si la date exacte est oubliée',
  },
  {
    key: 'anneeFin',
    label: 'Année de fin',
    type: 'number',
    table: false,
    col: 3,
    min: 1950,
    max: 2100,
  },
];

export const CATALOGS: Record<string, { url: string; config: CrudConfig }> = {
  corps: {
    url: '/api/corps',
    config: {
      title: 'Corps',
      description:
        'Consultez les corps de l’Administration pénitentiaire et leurs paramètres de carrière.',
      singular: 'corps',
      addLabel: 'Nouveau corps',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [
        { key: 'nom', label: 'Nom du corps', required: true, col: 8 },
        {
          key: 'ageRetraite',
          label: 'Âge de départ à la retraite',
          type: 'number',
          min: 40,
          max: 75,
          col: 4,
          hint: 'Vide : âge par défaut (60 ans)',
        },
      ],
      actions: [{ name: 'grades', label: 'Grades', icon: 'fa-award' }],
    },
  },
  grades: {
    url: '/api/grades',
    config: {
      title: 'Grades',
      description: 'Retrouvez les grades et leur rattachement aux différents corps.',
      singular: 'grade',
      addLabel: 'Nouveau grade',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [
        { key: 'nom', label: 'Nom du grade', required: true },
        {
          key: 'corps',
          label: 'Corps',
          type: 'ref',
          optionsUrl: '/api/corps/all',
          optionLabel: 'nom',
          required: true,
        },
      ],
    },
  },
  formations: {
    url: '/api/formations',
    config: {
      title: 'Formations certifiantes',
      description:
        'Organisez les formations proposées pour accompagner les parcours professionnels.',
      singular: 'formation',
      addLabel: 'Nouvelle formation',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [{ key: 'titre', label: 'Titre de la formation', required: true, col: 12 }],
    },
  },
  etudes: {
    url: '/api/etudes',
    config: {
      title: 'Études diplômantes',
      description: 'Consultez les études et les diplômes utilisés dans les dossiers des agents.',
      singular: 'étude',
      addLabel: 'Nouvelle étude',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [
        { key: 'intitule', label: 'Intitulé', required: true },
        {
          key: 'niveauEtude',
          label: "Niveau d'étude",
          type: 'catalogSelect',
          optionsUrl: '/api/niveaux-etude/all',
          optionLabel: 'nom',
          required: true,
        },
      ],
    },
  },
  'niveaux-etude': {
    url: '/api/niveaux-etude',
    config: {
      title: "Niveaux d'étude",
      description:
        'Retrouvez les niveaux d’étude associés aux diplômes et aux parcours des agents.',
      singular: "niveau d'étude",
      addLabel: 'Ajouter un niveau',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [{ key: 'nom', label: 'Nom du niveau', required: true, col: 12 }],
    },
  },
  affectations: {
    url: '/api/affectations',
    config: {
      title: 'Mutations de référence',
      description: 'Centralisez les références et les décisions utilisées pour les affectations.',
      singular: 'mutation',
      addLabel: 'Ajouter une mutation',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [
        { key: 'reference', label: 'Référence de la mutation', required: true },
        { key: 'type', label: 'Type', type: 'select', options: TYPES_AFFECTATION, required: true },
        { key: 'fichierDecision', label: 'Décision jointe', type: 'file', required: true },
      ],
    },
  },
  'arretes-integration': {
    url: '/api/arretes-integration',
    config: {
      title: 'Arrêtés d’intégration',
      description: 'Retrouvez les arrêtés qui encadrent l’intégration des agents.',
      singular: 'arrêté d’intégration',
      addLabel: 'Ajouter un arrêté',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [
        { key: 'reference', label: 'Référence de l’arrêté', required: true, col: 12 },
        {
          key: 'fichier',
          label: 'Fichier de l’arrêté',
          type: 'file',
          required: true,
          col: 12,
          table: true,
        },
      ],
    },
  },
  services: {
    url: '/api/services',
    config: {
      title: 'Services',
      description: 'Consultez les services, les établissements et leur organisation.',
      singular: 'service',
      addLabel: 'Nouveau service',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [
        { key: 'nom', label: 'Nom du service', required: true },
        { key: 'type', label: 'Type', type: 'select', options: TYPES_SERVICE, required: true },
        {
          key: 'parent',
          label: 'Relève de',
          type: 'ref',
          optionsUrl: '/api/services/all',
          optionLabel: 'nom',
          hint: "Pour une maison d'arrêt : sa direction régionale",
        },
      ],
    },
  },
  postes: {
    url: '/api/postes',
    config: {
      title: 'Postes',
      description: 'Retrouvez les fonctions et les postes occupés dans les services.',
      singular: 'poste',
      addLabel: 'Nouveau poste',
      writeRoles: ADMIN_RH,
      paged: true,
      fields: [{ key: 'nom', label: 'Nom du poste', required: true, col: 12 }],
    },
  },
  roles: {
    url: '/api/roles',
    config: {
      title: 'Rôles',
      description: 'Consultez les rôles et définissez les autorisations qui leur sont associées.',
      section: 'Administration',
      singular: 'rôle',
      addLabel: 'Nouveau rôle',
      writeRoles: [ROLES.ADMIN],
      paged: true,
      fields: [
        {
          key: 'nom',
          label: 'Nom du rôle',
          required: true,
          col: 12,
          pattern: '^[A-Za-z0-9_]+$',
          patternMessage: 'Lettres, chiffres et _ uniquement (ex. GESTIONNAIRE_RH)',
        },
        {
          key: 'autorisations',
          label: 'Autorisations',
          type: 'multiref',
          optionsUrl: '/api/autorisations/all',
          optionLabel: 'nom',
          col: 12,
        },
      ],
    },
  },
  autorisations: {
    url: '/api/autorisations',
    config: {
      title: 'Autorisations',
      description:
        'Retrouvez les droits d’accès utilisés pour les différents rôles de l’application.',
      section: 'Administration',
      singular: 'autorisation',
      addLabel: 'Nouvelle autorisation',
      writeRoles: [ROLES.ADMIN],
      paged: true,
      fields: [
        {
          key: 'nom',
          label: "Nom de l'autorisation",
          required: true,
          col: 12,
          pattern: '^[A-Za-z0-9_]+$',
          patternMessage: 'Lettres, chiffres et _ uniquement (ex. LIRE_PERSONNEL)',
        },
      ],
    },
  },
};

// ---- Sous-ressources d'un personnel (affichées dans sa fiche) ----
export const PERSONNEL_CFG: Record<
  | 'grades'
  | 'formations'
  | 'etudes'
  | 'affectations'
  | 'postes'
  | 'sanctions'
  | 'conges'
  | 'distinctions',
  CrudConfig
> = {
  grades: {
    validate: validerPeriode,
    title: 'Historique des grades',
    emptyMessage: 'Aucun grade enregistré dans ce dossier pour le moment.',
    singular: 'grade porté',
    addLabel: 'Ajouter un grade',
    writeRoles: GESTIONNAIRES,
    timeline: (r) => ({
      date: periode(r),
      titre: r.grade?.nom,
      sousTitre: r.grade?.corps?.nom,
      icon: 'fa-award',
      color: '#8B1E1E',
      enCours: enCours(r),
      fichiers: r.fichierAvancement
        ? [{ label: "Décision d'avancement", nom: r.fichierAvancement }]
        : [],
      audit: audit(r),
    }),
    fields: [
      {
        key: 'grade',
        label: 'Grade',
        type: 'ref',
        optionsUrl: '/api/grades/all',
        optionLabel: 'nom',
        required: true,
        col: 12,
      },
      ...PERIODE,
      {
        key: 'fichierAvancement',
        label: "Décision d'avancement du grade",
        type: 'file',
        required: true,
        col: 12,
      },
    ],
  },
  formations: {
    validate: validerPeriode,
    title: 'Formations suivies',
    emptyMessage: 'Aucune formation enregistrée dans ce dossier pour le moment.',
    singular: 'formation',
    addLabel: 'Ajouter une formation',
    writeRoles: GESTIONNAIRES,
    timeline: (r) => ({
      date: periode(r),
      titre: r.formation?.titre,
      sousTitre: r.lieu,
      icon: 'fa-graduation-cap',
      color: '#009245',
      details: r.description ? [{ label: 'Description', value: r.description }] : [],
      fichiers: r.fichierAttestation
        ? [{ label: 'Attestation de formation', nom: r.fichierAttestation }]
        : [],
      audit: audit(r),
    }),
    fields: [
      {
        key: 'formation',
        label: 'Formation',
        type: 'ref',
        optionsUrl: '/api/formations/all',
        optionLabel: 'titre',
        required: true,
        col: 12,
      },
      ...PERIODE,
      { key: 'lieu', label: 'Lieu', maxLength: 200, placeholder: 'Ex. : Bamako' },
      {
        key: 'description',
        label: 'Description',
        type: 'textarea',
        col: 12,
        table: false,
        maxLength: 1000,
      },
      { key: 'fichierAttestation', label: 'Attestation de formation', type: 'file', col: 12 },
    ],
  },
  etudes: {
    validate: validerPeriode,
    title: 'Études suivies',
    emptyMessage: 'Aucune étude enregistrée dans ce dossier pour le moment.',
    singular: 'étude',
    addLabel: 'Ajouter une étude',
    writeRoles: GESTIONNAIRES,
    timeline: (r) => ({
      date: periode(r),
      titre: r.etude?.intitule,
      sousTitre: [r.etude?.niveauEtude, r.lieu].filter((x) => x).join(' · '),
      icon: 'fa-book-bookmark',
      color: '#b38f00',
      details: [
        ...(r.option ? [{ label: 'Option', value: r.option }] : []),
        ...(r.description ? [{ label: 'Description', value: r.description }] : []),
      ],
      fichiers: r.fichierDiplome ? [{ label: 'Diplôme', nom: r.fichierDiplome }] : [],
      audit: audit(r),
    }),
    fields: [
      {
        key: 'etude',
        label: 'Étude et niveau de diplôme',
        type: 'ref',
        optionsUrl: '/api/etudes/all',
        optionLabel: 'intitule',
        required: true,
        col: 12,
      },
      ...PERIODE,
      { key: 'lieu', label: "Lieu de l'étude" },
      { key: 'option', label: "Option de l'étude" },
      {
        key: 'description',
        label: 'Description',
        type: 'textarea',
        col: 12,
        table: false,
        maxLength: 1000,
      },
      { key: 'fichierDiplome', label: 'Diplôme', type: 'file', required: true, col: 12 },
    ],
  },
  affectations: {
    validate: validerPeriode,
    title: 'Affectations',
    emptyMessage: 'Aucune affectation enregistrée dans ce dossier pour le moment.',
    singular: 'affectation',
    addLabel: 'Ajouter une affectation',
    writeRoles: GESTIONNAIRES,
    timeline: (r) => ({
      date: periode(r),
      titre: r.affectation?.reference,
      sousTitre: r.service?.nom,
      badge: label(TYPES_AFFECTATION, r.affectation?.type),
      icon: 'fa-right-left',
      color: '#4B0D0D',
      enCours: enCours(r),
      details: [
        ...(r.affectation?.reference
          ? [{ label: 'Référence', value: r.affectation.reference }]
          : []),
        ...(r.dureeMois != null ? [{ label: 'Durée', value: r.dureeMois + ' mois' }] : []),
      ],
      fichiers: r.affectation?.fichierDecision
        ? [{ label: 'Décision', nom: r.affectation.fichierDecision }]
        : [],
      audit: audit(r),
    }),
    actions: [{ name: 'postes', label: 'Postes', icon: 'fa-briefcase' }],
    fields: [
      { key: 'affectation.fichierDecision', label: 'Décision jointe', type: 'file', virtual: true },
      {
        key: 'affectation',
        label: 'Référence de la mutation',
        type: 'ref',
        optionsUrl: '/api/affectations/all',
        optionLabel: 'reference',
        required: true,
        col: 12,
        hint: 'La référence et les documents associés sont gérés dans le module Mutations de référence.',
      },
      {
        key: 'service',
        label: "Service d'affectation",
        type: 'ref',
        optionsUrl: '/api/services/all',
        optionLabel: 'nom',
        required: true,
        col: 12,
      },
      ...PERIODE,
    ],
  },
  postes: {
    validate: (m) =>
      !m.dateDebut
        ? 'La date de début est obligatoire.'
        : m.dateFin && m.dateFin < m.dateDebut
          ? 'La date de fin ne peut pas précéder la date de début.'
          : null,
    title: "Postes occupés pendant l'affectation",
    singular: 'poste',
    addLabel: 'Ajouter un poste',
    writeRoles: GESTIONNAIRES,
    timeline: (r) => ({
      date: periode(r),
      titre: r.poste?.nom,
      sousTitre: r.description,
      icon: 'fa-briefcase',
      color: '#009245',
      enCours: !r.dateFin,
      fichiers: r.fichier ? [{ label: 'Pièce justificative', nom: r.fichier }] : [],
      audit: audit(r),
    }),
    fields: [
      {
        key: 'poste.nom',
        label: 'Poste',
        required: true,
        col: 12,
        maxLength: 150,
        suggest: { url: '/api/postes/all', label: 'nom' },
      },
      {
        key: 'dateDebut',
        label: 'Date de début',
        type: 'date',
        required: true,
        table: false,
        col: 6,
      },
      { key: 'dateFin', label: 'Date de fin', type: 'date', table: false, col: 6 },
      { key: '_periode', label: 'Période', virtual: true, format: periode },
      { key: 'description', label: 'Description', type: 'textarea', col: 12, maxLength: 1000 },
      {
        key: 'fichier',
        label: 'Pièce justificative du poste',
        type: 'file',
        required: true,
        col: 12,
      },
    ],
  },
  conges: {
    title: 'Congés et absences',
    singular: 'congé ou absence',
    addLabel: 'Ajouter un congé ou une absence',
    emptyMessage: 'Aucun congé ni aucune absence enregistré pour cet agent.',
    writeRoles: GESTIONNAIRES,
    validate: validerPeriode,
    timeline: (r) => ({
      date: periode(r),
      titre: r.nature,
      sousTitre: r.observations,
      badge: r.type === 'CONGE' ? 'Congé' : 'Absence',
      icon: 'fa-calendar-days',
      color: '#009245',
      details: [
        ...(r.dureeJours != null
          ? [{ label: 'Durée', value: r.dureeJours + ' jour(s) calendaires' }]
          : []),
        ...(r.reference ? [{ label: 'Référence', value: r.reference }] : []),
      ],
      fichiers: r.fichier ? [{ label: 'Justificatif', nom: r.fichier }] : [],
      audit: audit(r),
    }),
    fields: [
      {
        key: 'type',
        label: 'Type',
        type: 'select',
        required: true,
        options: [
          { value: 'CONGE', label: 'Congé' },
          { value: 'ABSENCE', label: 'Absence' },
        ],
      },
      {
        key: 'nature',
        label: 'Nature',
        required: true,
        maxLength: 150,
        placeholder: 'Ex. : congé annuel, absence autorisée',
      },
      { key: 'dateDebut', label: 'Date de début', type: 'date', required: true },
      {
        key: 'dateFin',
        label: 'Date de fin',
        type: 'date',
        hint: 'Laissez vide si la date de fin est encore inconnue.',
      },
      {
        key: 'reference',
        label: 'Référence de la décision',
        maxLength: 200,
        placeholder: 'Ex. : Décision n° 2026-001',
      },
      {
        key: 'observations',
        label: 'Observations',
        type: 'textarea',
        maxLength: 1000,
        col: 12,
        placeholder: 'Précisez les informations utiles sur cette période',
      },
      { key: 'fichier', label: 'Justificatif', type: 'file', col: 12 },
    ],
  },
  distinctions: {
    title: 'Distinctions',
    singular: 'distinction',
    addLabel: 'Ajouter une distinction',
    emptyMessage: 'Aucune distinction enregistrée pour cet agent.',
    writeRoles: GESTIONNAIRES,
    timeline: (r) => ({
      date: fmtDate(r.dateAttribution),
      titre: r.intitule,
      sousTitre: r.motif,
      icon: 'fa-medal',
      color: '#b58a25',
      details: [
        ...(r.autorite ? [{ label: 'Autorité', value: r.autorite }] : []),
        ...(r.reference ? [{ label: 'Référence', value: r.reference }] : []),
      ],
      fichiers: r.fichier ? [{ label: 'Justificatif', nom: r.fichier }] : [],
      audit: audit(r),
    }),
    fields: [
      {
        key: 'intitule',
        label: 'Intitulé de la distinction',
        required: true,
        maxLength: 150,
        placeholder: 'Ex. : médaille du mérite',
      },
      { key: 'dateAttribution', label: 'Date d’attribution', type: 'date', required: true },
      {
        key: 'autorite',
        label: 'Autorité ayant décerné la distinction',
        maxLength: 200,
        placeholder: 'Ex. : ministère de la Justice',
      },
      {
        key: 'reference',
        label: 'Référence de la décision',
        maxLength: 200,
        placeholder: 'Ex. : Décret n° 2026-001',
      },
      {
        key: 'motif',
        label: 'Motif',
        type: 'textarea',
        maxLength: 1000,
        col: 12,
        placeholder: 'Précisez les raisons de cette distinction',
      },
      { key: 'fichier', label: 'Justificatif', type: 'file', col: 12 },
    ],
  },
  sanctions: {
    title: 'Sanctions disciplinaires',
    singular: 'sanction',
    addLabel: 'Ajouter une sanction',
    writeRoles: GESTIONNAIRES,
    timeline: (r) => ({
      date: fmtDate(r.dateSanction),
      titre: r.nature,
      sousTitre: r.motif,
      icon: 'fa-scale-balanced',
      color: '#8B1E1E',
      details: r.reference ? [{ label: 'Référence', value: r.reference }] : [],
      fichiers: r.fichier ? [{ label: 'Décision de sanction', nom: r.fichier }] : [],
      audit: audit(r),
    }),
    fields: [
      { key: 'nature', label: 'Nature de la sanction', required: true, maxLength: 120 },
      { key: 'dateSanction', label: 'Date de la sanction', type: 'date', required: true },
      { key: 'motif', label: 'Motif', type: 'textarea', required: true, col: 12, maxLength: 1000 },
      { key: 'reference', label: 'Référence de la décision', maxLength: 200 },
      { key: 'fichier', label: 'Décision de sanction', type: 'file', col: 12 },
    ],
  },
};

// Les limites suivent les tailles des champs enregistrés sur le serveur.
for (const [key, maxLength] of Object.entries({
  corps: 150,
  grades: 150,
  formations: 200,
  etudes: 200,
  'niveaux-etude': 100,
  services: 200,
  postes: 150,
  roles: 60,
  autorisations: 100,
})) {
  CATALOGS[key].config.fields[0].maxLength = maxLength;
}
CATALOGS.etudes.config.fields[1].maxLength = 100;
CATALOGS['arretes-integration'].config.fields[0].maxLength = 150;

for (const [key, placeholder] of Object.entries({
  corps: 'Ex. : corps administratif',
  grades: 'Ex. : inspecteur principal',
  formations: 'Ex. : gestion des ressources humaines',
  etudes: 'Ex. : licence en droit',
  'niveaux-etude': 'Ex. : licence',
  services: 'Ex. : direction régionale de Bamako',
  postes: 'Ex. : chef de division',
  roles: 'Ex. : GESTIONNAIRE_RH',
  autorisations: 'Ex. : LIRE_PERSONNEL',
})) {
  CATALOGS[key].config.fields[0].placeholder = placeholder;
}
