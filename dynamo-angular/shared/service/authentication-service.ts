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
import { Injectable } from '@angular/core';
import {
  AuthConfig,
  NullValidationHandler,
  OAuthService,
} from 'angular-oauth2-oidc';
import jwtDecode from 'jwt-decode';
import { AccessToken } from '../model/access-token';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {

  roles?: string[];

  constructor(private oauthService: OAuthService) {
    this.initConfiguration();
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

  public hasRole(role: string | string[]): boolean {
    if (!this.roles) {
      let decoded = jwtDecode<AccessToken>(this.oauthService.getAccessToken());
      this.roles = decoded.realm_access.roles;
    }

    return (
      this.roles.find((r) =>
        Array.isArray(role) ? role.includes(r) : r === role
      ) != undefined
    );
  }

  public initConfiguration(): void {

    let authConfig: AuthConfig = {
      issuer: environment.authIssuer,
      redirectUri: window.location.origin + '/home',
      clientId: environment.authClientId,
      scope: 'openid profile email offline_access',
      responseType: 'code',
      disableAtHashCheck: true
    };

    this.oauthService.configure(authConfig);
    this.oauthService.tokenValidationHandler = new NullValidationHandler();
    this.oauthService.loadDiscoveryDocumentAndTryLogin();
    this.oauthService.setupAutomaticSilentRefresh();
  }

  public isAuthenticated(): boolean {
    return (
      !!this.oauthService.getIdentityClaims() &&
      this.oauthService.hasValidAccessToken()
    );
  }
}
