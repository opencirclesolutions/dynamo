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
import {Component, OnInit, inject, forwardRef} from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup, NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ConfirmService } from '../../../../services/confirm.service';
import { SearchFormStateService } from '../../../../services/search-form-state.service';
import { NotificationService } from '../../../../services/notification.service';
import { AuthenticationService } from '../../../../services/authentication.service';
import { DynamoConfig } from '../../../../interfaces/dynamo-config';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { getNestedValue } from '../../../../functions/functions';
import { BaseSearchComponent } from '../base-search-component/base-search-component.component';
import { TimeFieldComponent } from '../../fields/time-field/time-field.component';
import { TimestampFieldComponent } from '../../fields/timestamp-field/timestamp-field.component';
import { NumberFieldComponent } from '../../fields/number-field/number-field.component';
import { ElementCollectionFieldComponent } from '../../fields/element-collection-field/element-collection-field.component';
import { DecimalFieldComponent } from '../../fields/decimal-field/decimal-field.component';
import { DateFieldComponent } from '../../fields/date-field/date-field.component';
import { TriStateCheckboxModule } from 'primeng/tristatecheckbox';
import { EnumFieldComponent } from '../../fields/enum-field/enum-field.component';
import { TooltipModule } from 'primeng/tooltip';
import { SelectManyFieldComponent } from '../../fields/select-many-field/select-many-field.component';
import { SelectEntityFieldComponent } from '../../fields/select-entity-field/select-entity-field.component';
import { StringFieldComponent } from '../../fields/string-field/string-field.component';
import { CommonModule } from '@angular/common';
import { DropdownModule } from 'primeng/dropdown';
import { PanelModule } from 'primeng/panel';

export interface SearchRow {
  index: number
  attributeName?: string
  am?: AttributeModelResponse
  required: boolean
  formGroup?: FormGroup
}

@Component({
  selector: 'd-flexible-search-form',
  standalone: true,
  imports: [TranslateModule, CommonModule, ReactiveFormsModule, PanelModule, DropdownModule, TooltipModule, TriStateCheckboxModule, StringFieldComponent, forwardRef(() => SelectManyFieldComponent),
    forwardRef(() => SelectEntityFieldComponent), EnumFieldComponent, TimeFieldComponent, DecimalFieldComponent, DateFieldComponent, TimestampFieldComponent, NumberFieldComponent, ElementCollectionFieldComponent],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FlexibleSearchFormComponent),
      multi: true,
    },
  ],
  templateUrl: './flexible-search-form.component.html',
  styleUrl: './flexible-search-form.component.css'
})
export class FlexibleSearchFormComponent
  extends BaseSearchComponent
  implements OnInit {
  searchRows: SearchRow[] = [];
  rowIndex = 0;
  searchableAttributeNames: any[] = [];
  formArray?: FormArray;

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
  protected override buildForm() {
    this.mainForm = this.formBuilder.group([]);
    this.formArray = this.formBuilder.array([]);

    this.searchableAttributeNames = this.searchAttributeModels.map((am) => {
      let option = {
        value: am.name,
        name: am.displayNames[this.locale],
      };
      return option;
    });
    this.searchableAttributeNames.sort((a, b) => a.name.localeCompare(b.name));
    this.addRequiredRows();
  }

  /**
   * Binds form values to the search object before executing a search
   */
  override bindBeforeSearch() {
    this.searchObject = {};

    this.searchRows.forEach((row) => {
      let formGroup = this.formArray?.at(row.index)!;
      let am = row.am;
      if (am) {
        let control = formGroup.get(row.attributeName!);
        if (control) {
          this.searchObject[row.attributeName!] = control.value;
        }

        let controlFrom = formGroup.get(this.getFromName(am));
        if (controlFrom && controlFrom.value) {
          this.searchObject[this.getFromName(am)] = controlFrom.value;
        }

        let controlTo = formGroup.get(this.getToName(am));
        if (controlTo && controlTo.value) {
          this.searchObject[this.getToName(am)] = controlTo.value;
        }
      }
    });
  }

  override isSearchable(am: AttributeModelResponse): boolean {
    return (
      am.searchMode == AttributeModelResponse.SearchModeEnum.ALWAYS ||
      am.searchMode == AttributeModelResponse.SearchModeEnum.ADVANCED
    );
  }

  override doClear(): void {
    this.searchObject = {};
    this.mainForm!.reset();
    this.searchRows = [];
    this.rowIndex = 0;
    this.formArray?.clear();
    this.addRequiredRows();
    this.onReset.emit({});
  }

  /**
   * Custom method for checking whether a form is valid. Takes into account
   * whether components are currently being shown and checks that at least one
   * value is present for an attribute that has two input components
   * @returns true when all (visible) components are valid
   */
  override isFormValid(): boolean {
    let valid: boolean = true;
    this.attributeModels.forEach((am) => {
      if (this.isSearchable(am) && am.requiredForSearching === true) {
        let formGroup = this.findFormGroupFor(am);
        if (this.needsTwoSearchFields(am)) {
          // at least one of two fields must be filled
          let controlFrom = formGroup?.get(this.getFromName(am))!;
          let controlTo = formGroup?.get(this.getToName(am))!;
          if (!controlFrom.value && !controlTo.value) {
            valid = false;
          }
        } else {
          // the field must contain a value
          let control = formGroup?.get(am.name);
          if (!control?.value) {
            valid = false;
          }
        }
      }
    });
    return valid;
  }

  findFormGroupFor(am: AttributeModelResponse): FormGroup | undefined {
    let match = this.searchRows.find((row) => row.attributeName === am.name);
    if (!match) {
      return undefined;
    }
    return match.formGroup;
  }

  /**
   * Removes a row for the search form
   * @param index the index of the row
   */
  deleteRow(index: number) {
    this.formArray?.removeAt(index);
    this.searchRows.splice(index, 1);
    this.searchRows.forEach((row) => {
      if (row.index > index) {
        row.index--;
      }
    });
    this.rowIndex--;
  }

  /**
   * Adds a new row to the search form
   * @param required whether the row is required
   */
  addRow(required: boolean) {
    let rowGroup = this.formBuilder?.group({});
    this.formArray?.push(rowGroup);

    let attributeControl = this.formBuilder.control(
      {
        disabled: required,
        value: undefined,
      },
      { validators: [] }
    );
    rowGroup.addControl('attribute', attributeControl);

    let row: SearchRow = {
      index: this.rowIndex++,
      required: required,
      formGroup: rowGroup,
    };

    // respond to changes by rendering one or more search fields
    attributeControl.valueChanges.subscribe((changes: any) => {
      row.attributeName = changes?.value;
      row.am = this.findAttributeModel(row.attributeName!);
      if (row.am) {
        if (this.needsTwoSearchFields(row.am!)) {
          this.buildRangeControls(rowGroup, row.am!, true);
        } else {
          let defaultValue = this.convertDefaultValue(
            row.am!.defaultSearchValue,
            row.am!
          );
          let valueControl = this.formBuilder.control(
            {
              disabled: false,
              value: defaultValue,
            },
            {
              validators:
                row.am?.requiredForSearching === true
                  ? [Validators.required]
                  : [],
            }
          );
          rowGroup.addControl(row.attributeName!, valueControl);
        }
      }
    });

    this.searchRows.push(row);
  }

  /**
   * Adds rows for the attributes that are marked as "required for searching"
   */
  addRequiredRows() {
    this.searchAttributeModels
      .filter((am) => am.requiredForSearching)
      .forEach((am) => {
        this.addRow(true);
        this.formArray
          ?.at(this.rowIndex - 1)
          .get('attribute')
          ?.setValue({
            value: am.name,
            name: am.displayNames[this.locale],
          });
      });
  }

  getFormControl(attributeName: string, rowIndex: number): FormControl<any> {
    return this.formArray?.at(rowIndex).get(attributeName) as FormControl<any>;
  }

  getFormGroup(index: number): FormGroup {
    return this.formArray?.at(index) as FormGroup;
  }

  protected override markAsTouched(): void {
    for (let i = 0; i < this.rowIndex; i++) {
      this.formArray?.at(i).markAllAsTouched();
    }
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // no specific action needed
  }

  protected override setDefaultValues(): void {
    this.attributeModels.forEach((am) => {
      // create extra rows for default values
      if (
        (am.defaultSearchValue ||
          am.defaultSearchValueFrom ||
          am.defaultSearchValueTo) &&
        !am.requiredForSearching
      ) {
        this.addRow(false);
        this.formArray
          ?.at(this.rowIndex - 1)
          .get('attribute')
          ?.setValue({
            value: am.name,
            name: am.displayNames[this.locale],
          });
      }
    });
  }

  protected override restoreState(): void {
    let state = this.stateService.retrieveState(this.getStateKey());
    this.attributeModels.forEach((am) => {
      let val = getNestedValue(state, am.name);
      let valFrom = getNestedValue(state, this.getFromName(am));
      let valTo = getNestedValue(state, this.getToName(am));

      if (val || valFrom || valTo) {
        let rowIndex = this.findOrCreateRow(am);
        let group: FormGroup = this.formArray?.at(rowIndex)! as FormGroup;

        group.get('attribute')?.setValue({
          value: am.name,
          name: am.displayNames[this.locale],
        });

        this.restoreStateValue(am.name, val, group);
        this.restoreStateValue(this.getFromName(am), valFrom, group);
        this.restoreStateValue(this.getToName(am), valTo, group);
      }
    });
  }

  restoreStateValue(name: string, value: any, formGroup: FormGroup) {
    if (value) {
      formGroup.get(name)?.patchValue(value);
    }
  }

  findOrCreateRow(am: AttributeModelResponse) {
    let index = this.searchRows.findIndex(
      (row) => row.attributeName === am.name
    );
    if (index >= 0) {
      return index;
    } else {
      this.addRow(false);
    }
    return this.rowIndex - 1;
  }
}
