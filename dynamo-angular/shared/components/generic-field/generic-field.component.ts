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
import {  FormControl } from '@angular/forms';
import { AttributeModelResponse, EntityModelResponse, FilterModel } from 'dynamo/model';

@Component({
  selector: 'app-generic-field',
  templateUrl: './generic-field.component.html',
  styleUrls: ['./generic-field.component.scss'],
})
export class GenericFieldComponent extends BaseComponent {
  @Input() enumValues?: any[];
  @Input() options?: any[];
  // the form control corresponding to this component
  @Input() formControl?: FormControl<any>;
  @Input() entityModel?: EntityModelResponse;
  @Input() fieldFilters?: FilterModel[] | undefined = [];

  getFormControl(am: AttributeModelResponse): FormControl<any> {
    return this.formGroup?.get(am.name) as FormControl<any>;
  }

  // the class that determines the actual component with (minus the validation error)
  getColClass() {
    return 'col-lg-9 col-md-8 col-sm-12';
  }

  getValidationColClass() {
    return 'col-lg-3 col-md-4 col-sm-12';
  }
}
