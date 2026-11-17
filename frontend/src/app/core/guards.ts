import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.initialize().pipe(
    map((session) => (session ? true : router.createUrlTree(['/login']))),
    catchError(() => of(router.createUrlTree(['/login']))),
  );
};

export const roleGuard =
  (...roles: string[]): CanActivateFn =>
  () =>
    inject(AuthService).hasRole(...roles) || inject(Router).createUrlTree(['/app/dashboard']);

/** Tant que le mot de passe est temporaire, seule la page « Mon profil » est accessible. */
export const passwordGuard: CanActivateChildFn = (_route, state) =>
  !inject(AuthService).mustChangePassword() ||
  state.url.startsWith('/app/profil') ||
  inject(Router).createUrlTree(['/app/profil']);
