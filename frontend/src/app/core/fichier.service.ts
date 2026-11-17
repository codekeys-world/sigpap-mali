import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { nomFichier } from './models';

/** Envoi, affichage et téléchargement des fichiers stockés sur le serveur (dossier uploads). */
@Injectable({ providedIn: 'root' })
export class FichierService {
  constructor(private http: HttpClient) {}

  /** Envoie un fichier et renvoie son chemin pour l'associer au dossier de l'agent. */
  upload(file: File, dossier: 'documents' | 'justificatifs' = 'documents') {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<{ chemin: string; nom: string; taille: number }>('/api/fichiers', fd, {
      params: { dossier },
    });
  }

  /** Affiche le fichier dans un nouvel onglet. */
  ouvrir(chemin: string) {
    this.http
      .get('/api/fichiers/voir', { params: { chemin }, responseType: 'blob' })
      .subscribe((b) => window.open(URL.createObjectURL(b), '_blank'));
  }

  /** Télécharge le fichier sous son nom d'origine. */
  telechargerFichier(chemin: string) {
    this.http
      .get('/api/fichiers/telecharger', { params: { chemin }, responseType: 'blob' })
      .subscribe((b) => this.telecharger(b, nomFichier(chemin)));
  }

  /** Enregistre un blob (exports, PDF…) sous un nom donné. */
  telecharger(blob: Blob, nom: string) {
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = nom;
    a.click();
    setTimeout(() => URL.revokeObjectURL(a.href), 2000);
  }
}
