import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';
import { NotificationService } from '../../core/notification.service';
import { ADMIN_RH, GESTIONNAIRES, SITUATIONS, nomFichier, situationDe } from '../../core/models';
import { SituationAgent } from '../../core/models/situation.model';
import { FichierService } from '../../core/fichier.service';
import { AlertComponent } from '../alert/alert.component';

interface Option {
  type: SituationAgent;
  label: string;
  icon: string;
  color: string;
  btn: string;
}

const CONFIG: Record<
  SituationAgent,
  {
    titre: string;
    date: string;
    motif: string;
    motifRequis: boolean;
    reference: string;
    avertissement?: string;
  }
> = {
  DECEDE: {
    titre: 'Déclarer le décès',
    date: 'Date du décès',
    motif: 'Motif du décès',
    motifRequis: true,
    reference: "N° de l'acte de décès / référence",
    avertissement:
      "Le compte de connexion de l'agent sera désactivé. Cette action est tracée dans le journal.",
  },
  MALADE: {
    titre: 'Déclarer la maladie',
    date: 'Date de début de la maladie',
    motif: 'Cause de la maladie',
    motifRequis: true,
    reference: 'Référence du certificat médical',
  },
  ABANDON_POSTE: {
    titre: "Déclarer l'abandon de poste",
    date: "Date de l'abandon de poste",
    motif: 'Observations',
    motifRequis: false,
    reference: 'Référence du constat / de la décision',
  },
  RETRAITE: {
    titre: 'Mettre à la retraite',
    date: 'Date de départ à la retraite',
    motif: "Motif (ex. limite d'âge)",
    motifRequis: false,
    reference: 'Référence de la décision',
    avertissement: "Le compte de connexion de l'agent sera désactivé.",
  },
  ACTIF: {
    titre: 'Retour en activité',
    date: 'Date de reprise',
    motif: 'Précisions',
    motifRequis: false,
    reference: 'Référence de la décision',
  },
};

/**
 * Boutons de changement de situation d'un agent : décédé, malade (avec la cause), abandon de poste, retraite, reprise.
 * mode « menu » : un seul bouton qui ouvre la liste ; mode « buttons » : un bouton par action.
 */
@Component({
  selector: 'app-situation-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, AlertComponent],
  templateUrl: './situation-menu.component.html',
  styleUrl: './situation-menu.component.css',
})
export class SituationMenuComponent {
  @Input({ required: true }) agent: any;
  @Input() mode: 'menu' | 'buttons' = 'menu';
  /** Restreint les actions proposées (ex. ['RETRAITE'] depuis le suivi des retraites). */
  @Input() only?: SituationAgent[];
  @Output() done = new EventEmitter<any>();

  chooser = false;
  type: SituationAgent | null = null;
  saving = false;
  envoi = false;
  fichierChemin: string | null = null;
  nomFichier = nomFichier;
  touched: Record<string, boolean> = {};
  f = { date: '', motif: '', reference: '' };

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private notif: NotificationService,
    private fichiers: FichierService,
  ) {}

  get actuelle() {
    return SITUATIONS[situationDe(this.agent)];
  }

  telechargerPiece() {
    if (this.fichierChemin) this.fichiers.telechargerFichier(this.fichierChemin);
  }
  get cfg() {
    return CONFIG[this.type!];
  }
  get infos() {
    return SITUATIONS[this.type!];
  }
  /** Annuler un décès / une retraite exige une justification. */
  get motifRequis() {
    return (
      this.cfg.motifRequis ||
      (this.type === 'ACTIF' && ['DECEDE', 'RETRAITE'].includes(situationDe(this.agent)))
    );
  }
  get motifLabel() {
    return this.type === 'ACTIF' && ['DECEDE', 'RETRAITE'].includes(situationDe(this.agent))
      ? "Raison de l'annulation"
      : this.cfg.motif;
  }

  private cle = '';
  private cache: Option[] = [];

  get options(): Option[] {
    const cle = `${situationDe(this.agent)}|${this.auth.session()?.role}|${this.only?.join(',')}`;
    if (cle !== this.cle) {
      this.cle = cle;
      this.cache = this.calculerOptions();
    }
    return this.cache;
  }

  private calculerOptions(): Option[] {
    if (!this.auth.hasRole(...GESTIONNAIRES)) return [];
    const s = situationDe(this.agent);
    const adminRh = this.auth.hasRole(...ADMIN_RH);
    const tous: Record<string, Option> = {
      DECEDE: {
        type: 'DECEDE',
        label: 'Déclarer décédé(e)',
        icon: 'fa-dove',
        color: '#4B0D0D',
        btn: 'btn-outline-dark',
      },
      MALADE: {
        type: 'MALADE',
        label: 'Déclarer malade',
        icon: 'fa-bed-pulse',
        color: '#b38f00',
        btn: 'btn-outline-warning',
      },
      ABANDON_POSTE: {
        type: 'ABANDON_POSTE',
        label: 'Déclarer abandon de poste',
        icon: 'fa-person-walking-arrow-right',
        color: '#E30613',
        btn: 'btn-outline-danger',
      },
      RETRAITE: {
        type: 'RETRAITE',
        label: 'Mettre à la retraite',
        icon: 'fa-umbrella-beach',
        color: '#6c757d',
        btn: 'btn-outline-secondary',
      },
      ACTIF: {
        type: 'ACTIF',
        icon: 'fa-user-check',
        color: '#009245',
        btn: 'btn-outline-success',
        label:
          s === 'MALADE'
            ? 'Guérison / reprise de service'
            : s === 'ABANDON_POSTE'
              ? 'Réintégration'
              : s === 'DECEDE'
                ? 'Annuler le décès (erreur de saisie)'
                : 'Annuler la mise à la retraite',
      },
    };
    let res: Option[] = [];
    if (s === 'DECEDE' || s === 'RETRAITE') res = adminRh ? [tous['ACTIF']] : [];
    else {
      if (s !== 'MALADE') res.push(tous['MALADE']);
      if (s !== 'ABANDON_POSTE') res.push(tous['ABANDON_POSTE']);
      if (adminRh) res.push(tous['DECEDE'], tous['RETRAITE']);
      if (s === 'MALADE' || s === 'ABANDON_POSTE') res.push(tous['ACTIF']);
    }
    return this.only ? res.filter((o) => this.only!.includes(o.type)) : res;
  }

  choisir(type: SituationAgent) {
    this.chooser = false;
    this.type = type;
    this.touched = {};
    this.f = { date: new Date().toISOString().slice(0, 10), motif: '', reference: '' };
    this.fichierChemin = null;
  }

  choisirFichier(ev: Event) {
    const file = (ev.target as HTMLInputElement).files?.[0];
    if (!file) {
      this.fichierChemin = null;
      return;
    }
    this.envoi = true;
    this.fichiers.upload(file, 'justificatifs').subscribe({
      next: (r) => {
        this.fichierChemin = r.chemin;
        this.envoi = false;
      },
      error: () => {
        this.fichierChemin = null;
        this.envoi = false;
      },
    });
  }
  fermer() {
    this.type = null;
  }

  get erreurs(): Record<string, string> {
    const e: Record<string, string> = {};
    const ajd = new Date().toISOString().slice(0, 10);
    if (!this.f.date) e['date'] = 'La date est obligatoire.';
    else if (this.f.date > ajd) e['date'] = 'La date ne peut pas être dans le futur.';
    else if (this.agent.dateNaissance && this.f.date < this.agent.dateNaissance)
      e['date'] = "La date ne peut pas précéder la naissance de l'agent.";
    if (this.motifRequis && !this.f.motif.trim()) e['motif'] = 'Ce champ est obligatoire.';
    if (this.f.motif.length > 500) e['motif'] = 'Maximum 500 caractères.';
    if (this.f.reference.length > 200) e['reference'] = 'Maximum 200 caractères.';
    return e;
  }
  get valide() {
    return Object.keys(this.erreurs).length === 0 && !this.envoi;
  }

  valider() {
    if (!this.type || this.saving) return;
    if (!this.valide) {
      this.touched['date'] = true;
      this.touched['motif'] = true;
      return;
    }
    this.saving = true;
    this.http
      .post<any>(`/api/personnels/${this.agent.slug}/situation`, {
        situation: this.type,
        date: this.f.date,
        motif: this.f.motif.trim() || null,
        reference: this.f.reference.trim() || null,
        fichier: this.fichierChemin,
      })
      .subscribe({
        next: (p) => {
          this.saving = false;
          this.notif.success(`Situation enregistrée : ${SITUATIONS[this.type!].label}.`);
          this.type = null;
          this.done.emit(p);
        },
        error: () => (this.saving = false),
      });
  }
}
