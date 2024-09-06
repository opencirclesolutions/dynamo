import { FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import {
  AttributeModelResponse,
  DateRangeFilterModel,
  ElementCollectionFilterModel,
  EqualsFilterModel,
  FilterModel,
  InstantRangeFilterModel,
  LocalDateTimeRangeFilterModel,
  NotFilterModel,
  NumberInFilterModel,
  NumberRangeFilterModel,
  OrFilterModel,
  PagingModel,
  SearchModel,
  TimeRangeFilterModel,
} from 'dynamo/model';
import { dateToString, dateToTimestamp, stringToTime } from './functions';
import { DynamoValidators } from '../validators/validators';

// maximum number of items to display in description of collection
const MAX_ITEMS = 3;

export function isString(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.STRING
  );
}

export function isDate(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.DATE
  );
}

export function isInstant(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.INSTANT
  );
}

export function isLocalDateTime(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.LOCAL_DATE_TIME
  );
}

export function isTime(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.TIME
  );
}

export function isEnum(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.ENUM
  );
}

export function isIntegral(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.INTEGRAL
  );
}

export function isBoolean(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.BOOL
  );
}

export function isDecimal(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.DECIMAL
  );
}

export function isMaster(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.MASTER
  );
}

export function isFreeDetail(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ==
    AttributeModelResponse.AttributeModelDataTypeEnum.MANY_TO_MANY
  );
}

export function isNestedDetail(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ==
    AttributeModelResponse.AttributeModelDataTypeEnum.ONE_TO_MANY
  );
}

export function isUrl(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return isString(am) && am!.url === true;
}

export function isBasic(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType !=
      AttributeModelResponse.AttributeModelDataTypeEnum.MANY_TO_MANY &&
    am.attributeModelDataType !=
      AttributeModelResponse.AttributeModelDataTypeEnum.ONE_TO_MANY &&
    am.attributeModelDataType !=
      AttributeModelResponse.AttributeModelDataTypeEnum.MASTER
  );
}

export function isElementCollection(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ===
    AttributeModelResponse.AttributeModelDataTypeEnum.ELEMENT_COLLECTION
  );
}

export function isLob(am?: AttributeModelResponse): boolean {
  if (!am) {
    return false;
  }

  return (
    am.attributeModelDataType ==
    AttributeModelResponse.AttributeModelDataTypeEnum.LOB
  );
}

/**
 *
 * @param am the attribute model
 * @param search whether we are dealing with a search component
 * @returns whether a list of values for filling a dropdown must be retrieved
 */
export function mustFetchListValues(
  am: AttributeModelResponse,
  search: boolean
) {
  if (search) {
    return am.searchSelectMode !== AttributeModelResponse.SelectModeEnum.LOOKUP;
  } else {
    return am.selectMode !== AttributeModelResponse.SelectModeEnum.LOOKUP;
  }
}

/**
 * Sets a nested attribute value on the specified attribute
 * @param obj  the object
 * @param am the attribute model
 * @param value  the value to set
 */
export function setNestedValue(
  obj: any,
  am: AttributeModelResponse,
  value: any
) {
  let name = am.name;

  let p = name.indexOf('.');
  while (p >= 0) {
    let part = name.substring(0, p);
    obj = obj[part];
    name = name.substring(p + 1);
    p = name.indexOf('.');
  }

  if (obj) {
    obj[name] = value;
  }
}

/**
 * Creates the validators for an input component
 * @param am the attribute model to base the validators on
 * @param elementCollectionPopup whether the component is inside an element collection popup
 * @returns the array of validators that was created
 */
export function createValidators(
  translate: TranslateService,
  am: AttributeModelResponse,
  elementCollectionPopup: boolean,
  searchMode: boolean
): ValidatorFn[] {
  let validators: ValidatorFn[] = [];
  if (am.required) {
    validators.push(Validators.required);
  }

  if (isString(am)) {
    if (am.maxLength) {
      validators.push(Validators.maxLength(am.maxLength));
    }
    if (am.minLength) {
      validators.push(Validators.minLength(am.minLength));
    }
    if (am.email === true) {
      validators.push(Validators.email);
    }

    if (am.url === true) {
      validators.push(new DynamoValidators(translate).urlValidator());
    }
  }

  if (isFreeDetail(am) || isNestedDetail(am)) {
    if (am.maxCollectionSize) {
      validators.push(Validators.maxLength(am.maxCollectionSize));
    }
    if (am.minCollectionSize) {
      validators.push(Validators.minLength(am.minCollectionSize));
    }
  }

  // validations specific for element collection dialog
  let elementCollectionDialog =
    isElementCollection(am) &&
    am.elementCollectionMode ===
      AttributeModelResponse.ElementCollectionModeEnum.DIALOG;

  if (isElementCollection(am) && !elementCollectionPopup) {
    validators.push(new DynamoValidators(translate).isArrayValidator());
  }

  if (!searchMode && elementCollectionDialog && elementCollectionPopup) {
    if (am.maxLength) {
      validators.push(Validators.maxLength(am.maxLength));
    }
    if (am.minLength) {
      validators.push(Validators.minLength(am.minLength));
    }
  }

  if (
    isIntegral(am) ||
    isDecimal(am) ||
    (elementCollectionDialog && !searchMode)
  ) {
    if (am.minValue) {
      validators.push(Validators.min(am.minValue));
    }
    if (am.maxValue) {
      validators.push(Validators.max(am.maxValue));
    }
  }

  // specific validator for collection size for element collections
  if (elementCollectionDialog && !elementCollectionPopup) {
    if (am.maxCollectionSize) {
      validators.push(Validators.maxLength(am.maxCollectionSize));
    }
    if (am.minCollectionSize) {
      validators.push(Validators.minLength(am.minCollectionSize));
    }
  }

  return validators;
}

/**
 *  Creates a  search model for searching on the provided attribute, with the provided value
 * @param attributeName the name of the attribute
 * @param value the value
 * @returns the search model
 */
export function createBasicSearchModel(attributeName: string, value: any) {
  let filter: EqualsFilterModel = createEqualsFilter(attributeName, value);
  let model: SearchModel = {
    paging: {
      pageNumber: 0,
      pageSize: 10,
      type: PagingModel.TypeEnum.PAGING,
    },
    sort: {
      sortField: 'id',
      sortDirection: 'ASC',
    },
    filters: [filter],
  };
  return model;
}

export function createEmptySearchModel() {
  let model: SearchModel = {
    paging: {
      pageNumber: 0,
      pageSize: 10,
      type: PagingModel.TypeEnum.PAGING,
    },
    sort: {
      sortField: 'id',
      sortDirection: 'ASC',
    },
  };
  return model;
}

/**
 * Creates a filter for filtering on a single attribute value
 * @param attributeName the name of the attribute to filter on
 * @param value the value to filter on
 * @returns the filter
 */
export function createEqualsFilter(
  attributeName: string,
  value: any
): EqualsFilterModel {
  let filter: EqualsFilterModel = {
    match: 'EQUALS',
    name: attributeName,
    value: value,
  };
  return filter;
}

/**
 * Creates a filter that matches if any of the provided filters match
 * @param filters the filters
 * @returns the filter
 */
export function createOrFilter(filters: FilterModel[]): OrFilterModel {
  let filter: OrFilterModel = {
    match: 'OR',
    orFilters: filters,
  };
  return filter;
}

/**
 * Creates a filter for filtering on the negation of the provided filter
 * @param not the filter to negate
 * @returns the filter
 */
export function createNotFilter(filter: FilterModel): NotFilterModel {
  let notFilter: NotFilterModel = {
    match: 'NOT',
    filter: filter,
  };
  return notFilter;
}

/**
 * Creates a filter for filtering on a numeric range
 * @param attributeName the name of the attribute to filter on
 * @param from the lower value of the range
 * @param to the upper value of the range
 * @returns the filter
 */
export function createNumberRangeFilter(
  attributeName: string,
  from: any,
  to: any
): NumberRangeFilterModel {
  let filter: NumberRangeFilterModel = {
    match: 'NUMBER_RANGE',
    name: attributeName,
    from: from,
    to: to,
  };
  return filter;
}

/**
 * Creates a filter for filtering on a date range
 * @param attributeName the name of the attribute to filter on
 * @param from the lower value of the range
 * @param to the upper value of the range
 * @returns the filter
 */
export function createDateRangeFilter(
  attributeName: string,
  from: Date,
  to: Date
): DateRangeFilterModel {
  let dateStrFrom: any = dateToString(from);
  let dateStrTo: any = dateToString(to);

  let filter: DateRangeFilterModel = {
    match: 'DATE_RANGE',
    name: attributeName,
    from: dateStrFrom,
    to: dateStrTo,
  };
  return filter;
}

/**
 * Creates a filter for filtering on a time stamp range
 * @param attributeName the name of the attribute to filter on
 * @param from the lower value of the range
 * @param to the upper value of the range
 * @returns the filter
 */
export function createTimestampFilter(
  attributeName: string,
  from: Date,
  to: Date,
  instant: boolean
): InstantRangeFilterModel {
  let dateStrFrom = dateToTimestamp(from, instant);
  let dateStrTo = dateToTimestamp(to, instant);

  if (instant) {
    let filter: InstantRangeFilterModel = {
      match: 'INSTANT_RANGE',
      name: attributeName,
      from: dateStrFrom,
      to: dateStrTo,
    };
    return filter;
  } else {
    let filter: LocalDateTimeRangeFilterModel = {
      match: 'LOCAL_DATE_TIME_RANGE',
      name: attributeName,
      from: dateStrFrom,
      to: dateStrTo,
    };
    return filter;
  }
}

/**
 * Creates a filter for filtering on an array of numbers
 * @param attributeName the name of the attribute to filter on
 * @param ids the IDs
 * @returns the filter
 */
export function createNumberInFilter(
  attributeName: string,
  ids: any[]
): NumberRangeFilterModel {
  let filter: NumberInFilterModel = {
    match: 'NUMBER_IN',
    name: attributeName,
    values: ids,
  };
  return filter;
}

/**
 * Creates a filter for filtering on a time stamp range
 * @param attributeName the name of the attribute to filter on
 * @param from the lower value of the range
 * @param to the upper value of the range
 * @returns the filter
 */
export function createTimeRangeFilter(
  attributeName: string,
  from: any,
  to: any
): TimeRangeFilterModel {
  let dateStrFrom: any = stringToTime(from);
  let dateStrTo: any = stringToTime(to);

  let filter: TimeRangeFilterModel = {
    match: 'TIME_RANGE',
    name: attributeName,
    from: dateStrFrom,
    to: dateStrTo,
  };
  return filter;
}

/**
 * Creates a filter for filtering on an array of numbers
 * @param attributeName the name of the attribute to filter on
 * @param ids the IDs
 * @returns the filter
 */
export function createElementCollectionFilter(
  am: AttributeModelResponse,
  vals: any[]
): ElementCollectionFilterModel {
  vals = vals.map((val) => {
    if (
      am.elementCollectionType ===
      AttributeModelResponse.ElementCollectionTypeEnum.INTEGRAL
    ) {
      return parseInt(val);
    }
    return val;
  });

  let filter: ElementCollectionFilterModel = {
    match: 'ELEMENT_COLLECTION',
    name: am.name,
    values: vals,
  };
  return filter;
}

/**
 * Truncates the descriptions of a collection of items
 * @param items the items
 * @param property the property that holds the description
 * @param translate the translate service
 * @returns the resulting string
 */
export function truncateDescriptions(
  items: any[],
  property: string,
  translate: TranslateService
) {
  if (items.length > MAX_ITEMS) {
    let surplus = items.length - MAX_ITEMS;
    let firstItems = items.slice(0, MAX_ITEMS);
    return (
      firstItems
        .map((val) => val[property] || translate.instant('display_unknown'))
        .join(', ') +
      ' ' +
      translate.instant('and_others', { surplus: surplus })
    );
  } else {
    return items
      .map((val) => val[property] || translate.instant('display_unknown'))
      .join(', ');
  }
}

/**
 * Returns a string describing the validation errors for an attribute
 * @param attribute the name of the attribute
 * @param formGroup the form group
 * @return the resulting string
 */
export function getErrorString(
  attribute: string,
  formGroup: FormGroup,
  translate: TranslateService
): string {
  if (!formGroup || !formGroup.controls) {
    return '';
  }

  let control = formGroup.controls[attribute]!;
  if (!control || !control.errors || !control.touched) {
    return '';
  }

  let errors: string[] = [];
  if (control.hasError('required')) {
    errors.push(translate.instant('value_required'));
  }
  if (control.hasError('maxlength')) {
    errors.push(
      translate.instant('maximum_length', {
        maxLength: control.getError('maxlength').requiredLength,
      })
    );
  }
  if (control.hasError('minlength')) {
    errors.push(
      translate.instant('minimum_length', {
        minLength: control.getError('minlength').requiredLength,
      })
    );
  }
  if (control.hasError('min')) {
    errors.push(
      translate.instant('minimum_value', {
        minValue: control.getError('min').min,
      })
    );
  }
  if (control.hasError('max')) {
    errors.push(
      translate.instant('maximum_value', {
        maxValue: control.getError('max').max,
      })
    );
  }

  if (control.hasError('email')) {
    errors.push(translate.instant('invalid_email'));
  }

  if (control.hasError('url')) {
    errors.push(translate.instant('invalid_url'));
  }

  if (control.hasError('custom')) {
    errors.push(translate.instant(control.getError('custom').value));
  }

  return errors.join(', ');
}
