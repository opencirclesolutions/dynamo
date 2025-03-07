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

import { Observable } from "rxjs";
import { StatusServiceInterface } from "./service/status.service";
import { CRUDServiceInterface } from "./service/crud.service";
import { FileServiceInterface } from "./service/file.service";
import { ModelServiceInterface } from "./service/model.service";
import { ExportServiceInterface } from "./service/export.service";
import { AutoFillServiceInterface } from "./service/autofill.service";

export interface DynamoAuthConfig {
  clientId?: string;
  redirectUri?: string;
  postLogoutRedirectUri?: string;
  issuer?: string;
  logoutUrl?: string;
  scope?: string;
}

export interface DynamoAppConfig extends DynamoAuthConfig {

}

/**
Access token supplied by Keycloak SSO
**/
export interface DynamoConfig {
  getConfiguration: () => Observable<DynamoAppConfig>
  getAutoFillService: () => AutoFillServiceInterface
  getCRUDService: () => CRUDServiceInterface
  getExportService: () => ExportServiceInterface
  getFileService: () => FileServiceInterface
  getModelService: () => ModelServiceInterface
  getStatusService: () => StatusServiceInterface
}
