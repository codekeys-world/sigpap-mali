import { AuditUserPipe } from '../../shared/audit-user/audit-user.pipe';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { Chart, ChartConfiguration, ChartType, registerables } from 'chart.js';
import { AuthService } from '../../core/auth.service';
import {
  GESTIONNAIRES,
  TYPES_AFFECTATION,
  TYPES_SERVICE,
  fmtDate,
  initiales,
  label,
} from '../../core/models';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { AlertComponent } from '../../shared/alert/alert.component';

Chart.register(...registerables);

const COULEURS = [
  '#8B1E1E',
  '#FFD600',
  '#009245',
  '#E30613',
  '#4B0D0D',
  '#b38f00',
  '#7a7a7a',
  '#d99a9a',
];

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    AuditUserPipe,
    PageHeaderComponent,
    CommonModule,
    RouterLink,
    AvatarComponent,
    AlertComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  @ViewChild('corpsChart') corpsRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('gauge') gaugeRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('ageChart') ageRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('ancChart') ancRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('anneesChart') anneesRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('etudesChart') etudesRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('retraitesChart') retraitesRef?: ElementRef<HTMLCanvasElement>;

  d: any;
  maj?: Date;
  chargement = false;
  erreur = false;
  lent = false;
  C = COULEURS;
  charts: Chart<any, any, any>[] = [];
  private timerLent: any;

  kpis = [
    {
      label: 'Personnels',
      icon: 'fa-users',
      bg: 'rgba(139,30,30,.1)',
      color: '#8B1E1E',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.total,
    },
    {
      label: 'Surveillants',
      icon: 'fa-user-shield',
      bg: 'rgba(0,146,69,.12)',
      color: '#009245',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.surveillants,
    },
    {
      label: 'Non surveillants',
      icon: 'fa-user-tie',
      bg: 'rgba(179,143,0,.12)',
      color: '#8c7000',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.total - d.surveillants,
    },
    {
      label: 'En formation',
      icon: 'fa-graduation-cap',
      bg: 'rgba(255,214,0,.25)',
      color: '#8a6d00',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.enFormation,
    },
    {
      label: 'Décédés',
      icon: 'fa-dove',
      bg: 'rgba(75,13,13,.1)',
      color: '#4B0D0D',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.decedes,
    },
    {
      label: 'Comptes inactifs',
      icon: 'fa-user-slash',
      bg: 'rgba(227,6,19,.1)',
      color: '#E30613',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.qualite.comptesInactifs,
    },
    {
      label: 'Malades',
      icon: 'fa-bed-pulse',
      bg: 'rgba(255,214,0,.25)',
      color: '#8a6d00',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.malades,
    },
    {
      label: 'Abandons de poste',
      icon: 'fa-person-walking-arrow-right',
      bg: 'rgba(227,6,19,.1)',
      color: '#E30613',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.abandons,
    },
    {
      label: 'Retraite < 12 mois',
      icon: 'fa-person-cane',
      bg: 'rgba(75,13,13,.1)',
      color: '#4B0D0D',
      link: '/app/retraites',
      params: {},
      value: (d: any) => d.retraites.dans12Mois,
    },
    {
      label: 'Retraités',
      icon: 'fa-umbrella-beach',
      bg: 'rgba(108,117,125,.15)',
      color: '#6c757d',
      link: '/app/retraites',
      params: {},
      value: (d: any) => d.retraites.retraites,
    },
    {
      label: 'Affectations en cours',
      icon: 'fa-location-dot',
      bg: 'rgba(0,146,69,.12)',
      color: '#009245',
      link: '/app/personnel',
      params: {},
      value: (d: any) => d.affectationsEnCours,
    },
  ];
  referentiels = [
    {
      label: 'Corps',
      icon: 'fa-sitemap',
      link: '/app/corps',
      value: (d: any) => d.referentiels.corps,
    },
    {
      label: 'Grades',
      icon: 'fa-award',
      link: '/app/grades',
      value: (d: any) => d.referentiels.grades,
    },
    {
      label: 'Services',
      icon: 'fa-building',
      link: '/app/services',
      value: (d: any) => d.referentiels.services,
    },
    {
      label: 'Formations',
      icon: 'fa-graduation-cap',
      link: '/app/formations',
      value: (d: any) => d.referentiels.formations,
    },
    {
      label: 'Études',
      icon: 'fa-book-bookmark',
      link: '/app/etudes',
      value: (d: any) => d.referentiels.etudes,
    },
    {
      label: "Niveaux d'étude",
      icon: 'fa-layer-group',
      link: '/app/niveaux-etude',
      value: (d: any) => d.referentiels.niveauxEtude,
    },
    {
      label: 'Postes',
      icon: 'fa-briefcase',
      link: '/app/postes',
      value: (d: any) => d.referentiels.postes,
    },
    {
      label: 'Mutations',
      icon: 'fa-right-left',
      link: '/app/mutations',
      value: (d: any) => d.referentiels.mutations,
    },
  ];
  qualite = [
    {
      label: 'Sans photo',
      icon: 'fa-camera',
      cls: 'yellow',
      value: (d: any) => d.qualite.sansPhoto,
    },
    {
      label: 'Grade ou corps à renseigner',
      icon: 'fa-award',
      cls: 'red',
      value: (d: any) => d.qualite.sansGrade,
    },
    {
      label: 'Sans service',
      icon: 'fa-building',
      cls: 'red',
      value: (d: any) => d.qualite.sansService,
    },
    {
      label: 'Sans contact',
      icon: 'fa-phone',
      cls: 'yellow',
      value: (d: any) => d.qualite.sansContact,
    },
    {
      label: 'Mots de passe temporaires',
      icon: 'fa-key',
      cls: 'yellow',
      value: (d: any) => d.qualite.motsDePasseTemporaires,
    },
  ];

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router,
  ) {}
  get canManage() {
    return this.auth.hasRole(...GESTIONNAIRES);
  }
  get isSelfDashboard() {
    return this.auth.hasRole('UTILISATEUR');
  }
  get ownPersonnelSlug() {
    return this.auth.session()?.slug;
  }
  get nom() {
    return this.auth.session()?.nom;
  }
  get prenom() {
    return this.auth.session()?.prenom;
  }

  get corpsChartHeight() {
    return Math.max(280, (this.d?.parCorps?.length || 0) * 48 + 50);
  }

  consulterCorps(corps: { slug?: string | null }) {
    if (!corps.slug) return;
    this.router.navigate(['/app/personnel'], { queryParams: { corpsSlug: corps.slug } });
  }

  ngOnInit() {
    this.charger();
  }
  ngOnDestroy() {
    this.detruire();
    clearTimeout(this.timerLent);
  }

  charger() {
    this.chargement = true;
    this.erreur = false;
    this.lent = false;
    clearTimeout(this.timerLent);
    this.timerLent = setTimeout(() => (this.lent = this.chargement), 3000); // message si le serveur est lent
    this.http.get<any>('/api/dashboard').subscribe({
      next: (d) => {
        this.detruire();
        this.d = d;
        this.maj = new Date();
        this.chargement = false;
        this.lent = false;
        setTimeout(() => this.draw());
      },
      error: () => {
        this.chargement = false;
        this.erreur = !this.d;
      },
    });
  }

  pct(v: number) {
    return this.d?.total ? Math.round((v * 1000) / this.d.total) / 10 : 0;
  }
  rel(v: number, list: { value: number }[]) {
    const m = Math.max(...list.map((x) => x.value), 1);
    return (v * 100) / m;
  }
  ini(nomComplet: string) {
    const p = nomComplet.split(' ');
    return initiales(p.slice(1).join(' '), p[0]);
  }
  typeService(v: string) {
    return v === 'NON_RENSEIGNE' ? 'Non renseigné' : label(TYPES_SERVICE, v);
  }
  typeAff(v: string) {
    return label(TYPES_AFFECTATION, v);
  }
  fmt = fmtDate;

  private detruire() {
    this.charts.forEach((c) => c.destroy());
    this.charts = [];
  }

  private chart<TType extends ChartType>(
    ref: ElementRef<HTMLCanvasElement> | undefined,
    cfg: ChartConfiguration<TType>,
  ) {
    if (ref) this.charts.push(new Chart<TType>(ref.nativeElement, cfg));
  }

  private bar(
    ref: ElementRef<HTMLCanvasElement> | undefined,
    data: { label: string; value: number }[],
    couleur: string,
  ) {
    this.chart(ref, {
      type: 'bar',
      data: {
        labels: data.map((x) => x.label),
        datasets: [{ data: data.map((x) => x.value), backgroundColor: couleur, borderRadius: 8 }],
      },
      options: {
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false } },
          y: { beginAtZero: true, ticks: { precision: 0 } },
        },
      },
    });
  }

  private draw() {
    const d = this.d;
    this.chart(this.corpsRef, {
      type: 'bar',
      data: {
        labels: d.parCorps.map((c: any) => c.label),
        datasets: [
          {
            label: 'Hommes',
            data: d.parCorps.map((c: any) => c.hommes),
            backgroundColor: '#8B1E1E',
            borderRadius: 8,
            stack: 's',
          },
          {
            label: 'Femmes',
            data: d.parCorps.map((c: any) => c.femmes),
            backgroundColor: '#FFD600',
            borderRadius: 8,
            stack: 's',
          },
        ],
      },
      options: {
        indexAxis: 'y',
        maintainAspectRatio: false,
        onClick: (_event, elements) => {
          if (elements.length) this.consulterCorps(d.parCorps[elements[0].index]);
        },
        plugins: { legend: { display: false } },
        scales: {
          x: { stacked: true, beginAtZero: true, ticks: { precision: 0 } },
          y: {
            stacked: true,
            grid: { display: false },
            ticks: {
              autoSkip: false,
              callback: function (value) {
                const name = this.getLabelForValue(Number(value));
                const limit = this.chart.width < 600 ? 18 : 46;
                return name.length > limit ? name.slice(0, limit - 1) + '…' : name;
              },
            },
          },
        },
      },
    });
    const p = this.pct(d.actifs);
    this.chart(this.gaugeRef, {
      type: 'doughnut',
      data: {
        datasets: [{ data: [p, 100 - p], backgroundColor: ['#009245', '#efecec'], borderWidth: 0 }],
      },
      options: {
        rotation: -90,
        circumference: 180,
        cutout: '78%',
        maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { enabled: false } },
      },
    });
    this.bar(this.ageRef, d.parTrancheAge, '#8B1E1E');
    this.bar(this.ancRef, d.parAnciennete, '#009245');
    this.bar(this.retraitesRef, d.retraites.departsParAnnee, '#4B0D0D');
    // recrutements et mutations sur les mêmes années
    const annees = Array.from(
      new Set<string>([
        ...d.recrutements.map((x: any) => x.label),
        ...d.mutations.map((x: any) => x.label),
      ]),
    ).sort();
    const val = (list: any[], a: string) => list.find((x) => x.label === a)?.value ?? 0;
    this.chart(this.anneesRef, {
      type: 'line',
      data: {
        labels: annees,
        datasets: [
          {
            label: 'Recrutements',
            data: annees.map((a) => val(d.recrutements, a)),
            borderColor: '#8B1E1E',
            backgroundColor: '#8B1E1E',
            tension: 0.3,
          },
          {
            label: 'Mutations',
            data: annees.map((a) => val(d.mutations, a)),
            borderColor: '#FFD600',
            backgroundColor: '#FFD600',
            tension: 0.3,
          },
        ],
      },
      options: {
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom', labels: { boxWidth: 10 } } },
        scales: {
          x: { grid: { display: false } },
          y: { beginAtZero: true, ticks: { precision: 0 } },
        },
      },
    });
    this.chart(this.etudesRef, {
      type: 'doughnut',
      data: {
        labels: d.etudesParNiveau.map((x: any) => x.label),
        datasets: [
          {
            data: d.etudesParNiveau.map((x: any) => x.value),
            backgroundColor: COULEURS,
            borderWidth: 2,
          },
        ],
      },
      options: {
        maintainAspectRatio: false,
        cutout: '60%',
        plugins: { legend: { position: 'right', labels: { boxWidth: 10 } } },
      },
    });
  }
}
