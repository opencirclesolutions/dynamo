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
import {Component, Input, inject, forwardRef} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {BaseEntityComponent} from '../../../base-entity/base-entity.component';
import {SelectOption} from '../../../../interfaces/select-option';
import {AuthenticationService} from '../../../../services/authentication.service';
import {AttributeModelResponse} from '../../../../interfaces/model/attributeModelResponse';
import {PagingModel} from '../../../../interfaces/model/pagingModel';
import {MessageModule} from 'primeng/message';
import {TooltipModule} from 'primeng/tooltip';
import {LookupFieldComponent} from '../lookup-field/lookup-field.component';
import {NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {MultiSelectModule} from 'primeng/multiselect';
import {EntityPopupDialogComponent} from "../../../dialogs/entity-popup-dialog/entity-popup-dialog.component";
import {Button} from "primeng/button";

@Component({
  selector: 'd-select-many-field',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectManyFieldComponent),
      multi: true,
    },
  ],
  imports: [MessageModule, TooltipModule, ReactiveFormsModule, MultiSelectModule, forwardRef(() => LookupFieldComponent), ReactiveFormsModule, Button],
  templateUrl: './select-many-field.component.html',
  styleUrl: './select-many-field.component.css'
})
export class SelectManyFieldComponent extends BaseEntityComponent {

  /**
   * Just some random stuff
   */

  @Input() searchMode: boolean = false;
  @Input() random: string = '';

  cache: SelectOption[] = [];

  constructor(...args: unknown[]);

  constructor() {
    const authService = inject(AuthenticationService);
    const translate = inject(TranslateService);
    super(authService, translate);
  }

  useLookupField(am: AttributeModelResponse) {
    if (this.searchMode) {
      return (
        am.searchSelectMode === AttributeModelResponse.SelectModeEnum.LOOKUP
      );
    }
    return am.selectMode === AttributeModelResponse.SelectModeEnum.LOOKUP;
  }

  override doSetNewValue(newValue: any): void {
    this.cache.push(newValue);
    this.formControl?.patchValue(this.cache);
  }

  override storeValue(): void {
    this.cache = this.formControl?.value || [];
  }

  mapQueryType(input: AttributeModelResponse.LookupQueryTypeEnum) {
    if (input === AttributeModelResponse.LookupQueryTypeEnum.PAGING) {
      return PagingModel.TypeEnum.PAGING;
    }
    return PagingModel.TypeEnum.ID_BASED;
  }

  override showQuickAddDialog() {
    this.storeValue();

    let componentRef = this.viewContainerRef.createComponent(EntityPopupDialogComponent);
    componentRef.instance.entityModelReference = this.entityModelReference;
    componentRef.instance.entityName = this.am.lookupEntityName!;
    componentRef.instance.readOnly = false;
    componentRef.instance.onDialogClosed = (obj: any): void => {
      this.afterQuickAddDialogClosed(obj);
    };
    componentRef.instance.showDialog();
  }
}
