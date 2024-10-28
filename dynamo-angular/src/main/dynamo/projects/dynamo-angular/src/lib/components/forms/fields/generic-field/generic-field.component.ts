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
import { FormControl } from '@angular/forms';
import { BaseComponent } from '../../../base/base.component';
import { EntityModelResponse } from '../../../../interfaces/model/entityModelResponse';
import { FilterModel } from '../../../../interfaces/model/filterModel';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { DecimalFieldComponent } from '../decimal-field/decimal-field.component';
import { DateFieldComponent } from '../date-field/date-field.component';
import { BooleanFieldComponent } from '../boolean-field/boolean-field.component';
import { TimestampFieldComponent } from '../timestamp-field/timestamp-field.component';
import { TimeFieldComponent } from '../time-field/time-field.component';
import { SelectEntityFieldComponent } from '../select-entity-field/select-entity-field.component';
import { SelectManyFieldComponent } from '../select-many-field/select-many-field.component';
import { EnumFieldComponent } from '../enum-field/enum-field.component';
import { NumberFieldComponent } from '../number-field/number-field.component';
import { ElementCollectionFieldComponent } from '../element-collection-field/element-collection-field.component';
import { StringFieldComponent } from '../string-field/string-field.component';

@Component({
  selector: 'd-generic-field',
  standalone: true,
  imports: [DecimalFieldComponent, DateFieldComponent, BooleanFieldComponent, TimestampFieldComponent, TimeFieldComponent, DateFieldComponent, SelectEntityFieldComponent, SelectManyFieldComponent, EnumFieldComponent, NumberFieldComponent, ElementCollectionFieldComponent, StringFieldComponent],
  templateUrl: './generic-field.component.html',
  styleUrl: './generic-field.component.css'
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
