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
import {
  Component,
  ContentChildren,
  Input,
  QueryList,
  TemplateRef,
  ViewContainerRef,
} from '@angular/core';
import { BaseCompositeComponent } from './base-composite.component';
import { EntityPopupDialogComponent } from './entity-popup-dialog/entity-popup-dialog.component';
import { EntityModelActionResponse, PagingModel } from 'dynamo/model';
import {
  AdditionalFormAction,
  AdditionalGlobalAction,
  AdditionalRowAction,
} from '../model/additional-action';
import { OverrideFieldDirective } from '../directives/override-field-directive';
import { FormGroup } from '@angular/forms';
import { HiddenFieldService } from '../service/hidden-field-service';

/**
 * Base class for components that manage a collection of entities
 */
@Component({
  selector: 'app-base-composite-collection',
  template: ` <p>base works!</p> `,
  styles: [],
})
export abstract class BaseCompositeCollectionComponent extends BaseCompositeComponent {
  // the query type
  @Input() queryType: PagingModel.TypeEnum = PagingModel.TypeEnum.ID_BASED;
  // any global actions that are not tied to a specific entity
  @Input() additionalGlobalActions: AdditionalGlobalAction[] = [];
  // any additional actions to carry out
  @Input() additionalRowActions: AdditionalRowAction[] = [];
  // any additional actions to carry out (detail form)
  @Input() additionalFormActions: AdditionalFormAction[] = [];
  // custom validators
  @Input() customValidatorTemplate?: TemplateRef<any>;
  // post process input form
  @Input() postProcessInputForm?: (formGroup: FormGroup) => void;
  // the export mode
  @Input() exportMode: 'FULL' | 'ONLY_VISIBLE_IN_GRID' = 'ONLY_VISIBLE_IN_GRID';
  // the reference of the entity model to use for export
  @Input() exportModeReference: string = '';
  // service for managing hidden fields
  @Input() hiddenFieldService?: HiddenFieldService;

  // overridden input components
  @ContentChildren(OverrideFieldDirective, { descendants: true })
  customInputs!: QueryList<OverrideFieldDirective>;

  /**
   * Executes an action defined in the entity model
   * @param viewContainerRef container ref
   * @param action the action to carry out
   * @param row the row
   * @returns nothing
   */
  callModelAction(
    viewContainerRef: ViewContainerRef | undefined,
    action: EntityModelActionResponse,
    row: any
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
          componentRef.instance.actionDisplayName =
            action.displayNames[this.locale];
          componentRef.instance.entityModel = model;
          componentRef.instance.entityName = this.entityName;
          componentRef.instance.fieldFilters = this.fieldFilters;
          componentRef.instance.injectedCustomInputs = this.customInputs;
          componentRef.instance.injectedHiddenFieldService =
            this.hiddenFieldService;
          if (row) {
            componentRef.instance.entityId = row.id;
          }
          componentRef.instance.readOnly = false;

          var callback = (event: any): void => {
            this.afterActionDialogClosed();
          };
          componentRef.instance.onDialogClosed = callback;
          componentRef.instance.showDialog();
        }
      });
  }

  filterEntityModelActions() {
    return this.entityModel?.actions
      ?.filter(
        (action) => action.type == EntityModelActionResponse.TypeEnum.CREATE
      )
      .filter((action) => this.isActionAllowed(action));
  }

  afterActionDialogClosed() {}
}
