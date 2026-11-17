import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CrudComponent } from '../../shared/crud/crud.component';
import { CATALOGS } from '../../shared/crud.config';

/** Page d'un référentiel (corps, grades, formations, études, services, postes, rôles, autorisations). */
@Component({
  selector: 'app-catalog-page',
  standalone: true,
  imports: [CommonModule, CrudComponent],
  templateUrl: './catalog-page.component.html',
  styleUrl: './catalog-page.component.css',
})
export class CatalogPageComponent {
  cat?: { url: string; config: any };
  grades: any[] | null = null;
  corpsNom = '';

  constructor(
    route: ActivatedRoute,
    private http: HttpClient,
  ) {
    route.data.subscribe((d) => (this.cat = CATALOGS[d['key']]));
  }

  onAction(e: { name: string; row: any }) {
    if (e.name === 'grades') {
      this.corpsNom = e.row.nom;
      this.http.get<any[]>(`/api/corps/${e.row.slug}/grades`).subscribe((g) => (this.grades = g));
    }
  }
}
