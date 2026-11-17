import { AuditUserPipe } from '../../shared/audit-user/audit-user.pipe';
import { AgentStatusComponent } from '../../shared/agent-status/agent-status.component';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { NotificationService } from '../../core/notification.service';
import { ConfirmService } from '../../core/confirm.service';
import { FichierService } from '../../core/fichier.service';
import {
  ADMIN_RH,
  GESTIONNAIRES,
  ROLE_LABELS,
  TYPES_AFFECTATION,
  TYPES_SERVICE,
  TimelineItem,
  fmtDate,
  infoSituation,
  label,
  remarqueSituation,
  situationDe,
} from '../../core/models';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { CrudComponent } from '../../shared/crud/crud.component';
import { PERSONNEL_CFG, periode } from '../../shared/crud.config';
import { TimelineComponent } from '../../shared/timeline/timeline.component';
import { CarteServiceComponent } from '../../shared/carte-service/carte-service.component';
import { SituationMenuComponent } from '../../shared/situation-menu/situation-menu.component';
import { JournalComponent } from '../journal/journal.component';
import { ROLES } from '../../core/models';

@Component({
  selector: 'app-personnel-detail',
  standalone: true,
  imports: [
    AuditUserPipe,
    AgentStatusComponent,
    PageHeaderComponent,
    CommonModule,
    RouterLink,
    CrudComponent,
    AvatarComponent,
    TimelineComponent,
    CarteServiceComponent,
    JournalComponent,
    SituationMenuComponent,
  ],
  templateUrl: './personnel-detail.component.html',
  styleUrl: './personnel-detail.component.css',
})
export class PersonnelDetailComponent implements OnInit {
  p: any;
  id!: string;
  tab = 'infos';
  posteRow: any = null;
  carteOuverte = false;
  mutations: any[] = [];
  situations: any[] = [];
  cfg = PERSONNEL_CFG;
  tabs = [
    { key: 'infos', label: 'Informations', icon: 'fa-user' },
    { key: 'mutations', label: 'Mutations', icon: 'fa-timeline' },
    { key: 'situation', label: 'Situations', icon: 'fa-heart-pulse' },
    { key: 'grades', label: 'Grades', icon: 'fa-award' },
    { key: 'formations', label: 'Formations', icon: 'fa-graduation-cap' },
    { key: 'etudes', label: 'Études', icon: 'fa-book-bookmark' },
    { key: 'affectations', label: 'Affectations', icon: 'fa-building' },
    { key: 'conges', label: 'Congés et absences', icon: 'fa-calendar-days' },
    { key: 'distinctions', label: 'Distinctions', icon: 'fa-medal' },
    { key: 'sanctions', label: 'Sanctions', icon: 'fa-scale-balanced' },
  ];
  get onglets() {
    const tabs = this.p?.surveillant ? this.tabs : this.tabs.filter((t) => t.key !== 'grades');
    return this.auth.hasRole(ROLES.ADMIN)
      ? [...tabs, { key: 'activite', label: 'Activité du compte', icon: 'fa-clock-rotate-left' }]
      : tabs;
  }

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private auth: AuthService,
    private notif: NotificationService,
    private confirm: ConfirmService,
    private fichiers: FichierService,
    private router: Router,
  ) {}

  get base() {
    return `/api/personnels/${this.id}`;
  }
  get canSeeAudit() {
    return this.auth.canViewAudit();
  }

  get canManage() {
    return this.auth.hasRole(...GESTIONNAIRES);
  }
  get canUploadSignature() {
    return this.auth.hasRole(...ADMIN_RH);
  }

  ngOnInit() {
    this.route.paramMap.subscribe((pm) => {
      this.id = pm.get('id')!;
      this.tab = 'infos';
      this.reload();
    });
  }

  reload() {
    this.http.get<any>(this.base).subscribe({
      next: (p) => (this.p = p),
      error: (error) => {
        if (error.status === 404) this.router.navigate(['/app/introuvable'], { replaceUrl: true });
      },
    });
    if (this.tab === 'mutations') this.chargerMutations();
    if (this.tab === 'situation') this.chargerSituations();
  }

  ouvrir(key: string) {
    this.tab = key;
    if (key === 'mutations') this.chargerMutations();
    if (key === 'situation') this.chargerSituations();
  }

  chargerMutations() {
    this.http
      .get<any[]>(`${this.base}/historique-mutations`)
      .subscribe((m) => (this.mutations = m));
  }

  get sit() {
    return situationDe(this.p);
  }
  get info() {
    return infoSituation(this.p);
  }
  get remarque() {
    return remarqueSituation(this.p);
  }

  chargerSituations() {
    this.http.get<any[]>(`${this.base}/situations`).subscribe((s) => (this.situations = s));
  }

  situationItems(): TimelineItem[] {
    return this.situations.map((s) => {
      const i = infoSituation(s);
      return {
        date: `${fmtDate(s.dateDebut)} au ${s.dateFin ? fmtDate(s.dateFin) : 'en cours'}`,
        titre: i.label,
        icon: i.icon,
        color: i.color,
        enCours: !s.dateFin,
        sousTitre: s.motif || undefined,
        details: s.reference ? [{ label: 'Référence', value: s.reference }] : [],
        fichiers: s.fichier ? [{ label: 'Pièce justificative', nom: s.fichier }] : [],
        audit: this.canSeeAudit
          ? `Saisi par ${s.createdBy || 'Non renseigné'}${s.createdDate ? ' le ' + fmtDate(s.createdDate) : ''}`
          : undefined,
      } as TimelineItem;
    });
  }

  /** Transforme les événements renvoyés par l'API (/historique-mutations) en éléments de timeline. */
  mutationItems(): TimelineItem[] {
    return this.mutations.map((m) => ({
      date: periode(m),
      titre: m.reference,
      sousTitre:
        (m.depuis ? `${m.depuis} vers ` : '') +
        m.service +
        (m.serviceParent ? ` (${m.serviceParent})` : ''),
      badge: label(TYPES_AFFECTATION, m.type),
      enCours: m.enCours,
      icon: m.enCours ? 'fa-location-dot' : 'fa-right-left',
      color: m.enCours ? '#009245' : '#8B1E1E',
      details: [
        { label: 'Service', value: `${m.service}, ${label(TYPES_SERVICE, m.typeService)}` },
        ...(m.depuis ? [{ label: "Service d'origine", value: m.depuis }] : []),
        ...(m.reference ? [{ label: 'Référence', value: m.reference }] : []),
        ...(m.dureeMois ? [{ label: 'Durée', value: m.dureeMois + ' mois' }] : []),
      ],
      liste: { titre: 'Postes occupés', items: m.postes },
      fichiers: [
        ...(m.fichierDecision ? [{ label: 'Fichier de la décision', nom: m.fichierDecision }] : []),
      ],
      audit: this.canSeeAudit
        ? `Saisi par ${m.creePar || 'Non renseigné'}${m.creeLe ? ' le ' + fmtDate(m.creeLe) : ''}`
        : undefined,
    }));
  }

  onAction(e: { name: string; row: any }) {
    if (e.name === 'postes') this.posteRow = e.row;
  }

  fiche() {
    this.http
      .get(`${this.base}/fiche`, { responseType: 'blob' })
      .subscribe((b) => window.open(URL.createObjectURL(b), '_blank'));
  }

  carte() {
    this.http
      .get(`${this.base}/carte`, { responseType: 'blob' })
      .subscribe((b) => window.open(URL.createObjectURL(b), '_blank'));
  }

  changerPhoto(ev: Event) {
    const file = (ev.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const fd = new FormData();
    fd.append('file', file);
    this.http.post<any>(`${this.base}/photo`, fd).subscribe((p) => {
      this.p = p;
      this.notif.success('Photo mise à jour.');
    });
  }

  async retirerPhoto() {
    const ok = await this.confirm.suppression(
      'Retirer la photo ?',
      `La photo de ${this.p.prenom} ${this.p.nom} sera supprimée du serveur.`,
    );
    if (!ok) return;
    this.http.delete(`${this.base}/photo`).subscribe(() => {
      this.notif.success('Photo retirée.');
      this.reload();
    });
  }

  telechargerPhoto() {
    this.http
      .get(`${this.base}/photo`, { params: { telecharger: true }, responseType: 'blob' })
      .subscribe((b) =>
        this.fichiers.telecharger(
          b,
          `photo-${this.p.matricule}.${(this.p.photo as string).split('.').pop()}`,
        ),
      );
  }

  infos(): [string, any][] {
    const p = this.p;
    return [
      ['Matricule', p.matricule],
      ['Type de personnel', p.surveillant ? 'Surveillant' : 'Non surveillant'],
      ['Situation', infoSituation(p).label],
      ['Sexe', p.sexe === 'M' ? 'Masculin' : 'Féminin'],
      ['Date de naissance', fmtDate(p.dateNaissance)],
      ['Lieu de naissance', p.lieuNaissance],
      ['Groupe sanguin', p.groupeSanguin],
      ['Téléphone', p.telephone],
      ['Contact d’urgence', p.contactUrgence],
      ['E-mail', p.email],
      ['Adresse', p.adresse],
      ['Date de recrutement', fmtDate(p.dateRecrutement)],
      ['Date de titularisation', fmtDate(p.dateTitularisation)],
      ['Arrêté d’intégration', p.arreteIntegration?.reference],
      ['Corps', p.corpsActuel?.nom || p.gradeActuel?.corps?.nom],
      ['Grade actuel', p.surveillant ? p.gradeActuel?.nom : 'Sans grade'],
      ['Service actuel', p.serviceCourant?.nom],
      ['Rôle', ROLE_LABELS[p.role?.nom] ?? p.role?.nom],
      ...(situationDe(p) !== 'ACTIF'
        ? [
            ['Depuis le', fmtDate(p.situationDepuis || p.dateDeces)] as [string, any],
            ['Motif / cause', p.situationMotif] as [string, any],
          ]
        : []),
    ];
  }

  telechargerPiece(chemin: string) {
    this.fichiers.telechargerFichier(chemin);
  }
}
