import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, shareReplay } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuditUserService {
  private authors = new Map<string, Observable<string>>();

  constructor(private http: HttpClient) {}

  identity(matricule?: string | null): Observable<string> {
    if (!matricule) return of('Non renseigné');
    if (matricule === 'SYSTEME') return of('Système');
    let author = this.authors.get(matricule);
    if (!author) {
      author = this.http
        .get<{ matricule: string; prenom: string; nom: string }>(
          `/api/auth/auteurs/${encodeURIComponent(matricule)}`,
        )
        .pipe(
          map((user) => {
            const name = [user.prenom, user.nom].filter(Boolean).join(' ');
            return name ? `${name} (${user.matricule})` : matricule;
          }),
          catchError(() => of(matricule)),
          shareReplay(1),
        );
      this.authors.set(matricule, author);
    }
    return author;
  }
}
