import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SituationSource, infoSituation } from '../../core/models';

@Component({
  selector: 'app-agent-status',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="agent-status"
      [class.centered]="centered"
      role="group"
      aria-label="Situation et type de personnel"
    >
      <span class="badge-soft status-badge" [ngClass]="situation.cls">
        <i class="fa-solid" [ngClass]="situation.icon" aria-hidden="true"></i>
        {{ situation.label }}
      </span>
      <span
        class="badge-soft status-badge personnel-type"
        [class.bordeaux]="agent.surveillant"
        [class.gray]="!agent.surveillant"
      >
        <i class="fa-solid fa-user-tie" aria-hidden="true"></i>
        {{ agent.surveillant ? 'Surveillant' : 'Non surveillant' }}
      </span>
    </div>
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
    }
    .agent-status {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem;
    }
    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.4rem 0.7rem;
      border-radius: 8px;
      font-size: 0.8rem;
      font-weight: 600;
      line-height: 1.4;
      white-space: normal;
      max-width: 100%;
    }
    .agent-status.centered {
      justify-content: center;
    }
    .status-badge i {
      flex-shrink: 0;
      font-size: 0.85em;
    }
    .personnel-type.gray {
      background: #eef1f5;
      color: #445266;
    }
  `,
})
export class AgentStatusComponent {
  @Input() centered = false;
  @Input({ required: true }) agent!: SituationSource & { surveillant?: boolean };

  get situation() {
    return infoSituation(this.agent);
  }
}
