import { Directive, Input, TemplateRef, ViewContainerRef } from "@angular/core";
import { KeycloakService } from "../services/keycloak.service";

/* 
  * Dieses Directive wird verwendet, um Elemente basierend auf den Rollen des Benutzers anzuzeigen oder auszublenden.
  * Es wird im HTML-Template der Komponenten verwendet.
  * Das Directive überprüft, ob der Benutzer die im Input angegebene Rolle hat.
  * Wenn der Benutzer die Rolle hat, wird das Element angezeigt, andernfalls wird es ausgeblendet.
*/

@Directive({
    selector: '[appHasRole]',
    standalone: true
})
export class HasRoleDirective {
    private hasView = false;

    constructor(
        private templateRef: TemplateRef<any>,
        private viewContainr: ViewContainerRef,
        private keycloakService: KeycloakService
    ) {}

    // Diese Methode wird aufgerufen, wenn das Directive im HTML-Template verwendet wird.
    // Sie überprüft, ob der Benutzer die im Input angegebene Rolle hat.
    // Wenn der Benutzer die Rolle hat und das Element nicht angezeigt wird, wird das Element angezeigt.
    // Wenn der Benutzer die Rolle nicht hat und das Element angezeigt wird, wird das Element ausgeblendet.
    @Input() set appHasRole(roles: string | string[]) {
        const roleList = Array.isArray(roles) ? roles : [roles];
        const hasRole = roleList.some(role => this.keycloakService.hasRoles(role));
        if (hasRole && !this.hasView) {
            this.viewContainr.createEmbeddedView(this.templateRef);
            this.hasView = true;
        } else if (!hasRole && this.hasView) {
            this.viewContainr.clear();
            this.hasView = false;
        } else if (!hasRole && !this.hasView) {
            this.viewContainr.clear();
        }
    }
}