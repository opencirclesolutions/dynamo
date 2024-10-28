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
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { ReactiveFormsModule } from '@angular/forms';
import { TooltipModule } from 'primeng/tooltip';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'd-number-field',
  standalone: true,
  imports: [ReactiveFormsModule, TooltipModule, MessageModule, InputNumberModule],
  templateUrl: './number-field.component.html',
  styleUrl: './number-field.component.css'
})
export class NumberFieldComponent extends BaseComponent {

  @Input() searchMode: boolean = false;

  constructor(translate: TranslateService) {
    super(translate)
  }

  isShowNumberSpinnerButtons(am: AttributeModelResponse) {
    return am.includeNumberSpinnerButton === true;
  }

}
