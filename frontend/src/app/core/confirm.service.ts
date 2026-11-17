import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

/** Boîtes de confirmation SweetAlert2 aux couleurs de l'AP. */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  /** Confirmation de suppression (le texte est affiché en texte brut : pas d'injection HTML). */
  async suppression(
    titre = 'Supprimer cet élément ?',
    texte = 'Cette action est définitive.',
  ): Promise<boolean> {
    const r = await Swal.fire({
      title: titre,
      text: texte,
      icon: 'warning',
      showCancelButton: true,
      reverseButtons: true,
      focusCancel: true,
      confirmButtonText: 'Oui, supprimer',
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#E30613',
      cancelButtonColor: '#6c757d',
      customClass: { popup: 'swal-ap' },
    });
    return r.isConfirmed;
  }

  async confirmer(
    titre: string,
    texte: string,
    confirmer = 'Confirmer',
    icon: 'question' | 'warning' | 'info' = 'question',
  ): Promise<boolean> {
    const r = await Swal.fire({
      title: titre,
      text: texte,
      icon,
      showCancelButton: true,
      reverseButtons: true,
      confirmButtonText: confirmer,
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#8B1E1E',
      cancelButtonColor: '#6c757d',
      customClass: { popup: 'swal-ap' },
    });
    return r.isConfirmed;
  }
}
