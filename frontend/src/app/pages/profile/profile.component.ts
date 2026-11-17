import { AuditUserPipe } from '../../shared/audit-user/audit-user.pipe';
import { AgentStatusComponent } from '../../shared/agent-status/agent-status.component';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { NotificationService } from '../../core/notification.service';
import { AuthService } from '../../core/auth.service';
import { Router } from '@angular/router';
import { ADMIN_RH, ROLE_LABELS, fmtDate } from '../../core/models';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { CarteServiceComponent } from '../../shared/carte-service/carte-service.component';
import { FichierService } from '../../core/fichier.service';

interface IdentitePersonnelle {
  nom: string;
  prenom: string;
  sexe: 'M' | 'F' | null;
  dateNaissance?: string | null;
  lieuNaissance?: string | null;
  telephone?: string | null;
  email?: string | null;
  adresse?: string | null;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    AuditUserPipe,
    AgentStatusComponent,
    PageHeaderComponent,
    CommonModule,
    FormsModule,
    AvatarComponent,
    CarteServiceComponent,
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent implements OnInit {
  me: any;
  identite: IdentitePersonnelle = { nom: '', prenom: '', sexe: null };
  show = false;
  carte = false;
  saving = false;
  f = { ancienMotDePasse: '', nouveauMotDePasse: '', confirmation: '' };

  constructor(
    private http: HttpClient,
    private notif: NotificationService,
    private auth: AuthService,
    private router: Router,
    private fichiers: FichierService,
  ) {}
  get canUploadSignature() {
    return this.auth.hasRole(...ADMIN_RH);
  }

  telechargerPhoto() {
    if (this.me?.photo) this.fichiers.telechargerFichier(this.me.photo);
  }
  get canPrintServiceCard() {
    return this.auth.canPrintServiceCard();
  }
  get canSeeAudit() {
    return this.auth.canViewAudit();
  }

  get canEditProfile() {
    return this.auth.canEditProfile();
  }
  get role() {
    return ROLE_LABELS[this.me?.role?.nom] ?? this.me?.role?.nom;
  }
  fmt(d: string) {
    return d ? 'le ' + fmtDate(d) : '';
  }

  /** Le bouton n'est actif que lorsque les trois champs sont cohérents. */
  get valide() {
    const { ancienMotDePasse: a, nouveauMotDePasse: n, confirmation: c } = this.f;
    return !!a && !!n.trim() && n.length >= 8 && n !== a && n === c;
  }

  get force(): number {
    const p = this.f.nouveauMotDePasse;
    return [
      p.length >= 8,
      /[a-z]/.test(p) && /[A-Z]/.test(p),
      /\d/.test(p),
      /[^A-Za-z0-9]/.test(p),
    ].filter(Boolean).length;
  }
  get forceTexte() {
    return ['très faible', 'faible', 'moyenne', 'bonne', 'excellente'][this.force];
  }
  get forceCouleur() {
    return ['#E30613', '#E30613', '#FFD600', '#009245', '#009245'][this.force];
  }

  ngOnInit() {
    this.chargerProfil();
  }

  private chargerProfil() {
    this.http.get<any>('/api/auth/moi').subscribe((profil) => this.afficherProfil(profil));
  }

  private afficherProfil(profil: any) {
    this.me = profil;
    this.auth.mettreAJourProfil(profil);
    const { nom, prenom, sexe, dateNaissance, lieuNaissance, telephone, email, adresse } = profil;
    this.identite = { nom, prenom, sexe, dateNaissance, lieuNaissance, telephone, email, adresse };
  }

  touched: Record<string, boolean> = {};
  get erreursIdentite(): Record<string, string> {
    const m = this.identite,
      e: Record<string, string> = {};
    for (const key of ['nom', 'prenom'] as const) {
      if (!m[key]?.trim()) e[key] = 'Ce champ est obligatoire.';
      else if (m[key].length > 100) e[key] = 'Maximum 100 caractères.';
    }
    if (!['M', 'F'].includes(m.sexe || '')) e['sexe'] = 'Choisissez le sexe.';
    if (m.email && (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(m.email.trim()) || m.email.length > 150))
      e['email'] = 'Saisissez une adresse e-mail valide.';
    if (m.telephone && !/^[+0-9 ().-]{6,30}$/.test(m.telephone.trim()))
      e['telephone'] = 'Saisissez un numéro de 6 à 30 caractères.';
    if (
      m.dateNaissance &&
      (m.dateNaissance > new Date().toISOString().slice(0, 10) ||
        !/^\d{4}-\d{2}-\d{2}$/.test(m.dateNaissance))
    )
      e['dateNaissance'] = 'Choisissez une date de naissance valide, non future.';
    if ((m.lieuNaissance?.length || 0) > 150) e['lieuNaissance'] = 'Maximum 150 caractères.';
    if ((m.adresse?.length || 0) > 255) e['adresse'] = 'Maximum 255 caractères.';
    return e;
  }
  get identiteValide() {
    return Object.keys(this.erreursIdentite).length === 0;
  }

  enregistrerIdentite() {
    if (!this.canEditProfile || !this.identiteValide || this.saving) return;
    this.saving = true;
    this.http.put<any>('/api/auth/moi', this.identite).subscribe({
      next: (profil) => {
        this.afficherProfil(profil);
        this.saving = false;
        this.notif.success('Vos informations personnelles ont été mises à jour.');
      },
      error: () => (this.saving = false),
    });
  }

  valider() {
    if (this.saving) return;
    if (!this.valide) {
      this.notif.warning(
        'Renseignez les trois champs, utilisez un nouveau mot de passe différent de l’ancien et confirmez-le.',
      );
      return;
    }
    const { ancienMotDePasse, nouveauMotDePasse, confirmation } = this.f;
    if (!ancienMotDePasse || !nouveauMotDePasse || !confirmation) {
      this.notif.error('Renseignez tous les champs pour continuer.');
      return;
    }
    if (nouveauMotDePasse.length < 8) {
      this.notif.error('Le nouveau mot de passe doit contenir au moins 8 caractères.');
      return;
    }
    if (nouveauMotDePasse !== confirmation) {
      this.notif.error('La confirmation ne correspond pas.');
      return;
    }
    this.saving = true;
    this.auth.changerMotDePasse({ ...this.f }).subscribe({
      next: () => {
        this.saving = false;
        const etaitTemporaire = this.auth.mustChangePassword();
        this.auth.motDePasseChange();
        this.notif.success('Mot de passe modifié avec succès.');
        if (etaitTemporaire) this.router.navigate(['/app/dashboard']);
        this.f = { ancienMotDePasse: '', nouveauMotDePasse: '', confirmation: '' };
        this.ngOnInit();
      },
      error: () => (this.saving = false),
    });
  }
}
