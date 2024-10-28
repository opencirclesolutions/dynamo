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
import { Component, Input } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BaseComponent } from '../../../base/base.component';
import { NotificationService } from '../../../../services/notification.service';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { MessageModule } from 'primeng/message';
import { ElementCollectionDialogComponent } from '../../../dialogs/element-collection-dialog/element-collection-dialog.component';
import { TooltipModule } from 'primeng/tooltip';
import { ReactiveFormsModule } from '@angular/forms';
import { ChipsModule } from 'primeng/chips';

@Component({
  selector: 'd-element-collection-field',
  standalone: true,
  imports: [MessageModule, TooltipModule, ChipsModule, ReactiveFormsModule, ElementCollectionDialogComponent],
  templateUrl: './element-collection-field.component.html',
  styleUrl: './element-collection-field.component.css'
})
export class ElementCollectionFieldComponent extends BaseComponent {

  @Input() searchMode: boolean = false;

  constructor(
    private messageService: NotificationService,
    translate: TranslateService
  ) {
    super(translate);
  }

  useChips(): boolean {
    return (
      this.am?.elementCollectionMode ===
      AttributeModelResponse.ElementCollectionModeEnum.CHIPS
    );
  }

  useDialog(): boolean {
    return (
      this.am?.elementCollectionMode ===
      AttributeModelResponse.ElementCollectionModeEnum.DIALOG
    );
  }

  /**
   * Validates the input after the user adds a new value
   * @param event the new value event
   */
  validateInput(event: any) {
    let am = this.am;
    if (
      am.elementCollectionType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.STRING
    ) {
      this.manageStringField(am, event);
    }

    if (
      am.elementCollectionType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.INTEGRAL
    ) {
      this.manageNumberField(am, event);
    }
  }

  /**
   * Manages the addition of a a string value
   * @param am the attribute model
   * @param event the event that fires after the user adds the value
   */
  private manageStringField(am: AttributeModelResponse, event: any) {
    let values = this.formGroup!.get(am.name)!.value as any[];
    let val = event.value as string;

    // prevent duplicates
    let index: number = values.findIndex((a) => a === val);
    if (index >= 0 && index !== values.length - 1) {
      values.pop();
    }

    // minimum/maximum checking not applicable to search mode
    if (this.searchMode) {
      return;
    }

    if (val && am.maxLength && val.length > am.maxLength) {
      values.pop();
      this.messageService.error(
        this.translate.instant('maximum_length', {
          maxLength: am.maxLength,
        })
      );
    }
    if (val && am.minLength && val.length < am.minLength) {
      values.pop();
      this.messageService.error(
        this.translate.instant('minimum_length', {
          minLength: am.minLength,
        })
      );
    }
  }

  /**
   * Manages the addition of a number field
   * @param am the attribute model
   * @param event the event that fires after the user adds the value
   */
  private manageNumberField(am: AttributeModelResponse, event: any) {
    let values = this.formGroup!.get(am.name)!.value as any[];

    if (isNaN(Number(event.value))) {
      values.pop();
      this.messageService.error(this.translate.instant('not_a_number'));
    }

    let number = parseInt(event.value);

    // prevent duplicates
    let index: number = values.findIndex((a) => a == number);
    if (index >= 0 && index !== values.length - 1) {
      values.pop();
    }

    // minimum/maximum checking not applicable to search mode
    if (this.searchMode) {
      return;
    }

    if (am.maxValue && number > am.maxValue) {
      values.pop();
      this.messageService.error(
        this.translate.instant('maximum_value', {
          maxValue: am.maxValue,
        })
      );
    }

    if (am.minValue && number < am.minValue) {
      values.pop();
      this.messageService.error(
        this.translate.instant('minimum_value', {
          minValue: am.minValue,
        })
      );
    }
  }

}
