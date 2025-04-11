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
import {Component, Input, OnInit, inject, forwardRef} from '@angular/core';
import { Router } from '@angular/router';
import {FormBuilder, NG_VALUE_ACCESSOR, ReactiveFormsModule, ValidatorFn, Validators} from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BaseSearchComponent } from '../base-search-component/base-search-component.component';
import { NotificationService } from '../../../../services/notification.service';
import { ConfirmService } from '../../../../services/confirm.service';
import { SearchFormStateService } from '../../../../services/search-form-state.service';
import { AuthenticationService } from '../../../../services/authentication.service';
import { DynamoConfig } from '../../../../interfaces/dynamo-config';
import { createEqualsFilter } from '../../../../functions/entitymodel-functions';
import { CascadeModel } from '../../../../interfaces/model/cascadeModel';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { getNestedValue } from '../../../../functions/functions';
import { ElementCollectionFieldComponent } from '../../fields/element-collection-field/element-collection-field.component';
import { SelectManyFieldComponent } from '../../fields/select-many-field/select-many-field.component';
import { SelectEntityFieldComponent } from '../../fields/select-entity-field/select-entity-field.component';
import { TimeFieldComponent } from '../../fields/time-field/time-field.component';
import { TimestampFieldComponent } from '../../fields/timestamp-field/timestamp-field.component';
import { DateFieldComponent } from '../../fields/date-field/date-field.component';
import { EnumFieldComponent } from '../../fields/enum-field/enum-field.component';
import { TriStateCheckboxModule } from 'primeng/tristatecheckbox';
import { TooltipModule } from 'primeng/tooltip';
import { DecimalFieldComponent } from '../../fields/decimal-field/decimal-field.component';
import { NumberFieldComponent } from '../../fields/number-field/number-field.component';
import { StringFieldComponent } from '../../fields/string-field/string-field.component';
import { CommonModule } from '@angular/common';
import { PanelModule } from 'primeng/panel';
import {Button} from "primeng/button";

@Component({
  selector: 'd-generic-search-form',
  standalone: true,
  imports: [TranslateModule, CommonModule, ReactiveFormsModule, PanelModule, TriStateCheckboxModule, TooltipModule, ElementCollectionFieldComponent, forwardRef(() => SelectManyFieldComponent),
    forwardRef(() => SelectEntityFieldComponent), TimeFieldComponent, TimestampFieldComponent, DateFieldComponent, EnumFieldComponent, DecimalFieldComponent, NumberFieldComponent, StringFieldComponent, Button],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => GenericSearchFormComponent),
      multi: true,
    },
  ],
  templateUrl: './generic-search-form.component.html',
  styleUrl: './generic-search-form.component.css'
})
export class GenericSearchFormComponent
  extends BaseSearchComponent
  implements OnInit {
  // whether advanced search mode is enabled
  @Input() advancedModeEnabled: boolean = false;

  advancedMode: boolean = false;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const messageService = inject(NotificationService);
    const router = inject(Router);
    const formBuilder = inject(FormBuilder);
    const translate = inject(TranslateService);
    const confirmService = inject(ConfirmService);
    const stateService = inject(SearchFormStateService);
    const authService = inject(AuthenticationService);
    const configuration = inject<DynamoConfig>("DYNAMO_CONFIG" as any);

    super(
      messageService,
      router,
      formBuilder,
      translate,
      confirmService,
      stateService,
      authService,
      configuration,
    );
  }

  /**
   * Builds the search form
   */
  buildForm() {
    this.mainForm = this.formBuilder.group([]);

    this.attributeModels.forEach((am) => {
      if (this.needsTwoSearchFields(am)) {
        this.buildRangeControls(this.mainForm!, am, false);
      } else {
        let validators: ValidatorFn[] = [];
        if (am.requiredForSearching === true) {
          validators.push(Validators.required);
        }

        let control = this.formBuilder.control(
          {
            disabled: false,
            value: am.defaultSearchValue,
          },
          { validators: validators }
        );
        this.mainForm!.addControl(am.name, control);
      }
    });

    this.setupCascading();
  }

  private setupCascading() {
    this.attributeModels.forEach((am) => {
      if (am.cascades && am.cascades.length > 0) {
        am.cascades
          .filter((cascade) => this.isCascadeApplicable(cascade))
          .forEach((cascade) => {
            let control = this.mainForm!.get(am.name)!;
            let cascadeControl = this.mainForm!.get(cascade.cascadeTo);
            if (cascadeControl) {
              control.valueChanges.subscribe((val) => {
                if (val) {
                  let cascadeFilter = createEqualsFilter(
                    cascade.filterPath,
                    val.value
                  );
                  this.cascadeFilters.set(cascade.cascadeTo, [cascadeFilter]);
                  this.fillOptions(this.findAttributeModel(cascade.cascadeTo)!);
                  cascadeControl?.reset();
                }
              });
            }
          });
      }
    });
  }

  isCascadeApplicable(cascade: CascadeModel): boolean {
    return (
      cascade.cascadeMode === CascadeModel.CascadeModeEnum.BOTH ||
      cascade.cascadeMode === CascadeModel.CascadeModeEnum.SEARCH
    );
  }

  setDefaultValues() {
    this.attributeModels.forEach((am) => {
      let defaultValue = this.convertDefaultValue(am.defaultSearchValue, am);
      if (defaultValue) {
        this.mainForm!.get(am.name)?.patchValue(defaultValue);
      }

      let defaultValueFrom = this.convertDefaultValue(
        am.defaultSearchValueFrom,
        am
      );
      if (defaultValueFrom) {
        this.mainForm!.get(this.getFromName(am))?.patchValue(defaultValueFrom);
      }

      let defaultValueTo = this.convertDefaultValue(
        am.defaultSearchValueTo,
        am
      );
      if (defaultValueTo) {
        this.mainForm!.get(this.getToName(am))?.patchValue(defaultValueTo);
      }
    });
  }

  /**
   * Binds form values to the search object before executing a search
   */
  override bindBeforeSearch() {
    this.searchObject = {};
    this.attributeModels
      .filter((am) => this.isSearchable(am))
      .forEach((am) => {
        let control = this.mainForm!.get(am.name);
        if (!this.isBoolean(am) && control && control.value) {
          this.searchObject[am.name] = control.value;
        }

        if (this.isBoolean(am) && control) {
          if (control.value === true || control.value === false) {
            this.searchObject[am.name] = control.value;
          }
        }

        let controlFrom = this.mainForm?.get(this.getFromName(am));
        if (controlFrom && controlFrom.value) {
          this.searchObject[this.getFromName(am)] = controlFrom.value;
        }

        let controlTo = this.mainForm?.get(this.getToName(am));
        if (controlTo && controlTo.value) {
          this.searchObject[this.getToName(am)] = controlTo.value;
        }
      });
  }

  /**
   * Clears the search filters
   */
  doClear(): void {
    this.searchObject = {};
    this.mainForm!.reset();

    // reset all cascading filters, refill the option lists
    let keys: string[] = [];
    for (let [key, value] of this.cascadeFilters) {
      keys.push(key);
    }
    this.cascadeFilters.clear();
    keys.forEach((key) => this.fillOptions(this.findAttributeModel(key)!));

    if (this.onReset) {
      this.onReset.emit({});
    }
  }

  /**
   * Custom method for checking whether a form is valid. Takes into account
   * whether components are currently being shown and checks that at least one
   * value is present for an attribute that has two input components
   * @returns true when all (visible) components are valid
   */
  isFormValid(): boolean {
    let valid: boolean = true;
    this.attributeModels.forEach((am) => {
      if (this.isSearchable(am) && am.requiredForSearching === true) {
        if (this.needsTwoSearchFields(am)) {
          let controlFrom = this.mainForm?.get(this.getFromName(am))!;
          let controlTo = this.mainForm?.get(this.getToName(am))!;
          if (!controlFrom.value && !controlTo.value) {
            valid = false;
          }
        } else {
          let control = this.mainForm?.get(am.name);
          if (!control?.value) {
            valid = false;
          }
        }
      }
    });
    return valid;
  }

  protected override restoreState(): void {
    let state = this.stateService.retrieveState(this.getStateKey());
    if (state) {
      this.attributeModels.forEach((am) => {
        this.restoreStateValue(am.name, state);
        this.restoreStateValue(this.getFromName(am), state);
        this.restoreStateValue(this.getToName(am), state);
      });
    }
  }

  restoreStateValue(attributeName: string, state: any) {
    let val = getNestedValue(state, attributeName);
    if (val) {
      this.mainForm!.get(attributeName)?.patchValue(val);
    }
  }

  protected override isSearchable(am: AttributeModelResponse): boolean {
    return (
      am.searchMode == AttributeModelResponse.SearchModeEnum.ALWAYS ||
      (this.advancedMode &&
        am.searchMode == AttributeModelResponse.SearchModeEnum.ADVANCED)
    );
  }

  protected override markAsTouched(): void {
    this.mainForm?.markAllAsTouched();
  }

  protected override onLookupFilled(am: AttributeModelResponse): void { }

  toAdvancedMode() {
    this.advancedMode = true;
    this.filterSearchModels(this.entityModel!);
  }

  toSimpleMode() {
    this.advancedMode = false;
    this.filterSearchModels(this.entityModel!);
  }
}
