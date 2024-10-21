/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import { Inject, Injectable } from '@angular/core';
import {
  AuthConfig,
  NullValidationHandler,
  OAuthService,
} from 'angular-oauth2-oidc';
import { DynamoConfig } from '../interfaces/dynamo-config';
import { first, map, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  roles?: string[];

  constructor(
    private oauthService: OAuthService,
    @Inject("DYNAMO_CONFIG") configuration: DynamoConfig,
  ) {
    this.oauthService.tokenValidationHandler = new NullValidationHandler()
    configuration.getConfiguration().pipe(
      map(c => ({
        issuer: c.issuer,
        redirectUrl: c.redirectUri || window.location.origin + '/home',
        clientId: c.clientId,
        scope: c.scope || 'openid profile email offline_access',
        responseType: 'code',
        disableAtHashCheck: true,
      } as AuthConfig)),
      tap(c => this.oauthService.configure(c)),
      tap(_ => this.oauthService.loadDiscoveryDocumentAndTryLogin()),
      tap(_ => this.oauthService.setupAutomaticSilentRefresh()),
      first(),
    ).subscribe(_ => { })
  }

  login() {
    this.oauthService.initLoginFlow();
  }

  logout() {
    this.oauthService.revokeTokenAndLogout();
    this.oauthService.logOut();
  }

  getAccessToken() {
    return this.oauthService.getAccessToken();
  }

  private getRealmRoles(claims: any): string[] | undefined {
    return claims?.realm_access?.roles;
  }

  public hasRole(role: string | string[]): boolean {
    if (!this.roles) {
      this.roles = this.getRealmRoles(this.oauthService.getIdentityClaims())
    }

    return (
      this.roles?.find((r) =>
        Array.isArray(role) ? role.includes(r) : r === role
      ) != undefined
    );
  }

  public isAuthenticated(): boolean {
    return (
      !!this.oauthService.getIdentityClaims() &&
      this.oauthService.hasValidAccessToken()
    );
  }
}
