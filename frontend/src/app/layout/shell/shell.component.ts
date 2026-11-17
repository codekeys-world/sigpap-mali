import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AvatarComponent } from '../../shared/avatar/avatar.component';
import { AlertComponent } from '../../shared/alert/alert.component';
import { AuthService } from '../../core/auth.service';
import { ROLES, ROLE_LABELS, initiales } from '../../core/models';

interface NavItem {
  label?: string;
  icon?: string;
  link?: string;
  section?: string;
  roles?: string[];
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    AvatarComponent,
    AlertComponent,
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css',
})
export class ShellComponent {
  menuOpen = false;
  q = '';
  nav: NavItem[] = [
    { label: 'Tableau de bord', icon: 'fa-gauge-high', link: '/app/dashboard' },
    { label: 'Personnel', icon: 'fa-users', link: '/app/personnel' },
    { label: 'Retraites', icon: 'fa-person-cane', link: '/app/retraites' },
    { section: 'Référentiels' },
    { label: 'Corps', icon: 'fa-sitemap', link: '/app/corps' },
    { label: 'Grades', icon: 'fa-award', link: '/app/grades' },
    { label: 'Formations', icon: 'fa-graduation-cap', link: '/app/formations' },
    { label: 'Études', icon: 'fa-book-bookmark', link: '/app/etudes' },
    { label: "Niveaux d'étude", icon: 'fa-layer-group', link: '/app/niveaux-etude' },
    { label: 'Mutations de référence', icon: 'fa-right-left', link: '/app/mutations' },
    {
      label: 'Arrêtés d’intégration',
      icon: 'fa-file-signature',
      link: '/app/arretes-integration',
      roles: [ROLES.ADMIN, ROLES.RH],
    },
    { label: 'Services', icon: 'fa-building', link: '/app/services' },
    { label: 'Postes', icon: 'fa-briefcase', link: '/app/postes' },
    { section: 'Administration', roles: [ROLES.ADMIN] },
    { label: 'Rôles', icon: 'fa-user-lock', link: '/app/roles', roles: [ROLES.ADMIN] },
    { label: 'Autorisations', icon: 'fa-key', link: '/app/autorisations', roles: [ROLES.ADMIN] },
    {
      label: "Journal d'activité",
      icon: 'fa-clock-rotate-left',
      link: '/app/journal',
      roles: [ROLES.ADMIN],
    },
  ];

  constructor(
    public auth: AuthService,
    private router: Router,
  ) {}

  s = this.auth.session;
  get ini() {
    return initiales(this.s()?.nom, this.s()?.prenom);
  }
  get roleLabel() {
    return ROLE_LABELS[this.s()?.role ?? ''] ?? this.s()?.role;
  }
  visible(n: NavItem) {
    if (this.auth.hasRole(ROLES.USER))
      return !n.section && (n.link === '/app/dashboard' || n.link === '/app/personnel');
    return !n.roles || this.auth.hasRole(...n.roles);
  }
  href(n: NavItem) {
    return n.link === '/app/personnel' && this.auth.hasRole(ROLES.USER)
      ? `/app/personnel/${this.auth.session()?.slug}`
      : n.link;
  }
  recherche() {
    this.router.navigate(['/app/personnel'], { queryParams: { q: this.q.trim() || null } });
  }
}
