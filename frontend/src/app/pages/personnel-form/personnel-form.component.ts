import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { FichierService } from '../../core/fichier.service';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { NotificationService } from '../../core/notification.service';
import { ROLE_LABELS, ROLES } from '../../core/models';
import { AvatarComponent } from '../../shared/avatar/avatar.component';

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
const TEL = /^[+0-9 ().-]{6,30}$/;
const MATRICULE = /^[A-Za-z0-9._-]{3,30}$/;

@Component({
  selector: 'app-personnel-form',
  standalone: true,
  imports: [PageHeaderComponent, CommonModule, FormsModule, RouterLink, AvatarComponent],
  templateUrl: './personnel-form.component.html',
  styleUrl: './personnel-form.component.css',
})
export class PersonnelFormComponent implements OnInit {
  id: string | null = null;
  m: any = {
    sexe: null,
    groupeSanguin: null,
    surveillant: true,
    actif: true,
    gradeActuel: null,
    corpsActuel: null,
    serviceCourant: null,
    role: null,
    arreteIntegration: null,
  };
  grades: any[] = [];
  corps: any[] = [];
  services: any[] = [];
  roles: any[] = [];
  arretesIntegration: any[] = [];
  saving = false;
  touched: Record<string, boolean> = {};
  photoFile: File | null = null;
  preview: string | null = null;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private auth: AuthService,
    private notif: NotificationService,
    private fichiers: FichierService,
  ) {}

  get canAssignRole() {
    return this.auth.hasRole(ROLES.ADMIN);
  }

  telechargerArrete() {
    if (this.m.arreteIntegration?.fichier)
      this.fichiers.telechargerFichier(this.m.arreteIntegration.fichier);
  }
  sameSlug = (a: any, b: any) => (a && b ? a.slug === b.slug : a === b);
  roleLabel(n: string) {
    return ROLE_LABELS[n] ?? n;
  }

  // ---------- validation ----------
  private static ajd = () => new Date().toISOString().slice(0, 10);

  /** Erreurs courantes : champ -> message. Le formulaire est valide quand l'objet est vide. */
  get e(): Record<string, string> {
    const m = this.m,
      err: Record<string, string> = {};
    const ajd = PersonnelFormComponent.ajd();
    const txt = (v: any) => (typeof v === 'string' ? v.trim() : '');
    if (!txt(m.matricule)) err['matricule'] = 'Le matricule est obligatoire.';
    else if (!MATRICULE.test(txt(m.matricule)))
      err['matricule'] = '3 à 30 caractères : lettres, chiffres, point, tiret ou _.';
    if (!txt(m.nom)) err['nom'] = 'Le nom est obligatoire.';
    else if (txt(m.nom).length < 2) err['nom'] = 'Minimum 2 caractères.';
    if (!txt(m.prenom)) err['prenom'] = 'Le prénom est obligatoire.';
    else if (txt(m.prenom).length < 2) err['prenom'] = 'Minimum 2 caractères.';
    if (!m.sexe) err['sexe'] = 'Le sexe est obligatoire.';
    if (m.dateNaissance) {
      if (m.dateNaissance > ajd)
        err['dateNaissance'] = 'La date de naissance ne peut pas être dans le futur.';
      else if (m.dateNaissance < '1930-01-01') err['dateNaissance'] = 'Date de naissance invalide.';
      else if (m.dateNaissance > this.ilYa(16))
        err['dateNaissance'] = "L'agent doit avoir au moins 16 ans.";
    }
    if (txt(m.telephone) && !TEL.test(txt(m.telephone)))
      err['telephone'] = 'Numéro invalide (chiffres, espaces, +, -, 6 à 30 caractères).';
    if (txt(m.contactUrgence).length > 150)
      err['contactUrgence'] = 'Le contact d’urgence ne doit pas dépasser 150 caractères.';
    if (!this.id && !txt(m.email))
      err['email'] = "L'adresse e-mail est obligatoire pour envoyer les identifiants.";
    else if (txt(m.email) && !EMAIL.test(txt(m.email))) err['email'] = 'Adresse e-mail invalide.';
    if (m.dateRecrutement) {
      if (m.dateRecrutement > ajd)
        err['dateRecrutement'] = 'La date de recrutement ne peut pas être dans le futur.';
      else if (m.dateNaissance && m.dateRecrutement < m.dateNaissance)
        err['dateRecrutement'] = 'Le recrutement ne peut pas précéder la naissance.';
    }
    if (m.dateTitularisation) {
      if (m.dateTitularisation > ajd)
        err['dateTitularisation'] = 'La date de titularisation ne peut pas être dans le futur.';
      else if (m.dateRecrutement && m.dateTitularisation < m.dateRecrutement)
        err['dateTitularisation'] = 'La titularisation ne peut pas précéder le recrutement.';
      else if (m.dateNaissance && m.dateTitularisation < m.dateNaissance)
        err['dateTitularisation'] = 'La titularisation ne peut pas précéder la naissance.';
    }
    if (!m.surveillant && !m.corpsActuel)
      err['corpsActuel'] = 'Le corps est obligatoire pour un agent non surveillant.';
    if (!this.id && !m.arreteIntegration)
      err['arreteIntegration'] = 'L’arrêté d’intégration est obligatoire.';
    if (this.id && m.motDePasse && m.motDePasse.length < 8)
      err['motDePasse'] = 'Minimum 8 caractères.';
    if (this.canAssignRole && !m.role) err['role'] = 'Le rôle est obligatoire.';
    for (const [key, max] of Object.entries({
      nom: 100,
      prenom: 100,
      lieuNaissance: 150,
      email: 150,
      adresse: 255,
    })) {
      if ((m[key]?.length || 0) > max) err[key] = `Maximum ${max} caractères.`;
    }
    return err;
  }
  get valid() {
    return Object.keys(this.e).length === 0;
  }
  get nbErreurs() {
    return Object.keys(this.e).length;
  }
  t(k: string) {
    this.touched[k] = true;
  }
  afficher(k: string) {
    return !!this.touched[k] && !!this.e[k];
  }
  private ilYa(ans: number) {
    const d = new Date();
    d.setFullYear(d.getFullYear() - ans);
    return d.toISOString().slice(0, 10);
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    this.http.get<any[]>('/api/grades/all').subscribe((l) => (this.grades = l));
    this.http.get<any[]>('/api/corps/all').subscribe((l) => (this.corps = l));
    this.http.get<any[]>('/api/services/all').subscribe((l) => (this.services = l));
    this.http
      .get<any[]>('/api/arretes-integration/all')
      .subscribe((l) => (this.arretesIntegration = l));
    if (this.canAssignRole) {
      this.http.get<any[]>('/api/roles/all').subscribe((l) => {
        this.roles = l;
        if (!this.id) this.m.role = l.find((r) => r.nom === 'UTILISATEUR') ?? null;
      });
    }
    if (this.id)
      this.http.get<any>(`/api/personnels/${this.id}`).subscribe(
        (p) =>
          (this.m = {
            ...p,
            corpsActuel: p.corpsActuel ?? p.gradeActuel?.corps,
            motDePasse: '',
          }),
      );
  }

  changerSurveillant(surveillant: boolean) {
    this.m.surveillant = surveillant;
    if (!surveillant) {
      this.m.corpsActuel = this.m.gradeActuel?.corps ?? this.m.corpsActuel;
      this.m.gradeActuel = null;
    }
  }

  choisirPhoto(ev: Event) {
    const f = (ev.target as HTMLInputElement).files?.[0] ?? null;
    if (f && f.size > 3 * 1024 * 1024) {
      this.notif.error('La photo ne doit pas dépasser 3 Mo.');
      return;
    }
    this.photoFile = f;
    this.preview = f ? URL.createObjectURL(f) : null;
  }

  save() {
    if (this.saving) return;
    if (!this.valid) {
      Object.keys(this.e).forEach((k) => (this.touched[k] = true));
      return;
    }
    this.saving = true;
    const body = {
      ...this.m,
      gradeActuel: this.m.gradeActuel?.slug ? { slug: this.m.gradeActuel.slug } : null,
      corpsActuel: this.m.corpsActuel?.slug ? { slug: this.m.corpsActuel.slug } : null,
      serviceCourant: this.m.serviceCourant?.slug ? { slug: this.m.serviceCourant.slug } : null,
      role: this.m.role?.slug ? { slug: this.m.role.slug } : null,
      arreteIntegration: this.m.arreteIntegration?.slug
        ? { slug: this.m.arreteIntegration.slug }
        : null,
    };
    const req = this.id
      ? this.http.put<any>(`/api/personnels/${this.id}`, body)
      : this.http.post<any>('/api/personnels', body);
    req.subscribe({
      next: (p) => {
        const fin = () => {
          this.notif.success(
            this.id
              ? 'Agent enregistré.'
              : 'Agent créé. Le mot de passe temporaire a été envoyé par e-mail.',
          );
          this.router.navigate(['/app/personnel', p.slug]);
        };
        if (!this.photoFile) {
          fin();
          return;
        }
        const fd = new FormData();
        fd.append('file', this.photoFile);
        this.http
          .post(`/api/personnels/${p.slug}/photo`, fd)
          .subscribe({ next: fin, error: () => (this.saving = false) });
      },
      error: (err) => {
        this.saving = false;
        this.notif.error(
          err?.error?.message || 'Impossible d’enregistrer les informations de l’agent.',
        );
      },
    });
  }
}
