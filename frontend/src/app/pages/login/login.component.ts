import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  readonly currentYear = new Date().getFullYear();
  matricule = '';
  motDePasse = '';
  email = '';
  siteWeb = '';
  nouveauMotDePasse = '';
  confirmation = '';
  jeton = '';
  mode: 'login' | 'forgot' | 'reset' = 'login';
  seSouvenirDeMoi = false;
  show = false;
  loading = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    private notif: NotificationService,
    private http: HttpClient,
    private route: ActivatedRoute,
  ) {
    this.jeton = this.route.snapshot.queryParamMap.get('reset') || '';
    if (this.jeton) this.mode = 'reset';
    auth.initialize().subscribe((session) => {
      if (session) router.navigate(['/app/dashboard']);
    });
  }

  get loginValide() {
    return !!this.matricule.trim() && this.matricule.trim().length <= 50 && !!this.motDePasse;
  }
  get emailValide() {
    return (
      this.email.trim().length <= 150 && /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(this.email.trim())
    );
  }
  get resetValide() {
    return (
      !!this.jeton &&
      this.nouveauMotDePasse.length >= 8 &&
      !!this.nouveauMotDePasse.trim() &&
      this.nouveauMotDePasse === this.confirmation
    );
  }

  go() {
    if (this.loading) return;
    if (!this.loginValide) {
      this.notif.warning('Saisissez votre matricule et votre mot de passe pour vous connecter.');
      return;
    }
    this.loading = true;
    this.auth.login(this.matricule.trim(), this.motDePasse, this.seSouvenirDeMoi).subscribe({
      next: (s) =>
        this.router.navigate([s.motDePasseTemporaire ? '/app/profil' : '/app/dashboard']),
      error: () => (this.loading = false),
    });
  }

  demanderLien() {
    if (this.loading) return;
    if (!this.emailValide) {
      this.notif.warning('Saisissez une adresse e-mail valide.');
      return;
    }
    this.loading = true;
    this.http
      .post('/api/auth/mot-de-passe-oublie', { email: this.email.trim(), siteWeb: this.siteWeb })
      .subscribe({
        next: () => {
          this.loading = false;
          this.notif.success(
            'Si cette adresse correspond à un compte actif, un lien de réinitialisation vient d’être envoyé.',
          );
        },
        error: () => {
          this.loading = false;
        },
      });
  }

  reinitialiser() {
    if (this.loading) return;
    if (!this.jeton) {
      this.notif.error('Le lien est absent ou invalide. Demandez-en un nouveau.');
      return;
    }
    if (this.nouveauMotDePasse.length < 8 || !this.nouveauMotDePasse.trim()) {
      this.notif.warning('Le mot de passe doit contenir au moins 8 caractères.');
      return;
    }
    if (this.nouveauMotDePasse !== this.confirmation) {
      this.notif.warning('La confirmation du mot de passe ne correspond pas.');
      return;
    }
    this.loading = true;
    this.http
      .post('/api/auth/reinitialiser-mot-de-passe', {
        jeton: this.jeton,
        nouveauMotDePasse: this.nouveauMotDePasse,
        confirmation: this.confirmation,
      })
      .subscribe({
        next: () => {
          this.loading = false;
          this.notif.success('Votre mot de passe a été modifié. Vous pouvez vous connecter.');
          this.mode = 'login';
          this.router.navigate(['/login']);
        },
        error: () => {
          this.loading = false;
        },
      });
  }
}
