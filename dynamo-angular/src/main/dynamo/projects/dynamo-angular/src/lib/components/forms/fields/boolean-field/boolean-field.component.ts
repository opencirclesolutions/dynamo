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
import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BaseComponent } from '../../../base/base.component';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { TooltipModule } from 'primeng/tooltip';
import { ReactiveFormsModule } from '@angular/forms';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { CheckboxModule } from 'primeng/checkbox';

@Component({
  selector: 'd-boolean-field',
  standalone: true,
  imports: [TooltipModule, ReactiveFormsModule, ToggleButtonModule, CheckboxModule],
  templateUrl: './boolean-field.component.html',
  styleUrl: './boolean-field.component.css'
})
export class BooleanFieldComponent extends BaseComponent {

  constructor(translate: TranslateService) {
    super(translate);
  }

  useCheckbox(am: AttributeModelResponse) {
    return (
      am.booleanFieldMode ==
      AttributeModelResponse.BooleanFieldModeEnum.CHECKBOX
    );
  }

  useToggle(am: AttributeModelResponse) {
    return (
      am.booleanFieldMode == AttributeModelResponse.BooleanFieldModeEnum.TOGGLE
    );
  }

  useSwitch(am: AttributeModelResponse) {
    return am.booleanFieldMode == AttributeModelResponse.BooleanFieldModeEnum.SWITCH
  }


}
