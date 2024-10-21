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
import { AbstractEntity } from '../model/abstractEntity';
import { SearchModel } from '../model/searchModel';
import { SearchResultsModelAbstractEntity } from '../model/searchResultsModelAbstractEntity';

export interface CRUDServiceInterface {
  /**
   * Delete an entity
   * @param entityName The name of the entity
   * @param id The ID of the entity
   */
  _delete: (entityName: string, id: string) => Observable<any>;

  /**
   * Executes an action defined in the entity model
   * @param entityName The name of the entity
   * @param actionId The id of the action to execute
   * @param body
   * @param reference Entity model reference
   * @param id ID of the entity to update (in case of update actions)
   */
  executeAction: (entityName: string, actionId: string, body: string, reference?: string, id?: string) => Observable<AbstractEntity>

  /**
   * Retrieve the details of a single entity
   * @param entityName The name of the entity
   * @param id The ID of the entity
   * @param reference The entity model reference
   */
  get: (entityName: string, id: string, reference?: string) => Observable<any>;

  /**
   * Instantiates a new entity
   * @param entityName The name of the entity
   * @param reference The entity model reference
   * @param observe set whether or not to return the data Observable as the body, response or events. defaults to returning the body.
   * @param reportProgress flag to report request and response progress.
   */
  init: (entityName: string, reference?: string, observe?: 'body', reportProgress?: boolean) => Observable<any>;

  /**
   * Retrieves a simple list of entities (without any sorting or filtering)
   * @param entityName The name of the entity
   * @param reference The entity model reference
   */
  list: (entityName: string, reference?: string) => Observable<any>;

  /**
   * Create a new entity
   * @param entityName The name of the entity
   * @param body
   * @param reference
   */
  post: (entityName: string, body: string, reference?: string) => Observable<AbstractEntity>;

  /**
   * Updates an existing entity
   * @param entityName The name of the entity
   * @param id The ID of the entity
   * @param body
   * @param reference Reference to specify the entity model to use
   */
  put: (entityName: string, id: string, body: string, reference?: string) => Observable<AbstractEntity>;

  /**
   * Executes a search request
   * @param entityName The name of the entity
   * @param searchModel
   * @param reference The entity model reference
   */
  search: (entityName: string, searchModel: SearchModel, reference?: string) => Observable<SearchResultsModelAbstractEntity>;

}
