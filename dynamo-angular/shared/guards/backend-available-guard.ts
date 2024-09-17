import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { StatusService } from 'dynamo/model';
import { catchError, map } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BackendAvailableGuard {
  constructor(
    private statusService: StatusService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

    return this.statusService.getStatus().pipe(
      map((status) => {
        if (status.status !== 'OK') {
          this.router.navigateByUrl('server-not-available');
          return false;
        }
        return true;
      }),
      catchError((error) => this.router.navigateByUrl('/server-not-available'))
    );
  }

}
