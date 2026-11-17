import { Injectable } from '@angular/core';

/** Compteur de requêtes HTTP en cours : alimente la barre de chargement en haut de l'écran. */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  count = 0;
  private timer: any;
  visible = false;

  start() {
    this.count++;
    clearTimeout(this.timer);
    this.timer = setTimeout(() => (this.visible = this.count > 0), 150); // évite le clignotement sur les requêtes rapides
  }
  stop() {
    this.count = Math.max(0, this.count - 1);
    if (this.count === 0) {
      clearTimeout(this.timer);
      this.visible = false;
    }
  }
}
