import { AuditUserPipe } from '../../shared/audit-user/audit-user.pipe';
import { AgentStatusComponent } from '../../shared/agent-status/agent-status.component';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { FichierService } from '../../core/fichier.service';
import { NotificationService } from '../../core/notification.service';
import { ConfirmService } from '../../core/confirm.service';
import {
  ADMIN_RH,
  GESTIONNAIRES,
  PageResult,
  infoSituation,
  remarqueSituation,
  situationDe,
} from '../../core/models';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { CarteServiceComponent } from '../../shared/carte-service/carte-service.component';
import { PagerComponent } from '../../shared/pager/pager.component';
import { SituationMenuComponent } from '../../shared/situation-menu/situation-menu.component';
import { AlertComponent } from '../../shared/alert/alert.component';

@Component({
  selector: 'app-personnel-list',
  standalone: true,
  imports: [
    AuditUserPipe,
    AgentStatusComponent,
    PageHeaderComponent,
    CommonModule,
    FormsModule,
    RouterLink,
    AvatarComponent,
    PagerComponent,
    CarteServiceComponent,
    SituationMenuComponent,
    AlertComponent,
  ],
  templateUrl: './personnel-list.component.html',
  styleUrl: './personnel-list.component.css',
})
export class PersonnelListComponent implements OnInit {
  data?: PageResult<any>;
  page = 0;
  vue: 'cartes' | 'tableau' =
    (localStorage.getItem('apes_vue_personnel') as any) === 'tableau' ? 'tableau' : 'cartes';
  size = this.vue === 'cartes' ? 12 : 10;
  f: any = {
    q: '',
    corpsSlug: '',
    gradeSlug: '',
    serviceSlug: '',
    statut: '',
    surveillant: false,
    enFormation: false,
    enEtude: false,
  };
  corps: any[] = [];
  grades: any[] = [];
  services: any[] = [];
  carteAgent: any = null;
  private timer: any;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private fichiers: FichierService,
    private notif: NotificationService,
    private confirm: ConfirmService,
  ) {}

  get canImport() {
    return this.auth.hasRole(...ADMIN_RH);
  }
  sit = situationDe;
  info = infoSituation;
  remarque = remarqueSituation;
  get canSeeAudit() {
    return this.auth.canViewAudit();
  }

  get canManage() {
    return this.auth.hasRole(...GESTIONNAIRES);
  }
  get gradesFiltres() {
    return this.f.corpsSlug
      ? this.grades.filter((g) => g.corps?.slug === this.f.corpsSlug)
      : this.grades;
  }

  ngOnInit() {
    this.http.get<any[]>('/api/corps/all').subscribe((l) => (this.corps = l));
    this.http.get<any[]>('/api/grades/all').subscribe((l) => (this.grades = l));
    this.http.get<any[]>('/api/services/all').subscribe((l) => (this.services = l));
    this.route.queryParamMap.subscribe((q) => {
      for (const key of ['q', 'corpsSlug', 'gradeSlug', 'serviceSlug', 'statut']) {
        this.f[key] = q.get(key) ?? '';
      }
      for (const key of ['surveillant', 'enFormation', 'enEtude']) {
        this.f[key] = q.get(key) === 'true';
      }
      this.rechercher();
    });
  }

  ouvrir(p: any) {
    this.router.navigate(['/app/personnel', p.slug]);
  }

  changerVue(v: 'cartes' | 'tableau') {
    this.vue = v;
    localStorage.setItem('apes_vue_personnel', v);
    this.size = v === 'cartes' ? 12 : 10;
    this.go(0);
  }

  private params(): HttpParams {
    let p = new HttpParams();
    ['q', 'corpsSlug', 'gradeSlug', 'serviceSlug'].forEach((k) => {
      if (this.f[k]) p = p.set(k, this.f[k]);
    });
    if (this.f.statut) p = p.set('statut', this.f.statut);
    ['surveillant', 'enFormation', 'enEtude'].forEach((k) => {
      if (this.f[k]) p = p.set(k, true);
    });
    return p;
  }

  saisie() {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.rechercher(), 350);
  }
  rechercher() {
    this.go(0);
  }
  changeSize(s: number) {
    this.size = s;
    this.go(0);
  }
  go(n: number) {
    this.page = n;
    this.http
      .get<PageResult<any>>('/api/personnels', {
        params: this.params().set('page', n).set('size', this.size),
      })
      .subscribe((d) => (this.data = d));
  }

  exporter(format: 'xlsx' | 'docx') {
    this.http
      .get('/api/personnels/export', {
        params: this.params().set('format', format),
        responseType: 'blob',
      })
      .subscribe((b) => this.fichiers.telecharger(b, `personnel-ap.${format}`));
  }

  async supprimerAgent(p: any) {
    const ok = await this.confirm.suppression(
      'Supprimer cet agent ?',
      `${p.prenom} ${p.nom} (${p.matricule}) sera supprimé définitivement, avec son historique, ses pièces justificatives et sa photo.`,
    );
    if (!ok) return;
    this.http.delete(`/api/personnels/${p.slug}`).subscribe(() => {
      this.notif.success('Agent supprimé.');
      this.go(this.page);
    });
  }
}
