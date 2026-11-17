import { AuditUserPipe } from '../audit-user/audit-user.pipe';
import { PageHeaderComponent } from '../page-header/page-header.component';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { NotificationService } from '../../core/notification.service';
import { FichierService } from '../../core/fichier.service';
import { ConfirmService } from '../../core/confirm.service';
import { AlertComponent } from '../alert/alert.component';
import { PageResult, TimelineItem, fmtDate, nomFichier } from '../../core/models';
import { CrudConfig, CrudField } from '../crud.config';
import { PagerComponent } from '../pager/pager.component';
import { TimelineComponent } from '../timeline/timeline.component';

const getPath = (o: any, path: string) =>
  path.split('.').reduce((a, k) => (a == null ? undefined : a[k]), o);
const setPath = (o: any, path: string, v: any) => {
  const keys = path.split('.');
  let cur = o;
  keys.slice(0, -1).forEach((k) => {
    if (cur[k] == null || typeof cur[k] !== 'object') cur[k] = {};
    cur = cur[k];
  });
  cur[keys[keys.length - 1]] = v;
};

/**
 * Composant CRUD générique : recherche + pagination (côté serveur pour les référentiels, côté client pour les
 * sous-listes d'un agent), vue tableau ou timeline, création/modification en modale, suppression, traçabilité.
 */
@Component({
  selector: 'app-crud',
  standalone: true,
  imports: [
    AuditUserPipe,
    PageHeaderComponent,
    CommonModule,
    FormsModule,
    PagerComponent,
    TimelineComponent,
    AlertComponent,
  ],
  templateUrl: './crud.component.html',
  styleUrl: './crud.component.css',
})
export class CrudComponent implements OnInit, OnChanges, OnDestroy {
  @Input({ required: true }) config!: CrudConfig;
  @Input({ required: true }) baseUrl!: string;
  @Input() embedded = false;
  @Output() action = new EventEmitter<{ name: string; row: any }>();
  @Output() changed = new EventEmitter<void>();

  rows: any[] = [];
  search = '';
  page = 0;
  size = 10;
  total = 0; // total côté serveur (mode paged)
  loaded = false;
  loadError = false;
  view: 'table' | 'timeline' = 'table';
  showForm = false;
  saving = false;
  uploading = 0;
  model: any = {};
  touched: Record<string, boolean> = {};
  refs: Record<string, any[]> = {};
  refLoading: Record<string, boolean> = {};
  refErrors: Record<string, string> = {};
  suggestions: Record<string, any[]> = {};
  private search$ = new Subject<void>();
  private sub?: Subscription;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private notif: NotificationService,
    private fichiers: FichierService,
    private confirm: ConfirmService,
  ) {}

  get canWrite() {
    return this.auth.hasRole(...this.config.writeRoles);
  }
  get canSeeTrace() {
    return this.auth.canViewAudit();
  }
  get cols(): CrudField[] {
    return this.config.fields.filter((f) => f.table !== false);
  }
  get formFields(): CrudField[] {
    return this.config.fields.filter((f) => !f.virtual);
  }

  get = getPath;
  nomFichier = nomFichier;
  sameSlug = (a: any, b: any) => (a && b ? a.slug === b.slug : a === b);

  ngOnInit() {
    if (this.config.timeline) this.view = 'timeline';
    this.sub = this.search$.pipe(debounceTime(300)).subscribe(() => {
      this.page = 0;
      this.load();
    });
    this.load();
    // Les références et suggestions alimentent uniquement le formulaire de modification.
    if (!this.canWrite) return;
    this.config.fields.forEach((f) => {
      if (
        (f.type === 'ref' || f.type === 'multiref' || f.type === 'catalogSelect') &&
        f.optionsUrl
      ) {
        this.chargerReference(f);
      }
      if (f.suggest)
        this.http.get<any[]>(f.suggest.url).subscribe((l) => (this.suggestions[f.key] = l));
    });
  }

  ngOnChanges(c: SimpleChanges) {
    if (c['baseUrl'] && !c['baseUrl'].firstChange) {
      this.page = 0;
      this.load();
    }
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  // ---------- chargement, recherche, pagination ----------

  load() {
    this.loaded = false;
    this.loadError = false;
    if (this.config.paged) {
      let params = new HttpParams().set('page', this.page).set('size', this.size);
      if (this.search.trim()) params = params.set('q', this.search.trim());
      this.http.get<PageResult<any>>(this.baseUrl, { params }).subscribe({
        next: (d) => {
          if (d.content.length === 0 && this.page > 0) {
            this.page--;
            this.load();
            return;
          }
          this.rows = d.content;
          this.total = d.page.totalElements;
          this.loaded = true;
        },
        error: () => {
          this.loaded = true;
          this.loadError = true;
        },
      });
    } else {
      this.http.get<any[]>(this.baseUrl).subscribe({
        next: (l) => {
          this.rows = l;
          this.loaded = true;
        },
        error: () => {
          this.loaded = true;
          this.loadError = true;
        },
      });
    }
  }

  onSearch(v: string) {
    this.search = v;
    if (this.config.paged) this.search$.next();
    else this.page = 0;
  }

  private matching(): any[] {
    const q = this.search.trim().toLowerCase();
    return q ? this.rows.filter((r) => JSON.stringify(r).toLowerCase().includes(q)) : this.rows;
  }
  visibleRows(): any[] {
    if (this.config.paged) return this.rows;
    return this.matching().slice(this.page * this.size, (this.page + 1) * this.size);
  }
  totalCount() {
    return this.config.paged ? this.total : this.matching().length;
  }
  goPage(p: number) {
    this.page = p;
    if (this.config.paged) this.load();
  }
  changeSize(s: number) {
    this.size = s;
    this.page = 0;
    if (this.config.paged) this.load();
  }

  timelineItems(): TimelineItem[] {
    return this.matching().map((r) => {
      const item = { ...this.config.timeline!(r), ref: r };
      return this.canSeeTrace ? item : { ...item, audit: undefined };
    });
  }

  // ---------- affichage ----------

  cell(r: any, f: CrudField): string {
    if (f.format) return f.format(r);
    const v = getPath(r, f.key);
    if (v === null || v === undefined || v === '') return 'Non renseigné';
    switch (f.type) {
      case 'ref':
        return v[f.optionLabel || 'nom'] ?? 'Non renseigné';
      case 'multiref':
        return (v as any[]).map((x) => x[f.optionLabel || 'nom']).join(', ') || 'Non renseigné';
      case 'select':
        return f.options?.find((o) => o.value === v)?.label ?? String(v);
      case 'catalogSelect':
        return String(v);
      case 'date':
        return fmtDate(v);
      case 'checkbox':
        return v ? 'Oui' : 'Non';
      default:
        return String(v);
    }
  }

  // ---------- validation ----------

  /** Message d'erreur d'un champ (null si valide). */
  erreur(f: CrudField): string | null {
    const v = getPath(this.model, f.key);
    const vide =
      v === null ||
      v === undefined ||
      v === '' ||
      (Array.isArray(v) && v.length === 0 && !!f.required);
    if (f.required && vide) return 'Ce champ est obligatoire.';
    if (v === null || v === undefined || v === '') return null;
    if (f.type === 'ref' && (!v || typeof v !== 'object' || !v.slug))
      return 'Choisissez une valeur dans la liste.';
    if (f.type === 'select' && f.options && !f.options.some((o) => o.value === v))
      return 'Choisissez une valeur dans la liste.';
    if (f.type === 'number') {
      const n = Number(v);
      if (!Number.isFinite(n)) return 'Nombre invalide.';
      if (!Number.isInteger(n)) return 'Saisissez un nombre entier.';
      if (f.min != null && n < f.min) return `La valeur minimale est ${f.min}.`;
      if (f.max != null && n > f.max) return `La valeur maximale est ${f.max}.`;
    } else if (typeof v === 'string') {
      const t = v.trim();
      if (f.required && !t) return 'Ce champ est obligatoire.';
      if (f.minLength && t.length < f.minLength) return `Minimum ${f.minLength} caractères.`;
      if (v.length > (f.maxLength ?? (f.type === 'textarea' ? 1000 : 255)))
        return 'Texte trop long.';
      if (f.pattern && !new RegExp(f.pattern).test(t))
        return f.patternMessage || 'Format invalide.';
    }
    return null;
  }
  get erreurGlobale(): string | null {
    return this.config.validate ? this.config.validate(this.model) : null;
  }
  /** Le formulaire est valide : tous les champs + la règle globale. */
  get valid(): boolean {
    return this.formFields.every((f) => !this.erreur(f)) && !this.erreurGlobale;
  }

  // ---------- formulaire ----------

  set(key: string, v: any) {
    setPath(this.model, key, v);
  }

  placeholder(f: CrudField): string {
    if (f.placeholder) return f.placeholder;
    if (f.type === 'date') return 'jj/mm/aaaa';
    const examples: Record<string, string> = {
      nom: 'Ex. : ' + f.label.toLowerCase(),
      titre: 'Ex. : formation en gestion administrative',
      reference: 'Ex. : Décision n° 2026-001',
      lieu: 'Ex. : Bamako',
      description: 'Précisez les informations utiles',
      anneeDebut: 'Ex. : 2020',
      anneeFin: 'Ex. : 2024',
    };
    if (examples[f.key]) return examples[f.key];
    if (f.type === 'number')
      return f.min != null && f.max != null ? `${f.min} à ${f.max}` : 'Saisir un nombre';
    return `Saisir ${f.label.toLocaleLowerCase()}`;
  }

  chargerReference(f: CrudField) {
    if (!f.optionsUrl) return;
    this.refLoading[f.key] = true;
    this.refErrors[f.key] = '';
    this.http.get<any[]>(f.optionsUrl).subscribe({
      next: (values) => {
        this.refs[f.key] = values;
        this.refLoading[f.key] = false;
      },
      error: () => {
        this.refs[f.key] = [];
        this.refLoading[f.key] = false;
        this.refErrors[f.key] = `Impossible de charger ${f.label.toLowerCase()}.`;
      },
    });
  }

  openNew() {
    this.touched = {};
    this.model = {};
    this.formFields
      .filter((f) => ['select', 'ref', 'catalogSelect'].includes(f.type || ''))
      .forEach((f) => setPath(this.model, f.key, null));
    this.config.fields
      .filter((f) => f.type === 'multiref')
      .forEach((f) => setPath(this.model, f.key, []));
    this.showForm = true;
  }
  openEdit(r: any) {
    this.touched = {};
    this.model = JSON.parse(JSON.stringify(r));
    this.showForm = true;
  }
  closeForm() {
    this.showForm = false;
  }

  isSelected(f: CrudField, o: any) {
    return ((getPath(this.model, f.key) as any[]) || []).some((x) => x.slug === o.slug);
  }
  toggle(f: CrudField, o: any) {
    const cur: any[] = getPath(this.model, f.key) || [];
    setPath(
      this.model,
      f.key,
      this.isSelected(f, o) ? cur.filter((x) => x.slug !== o.slug) : [...cur, o],
    );
  }

  onFile(ev: Event, f: CrudField) {
    if (this.uploading > 0 || this.saving) return;
    const file = (ev.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const editedModel = this.model;
    this.uploading++;
    this.touched[f.key] = true;
    this.fichiers.upload(file).subscribe({
      next: (res) => {
        if (this.model === editedModel) this.set(f.key, res.chemin);
        this.uploading--;
      },
      error: () => {
        this.uploading--;
        (ev.target as HTMLInputElement).value = '';
      },
    });
  }
  open(chemin: string) {
    this.fichiers.ouvrir(chemin);
  }
  download(chemin: string) {
    this.fichiers.telechargerFichier(chemin);
  }

  save() {
    if (this.saving || this.uploading > 0 || !this.canWrite) return;
    if (!this.valid) {
      this.formFields.forEach((f) => (this.touched[f.key] = true));
      if (this.erreurGlobale) this.notif.warning(this.erreurGlobale);
      return;
    }
    for (const f of this.formFields) {
      const v = getPath(this.model, f.key);
      if (f.required && (v === null || v === undefined || v === '')) {
        this.notif.error(`Le champ « ${f.label} » est obligatoire.`);
        return;
      }
    }
    const body = JSON.parse(JSON.stringify(this.model));
    this.formFields
      .filter((f) => f.type === 'ref')
      .forEach((f) => {
        const value = getPath(body, f.key);
        setPath(body, f.key, value?.slug ? { slug: value.slug } : null);
      });
    this.formFields
      .filter((f) => f.type === 'multiref')
      .forEach((f) => {
        const values = getPath(body, f.key);
        setPath(
          body,
          f.key,
          Array.isArray(values) ? values.map((value: any) => ({ slug: value.slug })) : [],
        );
      });
    this.formFields
      .filter((f) => f.type === 'number')
      .forEach((f) => {
        const v = getPath(body, f.key);
        setPath(body, f.key, v === '' || v === null || v === undefined ? null : Number(v));
      });
    this.saving = true;
    const req = this.model.slug
      ? this.http.put(`${this.baseUrl}/${this.model.slug}`, body)
      : this.http.post(this.baseUrl, body);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.notif.success(this.model.slug ? 'Modification enregistrée.' : 'Élément ajouté.');
        this.closeForm();
        this.load();
        this.changed.emit();
      },
      error: () => (this.saving = false),
    });
  }

  /** Confirmation SweetAlert puis suppression. */
  async supprimer(row: any) {
    const nom = this.cols.length ? this.cell(row, this.cols[0]) : '';
    const ok = await this.confirm.suppression(
      'Supprimer cet élément ?',
      `${this.config.singular.charAt(0).toUpperCase() + this.config.singular.slice(1)}${nom && nom !== 'Non renseigné' ? ' « ' + nom + ' »' : ''} sera supprimé définitivement.`,
    );
    if (!ok) return;
    this.http.delete(`${this.baseUrl}/${row.slug}`).subscribe(() => {
      this.notif.success('Élément supprimé.');
      this.load();
      this.changed.emit();
    });
  }
}
