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
import { FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SimpleChanges, TemplateRef } from '@angular/core';
import { Component, Input, OnChanges, OnInit } from '@angular/core';
import {
  createValidators,
  getErrorString,
  isBoolean,
  isDecimal,
  isEnum,
  isIntegral,
  isMaster,
  isString,
  isTime,
  isInstant,
  isLocalDateTime,
  isDate,
} from '../../functions/entitymodel-functions';
import {
  AttributeModelResponse,
  CRUDService,
  ModelService,
  EntityModelResponse,
} from 'dynamo/model';
import {
  getNestedValue,
  timeToDate,
  timestampToDate,
} from '../../functions/functions';
import { NotificationService } from '../../service/notification-service';
import { Router } from '@angular/router';
import { BaseCompositeComponent } from '../base-composite.component';
import { TranslateService } from '@ngx-translate/core';
import { AuthenticationService } from '../../service/authentication-service';
import { SelectOption } from '../../model/select-option';
import { concat, Observable, of, zip } from 'rxjs';

/**
 * A component for displaying a table/grid used for editing a one-to-many relationship
 */
@Component({
  selector: 'app-details-grid',
  templateUrl: './details-grid.component.html',
  styleUrls: ['./details-grid.component.scss'],
})
export class DetailsGridComponent
  extends BaseCompositeComponent
  implements OnInit, OnChanges
{

  // parent entity name
  @Input({required:true}) parentEntityName!: string;
  // parent reference name
  @Input() parentReference?: string;
  // the name of the attribute
  @Input({required:true}) attributeName!: string;
  // the entity model
  @Input() override entityModel?: EntityModelResponse = undefined;
  // the objects/rows that are being edited
  @Input() rows: any[] | undefined = [];
  // the default page size
  @Input() pageSize: number = 10;
  // form builder of the parent form
  @Input() formBuilder: FormBuilder | undefined;
  // form array, containing a form group for every row
  @Input() formArray: FormArray | undefined;
  // custom validators
  @Input() customValidatorTemplate?: TemplateRef<any>;
  // mapping of attribute names to nested entity model. Used for binding nested forms
  nestedEntityModelMap: Map<string, EntityModelResponse> = new Map<
    string,
    EntityModelResponse
  >();

  index: number = 0;

  constructor(
    service: CRUDService,
    modelService: ModelService,
    formBuilder: FormBuilder,
    messageService: NotificationService,
    router: Router,
    private translate: TranslateService,
    authService: AuthenticationService
  ) {
    super(service, modelService, messageService, router, authService);
    this.formBuilder = formBuilder;
  }

  getRows(): any[] {
    return this.rows ? this.rows.sort((a, b) => a.id - b.id) : [];
  }

  ngOnInit(): void {
    this.load();
    this.buildFormRows();
  }

  /**
   * Responds to chances by throwing away all form groups and rebuilding them
   * @param changes the changes
   */
  ngOnChanges(changes: SimpleChanges): void {
    this.buildFormRows();
  }

  private buildFormRows() {
    this.formArray?.clear();
    this.index = 0;
    this.rows?.forEach((row) => {
      this.addFormRow(row);
      this.index++;
    });
  }

  load() {
    if (this.entityModel) {
      this.init(this.entityModel);
    } else {
      this.entityModelService
        .getEntityModel(this.entityName)
        .subscribe((model) => {
          this.init(model);
          this.entityModel = model;
        });
    }
  }

  init(model: EntityModelResponse) {
    this.setupEnums(model);
    this.setupLookups(model);

    model.attributeNamesOrderedForGrid.forEach((attributeName) => {
      let attrib = this.entityModel
        ?.attributeModels!.filter((am) => am.visibleInForm && am.name !== 'id')
        .find((am) => am.name == attributeName);
      if (
        attrib &&
        attrib.visibleInForm &&
        (isString(attrib) ||
          isEnum(attrib) ||
          isIntegral(attrib) ||
          isBoolean(attrib) ||
          isDate(attrib) ||
          isMaster(attrib) ||
          isTime(attrib) ||
          isInstant(attrib) ||
          isDecimal(attrib) ||
          isLocalDateTime(attrib))
      ) {
        // this covers basically everything now except nested forms
        this.attributeModels.push(attrib);
      }
    });

    // load nested entity models
    let observables: Observable<
      [AttributeModelResponse, EntityModelResponse]
    >[] = this.attributeModels
      .filter((am) => this.isMaster(am))
      .map((am) => this.getNestedModel(am));

    if (observables.length > 0) {
      concat(...observables)
        // .pipe(
        //   finalize(() => {
        //     this.bindExistingEntity(false);
        //     this.initDone = true;
        //   })
        // )
        .subscribe((p) => {
          if (Array.isArray(p)) {
            this.nestedEntityModelMap.set(p[0].name, p[1]);
          }
        });
    }
  }

  getNestedModel(
    am: AttributeModelResponse
  ): Observable<[AttributeModelResponse, EntityModelResponse]> {
    return this.entityModelService
      .getNestedEntityModel2(this.parentEntityName, this.attributeName, am.name,
          this.parentReference
      )
      .pipe((obs) => zip(of(am), obs));
  }

  getFormControlName(am: AttributeModelResponse): string {
    return am.name;
  }

  getFormControl(
    am: AttributeModelResponse,
    rowIndex: number
  ): FormControl<any> {
    return this.formArray?.at(rowIndex).get(am.name) as FormControl<any>;
  }

  getFormGroup(index: number): FormGroup {
    let group = this.formArray?.at(index) as FormGroup;
    return group;
  }

  /**
   * Deal with a click on the "Plus" button, adds a new empty row to the table
   */
  addButtonClick(): void {
    let row = {};
    if (!this.rows) {
      this.rows = [];
    }

    this.rows?.push(row);
    this.addFormRow(row);
    this.index++;
  }

  /**
   * @param row Adds a new row containing a form to the table
   */
  public addFormRow(row: any): void {
    let rowGroup = this.formBuilder?.group({})!;

    this.formArray?.push(rowGroup);
    this.attributeModels.forEach((am) => {
      let val = getNestedValue(row, am.name);
      if (isEnum(am)) {
        let match: any = this.enumMap
          .get(am.name)!
          .find((v) => v.value === val);
        val = match;
      } else if (isDate(am)) {
        if (val) {
          val = new Date(val);
        }
      } else if (isTime(am)) {
        if (val) {
          val = timeToDate(val);
        }
      } else if (isInstant(am) || isLocalDateTime(am)) {
        val = timestampToDate(val, isInstant(am));
      } else if (isMaster(am)) {
        if (this.entityLists.get(am.lookupEntityName!)) {
          let match: any = this.entityLists
            .get(am.lookupEntityName!)!
            .find((v) => val && v.value === val.id);
          val = match;
        }
      }

      let validators = createValidators(this.translate, am, false, false);
      let control = this.formBuilder!.control(
        {
          disabled: false,
          value: val,
        },
        { validators: validators }
      );
      rowGroup?.addControl(am.name, control);
    });
  }

  deleteRow(index: number) {
    this.formArray?.removeAt(index);
    this.rows!.slice(index, index + 1);
    this.index--;
  }

  override getEnumValues(attribute: string): SelectOption[] {
    this.setupEnums(this.entityModel!);
    return this.enumMap.get(attribute)!;
  }

  getErrorString(attribute: string, formGroup: FormGroup): string {
    if (!formGroup) {
      return '';
    }
    return getErrorString(attribute, formGroup, this.translate);
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    for (let i = 0; i < this.formArray!.length; i++) {
      let formArray = this.formArray?.at(i)!;
      let control = formArray.get(am.name)!;

      if (isMaster(am) && control && this.rows && this.rows[i]) {
        let val = this.rows[i][am.name];
        if (val) {
          if (this.entityLists.get(am.lookupEntityName!)) {
            let match: any = this.entityLists
              .get(am.lookupEntityName!)!
              .find((v) => v.value === val.id);
            control.setValue(match);
          }
        }
      }
    }
  }

  isEditable(row: any, am: AttributeModelResponse): boolean {
    if (row.id) {
      return (
        am.editableType === AttributeModelResponse.EditableTypeEnum.EDITABLE
      );
    }
    return (
      am.editableType === AttributeModelResponse.EditableTypeEnum.EDITABLE ||
      am.editableType === AttributeModelResponse.EditableTypeEnum.CREATE_ONLY
    );
  }

  getTotalRecords() {
    return this.index;
  }

  getNestedEntityModel(
    am: AttributeModelResponse
  ): EntityModelResponse | undefined {
    return this.nestedEntityModelMap.get(am.name);
  }
}
