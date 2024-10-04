import { Component, OnInit } from '@angular/core';
import {
  AttributeModelResponse,
  CRUDService,
  ModelService,
} from 'dynamo/model';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { NotificationService } from '../../service/notification-service';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmService } from '../../service/confirm.service';
import { BaseSearchComponent } from '../base-search-form';
import { SearchFormStateService } from '../../service/search-form-state.service';
import { getNestedValue } from '../../functions/functions';
import { AuthenticationService } from '../../service/authentication-service';

export class SearchRow {
  index: number = 0;
  attributeName?: string;
  am?: AttributeModelResponse;
  required: boolean = false;
  formGroup?: FormGroup;
}

/**
 * A search form that allows the user to dynamically add rows containing search criteria
 */
@Component({
  selector: 'app-flexible-search-form',
  templateUrl: './flexible-search-form.component.html',
  styleUrls: ['./flexible-search-form.component.scss'],
})
export class FlexibleSearchFormComponent
  extends BaseSearchComponent
  implements OnInit
{
  searchRows: SearchRow[] = [];
  rowIndex = 0;
  searchableAttributeNames: any[] = [];
  formArray?: FormArray;

  constructor(
    service: CRUDService,
    entityModelService: ModelService,
    messageService: NotificationService,
    router: Router,
    formBuilder: FormBuilder,
    translate: TranslateService,
    confirmService: ConfirmService,
    stateService: SearchFormStateService,
    authService: AuthenticationService
  ) {
    super(
      service,
      entityModelService,
      messageService,
      router,
      formBuilder,
      translate,
      confirmService,
      stateService,
      authService
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
