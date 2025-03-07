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
import { Component, Input, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BaseComponent } from '../../../base/base.component';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { MessageModule } from 'primeng/message';
import { TooltipModule } from 'primeng/tooltip';
import { PasswordModule } from 'primeng/password';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'd-string-field',
  standalone: true,
  imports: [ReactiveFormsModule, MessageModule, TooltipModule, PasswordModule],
  templateUrl: './string-field.component.html',
  styleUrl: './string-field.component.css'
})
export class StringFieldComponent extends BaseComponent {

  @Input() allowTextAreas: boolean = true;
  @Input() classes: string = '';

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const translate = inject(TranslateService);

    super(translate)
  }

  isTextArea(am: AttributeModelResponse): boolean {
    return this.allowTextAreas && am.textFieldMode == AttributeModelResponse.TextFieldModeEnum.TEXTAREA;
  }

  isPassword(am: AttributeModelResponse): boolean {
    return am.textFieldMode == AttributeModelResponse.TextFieldModeEnum.PASSWORD;
  }

  isTextField(am: AttributeModelResponse): boolean {
    return !this.isTextArea(am) && !this.isPassword(am)
  }
}
