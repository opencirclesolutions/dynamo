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
import { Component, Input, OnInit, QueryList, TemplateRef, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { AdditionalFormAction } from '../../../interfaces/action';
import { OverrideFieldDirective } from '../../../directives/override-field.directive';
import { HiddenFieldService } from '../../../services/hidden-field.service';
import { AttributeModelResponse } from '../../../interfaces/model/attributeModelResponse';
import { GenericFormComponent } from '../../forms/generic-form/generic-form.component';
import { DialogModule } from 'primeng/dialog';

import { BaseCompositeComponent } from '../../forms/base-composite/base-composite.component';

@Component({
  selector: 'd-entity-popup-dialog',
  standalone: true,
  imports: [GenericFormComponent, DialogModule],
  templateUrl: './entity-popup-dialog.component.html',
  styleUrl: './entity-popup-dialog.component.css'
})
export class EntityPopupDialogComponent
  extends BaseCompositeComponent
  implements OnInit {
  // the ID of the entity that is being edited
  @Input() entityId?: number = undefined;
  // whether to open the form in read-only mode
  @Input() openInViewMode: boolean = false;
  // whether the component is in read-only mode
  @Input() readOnly: boolean = false;
  // post process input form (e.g. to add dependencies between components)
  @Input() postProcessInputForm?: (formGroup: FormGroup) => void;
  // method to call when the user closes the dialog
  @Input() onDialogClosed?: (event: any) => void = (event) => { };
  // additional actions to be placed in the button bar
  @Input() additionalActions: AdditionalFormAction[] = [];
  // additional validation function to carry out before submitting the form
  @Input() additionalValidation?: (formGroup: FormGroup) => string | undefined;
  // custom validator template
  @Input() customValidatorTemplate?: TemplateRef<any>;
  // optional ID for model-based action
  @Input() actionId?: string;
  // display name of action
  @Input() actionDisplayName?: string;
  // custom inputs (injected from split layout)
  @Input() injectedCustomInputs?: QueryList<OverrideFieldDirective>;
  // hidden field service
  @Input() injectedHiddenFieldService?: HiddenFieldService;

  // custom inputs (injected from split layout)
  @Input() popupDialogWidth: string = '75vw';

  @ViewChild(GenericFormComponent) form: GenericFormComponent | undefined;

  dialogVisible: boolean = false;

  ngOnInit(): void { }

  showDialog(): void {
    this.dialogVisible = true;
  }

  closeDialog(): void {
    this.dialogVisible = false;
  }

  saveAndCloseDialog(): void {
    if (this.form) {
      this.form.save();
    }
  }

  /**
   * Callback method that fires after the save method in the nested form is clicked
   * @param event the event to respond to
   */
  afterSave(event: any) {
    this.dialogVisible = false;
    if (this.readOnly === false && this.onDialogClosed) {
      this.onDialogClosed(event);
    }
  }

  protected override onLookupFilled(am: AttributeModelResponse): void { }

  getDialogWidth() {
    //return `{width: '80vw'}`

    return "{width : '90vw'}"
  }
}
