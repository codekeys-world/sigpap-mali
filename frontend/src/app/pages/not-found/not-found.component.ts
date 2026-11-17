import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="not-found" aria-labelledby="not-found-title">
      <img
        src="assets/logo-small.webp"
        width="90"
        height="116"
        alt="Emblème de l’Administration pénitentiaire du Mali"
      />
      <p class="code">404</p>
      <h1 id="not-found-title">Cette page est introuvable</h1>
      <p>
        Le lien a peut-être changé ou l’adresse contient une erreur. Retrouvez votre chemin depuis
        l’accueil ou votre espace personnel.
      </p>
      <div>
        <a class="btn btn-primary" routerLink="/">Retour à l’accueil</a>
        <a class="btn btn-outline-primary" routerLink="/app/dashboard">Mon espace</a>
      </div>
    </section>
  `,
  styles: `
    :host {
      display: block;
    }
    .not-found {
      max-width: 700px;
      margin: clamp(1rem, 6vh, 5rem) auto;
      padding: clamp(1.2rem, 4vw, 3rem);
      text-align: center;
      background: #fff;
      border: 1px solid #eadfda;
      border-radius: 20px;
    }
    .not-found img {
      object-fit: contain;
    }
    .code {
      font-size: clamp(3rem, 9vw, 5rem);
      font-weight: 800;
      color: #751919;
      margin: 0.5rem 0;
    }
    h1 {
      font-size: clamp(1.4rem, 4vw, 2rem);
      font-weight: 750;
    }
    p {
      color: #5d5350;
      line-height: 1.7;
    }
    div {
      display: flex;
      justify-content: center;
      flex-wrap: wrap;
      gap: 0.75rem;
      margin-top: 1.5rem;
    }
    a {
      min-height: 44px;
    }
  `,
})
export class NotFoundComponent {}
