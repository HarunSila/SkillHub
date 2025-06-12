import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideNativeDateAdapter } from '@angular/material/core';
import { KeycloakService } from './services/keycloak.service';
import { HttpTokenInterceptor } from './guard & interceptor/http-token.intereptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }), 
    provideRouter(routes), 
    provideAnimationsAsync(),
    provideHttpClient(withInterceptors([HttpTokenInterceptor])),
    provideNativeDateAdapter(),
    {
      provide: APP_INITIALIZER,
      deps: [KeycloakService],
      useFactory: (keycloakService: KeycloakService) => () => keycloakService.init(),
      multi: true
    }
  ]
};
