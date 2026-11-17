import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LoadingService } from '../core/loading.service';
import { AlertsHostComponent } from '../shared/alerts-host/alerts-host.component';
import { CookieConsentComponent } from '../shared/cookie-consent/cookie-consent.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AlertsHostComponent, CookieConsentComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  constructor(public loading: LoadingService) {}
}
