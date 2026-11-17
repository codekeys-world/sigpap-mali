import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  features = [
    {
      icon: 'fa-users',
      c: 'c1',
      title: 'Gestion du personnel',
      text: 'Enregistrement, modification, recherche et suppression des agents, avec filtres (grade, corps, formation, décès).',
    },
    {
      icon: 'fa-award',
      c: 'c4',
      title: 'Grades et corps',
      text: 'Historique de port de grade pour chaque agent et rattachement des grades à leur corps.',
    },
    {
      icon: 'fa-graduation-cap',
      c: 'c2',
      title: 'Formations et études',
      text: 'Suivi des formations certifiantes et des études diplômantes, avec dates exactes ou années.',
    },
    {
      icon: 'fa-building',
      c: 'c3',
      title: 'Affectations',
      text: "Directions, services centraux et maisons d'arrêt, postes occupés, décisions et fichiers joints.",
    },
    {
      icon: 'fa-id-card',
      c: 'c5',
      title: 'Carte professionnelle',
      text: "Génération de la carte professionnelle de l'agent aux couleurs de l'AP (PDF).",
    },
    {
      icon: 'fa-file-excel',
      c: 'c2',
      title: 'Exports Excel et Word',
      text: 'Extraction des listes de personnel selon les filtres choisis.',
    },
  ];
  constructor(public auth: AuthService) {}
}
