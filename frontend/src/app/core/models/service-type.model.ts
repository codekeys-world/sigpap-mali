export enum TypeService {
  DirectionGenerale = 'DIRECTION_GENERALE',
  DirectionRegionale = 'DIRECTION_REGIONALE',
  ServiceRattache = 'SERVICES_RATTACHE',
  MaisonArretCorrection = 'MAISON_ARRET_CORRECTION',
  CentrePenitencierAgricole = 'CENTRE_PENITENCIER_AGRICOLE',
  AutreService = 'AUTRE_SERVICE',
}

export const TYPES_SERVICE = [
  { value: TypeService.DirectionGenerale, label: 'Direction générale' },
  { value: TypeService.DirectionRegionale, label: 'Direction régionale' },
  { value: TypeService.ServiceRattache, label: 'Service rattaché' },
  { value: TypeService.MaisonArretCorrection, label: "Maison d'arrêt et de correction" },
  { value: TypeService.CentrePenitencierAgricole, label: 'Centre pénitentiaire agricole' },
  { value: TypeService.AutreService, label: 'Autre service (hors administration pénitentiaire)' },
];
