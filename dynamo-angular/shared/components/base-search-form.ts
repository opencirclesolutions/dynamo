import { Component, ContentChildren, EventEmitter, Input, Output, QueryList, TemplateRef } from '@angular/core';
import { BaseCompositeComponent } from './base-composite.component';
import {
  AttributeModelResponse,
  CRUDService,
  ModelService,
  EntityModelResponse,
} from 'dynamo/model';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidatorFn,
} from '@angular/forms';
import { NotificationService } from '../service/notification-service';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmService } from '../service/confirm.service';
import { parseISO } from 'date-fns';
import { timeToDate } from '../functions/functions';
import { FormInfo } from '../model/form-info';
import { SearchFormStateService } from '../service/search-form-state.service';
import { AuthenticationService } from '../service/authentication-service';
import { OverrideFieldDirective } from '../directives/override-field-directive';
import { DynamoValidators } from '../validators/validators';

/**
 * Basic component for search forms
 */
@Component({
  selector: 'app-base-search',
  template: ``,
  styles: [],
})
export abstract class BaseSearchComponent extends BaseCompositeComponent {

  @Input() injectedCustomInputs?: QueryList<OverrideFieldDirective>;

  @Input() stateStoreKey?: string = undefined;
  @Input() nested: boolean = false;
  @Input() loading: boolean = false;
  @Input() confirmClear: boolean = false;
  // whether to preserve the search terms between page visits
  @Input() preserveSearchTerms: boolean = true;

  @Output() onSearch: EventEmitter<any> = new EventEmitter();
  @Output() onReset: EventEmitter<any> = new EventEmitter();
  @Output() afterSearchFormBuilt = new EventEmitter<FormInfo>();

  protected searchObject: any = {};
  protected searchAttributeModels: AttributeModelResponse[] = [];
  protected mainForm?: FormGroup;
  protected formBuilder: FormBuilder;
  protected translate: TranslateService;
  protected changing: boolean = false;
  protected stateService: SearchFormStateService;

  @ContentChildren(OverrideFieldDirective, { descendants: true })
  customInputs!: QueryList<OverrideFieldDirective>;

  constructor(
    service: CRUDService,
    entityModelService: ModelService,
    messageService: NotificationService,
    router: Router,
    formBuilder: FormBuilder,
    translate: TranslateService,
    private confirmService: ConfirmService,
    stateService: SearchFormStateService,
    authService: AuthenticationService
  ) {
    super(service, entityModelService, messageService, router, authService);
    this.formBuilder = formBuilder;
    this.translate = translate;
    this.searchMode = true;
    this.stateService = stateService;
  }

  ngOnInit(): void {
    if (this.entityModel) {
      this.init(this.entityModel);
    } else {
      this.entityModelService
        .getEntityModel(this.entityName, this.entityModelReference)
        .subscribe({
          next: (model) => {
            this.entityModel = model;
            this.init(model);
          },
          error: (error) => this.messageService.error(error.error?.message),
        });
    }
  }

  protected init(model: EntityModelResponse) {
    this.attributeModels = model.attributeModels;
    this.filterSearchModels(model);
    this.buildForm();
    this.setupEnums(model);
    this.setDefaultValues();
    this.setupLookups(model);

    if (this.preserveSearchTerms) {
      this.restoreState();
    }

    this.afterSearchFormBuilt.emit({
      formGroup: this.mainForm!,
    });
  }

  protected filterSearchModels(model: EntityModelResponse) {
    this.searchAttributeModels = [];
    model.attributeNamesOrderedForSearch?.forEach((attributeName) => {
      let am = model.attributeModels!.find((am) => am.name! == attributeName);
      if (am && this.isSearchable(am)) {
        this.searchAttributeModels.push(am);
      }
    });
  }

  search(): void {
    if (!this.isFormValid()) {
      this.messageService.error(this.translate.instant('form_not_valid'));
      this.markAsTouched();
      return;
    }

    this.loading = true;
    if (this.onSearch) {
      this.bindBeforeSearch();
      console.log('Storing state: ' + JSON.stringify(this.searchObject));
      if (this.preserveSearchTerms) {
        this.stateService.storeState(this.entityName!, this.searchObject);
      }
      this.onSearch.emit(this.searchObject);
    }
  }

  getStateKey() {
    return this.stateStoreKey ? this.stateStoreKey : this.entityName
  }

  /**
   * Indicates whether two search fields (upper and lower bound) are needed
   * for an attribute
   * @param am the attribute model
   * @returns true if this is the case, false otherwise
   */
  protected needsTwoSearchFields(am: AttributeModelResponse): boolean {
    if (this.isDecimal(am) || this.isIntegral(am) || this.isDate(am)) {
      return am.searchForExactValue === false;
    }
    if (this.isTime(am)) {
      return true;
    }
    if (this.isLocalDateTime(am) || this.isInstant(am)) {
      return am.searchDateOnly === false;
    }

    return false;
  }

  getFromName(am: AttributeModelResponse): string {
    return am.name + '_from';
  }

  getToName(am: AttributeModelResponse): string {
    return am.name + '_to';
  }

  buildRangeControls(formGroup: FormGroup, am: AttributeModelResponse, includeDefaults: boolean) {
    let controlFrom = this.formBuilder.control(
      {
        disabled: false,
        value: includeDefaults ?  this.convertDefaultValue(am.defaultSearchValueFrom, am) : undefined,
      },
      { validators: [] }
    );
    formGroup.addControl(this.getFromName(am), controlFrom);

    let controlTo = this.formBuilder.control(
      {
        disabled: false,
        value: includeDefaults ?  this.convertDefaultValue(am.defaultSearchValueTo, am) : undefined,
      },
      { validators: [] }
    );
    formGroup.addControl(this.getToName(am), controlTo);

    this.addAtLeastOneValueValidator(am, controlTo, controlFrom);
  }

  clear(): void {
    if (this.confirmClear === true) {
      var callback = (event: any): void => {
        this.doClear();
      };
      this.confirmService.confirm('clear_confirmation', callback);
    } else {
      this.doClear();
    }
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

  /**
   * Adds a validator for verifying that at least one search field must be filled
   * @param am the attribute model of the attribute to search for
   * @param controlTo the lower bound control
   * @param controlFrom the upper bound control
   */
  addAtLeastOneValueValidator(
    am: AttributeModelResponse,
    controlTo: AbstractControl,
    controlFrom: AbstractControl
  ) {
    if (am.requiredForSearching === true) {
      let valsFrom: ValidatorFn[] = [new DynamoValidators(this.translate).atLeastOneRequiredValidator(controlTo)];
      let valsTo: ValidatorFn[] = [new DynamoValidators(this.translate).atLeastOneRequiredValidator(controlFrom)];
      controlFrom.addValidators(valsFrom);
      controlTo.addValidators(valsTo);

      // listen to changes and check validity after changes
      controlFrom.valueChanges.subscribe((val) => {
        if (!this.changing) {
          this.changing = true;
          controlTo.updateValueAndValidity();
          this.changing = false;
        }
      });
      controlTo.valueChanges.subscribe((val) => {
        if (!this.changing) {
          this.changing = true;
          controlFrom.updateValueAndValidity();
          this.changing = false;
        }
      });
    }
  }

  /**
   * Converts a default value (as stored in the meta data) to the
   * correct representation in the UI
   * @param defaultValue the default value
   * @param am the attribute model
   * @returns the result of the conversion
   */
  protected convertDefaultValue(defaultValue: any, am: AttributeModelResponse) {
    if (this.isEnum(am) && defaultValue) {
      // look up correct value in enum values
      let match: any = this.enumMap
        .get(am.name)!
        .find((v) => v.value === defaultValue);
      defaultValue = match;
    } else if (this.isDate(am) && defaultValue) {
      defaultValue = parseISO(defaultValue! as string);
    } else if (am.searchDateOnly && defaultValue) {
      defaultValue = parseISO(defaultValue! as string);
    } else if (this.isTime(am) && defaultValue) {
      defaultValue = timeToDate(defaultValue);
    }
    return defaultValue;
  }

  getTemplateForField(fieldName: string): TemplateRef<any> | undefined {
    return this.getCustomInput(fieldName) || undefined;
  }

  /**
   * Returns any configured custom component for an attribute
   * @param attributeName the name of the attribute
   * @returns
   */
  getCustomInput(attributeName: string): TemplateRef<any> | undefined {
    if (this.injectedCustomInputs && this.injectedCustomInputs.length > 0) {
      return this.injectedCustomInputs?.find((item) => {
        return item.attributeName === attributeName;
      })?.template;
    }

    return this.customInputs?.find((item) => {
      return item.attributeName === attributeName;
    })?.template;
  }

  protected abstract buildForm(): void;

  protected abstract isSearchable(am: AttributeModelResponse): boolean;

  protected abstract doClear(): void;

  protected abstract isFormValid(): boolean;

  protected abstract bindBeforeSearch(): void;

  protected abstract markAsTouched(): void;

  protected abstract setDefaultValues(): void;

  protected abstract restoreState(): void;
}
