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
import { Inject, Pipe, PipeTransform } from '@angular/core';
import { Observable, map, of } from 'rxjs';
import { DynamoConfig } from '../interfaces/dynamo-config';
import { CRUDServiceInterface } from '../interfaces/service/cRUD.service';

/**
 * Pipe for translating an entity to its display property value
 */
@Pipe({
  name: 'lookupEntity',
})
export class LookupEntityPipe implements PipeTransform {
  service: CRUDServiceInterface

  constructor(
    @Inject("DYNAMO_CONFIG") configuration: DynamoConfig,
  ) {
    this.service = configuration.getCRUDService()
  }

  transform(
    obj: any,
    entityName: string,
    displayProperty: string
  ): Observable<string | undefined> {
    if (!obj) {
      return of('');
    }

    return this.service
      .get(entityName, obj['id'])
      .pipe(map((entity) => entity[displayProperty]));
  }
}
