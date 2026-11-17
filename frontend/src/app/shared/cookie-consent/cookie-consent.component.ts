import { Component } from '@angular/core';

const KEY = 'sigpap_cookie_choice_v1';
const DURATION = 180 * 24 * 60 * 60 * 1000;

@Component({
  selector: 'app-cookie-consent',
  standalone: true,
  template: `
    @if (visible) {
      <section
        class="cookie-banner"
        aria-labelledby="cookie-title"
        aria-describedby="cookie-description"
      >
        <div>
          <h2 id="cookie-title">Cookies et confidentialité</h2>
          <p id="cookie-description">
            SIGPAP-MALI utilise des cookies nécessaires à la connexion et à la sécurité de votre
            espace. Aucun cookie publicitaire ni de mesure d’audience n’est actuellement utilisé.
            Refuser les cookies facultatifs ne bloque pas l’accès à l’application.
          </p>
          <details>
            <summary>En savoir plus</summary>
            <p>
              Les cookies nécessaires maintiennent votre session et protègent les formulaires. Le
              choix « Se souvenir de moi » prolonge la connexion uniquement à votre demande. Votre
              préférence est conservée sur cet appareil pendant six mois.
            </p>
          </details>
        </div>
        <div class="cookie-actions">
          <button class="btn btn-outline-primary" (click)="choose('necessary-only')">
            Refuser les cookies facultatifs
          </button>
          <button class="btn btn-primary" (click)="choose('accepted')">Accepter</button>
        </div>
      </section>
    }
  `,
  styleUrl: './cookie-consent.component.css',
})
export class CookieConsentComponent {
  visible = !this.savedChoice();

  private savedChoice(): boolean {
    try {
      const choice = JSON.parse(localStorage.getItem(KEY) || 'null');
      const age = Date.now() - choice?.savedAt;
      return ['accepted', 'necessary-only'].includes(choice?.choice) && age >= 0 && age < DURATION;
    } catch {
      return false;
    }
  }

  choose(choice: 'accepted' | 'necessary-only'): void {
    try {
      localStorage.setItem(KEY, JSON.stringify({ choice, savedAt: Date.now() }));
    } catch {
      /* Le choix reste valable pour cet affichage si le stockage est indisponible. */
    }
    this.visible = false;
    // Aucun traceur facultatif n'est chargé, quel que soit le choix.
  }
}
