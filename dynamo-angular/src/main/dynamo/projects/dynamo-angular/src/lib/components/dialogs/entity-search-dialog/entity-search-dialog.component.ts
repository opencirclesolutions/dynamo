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
import {Component, EventEmitter, forwardRef, Input, Output} from '@angular/core';
import {AttributeModelResponse} from '../../../interfaces/model/attributeModelResponse';
import {DialogModule} from 'primeng/dialog';

import {BaseCompositeComponent} from '../../forms/base-composite/base-composite.component';
import {TranslateModule} from "@ngx-translate/core";
import {SelectOption} from "../../../interfaces/select-option";
import {NG_VALUE_ACCESSOR} from "@angular/forms";
import LookupQueryTypeEnum = AttributeModelResponse.LookupQueryTypeEnum;
import {GenericSearchLayoutComponent} from "../../forms/search/generic-search-layout/generic-search-layout.component";

@Component({
  selector: 'd-entity-search-dialog',
  standalone: true,
  imports: [DialogModule, TranslateModule, GenericSearchLayoutComponent],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => EntitySearchDialogComponent),
      multi: true,
    },
  ],
  templateUrl: './entity-search-dialog.component.html',
  styleUrl: './entity-search-dialog.component.css'
})
export class EntitySearchDialogComponent
  extends BaseCompositeComponent {

  @Input() selectedIds: any[] = [];
  @Input() dialogVisible: boolean = false;
  @Input() multiSelect: boolean = false;
  @Input() queryType: LookupQueryTypeEnum = LookupQueryTypeEnum.PAGING;

  // the actually selected values
  selectedValues: SelectOption[] = [];
  // value cache, for holding the items selected in the popup
  valueCache: SelectOption[] = [];

  @Output() onRowSelect = new EventEmitter<any>();
  @Output() onChange = new EventEmitter<any>();

  protected onLookupFilled(am: AttributeModelResponse): void {
  }

  showDialog(): void {
    this.dialogVisible = true;
  }

  cancel(): void {
    this.dialogVisible = false;
  }

  /**
   * Closes the dialog and applies the selection
   */
  selectAndClose(): void {
    this.selectedValues = [];
    this.valueCache.forEach((element) => this.selectedValues.push(element));
    if (this.multiSelect) {
      this.onChange.emit(this.selectedValues);
    } else {
      this.onChange.emit(this.selectedValues[0]);
    }

    this.dialogVisible = false;
  }

}
