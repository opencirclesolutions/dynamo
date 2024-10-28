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
import { Component, Input, ViewChild, ViewContainerRef, inject } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { BaseComponent } from '../base/base.component';
import { EntityModelResponse } from '../../interfaces/model/entityModelResponse';
import { SelectOption } from '../../interfaces/select-option';
import { FilterModel } from '../../interfaces/model/filterModel';
import { AuthenticationService } from '../../services/authentication.service';
import { EntityPopupDialogComponent } from '../dialogs/entity-popup-dialog/entity-popup-dialog.component';

@Component({
  selector: 'd-base-entity',
  standalone: true,
  imports: [],
  templateUrl: './base-entity.component.html',
  styleUrl: './base-entity.component.css'
})
export abstract class BaseEntityComponent extends BaseComponent {
  private authService = inject(AuthenticationService);


  // entity model
  @Input() entityModel?: EntityModelResponse;
  // the available options in case of a dropdown
  @Input({ required: true }) options: SelectOption[] = [];
  // the optional entity model reference
  @Input() entityModelReference?: string;
  // whether to display the "quick add" button
  @Input() showQuickAddButton: boolean = true;
  // default filters to use
  @Input() defaultFilters: FilterModel[] = [];
  // the form control corresponding to this component
  @Input() formControl?: AbstractControl<any>;

  // container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  viewContainerRef!: ViewContainerRef;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const translate = inject(TranslateService);

    super(translate)
  }

  /**
   * Displays the dialog that can be used to quickly add a new entity
   */
  showQuickAddDialog() {
    this.storeValue();
    let componentRef = this.viewContainerRef.createComponent(EntityPopupDialogComponent);
    componentRef.instance.entityModelReference = this.entityModelReference;
    componentRef.instance.entityName = this.am.lookupEntityName!;
    componentRef.instance.readOnly = false;

    let callback = (obj: any): void => {
      this.afterQuickAddDialogClosed(obj);
    };
    componentRef.instance.onDialogClosed = callback;
    componentRef.instance.showDialog();
  }

  afterQuickAddDialogClosed(obj: any): void {
    let newObject: SelectOption = {
      value: obj.id,
      name: obj[this.am.displayPropertyName!],
    };
    this.options.push(newObject);
    this.doSetNewValue(newObject)
  }

  /**
   * Sets the newly created entity as the currently selected item
   * @param newValue the newly created entity
   */
  abstract doSetNewValue(newValue: SelectOption): void;

  storeValue(): void {
  }

  isQuickAddButtonVisible() {
    return this.am?.quickAddAllowed && this.showQuickAddButton && this.isWriteAllowed();
  }

  getFieldColClass() {
    return this.isQuickAddButtonVisible() ? 'col-10' : 'col-12';
  }

  isWriteAllowed(): boolean {
    if (!this.entityModel) {
      return false;
    }

    if (!this.entityModel.createAllowed) {
      return false;
    }

    if (!this.entityModel.writeRoles || this.entityModel.writeRoles.length == 0) {
      return true;
    };
    return this.authService.hasRole(this.entityModel.writeRoles)
  }

}
