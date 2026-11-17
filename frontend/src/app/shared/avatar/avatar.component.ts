import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PhotoService } from '../../core/photo.service';
import { initiales } from '../../core/models';

/** Photo de l'agent (ou ses initiales si aucune photo). */
@Component({
  selector: 'app-avatar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './avatar.component.html',
  styleUrl: './avatar.component.css',
})
export class AvatarComponent implements OnChanges {
  @Input() slug?: string | null;
  @Input() photo?: string | null;
  @Input() nom?: string;
  @Input() prenom?: string;
  @Input() initials?: string;
  @Input() size: '' | 'sm' | 'lg' | 'xl' | 'card' = '';
  @Input() color = 'c1';
  src: string | null = null;

  constructor(private photos: PhotoService) {}
  get alternative() {
    const name = [this.prenom, this.nom].filter(Boolean).join(' ');
    return name ? `Photo de ${name}` : 'Photo du profil';
  }
  get ini() {
    return this.initials ?? initiales(this.nom, this.prenom);
  }

  ngOnChanges() {
    this.src = null;
    if (this.slug && this.photo)
      this.photos.url(this.slug, this.photo).subscribe((u) => (this.src = u));
  }
}
