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
import { BaseComponent } from '../base-component';
import { TranslateService } from '@ngx-translate/core';
import { AttributeModelResponse } from 'dynamo/model';

/**
 * A component for managing an enumeration
 */
@Component({
  selector: 'app-enum-field',
  templateUrl: './enum-field.component.html',
  styleUrls: ['./enum-field.component.scss']
})
export class EnumFieldComponent extends BaseComponent {

  @Input({ required: true }) options!: any[];
  @Input() searchMode: boolean = false;

  constructor(translate: TranslateService) {
    super(translate)
  }

  useRadioButton() {
    if (this.searchMode) {
      return false;
    }
    return this.am.enumFieldMode === AttributeModelResponse.EnumFieldModeEnum.RADIO;
  }
}
