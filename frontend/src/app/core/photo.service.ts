import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, shareReplay } from 'rxjs';

/** Charge la photo d'un agent (route protégée par le jeton) et la met en cache sous forme d'URL blob. */
@Injectable({ providedIn: 'root' })
export class PhotoService {
  private cache = new Map<string, Observable<string | null>>();
  constructor(private http: HttpClient) {}

  url(slug: string, photo: string): Observable<string | null> {
    const key = `${slug}:${photo}`;
    let o = this.cache.get(key);
    if (!o) {
      o = this.http.get(`/api/personnels/${slug}/photo`, { responseType: 'blob' }).pipe(
        map((b) => URL.createObjectURL(b)),
        catchError(() => of(null)),
        shareReplay(1),
      );
      this.cache.set(key, o);
    }
    return o;
  }
}
