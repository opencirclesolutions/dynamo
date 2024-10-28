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
import { Component, Input, inject } from '@angular/core';
import {
  isBoolean,
  isDate,
  isDecimal,
  isEnum,
  isFreeDetail,
  isIntegral,
  isLob,
  isMaster,
  isNestedDetail,
  isString,
  isTime,
  isInstant,
  isLocalDateTime,
  isUrl,
  isElementCollection,
  getErrorString
} from '../../functions/entitymodel-functions';
import { getLocale, prependUrl } from '../../functions/functions';
import { FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { AttributeModelResponse } from '../../interfaces/model/attributeModelResponse';

@Component({
  selector: 'd-base',
  standalone: true,
  imports: [],
  templateUrl: './base.component.html',
  styleUrl: './base.component.css'
})
export abstract class BaseComponent {
  protected translate = inject(TranslateService);

  isDate = isDate;
  isBoolean = isBoolean;
  isDecimal = isDecimal;
  isEnum = isEnum;
  isIntegral = isIntegral;
  isMaster = isMaster;
  isString = isString;
  isInstant = isInstant;
  isLocalDateTime = isLocalDateTime;
  isFreeDetail = isFreeDetail;
  isTime = isTime;
  isNestedDetail = isNestedDetail;
  isLob = isLob;
  isUrl = isUrl;
  isElementCollection = isElementCollection;
  prependUrl = prependUrl;

  getLocale = getLocale();

  @Input() public locale: string = getLocale();
  @Input() am!: AttributeModelResponse;
  @Input() formGroup?: FormGroup;
  @Input() showValidationErrors: boolean = true;
  @Input() colClass: string = '';
  @Input() validationColClass: string = '';
  @Input() fullWidth: boolean = false;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
  }

  getDigitsInfo(am?: AttributeModelResponse) {
    return `1.${am!.precision}-${am!.precision}`
  }

  getErrorString(attribute: string): string {
    return getErrorString(attribute, this.formGroup!, this.translate);
  }

  getFromName(am: AttributeModelResponse): string {
    return am.name + '_from';
  }

  getToName(am: AttributeModelResponse): string {
    return am.name + '_to';
  }

  getPlaceholderFrom(am: AttributeModelResponse) {
    return this.translate.instant('placeholder_from', {
      placeholder: am.placeholders[this.locale],
    });
  }

  getPlaceholderTo(am: AttributeModelResponse) {
    return this.translate.instant('placeholder_to', {
      placeholder: am.placeholders[this.locale],
    });
  }


  getCalendarDateFormat(format: string) {
    //return getCalendarDateFormat(format);
    // PrimeNG calendar has wonky formatting that matches neither the JavaScript nor the java format
    return 'dd-mm-yy';
  }

}
