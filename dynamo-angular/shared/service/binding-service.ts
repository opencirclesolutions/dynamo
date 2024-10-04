import { Injectable } from '@angular/core';
import { AttributeModelResponse } from 'dynamo/model';
import { AbstractControl } from '@angular/forms';
import {
  isBoolean,
  isDate,
  isDecimal,
  isElementCollection,
  isEnum,
  isFreeDetail,
  isInstant,
  isIntegral,
  isLob,
  isLocalDateTime,
  isMaster,
  isNestedDetail,
  isString,
  isTime,
  mustFetchListValues,
} from '../functions/entitymodel-functions';
import {
  getSimpleLocale,
  timeToDate,
  timestampToDate,
  getNestedValue,
} from '../functions/functions';

/**
 *
 */
@Injectable()
export class BindingService {
  isDate = isDate;
  isBoolean = isBoolean;
  isDecimal = isDecimal;
  isEnum = isEnum;
  isIntegral = isIntegral;
  isMaster = isMaster;
  isString = isString;
  isLocalDateTime = isLocalDateTime;
  isInstant = isInstant;
  isFreeDetail = isFreeDetail;
  isTime = isTime;
  isNestedDetail = isNestedDetail;
  isLob = isLob;
  getSimpleLocale = getSimpleLocale;
  mustFetchListValues = mustFetchListValues;
  isElementCollection = isElementCollection;

  constructor() {}

  /**
   * Binds a value from an entity to a field
   * @param am the attribute model
   * @param control the control
   */
  public bindField(
    am: AttributeModelResponse,
    control: AbstractControl<any, any>,
    editObject: any,
    enumMap: Map<string, any[]>
  ) {
    if (
      this.isString(am) ||
      this.isDecimal(am) ||
      this.isIntegral(am) ||
      this.isBoolean(am) ||
      this.isElementCollection(am)
    ) {
      // no special conversion required
      control.patchValue(getNestedValue(editObject, am.name));
    } else if (this.isEnum(am)) {
      // enum value, look up in map
      let val = getNestedValue(editObject, am.name);
      let match: any = enumMap
        .get(am.name)!
        .find((option) => option.value === val);
      control.patchValue(match);
    } else if (this.isDate(am)) {
      let val = getNestedValue(editObject, am.name);
      if (val) {
        control.patchValue(new Date(val));
      } else {
        control.reset();
      }
    } else if (this.isTime(am)) {
      let val = getNestedValue(editObject, am.name) as string;
      if (val) {
        control.patchValue(timeToDate(val));
      } else {
        control.reset();
      }
    } else if (this.isInstant(am) || this.isLocalDateTime(am)) {
      let val = getNestedValue(editObject, am.name) as string;
      if (val) {
        control.patchValue(timestampToDate(val, this.isInstant(am)));
      } else {
        control.reset();
      }
    } else if (this.isNestedDetail(am)) {
      control.patchValue(getNestedValue(editObject, am.name));
    } else if (
      this.isMaster(am) ||
      (this.isFreeDetail(am) &&
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
