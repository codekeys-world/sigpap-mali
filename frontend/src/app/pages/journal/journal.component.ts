import { AlertComponent } from '../../shared/alert/alert.component';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { PageResult } from '../../core/models';
import { PagerComponent } from '../../shared/pager/pager.component';

const ACTIONS: Record<string, { label: string; icon: string; cls: string }> = {
  CREATION: { label: 'Création', icon: 'fa-plus', cls: 'green' },
  MODIFICATION: { label: 'Modification', icon: 'fa-pen', cls: 'yellow' },
  SUPPRESSION: { label: 'Suppression', icon: 'fa-trash', cls: 'red' },
  CONNEXION: { label: 'Connexion', icon: 'fa-right-to-bracket', cls: 'green' },
  ECHEC_CONNEXION: { label: 'Échec de connexion', icon: 'fa-user-lock', cls: 'red' },
  EXPORT: { label: 'Export', icon: 'fa-file-export', cls: 'bordeaux' },
  DOCUMENT: { label: 'Document (fiche, carte)', icon: 'fa-file-pdf', cls: 'bordeaux' },
  TELEVERSEMENT: { label: 'Envoi de fichier', icon: 'fa-upload', cls: 'gray' },
  CHANGEMENT_MOT_DE_PASSE: { label: 'Changement de mot de passe', icon: 'fa-key', cls: 'yellow' },
  CHANGEMENT_SITUATION: { label: 'Changement de situation', icon: 'fa-heart-pulse', cls: 'yellow' },
  IMPORT: { label: 'Import Excel', icon: 'fa-file-import', cls: 'green' },
  AUTRE: { label: 'Autre', icon: 'fa-circle', cls: 'gray' },
};
const RESSOURCES: Record<string, string> = {
  personnels: 'Personnel',
  corps: 'Corps',
  grades: 'Grades',
  formations: 'Formations',
  etudes: 'Études',
  services: 'Services',
  postes: 'Postes',
  roles: 'Rôles',
  autorisations: 'Autorisations',
  fichiers: 'Fichiers',
  'historiques-grades': 'Historique de grades',
  affectations: 'Affectations',
  auth: 'Compte',
  login: 'Connexion',
  'mot-de-passe': 'Mot de passe',
  photo: 'Photo',
  export: 'Export',
  fiche: 'Fiche de carrière',
  carte: 'Carte de service',
  situation: 'Situation',
  situations: 'Situations',
  import: 'Import Excel',
  modele: 'Modèle',
  retraites: 'Retraites',
};

@Component({
  selector: 'app-journal',
  standalone: true,
  imports: [AlertComponent, PageHeaderComponent, CommonModule, FormsModule, PagerComponent],
  templateUrl: './journal.component.html',
  styleUrl: './journal.component.css',
})
export class JournalComponent implements OnInit, OnDestroy {
  /** Mode intégré (onglet « Activité » d'un agent) : filtre sur un utilisateur, sans titre. */
  @Input() embedded = false;
  @Input() utilisateur = '';

  data?: PageResult<any>;
  page = 0;
  size = 20;
  detail: any = null;
  f: any = { q: '', action: '', resultat: '', debut: '', fin: '' };
  listeActions = Object.entries(ACTIONS)
    .filter(([k]) => k !== 'AUTRE')
    .map(([key, v]) => ({ key, label: v.label }));
  private q$ = new Subject<void>();
  private sub?: Subscription;

  constructor(private http: HttpClient) {}
  ngOnInit() {
    this.sub = this.q$.pipe(debounceTime(300)).subscribe(() => this.go(0));
    this.go(0);
  }
  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  a(l: any) {
    return ACTIONS[l.action] ?? ACTIONS['AUTRE'];
  }
  icone(appareil: string) {
    return appareil === 'Mobile'
      ? 'fa-mobile-screen'
      : appareil === 'Tablette'
        ? 'fa-tablet-screen-button'
        : 'fa-desktop';
  }
  ressource(l: any) {
    return (
      (l.ressource || '')
        .split(' › ')
        .filter((x: string) => x)
        .map((x: string) => RESSOURCES[x] ?? x)
        .join(' › ') || 'Non renseigné'
    );
  }

  lignes(l: any): [string, any][] {
    const d = new Date(l.dateAction);
    return [
      [
        'Date',
        d.toLocaleDateString('fr-FR', {
          weekday: 'long',
          day: '2-digit',
          month: 'long',
          year: 'numeric',
        }),
      ],
      ['Heure', d.toLocaleTimeString('fr-FR')],
      ['Utilisateur', l.utilisateur],
      ['Rôle', l.role],
      ['Action', this.a(l).label],
      ['Ressource', this.ressource(l) + (l.ressourceId ? ' #' + l.ressourceId : '')],
      ['Méthode HTTP', l.methode],
      ['Résultat (code HTTP)', String(l.statut) + (l.statut < 400 ? ' (succès)' : ' (échec)')],
      ['Adresse IP', l.ip],
      ['Navigateur', l.navigateur],
      ['Système', l.systeme],
      ['Appareil', l.appareil],
      ['Durée de traitement', l.dureeMs != null ? l.dureeMs + ' ms' : ''],
      ['Origine', l.origine],
      ['URL appelée', l.uri],
      ['Paramètres', l.parametres],
    ];
  }

  saisie(v: string) {
    this.f.q = v;
    this.q$.next();
  }
  changeSize(s: number) {
    this.size = s;
    this.go(0);
  }
  go(p: number) {
    this.page = p;
    let params = new HttpParams().set('page', p).set('size', this.size);
    for (const k of ['q', 'action', 'resultat', 'debut', 'fin'])
      if (this.f[k]) params = params.set(k, String(this.f[k]).trim());
    if (this.utilisateur) params = params.set('utilisateur', this.utilisateur);
    this.http.get<PageResult<any>>('/api/journal', { params }).subscribe((d) => (this.data = d));
  }
}
