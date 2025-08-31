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
import {Injectable} from '@angular/core';
import {AbstractControl} from '@angular/forms';
import {
  timeToDate,
  timestampToDate,
  getNestedValue,
} from '../functions/functions';
import {AttributeModelResponse} from '../interfaces/model/attributeModelResponse';
import {EntityModelFunctions} from "../functions/entitymodel-functions";

@Injectable({
  providedIn: 'root',
})
export class BindingService {

  private entityModelFunctions: EntityModelFunctions = new EntityModelFunctions();

  /**
   * Binds a value from an entity to a field
   * @param am the attribute model
   * @param control the control
   * @param editObject the object being edited
   * @param enumMap mapping from attribute name to enum values
   */
  public bindField(
    am: AttributeModelResponse,
    control: AbstractControl<any, any>,
    editObject: any,
    enumMap: Map<string, any[]>
  ) {
    if (
      this.entityModelFunctions.isString(am) ||
      this.entityModelFunctions.isDecimal(am) ||
      this.entityModelFunctions.isIntegral(am) ||
      this.entityModelFunctions.isBoolean(am) ||
      this.entityModelFunctions.isElementCollection(am)
    ) {
      // no special conversion required
      control.patchValue(getNestedValue(editObject, am.name));
    } else if (this.entityModelFunctions.isEnum(am)) {
      // enum value, look up in map
      let val = getNestedValue(editObject, am.name);
      let match: any = enumMap
        .get(am.name)!
        .find((option) => option.value === val);
      control.patchValue(match);
    } else if (this.entityModelFunctions.isDate(am)) {
      let val = getNestedValue(editObject, am.name);
      if (val) {
        control.patchValue(new Date(val));
      } else {
        control.reset();
      }
    } else if (this.entityModelFunctions.isTime(am)) {
      let val = getNestedValue(editObject, am.name) as string;
      if (val) {
        control.patchValue(timeToDate(val));
      } else {
        control.reset();
      }
    } else if (this.entityModelFunctions.isInstant(am) || this.entityModelFunctions.isLocalDateTime(am)) {
      let val = getNestedValue(editObject, am.name) as string;
      if (val) {
        control.patchValue(timestampToDate(val, this.entityModelFunctions.isInstant(am)));
      } else {
        control.reset();
      }
    } else if (this.entityModelFunctions.isNestedDetail(am)) {
      control.patchValue(getNestedValue(editObject, am.name));
    } else if (
      this.entityModelFunctions.isMaster(am) ||
      (this.entityModelFunctions.isFreeDetail(am) &&
        am.selectMode == AttributeModelResponse.SelectModeEnum.LOOKUP)
    ) {
      this.bindLookupField(am, control, editObject);
    }
  }

  /**
   * Binds the values for a lookup field - this translates the entities
   * to an array of objects containing a value and a name
   * @param am the attribute model
   * @param control the lookup field
   * @param editObject the object being edited
   */
  private bindLookupField(
    am: AttributeModelResponse,
    control: AbstractControl<any, any>,
    editObject: any
  ) {
    let val = getNestedValue(editObject, am.name)
    if (val) {
      if (Array.isArray(val)) {
        let mappedValues: any[] = [];
        (val as any[]).forEach((element) => {
          mappedValues.push({
            value: element.id,
            name: element[am.displayPropertyName!],
          });
        });
        control.patchValue(mappedValues);
      } else {
        let mappedValue = {
          value: val.id,
          name: val[am.displayPropertyName!],
        };
        control.patchValue(mappedValue);
      }
    }
  }
}
