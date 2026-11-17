import { AlertComponent } from '../alert/alert.component';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FichierService } from '../../core/fichier.service';
import { TimelineItem } from '../../core/models';
import { CrudAction } from '../crud.config';

/** Timeline verticale : l'événement le plus récent en premier. */
@Component({
  selector: 'app-timeline',
  standalone: true,
  imports: [CommonModule, AlertComponent],
  templateUrl: './timeline.component.html',
  styleUrl: './timeline.component.css',
})
export class TimelineComponent {
  @Input() items: TimelineItem[] = [];
  @Input() editable = false;
  @Input() actions: CrudAction[] = [];
  @Output() edit = new EventEmitter<any>();
  @Output() remove = new EventEmitter<any>();
  @Output() action = new EventEmitter<{ name: string; row: any }>();

  constructor(private fichiers: FichierService) {}
  ouvrir(nom: string) {
    this.fichiers.ouvrir(nom);
  }
  telecharger(nom: string) {
    this.fichiers.telechargerFichier(nom);
  }
}
