import { Injectable } from '@angular/core';
import { AttributeModelResponse, FilterModel, TimeRangeFilterModel } from 'dynamo/model';
import { createDateRangeFilter, createEqualsFilter, createNumberInFilter, createNumberRangeFilter, createTimeRangeFilter, createTimestampFilter, isBoolean, isDecimal, isEnum, isFreeDetail, isInstant, isIntegral, isLocalDateTime, isMaster, isString, isTime } from '../functions/entitymodel-functions';
import { isDate } from 'date-fns';
import { dateToString } from '../functions/functions';
import { SelectOption } from '../model/select-option';

@Injectable({
  providedIn: 'root'
})
export class CreateFilterService {

  constructor() { }

  /**
   * Constructs the search filters based on the entity model
   * @returns an array containing the filters that were constructed
   */
  public createFilters(searchObject: any, searchAttributeModels: AttributeModelResponse[],
    defaultFilters: FilterModel[]
  ): FilterModel[] {
    let filters: FilterModel[] = [];
    searchAttributeModels.forEach((am) => {

      if (
        !am.ignoreInSearchFilter &&
        (searchObject[am.name] ||
        searchObject[this.getFromName(am)] ||
        searchObject[this.getToName(am)])
      ) {
        if (isString(am)) {
          let filter = createEqualsFilter(am.name, searchObject[am.name]);
          filters.push(filter);
        } else if (isEnum(am)) {
          let filter = createEqualsFilter(
            am.name,
            searchObject[am.name].value
          );
          filters.push(filter);
        } else if (isIntegral(am)) {
          if (am.searchForExactValue === true) {
            let filter = createEqualsFilter(
              am.name,
              searchObject[am.name]
            );
            filters.push(filter);
          } else {
            if (
              searchObject[this.getFromName(am)] ||
              searchObject[this.getToName(am)]
            ) {
              let filter = createNumberRangeFilter(
                am.name,
                searchObject[this.getFromName(am)],
                searchObject[this.getToName(am)]
              );
              filters.push(filter);
            }
          }
        } else if (isDecimal(am)) {
          if (
            searchObject[this.getFromName(am)] ||
            searchObject[this.getToName(am)]
          ) {
            let filter = createNumberRangeFilter(
              am.name,
              searchObject[this.getFromName(am)],
              searchObject[this.getToName(am)]
            );
            filters.push(filter);
          }
        } else if (isDate(am)) {
          this.createDateFilter(searchObject, am, filters);
        } else if (isMaster(am)) {
          // single select
          if (am.multipleSearch) {
            let selectedOptions: SelectOption[] = searchObject[am.name];
            if (selectedOptions && selectedOptions.length > 0) {
              let mapped = selectedOptions.map((option) => option.value);
              let filter = createNumberInFilter(am.name, mapped);
              filters.push(filter);
            }
          } else {
            let filter = createEqualsFilter(
              am.name,
              searchObject[am.name].value
            );
            filters.push(filter);
          }
        } else if (isFreeDetail(am)) {
          // multiple select
          let selectedOptions: SelectOption[] = searchObject[am.name];
          if (selectedOptions && selectedOptions.length > 0) {
            let mapped = selectedOptions.map((option) => option.value);
            let filter = createNumberInFilter(am.name, mapped);
            filters.push(filter);
          }
        } else if (isInstant(am) || isLocalDateTime(am)) {
          this.createTimestampFilter(searchObject, am, filters);
        } else if (isTime(am)) {
          this.createTimeFilter(searchObject, am, filters);
        }
      }

      // special case for boolean (explicitly check for TRUE and FALSE)
      if (isBoolean(am)) {
        let value = searchObject[am.name];
        if (value === true || value === false) {
          let filter = createEqualsFilter(am.name, value);
          filters.push(filter);
        }
      }
    });

    if (defaultFilters) {
      defaultFilters.forEach((defaultFilter) => {
        filters.push(defaultFilter);
      });
    }
    console.log('Filters ' + JSON.stringify(filters));

    return filters;
  }

  createDateFilter(searchObject: any, am: AttributeModelResponse, filters: FilterModel[]): void {
    if (am.searchForExactValue === true) {
      let dateStr: any = dateToString(searchObject[am.name]);
      let filter = createEqualsFilter(am.name, dateStr);
      filters.push(filter);
    } else if (
      searchObject[this.getFromName(am)] ||
      searchObject[this.getToName(am)]
    ) {
      // upper bound, lower bound, or both specified
      let from = searchObject[this.getFromName(am)];
      let to = searchObject[this.getToName(am)];
      filters.push(createDateRangeFilter(am.name, from as Date, to as Date));
    }
  }

  createTimeFilter(searchObject: any, am: AttributeModelResponse, filters: FilterModel[]) {
    if (
      searchObject[this.getFromName(am)] ||
      searchObject[this.getToName(am)]
    ) {
      let from = searchObject[this.getFromName(am)];
      let to = searchObject[this.getToName(am)];

      let filter: TimeRangeFilterModel = createTimeRangeFilter(
        am.name,
        from,
        to
      );
      filters.push(filter);
    }
  }

  createTimestampFilter(searchObject: any, am: AttributeModelResponse, filters: FilterModel[]): void {
    if (am.searchDateOnly === true) {
      let dateStr: any = dateToString(searchObject[am.name]);
      let filter = createEqualsFilter(am.name, dateStr);
      filters.push(filter);
    } else {
      if (
        searchObject[this.getFromName(am)] ||
        searchObject[this.getToName(am)]
      ) {
        // upper bound, lower bound, or both specified
        let from = searchObject[this.getFromName(am)];
        let to = searchObject[this.getToName(am)];
        let filter = createTimestampFilter(
          am.name,
          from,
          to,
          isInstant(am)
        );
        filters.push(filter);
      }
    }
  }

  getFromName(am: AttributeModelResponse): string {
    return am.name + '_from';
  }

  getToName(am: AttributeModelResponse): string {
    return am.name + '_to';
  }
}
