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
import {Directive, inject, Input} from '@angular/core';
import {Router} from '@angular/router';
import {getLocale} from '../../../functions/functions';
import {FilterModel} from '../../../interfaces/model/filterModel';
import {SelectOption} from '../../../interfaces/select-option';
import {AttributeModelResponse} from '../../../interfaces/model/attributeModelResponse';
import {CRUDServiceInterface} from '../../../interfaces/service/crud.service';
import {NotificationService} from '../../../services/notification.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {DynamoConfig} from '../../../interfaces/dynamo-config';
import {ModelServiceInterface} from '../../../interfaces/service/model.service';
import {EntityModelResponse} from '../../../interfaces/model/entityModelResponse';
import {SearchModel} from '../../../interfaces/model/searchModel';
import {PagingModel} from '../../../interfaces/model/pagingModel';
import {SortModel} from '../../../interfaces/model/sortModel';
import {AdditionalGlobalAction} from '../../../interfaces/action';
import {EntityModelActionResponse} from '../../../interfaces/model/entityModelActionResponse';
import {EntityModelFunctions} from "../../../functions/entitymodel-functions";
import {TranslateService} from "@ngx-translate/core";
import {FormGroup, ValidatorFn} from "@angular/forms";
import {EqualsFilterModel} from "../../../interfaces/model/equalsFilterModel";

@Directive({
  standalone: true
})
export abstract class BaseCompositeComponent {
  protected messageService = inject(NotificationService);
  protected router = inject(Router);
  protected authService = inject(AuthenticationService);
  protected translate = inject(TranslateService);

  protected entityModelFunctions: EntityModelFunctions = new EntityModelFunctions();

  // the name of the entity to display
  @Input({required: true}) entityName: string = '';
  // optional reference to further specify which entity model to use
  @Input() entityModelReference?: string = undefined;
  // the locale to use
  @Input() public locale: string = getLocale();
  // default filters to apply when searching
  @Input() defaultFilters: FilterModel[] = [];
  // filters to apply to individual fields
  @Input() fieldFilters: Map<string, FilterModel[]> = new Map<
    string,
    FilterModel[]
  >();
  // cascading filters to apply to individual fields
  @Input() cascadeFilters: Map<string, FilterModel[]> = new Map<
    string,
    FilterModel[]
  >();

  @Input() entityModel?: EntityModelResponse;

  protected enumMap: Map<string, SelectOption[]> = new Map<
    string,
    SelectOption[]
  >();
  protected simpleEnumMap: Map<string, Map<string, string>> = new Map<
    string,
    Map<string, string>
  >();
  protected entityLists: Map<string, SelectOption[]> = new Map<
    string,
    SelectOption[]
  >();
  protected searchMode: boolean = false;
  protected attributeModels: AttributeModelResponse[] = [];

  protected service: CRUDServiceInterface
  protected entityModelService: ModelServiceInterface

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const configuration = inject<DynamoConfig>("DYNAMO_CONFIG" as any);

    this.service = configuration.getCRUDService()
    this.entityModelService = configuration.getModelService()
  }

  lookupEntities(key: string): any[] {
    return this.entityLists.get(key) || [];
  }

  protected setupEnums(model: EntityModelResponse) {
    model.attributeModels.forEach((am) => {
      if (this.entityModelFunctions.isEnum(am)) {
        let descriptions = am.enumDescriptions![this.locale];
        let values: SelectOption[] = [];
        let simpleValues = new Map<string, string>();

        Object.keys(descriptions).forEach((key) => {
          values.push({value: key, name: descriptions[key]});
          simpleValues.set(key, descriptions[key]);
        });

        values.sort((a, b) => a.name.localeCompare(b.name));
        this.enumMap.set(am.name, values);
        this.simpleEnumMap.set(am.name, simpleValues);
      }
    });
  }

  /**
   * Set up the contents of lookup (combo, multi-select, or auto-complete fields)
   * @param model the entity model
   */
  protected setupLookups(model: EntityModelResponse) {
    model.attributeModels.forEach((am) => {
      if (
        (this.entityModelFunctions.isMaster(am) || this.entityModelFunctions.isFreeDetail(am)) &&
        (this.searchMode
          ? am.searchMode == AttributeModelResponse.SearchModeEnum.ALWAYS ||
          am.searchMode == AttributeModelResponse.SearchModeEnum.ADVANCED
          : am.visibleInForm && this.isLookupEditable(am)) &&
        am.lookupEntityName &&
        this.entityModelFunctions.mustFetchListValues(am, this.searchMode)
      ) {
        this.fillOptions(am);
      }
    });
  }

  isLookupEditable(am: AttributeModelResponse) {
    return (
      am.editableType === AttributeModelResponse.EditableTypeEnum.EDITABLE ||
      am.editableType === AttributeModelResponse.EditableTypeEnum.CREATE_ONLY
    );
  }

  /**
   * Looks up the available options for a selection component
   * @param am the attribute model to base the selection on
   */
  protected fillOptions(am: AttributeModelResponse) {
    let filters: FilterModel[] = this.getCombinedFilters(am);

    if (filters && filters.length > 0) {
      // with filters
      let searchModel: SearchModel = {
        paging: {
          pageNumber: 0,
          pageSize: 100,
          type: PagingModel.TypeEnum.PAGING,
        },
        sort: {
          sortField: am.displayPropertyName!,
          sortDirection: SortModel.SortDirectionEnum.ASC,
        },
        filters: filters,
      };
      this.service
        .search(am.lookupEntityName!, searchModel, am.lookupEntityReference)
        .subscribe({
          next: (res) => {
            let temp: any[] = res.results!;
            this.handleFillOptionsResponse(temp, am);
          },
          error: (error) => this.messageService.error(error.error.message),
        });
    } else {
      // without filters (just a list)
      this.service
        .list(am.lookupEntityName!, am.lookupEntityReference)
        .subscribe({
          next: (res) => {
            let temp: any[] = res;
            this.handleFillOptionsResponse(temp, am);
          },
          error: (error) => this.messageService.error(error.error.message),
        });
    }
  }

  /**
   * Handles a response message that is intended to fill an option list
   * (e.g. for a dropdown)
   * @param temp the options from the response
   * @param am the attribute model
   */
  private handleFillOptionsResponse(temp: any[], am: AttributeModelResponse) {
    let options: SelectOption[] = [];

    temp.forEach((element) => {
      let option: SelectOption = {
        value: element.id,
        name: element[am.displayPropertyName!],
      };
      options.push(option);
    });
    this.entityLists.set(am.lookupEntityName!, options);
    this.onLookupFilled(am);
  }

  /**
   * Looks up an attribute model given the attribute name
   * @param name the name of the attribute
   * @returns the attribute model, or undefined if it cannot be found
   */
  findAttributeModel(name: string): AttributeModelResponse | undefined {
    return this.attributeModels.find((am) => am.name === name);
  }

  getCalendarDateFormat(format: string): string {
    //return getCalendarDateFormat(format);
    // PrimeNG calendar has wonky formatting that matches neither the JavaScript nor the java format
    return 'dd-mm-yy';
  }

  isShowNumberSpinnerButtons(am: AttributeModelResponse) {
    return am.includeNumberSpinnerButton === true;
  }

  getFieldFilters(am: AttributeModelResponse): FilterModel[] {
    return this.fieldFilters.get(am.name) || [];
  }

  getCascadeFilters(am: AttributeModelResponse): FilterModel[] {
    return this.cascadeFilters.get(am.name) || [];
  }

  /**
   * Returns the combination of field filters and cascade filters for
   * a certain attribute model
   * @param am  the attribute model
   * @returns the array of filters
   */
  getCombinedFilters(am: AttributeModelResponse): FilterModel[] {
    let fieldFilters = this.getFieldFilters(am);
    let cascadeFilters = this.getCascadeFilters(am);

    let filters: FilterModel[] = [];
    fieldFilters.forEach((fieldFilter) => filters.push(fieldFilter));
    cascadeFilters.forEach((cascadeFilter) => filters.push(cascadeFilter));
    return filters;
  }

  /**
   * Executes a global action
   * @param action the global action
   */
  callAdditionalGlobalAction(action: AdditionalGlobalAction) {
    action.action();
  }

  /**
   * Determines whether a global action is disabled
   * @param action the action
   * @returns true if the action is disabled, false otherwise
   */
  isGlobalActionDisabled(action: AdditionalGlobalAction): boolean {
    if (!action.enabled) {
      return false;
    }
    return !action.enabled();
  }

  /**
   * Determines whether read-only access to the component is allowed
   * @returns true if this the case, false otherwise
   */
  isReadAllowed(): boolean {
    if (!this.entityModel) {
      return false;
    }

    if (!this.entityModel.readRoles || this.entityModel.readRoles.length == 0) {
      return true;
    }

    return this.authService.hasRole(this.entityModel.readRoles);
  }

  /**
   * Determines whether deletion functionality for the component is enabled
   * @returns true if this is the case, false otherwise
   */
  isDeleteAllowed(): boolean {
    if (!this.entityModel) {
      return false;
    }

    if (
      !this.entityModel.deleteRoles ||
      this.entityModel.deleteRoles.length == 0
    ) {
      return true;
    }

    return this.authService.hasRole(this.entityModel.deleteRoles);
  }

  /**
   * Determines whether deletion functionality for the component is enabled
   * @returns true if this is the case, false otherwise
   */
  isWriteAllowed(): boolean {
    if (!this.entityModel) {
      return false;
    }

    if (
      !this.entityModel.writeRoles ||
      this.entityModel.writeRoles.length == 0
    ) {
      return true;
    }
    return this.authService.hasRole(this.entityModel.writeRoles);
  }

  /**
   * Determines whether deletion functionality for the component is enabled
   * @returns true if this is the case, false otherwise
   */
  isActionAllowed(action: EntityModelActionResponse): boolean {
    if (!action.roles || action.roles.length == 0) {
      return true;
    }
    return this.authService.hasRole(action.roles);
  }

  /**
   * Determines whether an attribute model has a non-empty "group together with" list
   * @param am the attribute model
   * @returns true if this is the case, false otherwise
   */
  hasGroupTogetherWith(am: AttributeModelResponse) {
    return am.groupTogetherWith && am.groupTogetherWith.length > 0;
  }

  getEnumValues(name: string): SelectOption[] {
    return this.enumMap.get(name)!;
  }

  getEnumMap(am: AttributeModelResponse): Map<string, string> {
    return this.simpleEnumMap.get(am.name)!;
  }

  /**
   * Determines the column class to use for grouped together attributes
   * @param am the attribute model on which the grouped together components are defined
   * @returns the column class
   */
  getGroupTogetherColClass(am: AttributeModelResponse) {
    if (!am.groupTogetherWith) {
      return '';
    }
    switch (am.groupTogetherWith!.length) {
      case 1:
        return 'col-lg-6 col-md-6 col-sm-12';
      default:
        return 'col-lg-4 col-md-6 col-sm-12';
    }
  }

  /**
   * Checks whether an attribute is grouped with another attribute
   * @param attributeModels the collection of all attribute models
   * @param am the attribute
   * @returns true if this is the case, false otherwise
   */
  isGroupedWithOther(
    attributeModels: AttributeModelResponse[],
    am: AttributeModelResponse
  ): boolean {
    return attributeModels.filter(
      (other) =>
        other.groupTogetherWith &&
        other.groupTogetherWith.indexOf(am.name) >= 0
    ).length > 0;
  }

  /**
   * Returns a list of attribute models that are not grouped with other
   * @param attributeModels the attribute models
   * @returns the list of attribute models
   */
  getUngroupedAttributeModels(attributeModels: AttributeModelResponse[]) {
    return attributeModels.filter(
      (am) => !this.isGroupedWithOther(attributeModels, am)
    );
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

  mustFetchListValues(am: AttributeModelResponse, search: boolean): boolean {
    return this.entityModelFunctions.mustFetchListValues(am, search);
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

  protected abstract onLookupFilled(am: AttributeModelResponse): void;
}
