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
import { Observable } from 'rxjs';
import { EntityModelResponse } from '../model/entityModelResponse';

export interface ModelServiceInterface {
  getActionEntityModel: (entityName: string, actionId: string, reference?: string) => Observable<EntityModelResponse>
  getEntityModel: (entityName: string, reference?: string) => Observable<EntityModelResponse>
  getNestedEntityModel: (entityName: string, attributeName: string, reference?: string) => Observable<EntityModelResponse>
  getNestedEntityModel2(entityName: string, attributeName: string, secondAttribute: string, reference?: string,): Observable<EntityModelResponse>
}
