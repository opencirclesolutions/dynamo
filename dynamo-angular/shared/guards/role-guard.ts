import { Injectable, Injector } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { AuthenticationService } from '../service/authentication-service';
import { NotificationService } from '../service/notification-service';
import { TranslateService } from '@ngx-translate/core';

/**
 * A guard that checks whether a user is in one of the roles mentioned in the "roles"
 * array that can be configured as data on the route
 */
@Injectable({
  providedIn: 'root',
})
export class RoleGuard {

  auth?: AuthenticationService;

  constructor(auth: AuthenticationService, private router: Router,
    private logMessageService: NotificationService, private translate: TranslateService,
  ) {
    this.auth = auth;
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

    if (!this.auth) {
      return false;
    }

    let roles: string[] = route.data['roles'] || [];
    if (this.auth.hasRole(roles)) {
      return true;
    }
    this.logMessageService.error(
      this.translate.instant('screen_not_accessible')
    )
    return this.router.parseUrl('/home');

  }
}
