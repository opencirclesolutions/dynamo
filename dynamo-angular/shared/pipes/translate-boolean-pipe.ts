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

/**
 * Pipe for translation a boolean value to its string representation
 */
@Pipe({
  name: 'translateBoolean',
})
export class TranslateBooleanPipe implements PipeTransform {
  constructor() {}

  transform(value: boolean, locale: string, trueRepresentations: {[key: string]: string},
      falseRepresentations: {[key: string]: string}): string | undefined {

    if (value === null || value === undefined) {
      return '';
    }

    if (value === true) {
      if (trueRepresentations[locale]) {
        return trueRepresentations[locale]
      }
      return 'true'
    }

    if (value === false) {
      if (falseRepresentations[locale]) {
        return falseRepresentations[locale]
      }
      return 'false'
    }
    return 'fallback';
  }

}
