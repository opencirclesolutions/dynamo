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
import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { truncateDescriptions } from '../functions/entitymodel-functions';

const MAX_ITEMS = 3;

/**
 * Pipe for translating an entity to its display property value
 */
@Pipe({
  name: 'translateEntity',
})
export class TranslateEntityPipe implements PipeTransform {

  constructor(private translate: TranslateService) {

  }

  transform(obj: any, displayProperty: string): string {

    if (!obj) {
      return '';
    }

    if (Array.isArray(obj)) {
      return truncateDescriptions(obj,displayProperty, this.translate)
    }

    return obj[displayProperty] || this.translate.instant('display_unknown')
  }

}
