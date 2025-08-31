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
import {Component, Input, inject, Directive} from '@angular/core';
import { getLocale, prependUrl } from '../../functions/functions';
import {FormGroup, ValidatorFn} from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { AttributeModelResponse } from '../../interfaces/model/attributeModelResponse';
import {EntityModelFunctions} from "../../functions/entitymodel-functions";
import {EqualsFilterModel} from "../../interfaces/model/equalsFilterModel";

@Directive({
  selector: 'd-base',
  standalone: true
})
export abstract class BaseComponent {
  protected translate = inject(TranslateService);
  protected entityModelFunctions = new EntityModelFunctions();

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

  isString(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isString(am);
  }

  isDate(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isDate(am);
  }

  isInstant(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isInstant(am);
  }

  isLocalDateTime(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isLocalDateTime(am);
  }

  isTime(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isTime(am);
  }

  isEnum(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isEnum(am);
  }

  isIntegral(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isIntegral(am);
  }

  isBoolean(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isBoolean(am);
  }

  isDecimal(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isDecimal(am);
  }

  isMaster(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isMaster(am);
  }

  isFreeDetail(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isFreeDetail(am);
  }

  isNestedDetail(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isNestedDetail(am);
  }

  isBasic(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isBasic(am);
  }

  isElementCollection(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isElementCollection(am);
  }

  isLob(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isLob(am);
  }

  isUrl(am?: AttributeModelResponse): boolean {
    return this.entityModelFunctions.isUrl(am);
  }

  setNestedValue(obj: any, am: AttributeModelResponse, value: any) {
    this.entityModelFunctions.setNestedValue(obj, am, value);
  }

  createValidators(am: AttributeModelResponse, elementCollectionPopup: boolean, searchMode: boolean): ValidatorFn[] {
    return this.entityModelFunctions.createValidators(this.translate, am, elementCollectionPopup, searchMode);
  }

  createEqualsFilter(attributeName: string, value: any): EqualsFilterModel {
    return this.entityModelFunctions.createEqualsFilter(attributeName, value);
  }

  getErrorString(attribute: string): string {
    return this.entityModelFunctions.getErrorString(attribute, this.formGroup!, this.translate);
  }
}
