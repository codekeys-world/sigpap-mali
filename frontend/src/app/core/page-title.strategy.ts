import { Injectable } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { RouterStateSnapshot, TitleStrategy } from '@angular/router';

@Injectable()
export class PageTitleStrategy extends TitleStrategy {
  constructor(
    private title: Title,
    private meta: Meta,
  ) {
    super();
  }

  override updateTitle(snapshot: RouterStateSnapshot): void {
    let route = snapshot.root;
    while (route.firstChild) route = route.firstChild;
    const label = this.buildTitle(snapshot) || 'Accueil';
    const title = `${label} | SIGPAP-MALI`;
    const descriptions: Record<string, string> = {
      Accueil:
        'SIGPAP-MALI centralise la gestion des agents et le suivi des carrières de l’Administration pénitentiaire du Mali.',
      Connexion:
        'Connectez-vous à votre espace sécurisé SIGPAP-MALI avec votre matricule et votre mot de passe.',
      'Tableau de bord':
        'Consultez les effectifs, la répartition par corps et les indicateurs du personnel dans votre périmètre autorisé.',
      'Dossier individuel':
        'Consultez l’identité, la situation, les affectations, les formations et les documents de carrière d’un agent.',
      'Mon profil':
        'Consultez vos informations personnelles et gérez votre mot de passe dans votre espace SIGPAP-MALI.',
      'Page introuvable':
        'Cette page SIGPAP-MALI est introuvable. Retrouvez l’accueil ou votre espace personnel depuis cette page.',
    };
    const description =
      route.data['description'] ||
      descriptions[label] ||
      `${label} : consultez et gérez les informations du personnel de l’Administration pénitentiaire du Mali dans SIGPAP-MALI.`;
    this.title.setTitle(title);
    this.meta.updateTag({ name: 'description', content: description });
    this.meta.updateTag({
      name: 'robots',
      content: snapshot.url.split(/[?#]/)[0] === '/' ? 'index, follow' : 'noindex, nofollow',
    });
    this.meta.updateTag({ property: 'og:title', content: title });
    this.meta.updateTag({ property: 'og:description', content: description });
    this.meta.updateTag({ property: 'og:type', content: 'website' });
  }
}
