import {
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, filter, finalize, switchMap, throwError } from 'rxjs';
import { LoadingService } from './loading.service';
import { AuthService } from './auth.service';
import { NotificationService } from './notification.service';

/** Transmet les cookies d'authentification et centralise les erreurs de l'API. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const notif = inject(NotificationService);
  const loading = inject(LoadingService);
  const isApi = req.url === '/api' || req.url.startsWith('/api/');
  const request = isApi ? req.clone({ withCredentials: true }) : req;
  const needsCsrf =
    isApi && !['GET', 'HEAD', 'OPTIONS'].includes(req.method) && !req.headers.has('X-XSRF-TOKEN');

  const afficher = (err: HttpErrorResponse, body: any) => {
    const status = err.status;
    const message: string | undefined = body?.message;
    const details: string[] = body?.details ? Object.values(body.details) : [];
    if (status === 0) {
      notif.error(
        'Le serveur est momentanément inaccessible. Vérifiez votre connexion, puis réessayez.',
        'Serveur injoignable',
      );
    } else if (status === 401) {
      if (req.url.includes('/api/auth/login')) {
        notif.error(message ?? 'Matricule ou mot de passe incorrect.', 'Connexion refusée');
      } else if (
        !['/api/auth/moi', '/api/auth/csrf', '/api/auth/logout'].some((path) =>
          req.url.includes(path),
        )
      ) {
        auth.logout();
        notif.warning('Votre session a expiré, veuillez vous reconnecter.', 'Session expirée');
      }
    } else if (status === 400 && details.length) {
      notif.warning('Veuillez corriger les champs suivants :', 'Données invalides', details);
    } else if (status === 400) {
      notif.warning(message ?? 'La requête est invalide.', 'Action impossible');
    } else if (status === 403) {
      notif.warning(
        message ?? "Vous n'avez pas le droit d'effectuer cette action.",
        'Accès refusé',
      );
    } else if (status === 404) {
      notif.info(message ?? 'Élément introuvable.', 'Introuvable');
    } else if (status === 409) {
      notif.warning(
        message ?? "L'opération est en conflit avec des données existantes.",
        'Conflit',
      );
    } else if (status === 413) {
      notif.warning(message ?? 'Le fichier est trop volumineux.', 'Fichier trop volumineux');
    } else if (status === 429) {
      notif.warning(message ?? 'Trop de tentatives, réessayez plus tard.', 'Trop de tentatives');
    } else if (status >= 500) {
      notif.error(
        message ?? 'Une erreur interne est survenue. Contactez l’administrateur.',
        'Erreur du serveur',
      );
    } else {
      notif.error(message ?? 'Une erreur est survenue.');
    }
  };

  loading.start();
  // Toutes les modifications utilisent le jeton serveur, sans dépendre de document.cookie.
  // next évite de réentrer dans cet intercepteur pour la requête de préparation.
  const response = needsCsrf
    ? next(
        new HttpRequest('GET', '/api/auth/csrf', {
          withCredentials: true,
          responseType: 'text',
        }),
      ).pipe(
        filter((event): event is HttpResponse<string> => event instanceof HttpResponse),
        switchMap((event) =>
          next(request.clone({ setHeaders: { 'X-XSRF-TOKEN': event.body ?? '' } })),
        ),
      )
    : next(request);
  return response.pipe(
    finalize(() => loading.stop()),
    catchError((err: HttpErrorResponse) => {
      if (err.error instanceof Blob) {
        err.error.text().then((text) => {
          try {
            afficher(err, JSON.parse(text));
          } catch {
            afficher(err, null);
          }
        });
      } else {
        afficher(err, err.error);
      }
      return throwError(() => err);
    }),
  );
};
