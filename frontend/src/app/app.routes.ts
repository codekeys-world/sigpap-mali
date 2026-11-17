import { Routes } from '@angular/router';
import { authGuard, passwordGuard, roleGuard } from './core/guards';
import { ADMIN_RH, GESTIONNAIRES, ROLES } from './core/models';

export const routes: Routes = [
  {
    path: '',
    title: 'Accueil',
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    title: 'Connexion',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'app',
    title: 'Espace personnel',
    loadComponent: () => import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    canActivate: [authGuard],
    canActivateChild: [passwordGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        title: 'Tableau de bord',
        loadComponent: () =>
          import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'personnel',
        title: 'Personnel',
        loadComponent: () =>
          import('./pages/personnel-list/personnel-list.component').then(
            (m) => m.PersonnelListComponent,
          ),
      },
      {
        path: 'personnel/import',
        title: 'Importer des agents',
        loadComponent: () =>
          import('./pages/import-agents/import-agents.component').then(
            (m) => m.ImportAgentsComponent,
          ),
        canActivate: [roleGuard(...ADMIN_RH)],
      },
      {
        path: 'retraites',
        title: 'Suivi des retraites',
        loadComponent: () =>
          import('./pages/retraites/retraites.component').then((m) => m.RetraitesComponent),
      },
      {
        path: 'personnel/nouveau',
        title: 'Nouvel agent',
        loadComponent: () =>
          import('./pages/personnel-form/personnel-form.component').then(
            (m) => m.PersonnelFormComponent,
          ),
        canActivate: [roleGuard(...GESTIONNAIRES)],
      },
      {
        path: 'personnel/:id/modifier',
        title: 'Modifier un agent',
        loadComponent: () =>
          import('./pages/personnel-form/personnel-form.component').then(
            (m) => m.PersonnelFormComponent,
          ),
        canActivate: [roleGuard(...GESTIONNAIRES)],
      },
      {
        path: 'personnel/:id',
        title: 'Dossier individuel',
        loadComponent: () =>
          import('./pages/personnel-detail/personnel-detail.component').then(
            (m) => m.PersonnelDetailComponent,
          ),
      },
      {
        path: 'corps',
        title: 'Corps',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'corps' },
      },
      {
        path: 'grades',
        title: 'Grades',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'grades' },
      },
      {
        path: 'formations',
        title: 'Formations',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'formations' },
      },
      {
        path: 'etudes',
        title: 'Études',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'etudes' },
      },
      {
        path: 'niveaux-etude',
        title: 'Niveaux d’étude',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'niveaux-etude' },
      },
      {
        path: 'mutations',
        title: 'Mutations de référence',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'affectations' },
      },
      {
        path: 'arretes-integration',
        title: 'Arrêtés d’intégration',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'arretes-integration' },
        canActivate: [roleGuard(...ADMIN_RH)],
      },
      {
        path: 'services',
        title: 'Services',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'services' },
      },
      {
        path: 'postes',
        title: 'Postes',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'postes' },
      },
      {
        path: 'roles',
        title: 'Rôles',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'roles' },
        canActivate: [roleGuard(ROLES.ADMIN)],
      },
      {
        path: 'autorisations',
        title: 'Autorisations',
        loadComponent: () =>
          import('./pages/catalog-page/catalog-page.component').then((m) => m.CatalogPageComponent),
        data: { key: 'autorisations' },
        canActivate: [roleGuard(ROLES.ADMIN)],
      },
      {
        path: 'profil',
        title: 'Mon profil',
        loadComponent: () =>
          import('./pages/profile/profile.component').then((m) => m.ProfileComponent),
      },
      {
        path: 'journal',
        title: 'Journal d’activité',
        loadComponent: () =>
          import('./pages/journal/journal.component').then((m) => m.JournalComponent),
        canActivate: [roleGuard(ROLES.ADMIN)],
      },
      {
        path: '**',
        title: 'Page introuvable',
        loadComponent: () =>
          import('./pages/not-found/not-found.component').then((m) => m.NotFoundComponent),
      },
    ],
  },
  {
    path: '**',
    title: 'Page introuvable',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then((m) => m.NotFoundComponent),
  },
];
