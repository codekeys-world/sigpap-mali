import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertType } from '../../core/notification.service';

const ICONES: Record<AlertType, string> = {
  success: 'fa-circle-check',
  error: 'fa-circle-xmark',
  warning: 'fa-triangle-exclamation',
  info: 'fa-circle-info',
};

/**
 * Alerte unique de l'application. Elle s'adapte au cas : succès, erreur, avertissement ou information
 * (couleur, icône, titre), avec un message, une liste de détails (ex. champs invalides) et un contenu libre.
 * Utilisable en ligne (<app-alert type="warning" message="…" />) ou via NotificationService (affichage flottant).
 */
@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.css',
})
export class AlertComponent {
  @Input() type: AlertType = 'info';
  @Input() titre?: string;
  @Input() message?: string;
  @Input() details?: string[];
  @Input() dismissible = false;
  @Input() duree = 0;
  @Output() closed = new EventEmitter<void>();
  get icone() {
    return ICONES[this.type];
  }
}
