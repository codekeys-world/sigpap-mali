import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

/** Pagination réutilisable (numéros de page + taille de page). `page` est indexée à partir de 0. */
@Component({
  selector: 'app-pager',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pager.component.html',
  styleUrl: './pager.component.css',
})
export class PagerComponent {
  @Input() page = 0;
  @Input() size = 10;
  @Input() total = 0;
  @Input() sizes = [5, 10, 20, 50];
  @Output() pageChange = new EventEmitter<number>();
  @Output() sizeChange = new EventEmitter<number>();

  get totalPages() {
    return Math.max(1, Math.ceil(this.total / this.size));
  }
  get debut() {
    return this.total ? this.page * this.size + 1 : 0;
  }
  get fin() {
    return Math.min(this.total, (this.page + 1) * this.size);
  }
  get pages(): number[] {
    const start = Math.max(0, Math.min(this.page - 2, this.totalPages - 5));
    const end = Math.min(this.totalPages, start + 5);
    return Array.from({ length: end - start }, (_, i) => start + i);
  }
  go(p: number) {
    if (p >= 0 && p < this.totalPages && p !== this.page) this.pageChange.emit(p);
  }
}
