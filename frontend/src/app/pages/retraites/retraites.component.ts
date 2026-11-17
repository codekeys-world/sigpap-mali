import { AlertComponent } from '../../shared/alert/alert.component';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { FichierService } from '../../core/fichier.service';
import { ADMIN_RH, PageResult, fmtDate } from '../../core/models';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { PagerComponent } from '../../shared/pager/pager.component';
import { SituationMenuComponent } from '../../shared/situation-menu/situation-menu.component';

@Component({
  selector: 'app-retraites',
  standalone: true,
  imports: [
    AlertComponent,
    PageHeaderComponent,
    CommonModule,
    FormsModule,
    RouterLink,
    AvatarComponent,
    PagerComponent,
    SituationMenuComponent,
  ],
  templateUrl: './retraites.component.html',
  styleUrl: './retraites.component.css',
})
export class RetraitesComponent implements OnInit, OnDestroy {
  data?: PageResult<any>;
  page = 0;
  size = 10;
  q = '';
  etat = 'DEPASSEE';
  horizon = 24;
  fmt = fmtDate;
  onglets = [
    { label: 'Âge dépassé', etat: 'DEPASSEE', horizon: 24, icon: 'fa-triangle-exclamation' },
    { label: 'Dans 12 mois', etat: 'A_VENIR', horizon: 12, icon: 'fa-hourglass-end' },
    { label: 'Dans 24 mois', etat: 'A_VENIR', horizon: 24, icon: 'fa-hourglass-half' },
    { label: 'Dans 5 ans', etat: 'A_VENIR', horizon: 60, icon: 'fa-hourglass-start' },
    { label: 'Retraités', etat: 'RETRAITE', horizon: 24, icon: 'fa-umbrella-beach' },
    {
      label: 'Naissance manquante',
      etat: 'NON_RENSEIGNE',
      horizon: 24,
      icon: 'fa-circle-question',
    },
    { label: 'Tous', etat: 'TOUS', horizon: 24, icon: 'fa-list' },
  ];
  private q$ = new Subject<void>();
  private sub?: Subscription;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private fichiers: FichierService,
  ) {}
  get canAct() {
    return this.auth.hasRole(...ADMIN_RH);
  }
  abs(n: number) {
    return Math.abs(n);
  }

  ngOnInit() {
    this.sub = this.q$.pipe(debounceTime(300)).subscribe(() => this.go(0));
    this.go(0);
  }
  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  /** Représentation minimale de l'agent attendue par le composant de situation. */
  agentDe(l: any) {
    return {
      slug: l.slug,
      nom: l.nom,
      prenom: l.prenom,
      matricule: l.matricule,
      dateNaissance: l.dateNaissance,
      situation: l.situation,
    };
  }

  choisir(o: { etat: string; horizon: number }) {
    this.etat = o.etat;
    this.horizon = o.horizon;
    this.go(0);
  }
  saisie(v: string) {
    this.q = v;
    this.q$.next();
  }
  changeSize(s: number) {
    this.size = s;
    this.go(0);
  }

  private params() {
    let p = new HttpParams().set('etat', this.etat).set('horizon', this.horizon);
    if (this.q.trim()) p = p.set('q', this.q.trim());
    return p;
  }
  go(n: number) {
    this.page = n;
    this.http
      .get<PageResult<any>>('/api/retraites', {
        params: this.params().set('page', n).set('size', this.size),
      })
      .subscribe((d) => {
        d.content.forEach((l: any) => (l.agent = this.agentDe(l))); // objet stable (évite NG0100)
        this.data = d;
      });
  }
  exporter() {
    this.http
      .get('/api/retraites/export', { params: this.params(), responseType: 'blob' })
      .subscribe((b) => this.fichiers.telecharger(b, 'suivi-retraites-ap.xlsx'));
  }
}
