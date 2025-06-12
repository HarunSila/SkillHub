import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { KeycloakService } from "../services/keycloak.service";

/*
* Dieser Interceptor wird verwendet, um das Token zum Header der Anfrage hinzuzufügen.
* Das Token wird aus dem Keycloak-Service entnommen.
* Durch das Token kann der Backend-Server den Benutzer identifizieren und die Anfrage autorisieren.
*/

export const HttpTokenInterceptor: HttpInterceptorFn = (req, next) => {
    const keycloakService = inject(KeycloakService);
    const token = keycloakService.keycloak.token;
        if(token) {
            req = req.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
        }
        return next(req);
}
