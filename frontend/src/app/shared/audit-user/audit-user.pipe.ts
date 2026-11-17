import { Pipe, PipeTransform } from '@angular/core';
import { AuditUserService } from '../../core/audit-user.service';

@Pipe({ name: 'auditUser', standalone: true })
export class AuditUserPipe implements PipeTransform {
  constructor(private authors: AuditUserService) {}

  transform(matricule?: string | null) {
    return this.authors.identity(matricule);
  }
}
