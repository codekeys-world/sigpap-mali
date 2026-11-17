import { Component } from '@angular/core';
import { NotificationService } from '../../core/notification.service';
import { AlertComponent } from '../alert/alert.component';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [AlertComponent],
  templateUrl: './alerts-host.component.html',
  styleUrl: './alerts-host.component.css',
})
export class AlertsHostComponent {
  constructor(public notif: NotificationService) {}
}
