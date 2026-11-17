export enum TypeAffectation {
  PremiereAffectation = 'PREMIERE_AFFECTATION',
  Mutation = 'MUTATION',
  Nomination = 'NOMINATION',
  Detachement = 'DETACHEMENT',
  MiseADisposition = 'MISE_A_DISPOSITION',
  Autre = 'AUTRE',
}

export const TYPES_AFFECTATION = [
  { value: TypeAffectation.PremiereAffectation, label: 'Première affectation' },
  { value: TypeAffectation.Mutation, label: 'Mutation' },
  { value: TypeAffectation.Nomination, label: 'Nomination' },
  { value: TypeAffectation.Detachement, label: 'Détachement' },
  { value: TypeAffectation.MiseADisposition, label: 'Mise à disposition' },
  { value: TypeAffectation.Autre, label: 'Autre' },
];
