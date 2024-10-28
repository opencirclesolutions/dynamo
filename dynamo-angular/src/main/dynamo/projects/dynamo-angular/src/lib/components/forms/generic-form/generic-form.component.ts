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
import { Component, ContentChildren, EventEmitter, Input, OnInit, Output, QueryList, TemplateRef, ViewChild, ViewContainerRef, inject } from '@angular/core';
import {
  FormGroup,
  AbstractControl,
  FormArray,
  FormControl,
  ReactiveFormsModule,
} from '@angular/forms';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import {
  createEqualsFilter,
  createValidators,
  getErrorString,
  setNestedValue,
} from '../../../functions/entitymodel-functions';
import {
  dateToTimestamp,
  getNestedValue,
  stringToTime,
  timeToDate,
  timestampToDate,
} from '../../../functions/functions';
import { formatISO, parseISO } from 'date-fns';
import { Observable, concat, concatMap, finalize, map, of, zip } from 'rxjs';
import { AttributeGroupMode } from '../../../interfaces/mode';
import { HiddenFieldService } from '../../../services/hidden-field.service';
import { AdditionalActionMode, AdditionalFormAction } from '../../../interfaces/action';
import { OverrideFieldDirective } from '../../../directives/override-field.directive';
import { FileClearInfo, FileUploadInfo, FormInfo } from '../../../interfaces/info';
import { NotificationService } from '../../../services/notification.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BindingService } from '../../../services/binding.service';
import { AuthenticationService } from '../../../services/authentication.service';
import { ConfirmService } from '../../../services/confirm.service';
import { DynamoConfig } from '../../../interfaces/dynamo-config';
import { FileServiceInterface } from '../../../interfaces/service/file.service';
import { AttributeModelResponse } from '../../../interfaces/model/attributeModelResponse';
import { AttributeGroupResponse } from '../../../interfaces/model/attributeGroupResponse';
import { EntityModelResponse } from '../../../interfaces/model/entityModelResponse';
import { SelectOption } from '../../../interfaces/select-option';
import { AbstractEntity } from '../../../interfaces/model/abstractEntity';
import { EntityModelActionResponse } from '../../../interfaces/model/entityModelActionResponse';
import { CascadeModel } from '../../../interfaces/model/cascadeModel';
import { EntityPopupDialogComponent } from '../../dialogs/entity-popup-dialog/entity-popup-dialog.component';
import { AutoFillDialogComponent } from '../../dialogs/auto-fill-dialog/auto-fill-dialog.component';
import { CommonModule } from '@angular/common';
import { FileUploadComponent } from '../../blocks/file-upload/file-upload.component';
import { DetailsGridComponent } from '../details-grid/details-grid.component';
import { FieldViewComponent } from '../field-view/field-view.component';
import { TabViewModule } from 'primeng/tabview';
import { PanelModule } from 'primeng/panel';
import { GenericFormViewComponent } from '../generic-form-view/generic-form-view.component';
import { DividerModule } from 'primeng/divider';
import { GenericFieldComponent } from '../fields/generic-field/generic-field.component';
import { BaseCompositeComponent } from '../base-composite/base-composite.component';

@Component({
  selector: 'd-generic-form',
  standalone: true,
  imports: [TranslateModule, CommonModule, TabViewModule, PanelModule, DividerModule, ReactiveFormsModule, GenericFieldComponent, FileUploadComponent, DetailsGridComponent, FieldViewComponent, GenericFormViewComponent],
  templateUrl: './generic-form.component.html',
  styleUrl: './generic-form.component.css'
})
export class GenericFormComponent
  extends BaseCompositeComponent
  implements OnInit {
  protected formBuilder = inject(FormBuilder);
  private translate = inject(TranslateService);
  private confirmService = inject(ConfirmService);
  private bindingService = inject(BindingService);
  private hiddenFieldService = inject(HiddenFieldService);

  // the ID of the entity that is being edited
  @Input() entityId?: number = undefined;
  // whether navigation is allowed
  @Input() navigationAllowed: boolean = true;
  // the route to use when navigating back to the main screen
  @Input() navigateBackRoute?: string = undefined;
  // whether to open the screen in view mode
  @Input() openInViewMode: boolean = false;
  // the attribute grouping mode
  @Input() attributeGroupMode: AttributeGroupMode = AttributeGroupMode.PANEL;
  // the class that is applied to the entire form. can be used to set the width of the entire form
  @Input() formWidthClass: string = 'col-lg-8 col-md-10 col-sm-12';
  // the number of columns to display
  @Input() numberOfColumns: number = 1;
  // whether the component is in read only mode
  @Input() readOnly: boolean = false;
  // additional validation function to carry out before submitting the form
  @Input() additionalValidation?: (formGroup: FormGroup) => string | undefined;

  // post process input form (add dependencies between components)
  @Input() postProcessInputForm?: (formGroup: FormGroup) => void;
  // callback to check whether an attribute is visible
  @Input() attributeVisible?: (
    am: AttributeModelResponse,
    editObject: any,
    formGroup: FormGroup
  ) => boolean;

  // callback to enable/disable model action
  @Input() modelActionEnabled?: (action: EntityModelActionResponse, editObject: any, formGroup: FormGroup) => boolean;

  // callback that is used to modify the event before save
  @Input() injectedHiddenFieldService?: HiddenFieldService;

  // whether to ask for confirmation before saving
  @Input() confirmSave: boolean = false;
  // whether the form is nested
  @Input() nested: boolean = false;
  // whether the form is in free form mode
  @Input() freeFormMode: boolean = false;
  // custom components
  @Input() freeFormTemplate?: TemplateRef<any>;
  // custom validators
  @Input() customValidatorTemplate?: TemplateRef<any>;

  // additional button bar actions
  @Input() additionalActions: AdditionalFormAction[] = [];
  // custom inputs (injected from split layout)
  @Input() injectedCustomInputs?: QueryList<OverrideFieldDirective>;
  // whether form fill functionality is
  @Input() formFillEnabled: boolean = false;

  // callback that is carried out after creating a new entity
  @Output() afterEntityCreated = new EventEmitter<FormInfo>();
  // callback that is carried out after performing a save operation
  @Output() afterSave = new EventEmitter<any>();
  // callback that is carried out after performing a save operation
  @Output() onClose = new EventEmitter<any>();
  // optional action ID (when the form is used to carry out a model based action)
  @Input() actionId?: string;
  @Input() actionDisplayName?: string;

  //container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  vcr!: ViewContainerRef;

  @ViewChild('formFillContainerRef', { read: ViewContainerRef })
  formFillContainerRef!: ViewContainerRef;

  @ContentChildren(OverrideFieldDirective, { descendants: true })
  customInputs!: QueryList<OverrideFieldDirective>;

  // map for keeping track of uploaded files
  fileMap: Map<AttributeModelResponse, File> = new Map<
    AttributeModelResponse,
    File
  >();

  // map to keep track of which file fields must be cleared
  fileClearMap: Map<AttributeModelResponse, boolean> = new Map<
    AttributeModelResponse,
    boolean
  >();

  // mapping of attribute names to nested entity model. Used for binding nested forms
  nestedEntityModelMap: Map<string, EntityModelResponse> = new Map<
    string,
    EntityModelResponse
  >();

  viewMode: boolean = false;
  editObject: any = {};
  visibleAttributeModels: AttributeModelResponse[] = [];
  mainForm!: FormGroup;
  nestedFormGroups: Map<String, FormGroup> = new Map<String, FormGroup>();
  loading: boolean = false;
  initDone: boolean = false;
  private fileController: FileServiceInterface

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const messageService = inject(NotificationService);
    const router = inject(Router);
    const authService = inject(AuthenticationService);
    const configuration = inject<DynamoConfig>("DYNAMO_CONFIG" as any);

    super(messageService, router, authService, configuration);
    const hiddenFieldService = this.hiddenFieldService;

    this.hiddenFieldService = hiddenFieldService;
    this.fileController = configuration.getFileService()
  }

  ngOnInit(): void {
    // FIXME: very unlikely to be what's meant; I've rewritten to what I think it's supposed to be
    //    this.hiddenFieldService = this.hiddenFieldService
    //      ? this.injectedHiddenFieldService
    //      : this.hiddenFieldService;AbstractControl
    this.hiddenFieldService = this.injectedHiddenFieldService || this.hiddenFieldService

    this.viewMode = this.openInViewMode || this.readOnly ? true : false;
    if (!this.entityModel) {
      this.entityModelService
        .getEntityModel(this.entityName, this.entityModelReference)
        .subscribe({
          next: (model) => {
            this.entityModel = model;
            this.init(this.entityModel);
          },
          error: (error) =>
            error.error
              ? this.messageService.error(error.error.message)
              : this.messageService.error('Unknown error'),
        });
    } else {
      this.init(this.entityModel);
    }
  }

  getAttributeModelGroups(): string[] {
    return (
      this.entityModel?.attributeGroups
        .sort((a: { index: any; }, b: { index: any; }) => a.index! - b.index!)
        .map((group: { groupName: any; }) => group.groupName!) || []
    );
  }

  getAttributeModelsForGroup(group: string): AttributeModelResponse[] {
    let matches: string[] = this.findGroup(group)?.attributes || [];
    return this.visibleAttributeModels
      .filter((m) => matches.findIndex((match) => match === m.name) >= 0)
      .filter((am) => !this.isGroupedWithOther(this.visibleAttributeModels, am));
  }

  findGroup(groupName: string): AttributeGroupResponse {
    return this.entityModel!.attributeGroups.find(
      (group: { groupName: string; }) => group.groupName === groupName
    )!;
  }

  getGroupDescription(groupName: string) {
    return this.findGroup(groupName).groupDescriptions[this.locale];
  }

  getNestedEntityModel(
    am: AttributeModelResponse
  ): EntityModelResponse | undefined {
    return this.nestedEntityModelMap.get(am.name);
  }

  isNestedEntityModelReady(am: AttributeModelResponse): boolean {
    if (!this.isNestedDetail(am)) {
      return true;
    }
    return this.nestedEntityModelMap?.has(am.name);
  }

  init(model: EntityModelResponse) {
    this.attributeModels = model.attributeModels;
    if (!model.updateAllowed) {
      this.viewMode = true;
    }

    // filter out invisible attributes, or ID attribute when creating a new entity
    this.visibleAttributeModels = this.attributeModels
      .filter((am) => am.visibleInForm)
      .filter((am) => am.name !== 'id');

    this.setupEnums(model);
    this.buildForm();
    this.setupCascading();

    // load nested entity models
    let observables: Observable<
      [AttributeModelResponse, EntityModelResponse]
    >[] = this.visibleAttributeModels
      .filter(
        (am) =>
          this.isNestedDetail(am) || this.isFreeDetail(am) || this.isMaster(am)
      )
      .map((am) => this.getNestedModel(am));

    if (observables.length > 0) {
      concat(...observables)
        .pipe(
          finalize(() => {
            this.bindExistingEntity(false);
            this.initDone = true;
          })
        )
        .subscribe((p) => {
          if (Array.isArray(p)) {
            this.nestedEntityModelMap.set(p[0].name, p[1]);
          }
        });
    } else {
      this.bindExistingEntity(false);
    }
  }

  getNestedModel(
    am: AttributeModelResponse
  ): Observable<[AttributeModelResponse, EntityModelResponse]> {
    return this.entityModelService
      .getNestedEntityModel(this.entityName, am.name, this.entityModelReference)
      .pipe((obs) => zip(of(am), obs));
  }

  /**
   * Binds the values of an existing entity to the input components
   */
  public bindExistingEntity(afterSave: boolean) {
    if (this.entityId) {
      this.service
        .get(this.entityName, this.entityId.toString())
        .subscribe((entity) => {
          console.log('Retrieved entity: ' + JSON.stringify(entity));

          this.bindFields(entity);

          if (afterSave) {
            this.afterSave.emit(this.editObject);
          }
        });
    } else {
      this.service
        .init(this.entityName, this.entityModelReference)
        .subscribe((entity) => {
          console.log('Retrieved init entity: ' + JSON.stringify(entity));

          this.bindFields(entity);

          this.viewMode = false;
          this.setupLookups(this.entityModel!);

          this.afterEntityCreated.emit({ formGroup: this.mainForm });
        });
    }
  }

  bindFields(entity: any) {
    this.editObject = entity;
    this.fileMap.clear();
    this.visibleAttributeModels.forEach((am) => {
      let control = this.mainForm!.get(am.name);

      if (control) {
        this.bindingService.bindField(
          am,
          control,
          this.editObject,
          this.enumMap
        );
      }

      if (this.isLob(am)) {
        let value = this.getNestedValue(this.editObject, am);
        if (value) {
          this.fileClearMap.set(am, false);
        }
      }
    });
    this.setupLookups(this.entityModel!);
  }

  /**
   * Resets/clears the form
   */
  resetForm() {
    this.mainForm!.reset();
    this.editObject = {};
    this.bindExistingEntity(false);
  }

  /**
   * Binds the values of the entity to the free detail field
   * @param am the attribute model
   * @param control  the control to bind to
   */
  private bindFreeDetailField(
    am: AttributeModelResponse,
    control: AbstractControl<any, any>
  ) {
    let values: any[] = getNestedValue(this.editObject, am.name);
    let matches: SelectOption[] = [];
    if (values) {
      // supplied values are objects containing an ID field
      values.forEach((val) => {
        if (this.entityLists.get(am.lookupEntityName!)) {
          let match: SelectOption = this.entityLists
            .get(am.lookupEntityName!)!
            .find((option) => option.value === val.id)!;
          if (match) {
            matches.push(match);
          }
        }
      });
      control.setValue(matches);
    }
  }

  setDefaultValues() {
    this.visibleAttributeModels.forEach((am) => {
      let defaultValue: any = this.entityId ? undefined : am.defaultValue;
      if (this.isEnum(am) && am.defaultValue) {
        // look up correct value in enum values
        let match: any = this.enumMap
          .get(am.name)!
          .find((v) => v.value === am.defaultValue);
        defaultValue = match;
      } else if (this.isDate(am) && defaultValue) {
        defaultValue = parseISO(defaultValue! as string);
      } else if (this.isTime(am) && defaultValue) {
        defaultValue = timeToDate(defaultValue);
      } else if (this.isInstant(am) && defaultValue) {
        defaultValue = timestampToDate(defaultValue, true);
      } else if (this.isLocalDateTime(am) && defaultValue) {
        defaultValue = timestampToDate(defaultValue, false);
      }
      if (defaultValue) {
        this.mainForm.get(am.name)?.patchValue(defaultValue);
      }
    });
  }

  buildForm() {
    this.mainForm = this.formBuilder.group([]);

    this.visibleAttributeModels.forEach((am) => {
      let validators = createValidators(this.translate, am, false, false);
      if (!this.isNestedDetail(am)) {
        let control = this.formBuilder.control(
          {
            disabled: !this.isEditable(am),
            value: undefined,
          },
          { validators: validators }
        );
        this.mainForm!.addControl(am.name, control);
      } else {
        // for nested details, create a FormArray
        let array = this.formBuilder.array([], validators);
        this.mainForm!.addControl(am.name, array);
      }
    });

    this.setDefaultValues();

    if (this.postProcessInputForm != null) {
      this.postProcessInputForm(this.mainForm);
    }
  }

  getFormArray(am: AttributeModelResponse): FormArray {
    return this.mainForm?.get(am.name) as FormArray;
  }

  getErrorString(attribute: string): string {
    return getErrorString(attribute, this.mainForm!, this.translate);
  }

  save(): void {
    if (this.confirmSave === true) {
      var callback = (event: any): void => {
        this.doSave();
      };
      this.confirmService.confirm('save_confirmation', callback);
    } else {
      this.doSave();
    }
  }

  detailIdMap: Map<string, number[]> = new Map<string, number[]>();

  doSave() {
    this.detailIdMap.clear();
    this.attributeModels.forEach((am) => {
      if (this.isNestedDetail(am)) {
        let rows = (this.editObject[am.name] as Array<any>) || [];
        if (rows.length > 0) {
          let rowsIds = rows.map((row) => row['id']);
          this.detailIdMap.set(am.name, rowsIds);
        }
      }
    });

    let editObject: any = {};

    console.log(JSON.stringify(this.editObject));

    if (!this.mainForm!.valid) {
      this.messageService.error(this.translate.instant('form_not_valid'));
      this.mainForm!.markAllAsTouched();
      return;
    }

    if (this.additionalValidation) {
      let valMessage = this.additionalValidation(this.mainForm);
      if (valMessage) {
        this.messageService.error(valMessage);
        this.mainForm!.markAllAsTouched();
        return;
      }
    }

    this.convertBeforeSave(editObject);

    this.hiddenFieldService?.getFieldValues().forEach((val, key) => {
      editObject[key] = this.wrapInObject(key, val);
    });

    console.log(JSON.stringify(editObject));

    this.loading = true;
    if (this.editObject['id']) {
      this.handlePut(editObject);
    } else {
      this.handlePost(editObject);
    }
  }

  private wrapInObject(attribute: string, value: any): any {
    let am = this.findAttributeModel(attribute);
    if (this.isMaster(am)) {
      return { id: value };
    } else if (this.isFreeDetail(am)) {
      if (Array.isArray(value)) {
        let arr = value as any[];
        return arr.map((val) => {
          id: val;
        });
      }
    }
    return value;
  }

  /**
   * Binds form values to entity fields before executing the "save" action
   * @param editObject the entity being edited
   */
  private convertBeforeSave(editObject: any) {
    this.attributeModels.forEach((am) => {
      let control = this.mainForm!.get(am.name);
      if (control && control.value && this.isEditable(am)) {
        if (this.isMaster(am)) {
          // send an object containing an ID
          if (control.value.value) {
            editObject[am.name] = {
              id: control.value.value,
            };
          }
        } else if (this.isNestedDetail(am)) {
          this.convertNestedDetails(control, am, editObject);
        } else if (this.isFreeDetail(am)) {
          // send an array of objects each containing an ID
          let selected: SelectOption[] = control.value;
          if (selected) {
            let ids: any[] = [];
            selected.forEach((sel) => {
              ids.push({
                id: sel.value,
              });
            });
            editObject[am.name] = ids;
          }
        } else if (this.isEnum(am)) {
          editObject[am.name] = control.value.value;
        } else if (this.isDate(am)) {
          editObject[am.name] = formatISO(control.value, {
            representation: 'date',
          });
        } else if (this.isInstant(am) || this.isLocalDateTime(am)) {
          editObject[am.name] = dateToTimestamp(
            control.value,
            this.isInstant(am)
          );
        } else if (this.isTime(am)) {
          // format as time, take first 4 chars only
          editObject[am.name] = stringToTime(control.value);
        } else if (this.isElementCollection(am)) {
          let val = control.value;
          if (Array.isArray(val)) {
            editObject[am.name] = control.value;
          }
        } else {
          editObject[am.name] = control.value;
        }
      }
    });
  }

  /**
   * Handles a PUT request to update an existing entity
   * @param editObject the edit object
   */
  private handlePut(editObject: any) {
    let putObservable = this.service.put(
      this.entityName,
      this.editObject['id'],
      JSON.stringify(editObject),
      this.entityModelReference
    );
    if (this.actionId) {
      putObservable = this.service.executeAction(
        this.entityName,
        this.actionId,
        JSON.stringify(editObject),
        this.entityModelReference,
        this.entityId?.toLocaleString()
      );
    }

    putObservable
      .pipe(
        concatMap((req) => concat(...this.createUploadFileObservables(req))),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (v: any) => { },
        error: (error) =>
          error.error
            ? this.messageService.error(error.error.message)
            : this.messageService.error('Unknown error'),
        complete: () => {
          this.afterPut();
        },
      });
  }

  private afterPut() {
    let msg = this.translate.instant('entity_update', {
      name: this.entityModel!.displayNames[this.locale],
      id: this.entityId,
    });
    this.messageService.info(msg);

    if (this.navigationAllowed && !this.openInViewMode) {
      this.router.navigate([this.navigateBackRoute]);
    } else {
      this.viewMode = this.openInViewMode || this.readOnly || false;
      this.bindExistingEntity(true);
    }
  }

  private handlePost(editObject: any) {
    let postObservable = this.service.post(
      this.entityName,
      JSON.stringify(editObject),
      this.entityModelReference
    );
    if (this.actionId) {
      postObservable = this.service.executeAction(
        this.entityName,
        this.actionId,
        JSON.stringify(editObject),
        this.entityModelReference
      );
    }

    let fileInteractions = this.fileMap.size > 0 || this.mustClearFileUploads();

    if (fileInteractions) {
      // execute any upload-related actions
      postObservable
        .pipe(
          concatMap((entity) =>
            concat(...this.createUploadFileObservables(entity))
          ),
          finalize(() => {
            this.loading = false;
          })
        )
        .subscribe({
          next: (v: AbstractEntity) => {
            this.entityId = v.id! as any;
          },
          error: (error) => this.messageService.error(error.error.message),
          complete: () => {
            this.afterPost();
          },
        });
    } else {
      postObservable
        .pipe(
          finalize(() => {
            this.loading = false;
          })
        )
        .subscribe({
          next: (entity: AbstractEntity) => {
            this.entityId = entity.id! as any;
          },
          error: (error) => this.messageService.error(error.error.message),
          complete: () => {
            this.afterPost();
          },
        });
    }
  }

  afterPost() {
    let msg = this.translate.instant('entity_create', {
      name: this.entityModel!.displayNames[this.locale],
      id: this.entityId,
    });
    this.messageService.info(msg);

    if (this.navigationAllowed && !this.openInViewMode) {
      this.router.navigate([this.navigateBackRoute]);
    } else {
      this.bindExistingEntity(true);
      this.viewMode = this.openInViewMode || false;
    }
  }

  private createUploadFileObservables(
    v: AbstractEntity
  ): Observable<AbstractEntity>[] {
    let observables: Observable<AbstractEntity>[] = [];

    // create observables for clearing existing uploads
    this.fileClearMap.forEach((val, am) => {
      if (val === true) {
        observables.push(
          this.fileController
            .clear(v.id!.toString(), this.entityName, am!.name)
            .pipe(map((_) => v))
        );
      }
    });

    // create observables for uploading files
    this.fileMap.forEach((val, am) => {
      let file = this.fileMap.get(am);
      if (file) {
        observables.push(
          this.fileController
            .upload(
              v.id!.toString(),
              this.entityName,
              am!.name,
              this.fileMap.get(am!)!
            )
            .pipe(map((_) => v))
        );
      }
    });
    return observables;
  }

  /**
   * Converts nested detail values. This is needed when a
   * nested grid contains items that have been selected
   * in a dropdown or multi-select
   *
   * @param control the control (a FormArray)
   * @param am the attribute model
   * @param editObject the object that is being edited
   */
  private convertNestedDetails(
    control: AbstractControl<any, any>,
    am: AttributeModelResponse,
    editObject: any
  ) {
    let nestedValue = control?.value;
    console.log('Nested value: ' + JSON.stringify(nestedValue));

    let nestedObjects: any[] = nestedValue as Array<any>;
    let rowIds = this.detailIdMap.get(am.name) || [];

    let index: number = 0;

    nestedObjects.forEach((no) => {
      this.nestedEntityModelMap.get(am.name)?.attributeModels.forEach((nam) => {
        if (this.isEnum(nam)) {
          if (no[nam.name]) {
            no[nam.name] = no[nam.name].value;
          }
        } else if (this.isMaster(nam)) {
          let val = no[nam.name];
          if (val && val.value) {
            no[nam.name] = {
              id: val.value,
            };
          }
        } else if (this.isDate(nam)) {
          let val = no[nam.name];
          if (val) {
            no[nam.name] = formatISO(val, { representation: 'date' });
          }
        } else if (this.isInstant(nam) || this.isLocalDateTime(nam)) {
          let val = no[nam.name];
          if (val) {
            no[nam.name] = dateToTimestamp(val, this.isInstant(nam));
          }
        } else if (this.isTime(nam)) {
          let val = no[nam.name];
          if (val) {
            no[nam.name] = stringToTime(val);
          }
        } else {
          // nothing specific needed
        }
      });
      if (rowIds && rowIds.length > index) {
        no['id'] = rowIds[index];
      }
      index++;
    });

    console.log('Nested value after: ' + JSON.stringify(nestedValue));
    editObject[am.name] = nestedObjects;
  }

  isEditable(am: AttributeModelResponse): boolean {
    if (this.entityId) {
      return (
        am.editableType === AttributeModelResponse.EditableTypeEnum.EDITABLE
      );
    }
    return (
      am.editableType === AttributeModelResponse.EditableTypeEnum.EDITABLE ||
      am.editableType === AttributeModelResponse.EditableTypeEnum.CREATE_ONLY
    );
  }

  getNewObjectCaption(): string {
    if (this.actionId) {
      return this.actionDisplayName!;
    }

    return this.translate.instant('create_entity_caption', {
      name: this.entityModel!.displayNames[this.locale],
    });
  }

  getUpdateObjectCaption(): string {
    if (this.actionId) {
      return this.actionDisplayName!;
    }

    let display: string = this.entityModel?.displayProperty || '';
    let displayProp = display ? getNestedValue(this.editObject, display) : 'Unknown';
    let captionKey = this.readOnly
      ? 'view_entity_caption'
      : 'update_entity_caption';

    return this.translate.instant(captionKey, {
      name: this.entityModel!.displayNames[this.locale],
      title: displayProp,
    });
  }

  mustClearFileUploads() {
    let mustClear: boolean = false;
    this.fileClearMap.forEach((val, am) => {
      if (val === true) {
        mustClear = true;
      }
    });
    return mustClear;
  }

  /**
   * Respond to a file upload event from a nested component
   * @param info data about the uploaded file
   */
  onFileUpload(info: FileUploadInfo) {
    this.fileMap.set(info.am, info.file);
    let fileName = info.fileName;

    if (info.am.fileNameAttribute) {
      let fileNameAm = this.findAttributeModel(info.am.fileNameAttribute)!;
      setNestedValue(this.editObject, fileNameAm, fileName);
    }
    if (this.fileClearMap.has(info.am)) {
      this.fileClearMap.set(info.am, false);
    }
  }

  /**
   * Respond to a "clear upload" event from a nested component
   * @param info data about the cleared property
   */
  onFileClear(info: FileClearInfo) {
    this.fileMap.delete(info.am);
    if (info.am.fileNameAttribute) {
      let fileNameAm = this.findAttributeModel(info.am.fileNameAttribute)!;
      setNestedValue(this.editObject, fileNameAm, undefined);
    }
    if (this.fileClearMap.has(info.am)) {
      this.fileClearMap.set(info.am, true);
    }
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    let control = this.mainForm!.get(am.name)!;
    if (this.isMaster(am)) {
      let val = getNestedValue(this.editObject, am.name);
      if (val) {
        if (this.entityLists.get(am.lookupEntityName!)) {
          let match: any = this.entityLists
            .get(am.lookupEntityName!)!
            .find((v) => v.value === val.id);
          control.setValue(match);
        }
      }
    } else if (this.isFreeDetail(am)) {
      this.bindFreeDetailField(am, control);
    }
  }

  useLookupField(am: AttributeModelResponse) {
    return am.selectMode === AttributeModelResponse.SelectModeEnum.LOOKUP;
  }

  back() {
    if (this.navigateBackRoute) {
      this.router.navigateByUrl(this.navigateBackRoute);
    }
  }

  backEnabled(): boolean {
    return this.navigationAllowed;
  }

  editMode() {
    this.viewMode = false;
    this.bindExistingEntity(false);
  }

  isUpdateMode(): boolean {
    if (this.entityId) {
      return true;
    }
    return false;
  }

  getNestedValue(obj: any, am: AttributeModelResponse): any {
    return getNestedValue(obj, am.name);
  }

  // the class to use for the total form width
  getFormWidthClass() {
    return this.formWidthClass;
  }

  // the class that determines the width of each column
  getColumnsClass() {
    if (this.numberOfColumns === 2) {
      return 'col-lg-6 col-md-6 col-sm-12';
    } else if (this.numberOfColumns === 3) {
      return 'col-lg-4 col-md-6 col-sm-12';
    }
    return 'col-lg-12 col-md-12 col-sm-12';
  }

  // the class that determines the actual component with (minus the validation error)
  getColClass() {
    return 'col-lg-9 col-md-8 col-sm-12';
  }

  getValidationColClass() {
    return 'col-lg-3 col-md-4 col-sm-12';
  }

  useGroupTabs(): boolean {
    return this.attributeGroupMode === AttributeGroupMode.TAB;
  }

  useGroupPanels(): boolean {
    return this.attributeGroupMode === AttributeGroupMode.PANEL;
  }

  callAdditionalAction(action: AdditionalFormAction): void {
    action.action(this.editObject);
  }

  /**
   * Filters the applicable additional (form) actions
   * @returns the form actions that are applicable given the currently active form mode
   */
  filterAdditionalActions(): AdditionalFormAction[] {
    if (this.viewMode) {
      return this.additionalActions.filter(
        (action) =>
          action.mode === AdditionalActionMode.BOTH ||
          action.mode == AdditionalActionMode.VIEW
      );
    }
    return this.additionalActions.filter(
      (action) =>
        action.mode === AdditionalActionMode.BOTH ||
        action.mode == AdditionalActionMode.EDIT
    );
  }

  /**
   * Checks whether an entity model action is enabled
   * @param action the action
   * @returns true when the action is enabled, false otherwise
   */
  isModelActionEnabled(action: EntityModelActionResponse): boolean {
    if (!this.modelActionEnabled) {
      return true;
    }

    return this.modelActionEnabled(action, this.editObject, this.mainForm)
  }

  /**
   * Checks whether an additional form action is enabled
   * @param action the action
   * @returns true when the action is disabled, false otherwise
   */
  isAdditionalFormActionDisabled(action: AdditionalFormAction): boolean {
    if (!action.enabled) {
      return false;
    }
    return !action.enabled(this.editObject);
  }

  getFormControl(am: AttributeModelResponse): FormControl<any> {
    return this.mainForm.get(am.name) as FormControl<any>;
  }

  closeDialog() {
    this.onClose.emit();
  }

  cancelEdit() {
    this.viewMode = true;
  }

  /**
   * Sets up cascading for any fields that support it
   */
  private setupCascading() {
    this.visibleAttributeModels.forEach((am) => {
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
                } else {
                  this.cascadeFilters.set(cascade.cascadeTo, []);
                  this.fillOptions(this.findAttributeModel(cascade.cascadeTo)!);
                }
              });
            }
          });
      }
    });
  }

  /**
   * Checks whether cascading is applicable for a certain field
   * @param cascade the cascade model
   * @returns true if cascading is applicable, false otherwise
   */
  isCascadeApplicable(cascade: CascadeModel): boolean {
    return (
      cascade.cascadeMode === CascadeModel.CascadeModeEnum.BOTH ||
      cascade.cascadeMode === CascadeModel.CascadeModeEnum.EDIT
    );
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

  /**
   * Filters the entity model actions, returning only those that are appropriate
   * inside the edit form
   * @returns the entity model actions that are appropriate
   */
  filterEntityModelActions() {
    if (!this.entityId) {
      return [];
    }

    return this.entityModel?.actions
      ?.filter(
        (action) => action.type == EntityModelActionResponse.TypeEnum.UPDATE
      )
      .filter(action => action.formMode == EntityModelActionResponse.FormModeEnum.BOTH
        || (action.formMode == EntityModelActionResponse.FormModeEnum.EDIT && !this.viewMode)
        || (action.formMode == EntityModelActionResponse.FormModeEnum.VIEW && this.viewMode)
      )
      .filter((action) => this.isActionAllowed(action));
  }

  /**
   * Executes an action defined in the entity model
   * @param viewContainerRef container ref
   * @param action the action to carry out
   * @param row the row
   */
  callModelAction(
    viewContainerRef: ViewContainerRef | undefined,
    action: EntityModelActionResponse
  ) {
    if (!viewContainerRef) {
      return;
    }

    let componentRef = viewContainerRef?.createComponent(
      EntityPopupDialogComponent
    );
    this.entityModelService
      .getActionEntityModel(
        this.entityName,
        action.id!,
        this.entityModelReference
      )
      .subscribe((model) => {
        if (componentRef) {
          componentRef.instance.actionId = action.id;
          componentRef.instance.entityModel = model;
          componentRef.instance.entityName = this.entityName;
          componentRef.instance.fieldFilters = this.fieldFilters;
          componentRef.instance.entityId = this.entityId;
          componentRef.instance.readOnly = false;

          var callback = (event: any): void => {
            this.service
              .get(
                this.entityName,
                this.entityId!.toLocaleString(),
                this.entityModelReference
              )
              .subscribe((entity) => this.bindExistingEntity(entity));
          };
          componentRef.instance.onDialogClosed = callback;
          componentRef.instance.showDialog();
        }
      });
  }

  showFormFillDialog() {
    let componentRef = this.formFillContainerRef?.createComponent(
      AutoFillDialogComponent
    );

    componentRef.instance.entityName = this.entityName;
    componentRef.instance.entityModelReference = this.entityModelReference;
    var callback = (event: any): void => {
      this.bindFields(event);
    };
    componentRef.instance.onFormFillCompleted = callback;
    componentRef.instance.openDialog();
  }

  /**
   * Respond to a successful form fill event
   * @param event the form fill event
   */
  formFill(event: any) {
    console.log(JSON.stringify(event));
    this.bindFields(event);
  }

  /**
   * Determines whether to display an input field at full width
   * @param am the attribute model
   * @returns whether to display the input field at full width
   */
  showFullWidth(am: AttributeModelResponse): boolean {
    return (am.groupTogetherWith && am.groupTogetherWith?.length > 0)
      || this.isGroupedWithOther(this.visibleAttributeModels, am)
  }

  /**
   * Determines whether an attribute is currently visible
   * @param am the attribute model
   * @returns
   */
  isAttributeVisible(am: AttributeModelResponse) {
    if (this.attributeVisible) {
      return this.attributeVisible(am, this.editObject, this.mainForm);
    }
    return true;
  }


}
