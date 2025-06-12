import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from "@angular/router";
import { KeycloakService } from "../services/keycloak.service";
import { inject } from "@angular/core";

/*
    *  Ein Guard, um zu überprüfen, ob der Benutzer authentifiziert ist.
    *  Wenn der Benutzer nicht authentifiziert ist, wird er zur Anmeldeseite weitergeleitet.
    *  Die Authentifizierung erfolgt über den Keycloak-Service.
*/

export const AuthGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const router: Router = inject(Router);
    const keycloakService: KeycloakService = inject(KeycloakService);
    if(keycloakService.keycloak?.isTokenExpired()) {
        router.navigate(['/login']);
        return false;
    } else if (route.data['role'] && !keycloakService.keycloak.hasResourceRole(route.data['role'])) {
        if (route.data['alternative'] && keycloakService.keycloak.hasResourceRole(route.data['alternative']))
            return true;
        else return false;
    }
    return true;
}