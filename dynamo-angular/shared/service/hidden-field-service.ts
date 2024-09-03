import { Injectable } from '@angular/core';

/**
 * A service for passing
 */
@Injectable(
)
export class HiddenFieldService {

  private fieldValues: Map<string,any> = new Map<string,any>();

  constructor() { }

  setFieldValue(attribute: string, value?: any) {
    this.fieldValues.set(attribute, value);
  }

  getFieldValues(): Map<string,any> {
    return this.fieldValues
  }

}
