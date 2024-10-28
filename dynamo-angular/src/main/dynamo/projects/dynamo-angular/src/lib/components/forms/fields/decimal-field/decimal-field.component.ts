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
import { getSimpleLocale } from '../../../../functions/functions';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { BaseComponent } from '../../../base/base.component';
import { TooltipModule } from 'primeng/tooltip';
import { InputNumberModule } from 'primeng/inputnumber';
import { ReactiveFormsModule } from '@angular/forms';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'd-decimal-field',
  standalone: true,
  imports: [TooltipModule, ReactiveFormsModule, MessageModule, InputNumberModule],
  templateUrl: './decimal-field.component.html',
  styleUrl: './decimal-field.component.css'
})
export class DecimalFieldComponent extends BaseComponent {

  getSimpleLocale = getSimpleLocale;

  @Input() searchMode: boolean = false;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const translate = inject(TranslateService);

    super(translate)
  }

  getDecimalMode(am: AttributeModelResponse): string {
    return am.currencyCode ? 'currency' : 'decimal';
  }

  getSuffix(am: AttributeModelResponse): string | undefined {
    if (am.percentage) {
      return '%';
    }
    return undefined;
  }
}
