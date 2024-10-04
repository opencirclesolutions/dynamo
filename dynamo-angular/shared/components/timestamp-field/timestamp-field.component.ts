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

@Component({
  selector: 'app-timestamp-field',
  templateUrl: './timestamp-field.component.html',
  styleUrls: ['./timestamp-field.component.scss']
})
export class TimestampFieldComponent extends BaseComponent {

  @Input() searchMode: boolean = false;

  constructor(translate: TranslateService) {
    super(translate)
  }

  override getCalendarDateFormat(format: string) {
    //return getCalendarDateFormat(format);
    // PrimeNG calendar has wonky formatting that matches neither the JavaScript nor the java format
    return 'dd-mm-yy';
  }
}
