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
import { AttributeModelResponse } from './attributeModelResponse';
import { AttributeGroupResponse } from './attributeGroupResponse';
import { EntityModelActionResponse } from './entityModelActionResponse';


export interface EntityModelResponse {
  attributeModels: Array<AttributeModelResponse>;
  attributeGroups: Array<AttributeGroupResponse>;
  attributeNamesOrdered: Array<string>;
  attributeNamesOrderedForGrid: Array<string>;
  attributeNamesOrderedForSearch: Array<string>;
  descriptions: { [key: string]: string; };
  displayNames: { [key: string]: string; };
  displayNamesPlural: { [key: string]: string; };
  displayProperty?: string;
  reference?: string;
  sortProperty: string;
  sortAscending: boolean;
  deleteAllowed: boolean;
  listAllowed: boolean;
  createAllowed: boolean;
  updateAllowed: boolean;
  exportAllowed: boolean;
  actions?: Array<EntityModelActionResponse>;
  readRoles?: Array<string>;
  writeRoles?: Array<string>;
  deleteRoles?: Array<string>;
}

