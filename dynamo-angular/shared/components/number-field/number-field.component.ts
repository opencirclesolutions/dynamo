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
import { AttributeModelResponse } from 'dynamo/model';
import { TranslateService } from '@ngx-translate/core';

/**
 * A component for displaying a number field (with input mask, optional currency and percentage signs, and
 * spinner buttons)
 */
@Component({
  selector: 'app-number-field',
  templateUrl: './number-field.component.html',
  styleUrls: ['./number-field.component.scss']
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
