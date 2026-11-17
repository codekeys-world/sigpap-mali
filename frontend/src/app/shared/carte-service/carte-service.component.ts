import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FichierService } from '../../core/fichier.service';
import { HttpClient } from '@angular/common/http';
import { PhotoService } from '../../core/photo.service';
import { fmtDate, initiales } from '../../core/models';
import { AlertComponent } from '../alert/alert.component';
import { NotificationService } from '../../core/notification.service';
import { switchMap } from 'rxjs';

/** CR80 landscape PVC card (85.6 x 54 mm), screen preview and actual-size print layout. */
export const CARTE_CSS = `
.cartes{display:flex;flex-wrap:wrap;gap:8mm;justify-content:center;align-items:flex-start}
.card-pro{width:85.6mm;height:54mm;border-radius:3mm;overflow:hidden;position:relative;box-sizing:border-box;
  font-family:'Segoe UI',Arial,sans-serif;color:#292929;background:#fff;-webkit-print-color-adjust:exact;print-color-adjust:exact;
  border:.35mm solid #d6d6d6;box-shadow:0 0 0 .2mm rgba(75,13,13,.16),0 3mm 8mm rgba(30,20,20,.14)}
.card-pro.preview{zoom:1.25}
.front-top{height:9mm;display:flex;align-items:center;gap:2mm;padding:0 3mm}
.front-country{display:flex;flex:1;flex-direction:column;gap:.5mm;color:#4B0D0D}
.front-country strong{font-size:7pt;letter-spacing:.04em;line-height:1}
.front-country small{font-size:4.6pt;font-weight:700;letter-spacing:.035em}
.front-logo{width:8mm;height:8mm;object-fit:contain}
.mali-flag{display:inline-flex;width:19mm;height:4.2mm;border:.15mm solid #dedede;flex:0 0 auto}
.mali-flag i{flex:1}.mali-flag i:nth-child(1){background:#009245}.mali-flag i:nth-child(2){background:#FFD600}.mali-flag i:nth-child(3){background:#E30613}
.front-band{height:7mm;display:flex;align-items:center;padding:0 3mm;color:#fff;background:#4B0D0D;font-size:6pt;font-weight:800;letter-spacing:.02em;white-space:nowrap}
.front-band:after{content:'';position:absolute;top:16mm;left:0;right:0;height:.65mm;background:#E30613}
.front-main{display:grid;grid-template-columns:23mm minmax(0,1fr);gap:2.5mm;padding:2mm 3mm 0;align-items:start}
.cp-photo{width:23mm;height:27mm;object-fit:cover;object-position:center 25%;background:#f0f0f0;display:flex;align-items:center;justify-content:center;
  color:#4B0D0D;font-size:15pt;font-weight:800;border:.45mm solid #dedede;border-radius:.8mm}
.cp-info{min-width:0;display:grid;grid-template-columns:1fr 1fr;column-gap:2mm;align-content:start}
.cp-name{grid-column:1/-1;margin-bottom:1mm;color:#4B0D0D;font-size:7.2pt;font-weight:800;line-height:1.1;overflow-wrap:anywhere;text-transform:uppercase}
.cp-field{min-width:0;margin-bottom:.8mm}.cp-field.service,.cp-field.fonction{grid-column:1/-1}
.cp-lbl{display:block;color:#8B1E1E;font-size:4.2pt;font-weight:800;letter-spacing:.045em;text-transform:uppercase}
.cp-val{display:block;color:#292929;font-size:5.8pt;font-weight:700;line-height:1.1;overflow-wrap:anywhere}
.front-bottom{position:absolute;left:3mm;right:33mm;bottom:1.4mm;display:flex;align-items:flex-end;justify-content:space-between;gap:2mm}
.front-date{color:#666;font-size:4.2pt}
.front-card-number{color:#4B0D0D;font-size:3.8pt;font-weight:700;white-space:nowrap}
.front-signature{position:absolute;right:3mm;bottom:1.5mm;width:27mm;height:6mm;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;color:#4B0D0D}
.front-signature img{display:block;max-width:26mm;max-height:3.5mm;object-fit:contain}
.signature-line{width:24mm;border-top:.25mm solid #777;margin-bottom:.5mm}
.front-director-name,.front-director-name-print{display:block;width:27mm;max-width:27mm;padding:0;border:0;background:transparent;color:#4B0D0D;text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font:700 4pt 'Segoe UI',Arial,sans-serif}
.front-director-name{pointer-events:auto!important;cursor:text;outline:none}
.front-director-name:focus{outline:.2mm solid #8B1E1E;outline-offset:.4mm}
.front-director-name-print{height:auto}
.front-tricolor{position:absolute;left:0;right:0;bottom:0;height:1mm;background:linear-gradient(90deg,#009245 0 33.3%,#FFD600 33.3% 66.6%,#E30613 66.6%)}
.card-pro.verso{background:#fff}
.back-head{height:8mm;display:flex;align-items:center;justify-content:center;color:#fff;background:#4B0D0D;font-size:6.2pt;font-weight:800;letter-spacing:.09em}
.back-center{position:absolute;top:10mm;left:4mm;right:4mm;display:grid;grid-template-columns:15mm minmax(0,1fr);align-items:start;gap:1.2mm 3mm;text-align:left} .back-passage{grid-column:2;color:#222;font-size:5.8pt;line-height:1.35}.back-passage strong{font-weight:800}
.back-logo{width:10mm;height:10mm;object-fit:contain;flex:0 0 auto}
.back-copy{max-width:none;color:#444;font-size:5.1pt;line-height:1.4}
.back-copy b{color:#4B0D0D}
.back-qr{position:absolute;left:2mm;bottom:3mm;width:17mm;display:flex;flex-direction:column;align-items:center;gap:.5mm}
.back-qr img{width:17mm;height:17mm;object-fit:contain}
.back-qr span{font-size:4pt;font-weight:700;color:#4B0D0D;overflow-wrap:anywhere;text-align:center}
.back-emergency{position:absolute;right:5mm;bottom:9mm;width:53mm}
.back-emergency b,.back-emergency span{display:block}
.back-emergency b{font-size:5pt;color:#8B1E1E;text-transform:uppercase}
.back-emergency span{font-size:6pt;font-weight:700;color:#292929;overflow-wrap:anywhere}
.cv-mat{position:absolute;left:3mm;right:3mm;bottom:2.8mm;text-align:center;color:#4B0D0D;font-family:Consolas,monospace;font-size:5.2pt;font-weight:700;letter-spacing:.08em}
.signature-upload-tools{display:flex;flex-wrap:wrap;align-items:center;gap:.75rem;margin-top:1rem;padding:.7rem .85rem;border:1px solid #eadede;border-radius:12px;background:#fff}
.signature-upload-tools .form-text{margin:0}
@media print{@page{size:85.6mm 54mm;margin:0}html,body{width:85.6mm!important;height:54mm!important;margin:0!important;padding:0!important}
  .cartes{display:block!important;width:85.6mm!important;padding:0!important;margin:0!important;background:transparent!important}
  .card-pro,.card-pro.preview{display:block!important;width:85.6mm!important;height:54mm!important;margin:0!important;border-radius:0!important;zoom:1!important;box-shadow:none!important;break-after:page;page-break-after:always}
  .card-pro:last-child{break-after:auto;page-break-after:auto}}
`;

@Component({
  selector: 'app-carte-service',
  standalone: true,
  imports: [CommonModule, FormsModule, AlertComponent],
  encapsulation: ViewEncapsulation.None,
  templateUrl: './carte-service.component.html',
  styleUrl: './carte-service.component.css',
})
export class CarteServiceComponent implements OnInit, OnDestroy {
  @Input({ required: true }) agent: any;
  @Input() canPdf = false;
  @Input() canUploadSignature = false;
  @Output() closed = new EventEmitter<void>();
  @ViewChild('zone') zone!: ElementRef<HTMLElement>;
  src: string | null = null;
  qrSrc: string | null = null;
  signatureUrl: string | null = null;
  nomDirecteur = 'Le Directeur Général';
  private nomDirecteurModifie = false;
  detail: any = null;
  loadingDetail = true;
  detailReady = false;
  detailError = false;
  logo = `${location.origin}/assets/logo-small.webp`;
  aujourdhui = fmtDate(new Date().toISOString());

  constructor(
    private photos: PhotoService,
    private http: HttpClient,
    private notif: NotificationService,
    private fichiers: FichierService,
  ) {}
  get a() {
    return this.detail || this.agent;
  }

  telechargerSignature() {
    if (!this.signatureUrl) return;
    this.http
      .get('/api/personnels/signature-directeur', { responseType: 'blob' })
      .subscribe((signature) => this.fichiers.telecharger(signature, 'signature-directeur.png'));
  }
  get ini() {
    return initiales(this.a.nom, this.a.prenom);
  }
  get naissance() {
    return fmtDate(this.a.dateNaissance) || '-';
  }

  ngOnInit() {
    this.chargerSignature();
    this.chargerNomDirecteur();
    if (!this.agent?.slug) {
      this.loadingDetail = false;
      this.detailError = true;
      return;
    }
    this.http.get<any>(`/api/personnels/${this.agent.slug}`).subscribe({
      next: (p) => {
        this.detail = {
          ...this.agent,
          ...p,
          nom: p?.nom || this.agent.nom,
          prenom: p?.prenom || this.agent.prenom,
          matricule: p?.matricule || this.agent.matricule,
          gradeActuel: p?.gradeActuel || this.agent.gradeActuel,
          serviceCourant: p?.serviceCourant || this.agent.serviceCourant,
          dateNaissance: p?.dateNaissance || this.agent.dateNaissance,
        };
        this.chargerPhoto();
        this.chargerQr();
      },
      error: () => {
        this.loadingDetail = false;
        this.detailError = true;
      },
    });
  }

  ngOnDestroy() {
    if (this.signatureUrl) URL.revokeObjectURL(this.signatureUrl);
    if (this.qrSrc) URL.revokeObjectURL(this.qrSrc);
  }

  private chargerSignature() {
    this.http.get('/api/personnels/signature-directeur', { responseType: 'blob' }).subscribe({
      next: (blob) => {
        if (blob.size) this.remplacerSignature(URL.createObjectURL(blob));
      },
      error: () => {},
    });
  }

  private chargerNomDirecteur() {
    this.http.get('/api/personnels/signature-directeur/nom', { responseType: 'text' }).subscribe({
      next: (nom) => {
        if (!this.nomDirecteurModifie && nom.trim()) this.nomDirecteur = nom.trim();
      },
      error: () => this.notif.error('Impossible de charger le nom du Directeur.'),
    });
  }

  get nomDirecteurValide() {
    return !!this.nomDirecteur.trim() && this.nomDirecteur.length <= 120;
  }

  modifierNomDirecteur(nom: string) {
    this.nomDirecteurModifie = true;
    this.nomDirecteur = nom;
  }

  enregistrerNomDirecteur(apresEnregistrement?: () => void) {
    if (!this.nomDirecteurModifie) {
      apresEnregistrement?.();
      return;
    }
    const nom = this.nomDirecteur.trim();
    if (!this.nomDirecteurValide) {
      this.notif.warning('Saisissez le nom du Directeur.');
      return;
    }
    this.http
      .get('/api/auth/csrf', { responseType: 'text' })
      .pipe(
        switchMap(() =>
          this.http.put<void>('/api/personnels/signature-directeur/nom', null, { params: { nom } }),
        ),
      )
      .subscribe({
        next: () => {
          this.nomDirecteur = nom;
          this.nomDirecteurModifie = false;
          this.notif.success('Le nom du Directeur a été enregistré.');
          apresEnregistrement?.();
        },
        error: () => this.notif.error('Impossible d’enregistrer le nom du Directeur.'),
      });
  }

  private remplacerSignature(url: string) {
    if (this.signatureUrl) URL.revokeObjectURL(this.signatureUrl);
    this.signatureUrl = url;
  }

  uploaderSignature(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
      this.notif.error('La signature ne doit pas depasser 2 Mo.');
      return;
    }
    if (!['image/png', 'image/jpeg'].includes(file.type)) {
      this.notif.warning('Selectionnez un fichier PNG ou JPEG.');
      return;
    }
    const data = new FormData();
    data.append('file', file);
    this.http
      .get('/api/auth/csrf', { responseType: 'text' })
      .pipe(switchMap(() => this.http.post<void>('/api/personnels/signature-directeur', data)))
      .subscribe({
        next: () => {
          this.chargerSignature();
          this.notif.success(
            'La signature du Directeur a été enregistrée pour les prochaines cartes.',
          );
        },
      });
  }

  private chargerPhoto() {
    const p = this.a;
    if (p?.slug && p?.photo) this.photos.url(p.slug, p.photo).subscribe((u) => (this.src = u));
  }

  private chargerQr() {
    this.http.get(`/api/personnels/${this.a.slug}/carte/qr`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        if (this.qrSrc) URL.revokeObjectURL(this.qrSrc);
        this.qrSrc = URL.createObjectURL(blob);
        this.detailReady = !!(
          this.a.nom &&
          this.a.prenom &&
          this.a.matricule &&
          this.a.numeroCarte
        );
        this.detailError = !this.detailReady;
        this.loadingDetail = false;
      },
      error: () => {
        this.loadingDetail = false;
        this.detailError = true;
      },
    });
  }

  async imprimer() {
    if (this.loadingDetail || !this.detailReady || !this.nomDirecteurValide) return;
    const w = window.open('', '_blank', 'width=980,height=720');
    if (!w) {
      alert('Autorisez les fenêtres contextuelles pour imprimer la carte.');
      return;
    }
    const copie = this.zone.nativeElement.cloneNode(true) as HTMLElement;
    copie.querySelectorAll<HTMLInputElement>('.front-director-name').forEach((input) => {
      const nom = w.document.createElement('span');
      nom.className = 'front-director-name-print';
      nom.textContent = input.value;
      input.replaceWith(nom);
    });
    w.document.write(
      `<!doctype html><html lang="fr"><head><meta charset="utf-8"><title>Carte de service - ${this.a.matricule}</title><style>${CARTE_CSS}</style></head><body>${copie.innerHTML}</body></html>`,
    );
    w.document.close();
    await Promise.all(
      Array.from(w.document.images).map((img) => img.decode().catch(() => undefined)),
    );
    await w.document.fonts?.ready;
    w.focus();
    w.print();
  }

  pdf() {
    if (this.loadingDetail || !this.detailReady) return;
    if (this.canUploadSignature && this.nomDirecteurModifie) {
      this.enregistrerNomDirecteur(() => this.ouvrirPdf());
      return;
    }
    this.ouvrirPdf();
  }

  private ouvrirPdf() {
    this.http
      .get(`/api/personnels/${this.a.slug}/carte`, { responseType: 'blob' })
      .subscribe((blob) => window.open(URL.createObjectURL(blob), '_blank'));
  }
}
