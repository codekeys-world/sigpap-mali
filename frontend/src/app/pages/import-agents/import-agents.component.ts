import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FichierService } from '../../core/fichier.service';
import { NotificationService } from '../../core/notification.service';
import { AlertComponent } from '../../shared/alert/alert.component';

@Component({
  selector: 'app-import-agents',
  standalone: true,
  imports: [PageHeaderComponent, CommonModule, RouterLink, AlertComponent],
  templateUrl: './import-agents.component.html',
  styleUrl: './import-agents.component.css',
})
export class ImportAgentsComponent {
  fichier: File | null = null;
  res: any = null;
  chargement = false;
  mode: 'verif' | 'import' = 'verif';
  /** Vrai quand le fichier courant a été vérifié et contient au moins une ligne valide. */
  peutImporter = false;

  constructor(
    private http: HttpClient,
    private fichiers: FichierService,
    private notif: NotificationService,
  ) {}

  modele() {
    this.http
      .get('/api/personnels/import/modele', { responseType: 'blob' })
      .subscribe((b) => this.fichiers.telecharger(b, 'modele-import-agents.xlsx'));
  }

  choisir(ev: Event) {
    this.fichier = (ev.target as HTMLInputElement).files?.[0] ?? null;
    this.res = null;
    this.peutImporter = false;
    if (this.fichier && (!/\.xlsx$/i.test(this.fichier.name) || this.fichier.size === 0)) {
      this.notif.warning('Choisissez un fichier Excel .xlsx non vide.');
      this.fichier = null;
      (ev.target as HTMLInputElement).value = '';
    }
  }

  lancer(simulation: boolean) {
    if (!this.fichier || this.chargement || (!simulation && !this.peutImporter)) return;
    const fd = new FormData();
    fd.append('file', this.fichier);
    this.chargement = true;
    this.mode = simulation ? 'verif' : 'import';
    this.http.post<any>('/api/personnels/import', fd, { params: { simulation } }).subscribe({
      next: (r) => {
        this.chargement = false;
        this.res = r;
        this.peutImporter = simulation && r.valides > 0;
        if (!simulation) this.notif.success(`${r.crees} agent(s) importé(s).`);
      },
      error: () => (this.chargement = false),
    });
  }

  telechargerComptes() {
    const lignes = [
      'Matricule;Agent;Mot de passe temporaire',
      ...this.res.comptes.map((c: any) => `${c.matricule};${c.nom};${c.motDePasse}`),
    ];
    this.fichiers.telecharger(
      new Blob(['\ufeff' + lignes.join('\n')], { type: 'text/csv;charset=utf-8' }),
      'mots-de-passe-temporaires.csv',
    );
  }
}
