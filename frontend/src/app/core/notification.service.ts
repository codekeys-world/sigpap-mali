import { Injectable } from '@angular/core';

export type AlertType = 'success' | 'error' | 'warning' | 'info';

export interface Alerte {
  id: number;
  type: AlertType;
  titre: string;
  message: string;
  details?: string[];
  duree: number;
}

const TITRES: Record<AlertType, string> = {
  success: 'Opération réussie',
  error: 'Une erreur est survenue',
  warning: 'Attention',
  info: 'Information',
};
const DUREES: Record<AlertType, number> = { success: 4000, info: 5000, warning: 7000, error: 9000 };

/** File d'alertes affichées par <app-alerts> : le style, l'icône, le titre et la durée s'adaptent au type. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  alertes: Alerte[] = [];
  private seq = 0;

  success(message: string, titre?: string) {
    this.push('success', message, titre);
  }
  error(message: string, titre?: string, details?: string[]) {
    this.push('error', message, titre, details);
  }
  warning(message: string, titre?: string, details?: string[]) {
    this.push('warning', message, titre, details);
  }
  info(message: string, titre?: string) {
    this.push('info', message, titre);
  }

  push(type: AlertType, message: string, titre?: string, details?: string[]) {
    // évite d'empiler deux fois la même alerte (ex. plusieurs requêtes en échec en même temps)
    if (this.alertes.some((a) => a.type === type && a.message === message)) return;
    const duree = DUREES[type] + (details?.length ?? 0) * 1500;
    const a: Alerte = {
      id: ++this.seq,
      type,
      titre: titre ?? TITRES[type],
      message,
      details,
      duree,
    };
    this.alertes.push(a);
    setTimeout(() => this.dismiss(a.id), duree);
  }

  dismiss(id: number) {
    this.alertes = this.alertes.filter((a) => a.id !== id);
  }
}
