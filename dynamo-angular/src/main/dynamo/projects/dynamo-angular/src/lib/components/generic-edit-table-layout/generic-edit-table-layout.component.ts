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
import { Component, Input, OnChanges, SimpleChanges, ViewChild, ViewContainerRef, inject } from '@angular/core';
import { GenericTableComponent } from '../generic-table/generic-table.component';
import { Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { BaseCompositeCollectionComponent } from '../forms/base-composite-collection/base-composite-collection.component';
import { PopupButtonMode } from '../../interfaces/mode';
import { NotificationService } from '../../services/notification.service';
import { AuthenticationService } from '../../services/authentication.service';
import { DynamoConfig } from '../../interfaces/dynamo-config';
import { HiddenFieldService } from '../../services/hidden-field.service';
import { EntityPopupDialogComponent } from '../dialogs/entity-popup-dialog/entity-popup-dialog.component';
import { AttributeModelResponse } from '../../interfaces/model/attributeModelResponse';
import { TranslateModule } from '@ngx-translate/core';
import {Button} from "primeng/button";

@Component({
  selector: 'd-generic-edit-table-layout',
  standalone: true,
  imports: [TranslateModule, GenericTableComponent, Button],
  providers: [HiddenFieldService],
  templateUrl: './generic-edit-table-layout.component.html',
  styleUrl: './generic-edit-table-layout.component.css'
})
export class GenericEditTableLayoutComponent
  extends BaseCompositeCollectionComponent
  implements OnChanges {
  // additional validation function to carry out before submitting the form
  @Input() additionalValidation?: (formGroup: FormGroup) => string | undefined;

  @ViewChild(GenericTableComponent) table?: GenericTableComponent;

  //container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  vcr!: ViewContainerRef;

  defaultPageSize = 10;
  searchObject: any = {};
  tableCaptionPlural: string = '';
  loading: boolean = false;
  popupButtonMode: PopupButtonMode = PopupButtonMode.EDIT;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const messageService = inject(NotificationService);
    const router = inject(Router);
    const authService = inject(AuthenticationService);
    const hiddenFieldService = inject(HiddenFieldService);
    const configuration = inject<DynamoConfig>("DYNAMO_CONFIG" as any);

    super(messageService, router, authService, configuration);
    this.hiddenFieldService = hiddenFieldService;
  }

  ngOnChanges(changes: SimpleChanges): void { }

  ngOnInit(): void {
    this.entityModelService
      .getEntityModel(this.entityName, this.entityModelReference)
      .subscribe({
        next: (model) => {
          this.entityModel = model;
          this.tableCaptionPlural =
            this.entityModel.displayNamesPlural[this.locale];
        },
        error: (error) => this.messageService.error(error.error?.message),
      });
  }

  onSearch(searchObject: any) {
    this.searchObject = searchObject;
    this.loading = true;
    if (this.table) {
      this.table.searchObject = searchObject!;
      this.table.search();
    }
  }

  search() {
    if (this.table) {
      this.table.defaultFilters = this.defaultFilters;
      this.table!.search();
    }
  }

  searchComplete() {
    this.loading = false;
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // no action necessary
  }

  /**
   * Displays a popup dialog containing the selected entity
   * @param row the selected entity
   */
  openNewEntityDialog(): void {
    let componentRef = this.vcr!.createComponent(EntityPopupDialogComponent);

    componentRef.instance.entityModel = this.entityModel;
    componentRef.instance.entityModelReference = this.entityModelReference;
    componentRef.instance.entityName = this.entityName;
    componentRef.instance.fieldFilters = this.fieldFilters;
    componentRef.instance.additionalActions = this.additionalFormActions;
    componentRef.instance.postProcessInputForm = this.postProcessInputForm;
    componentRef.instance.additionalValidation = this.additionalValidation;
    componentRef.instance.customValidatorTemplate = this.customValidatorTemplate;
    componentRef.instance.injectedCustomInputs = this.customInputs;
    componentRef.instance.injectedHiddenFieldService = this.hiddenFieldService;
    componentRef.instance.readOnly = false;

    var callback = (obj: any): void => {
      this.search();
    };
    componentRef.instance.onDialogClosed = callback;
    componentRef.instance.showDialog();
  }
}
