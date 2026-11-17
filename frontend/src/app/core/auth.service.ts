import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, shareReplay, switchMap, tap, throwError } from 'rxjs';
import { ProfilAuthentifie, Session } from './models';

const KEY = 'apes_profile';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _session = signal<Session | null>(this.read());
  readonly session = this._session.asReadonly();
  private initializing?: Observable<Session | null>;
  private initialized = false;

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {
    try {
      localStorage.removeItem('apes_session');
      sessionStorage.removeItem('apes_session');
    } catch (error) {
      console.warn('Impossible de supprimer l’ancien jeton stocké dans le navigateur.', error);
    }
  }

  private read(): Session | null {
    try {
      const session = JSON.parse(sessionStorage.getItem(KEY) || 'null') as Session | null;
      if (session?.slug && session.role) return session;
      sessionStorage.removeItem(KEY);
      return null;
    } catch {
      return null;
    }
  }

  private enregistrer(session: Session | null) {
    if (session) sessionStorage.setItem(KEY, JSON.stringify(session));
    else sessionStorage.removeItem(KEY);
    this._session.set(session);
  }

  private profilDepuisReponse(reponse: ProfilAuthentifie): Session {
    const role = typeof reponse?.role === 'string' ? reponse.role : reponse?.role?.nom;
    const autorisations =
      reponse.autorisations ??
      (typeof reponse.role === 'object' ? reponse.role?.autorisations : []) ??
      [];
    return {
      slug: reponse.slug,
      matricule: reponse.matricule,
      nom: reponse.nom,
      prenom: reponse.prenom,
      photo: reponse.photo ?? null,
      role: role ?? '',
      autorisations: autorisations.map((autorisation) =>
        typeof autorisation === 'string' ? autorisation : autorisation.nom,
      ),
      motDePasseTemporaire: reponse.motDePasseTemporaire,
    };
  }

  initialize(): Observable<Session | null> {
    if (this.initialized) return of(this._session());
    if (!this.initializing) {
      this.initializing = this.http.get('/api/auth/csrf', { responseType: 'text' }).pipe(
        switchMap(() => this.http.get<ProfilAuthentifie>('/api/auth/moi')),
        map((profil) => this.profilDepuisReponse(profil)),
        tap((profil) => {
          this.enregistrer(profil);
          this.initialized = true;
        }),
        catchError((error) => {
          if (error.status === 401) {
            this.enregistrer(null);
            this.initialized = true;
            return of(null);
          }
          this.initializing = undefined;
          return throwError(() => error);
        }),
        shareReplay(1),
      );
    }
    return this.initializing;
  }

  login(matricule: string, motDePasse: string, seSouvenirDeMoi = false) {
    return this.http.get('/api/auth/csrf', { responseType: 'text' }).pipe(
      switchMap((token) =>
        this.http.post<Session>(
          '/api/auth/login',
          { matricule, motDePasse, seSouvenirDeMoi },
          {
            headers: { 'X-XSRF-TOKEN': token },
          },
        ),
      ),
      tap((profil) => {
        this.enregistrer(profil);
        this.initialized = true;
        this.initializing = undefined;
      }),
    );
  }

  changerMotDePasse(demande: {
    ancienMotDePasse: string;
    nouveauMotDePasse: string;
    confirmation: string;
  }) {
    // Utiliser le jeton frais renvoyé par le serveur, même si le cookie lisible est absent ou périmé.
    return this.http.get('/api/auth/csrf', { responseType: 'text' }).pipe(
      switchMap((token) =>
        this.http.post<void>('/api/auth/mot-de-passe', demande, {
          headers: { 'X-XSRF-TOKEN': token },
        }),
      ),
    );
  }

  logout() {
    this.enregistrer(null);
    this.initialized = true;
    this.initializing = undefined;
    this.http.post<void>('/api/auth/logout', {}).subscribe();
    this.router.navigate(['/login']);
  }

  /** Appelé après un changement de mot de passe réussi. */
  motDePasseChange() {
    const s = this._session();
    if (!s) return;
    this.enregistrer({ ...s, motDePasseTemporaire: false });
  }

  mettreAJourIdentite(nom: string, prenom: string) {
    const s = this._session();
    if (!s) return;
    this.enregistrer({ ...s, nom, prenom });
  }
  mettreAJourProfil(reponse: ProfilAuthentifie) {
    this.enregistrer(this.profilDepuisReponse(reponse));
  }
  isLoggedIn(): boolean {
    return !!this._session();
  }
  canPrintServiceCard(): boolean {
    return this.isLoggedIn() && !this.hasRole('UTILISATEUR');
  }
  canEditProfile(): boolean {
    return this.isLoggedIn() && !this.hasRole('UTILISATEUR');
  }
  canViewAudit(): boolean {
    return this.isLoggedIn() && !this.hasRole('UTILISATEUR');
  }
  mustChangePassword(): boolean {
    return !!this._session()?.motDePasseTemporaire;
  }
  hasRole(...roles: string[]): boolean {
    const r = this._session()?.role;
    return !!r && roles.includes(r);
  }
}
