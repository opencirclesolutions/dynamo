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
import {Component, forwardRef, inject, Input, OnInit, ViewChild, ViewContainerRef} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {EntityModelResponse} from '../../../../interfaces/model/entityModelResponse';
import {BaseComponent} from '../../../base/base.component';
import {PagingModel} from '../../../../interfaces/model/pagingModel';
import {FilterModel} from '../../../../interfaces/model/filterModel';
import {SelectOption} from '../../../../interfaces/select-option';
import {AuthenticationService} from '../../../../services/authentication.service';
import {EntityPopupDialogComponent} from '../../../dialogs/entity-popup-dialog/entity-popup-dialog.component';
import {truncateDescriptions} from '../../../../functions/entitymodel-functions';
import {DialogModule} from 'primeng/dialog';
import {EntitySearchDialogComponent} from "../../../dialogs/entity-search-dialog/entity-search-dialog.component";

@Component({
  selector: 'd-lookup-field',
  standalone: true,
  imports: [TranslateModule, DialogModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => LookupFieldComponent),
      multi: true,
    },
  ],
  templateUrl: './lookup-field.component.html',
  styleUrl: './lookup-field.component.css'
})
export class LookupFieldComponent
  extends BaseComponent
  implements ControlValueAccessor, OnInit {
  private authService = inject(AuthenticationService);

  // the name of the entity to display
  @Input({required: true}) entityName: string = '';
  // optional reference to more specific entity model
  @Input() entityModelReference?: string;
  // entity model
  @Input() entityModel?: EntityModelResponse;

  // the name of the property that is used as the display value
  @Input({required: true}) displayPropertyName: string = '';
  // whether selecting multiple options is allowed
  @Input({required: true}) multiSelect: boolean = false;
  // the class to apply to the div that holds the components. set to "row" for responsive behavior by default
  @Input() rowClass: string = 'row';
  // the default filters to apply to every search
  @Input() defaultFilters: FilterModel[] = [];
  //
  @Input() class: string = 'main';
  // whether to display a quick add button
  @Input() showQuickAddButton: boolean = true;
  // whether to display a quick add button
  @Input() queryType: PagingModel.TypeEnum = PagingModel.TypeEnum.ID_BASED;

  onChange: any = () => {
  };
  onTouched: any = () => {
  };

  // the actually selected values
  selectedValues: SelectOption[] = [];
  // value cache, for holding the items selected in the popup
  valueCache: SelectOption[] = [];
  //dialogVisible: boolean = false;
  disabled: boolean = false;
  selectedIds: any[] = [];
  header: string = '';

  // container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', {read: ViewContainerRef})
  popupDialogRef!: ViewContainerRef;

  @ViewChild('searchDialogContainerRef', {read: ViewContainerRef})
  searchDialogRef!: ViewContainerRef;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const translate = inject(TranslateService);
    super(translate);
  }

  ngOnInit(): void {
  }

  showDialog(): void {
    console.log("Trying to open dialog2!");

    this.valueCache = [];
    this.selectedValues.forEach((element) => this.valueCache.push(element));
    this.selectedIds = [];
    this.selectedValues.forEach((element) =>
      this.selectedIds.push(element.value)
    );

    this.header = this.translate.instant('search_header', {title: ''});

    // let componentRef = this.searchDialogRef.createComponent(EntitySearchDialogComponent);
    // componentRef.instance.entityModelReference = this.entityModelReference;
    // componentRef.instance.entityName = this.entityName;
    // componentRef.instance.selectedIds = this.selectedIds;
    // componentRef.instance.multiSelect = this.multiSelect;
    // componentRef.instance.defaultFilters = this.defaultFilters;
    // // // TODO: query type
    // componentRef.instance.showDialog();
  }

  showQuickAddDialog() {
    console.log('Showing quick add dialog');
    let componentRef = this.popupDialogRef.createComponent(EntityPopupDialogComponent);
    componentRef.instance.entityModelReference = this.entityModelReference;
    componentRef.instance.entityName = this.entityName;
    componentRef.instance.readOnly = false;
    componentRef.instance.onDialogClosed = (obj: any): void => {
      this.afterQuickAddDialogClosed(obj);
    };
    componentRef.instance.showDialog();
  }

  // showSearchDialog(): void {
  //   let componentRef = this.searchDialogRef.createComponent(EntitySearchDialogComponent);
  //   componentRef.instance.entityModelReference = this.entityModelReference;
  //   componentRef.instance.entityName = this.entityName;
  //   //componentRef.instance.readOnly = false;
  //   // componentRef.instance.onDialogClosed = (obj: any): void => {
  //   //   this.afterQuickAddDialogClosed(obj);
  //   // };
  //   componentRef.instance.showDialog();
  // }

  afterQuickAddDialogClosed(obj: any) {
    if (!this.multiSelect) {
      this.selectedValues = [];
      this.selectedIds = [];
    }
    this.selectedValues.push({
      value: obj.id,
      name: obj[this.displayPropertyName],
    });
  }

  getHeader(): string {
    return this.header;
  }

  /**
   * Closes the dialog and applies the selection
   */
  // selectAndClose(): void {
  //   this.selectedValues = [];
  //   this.valueCache.forEach((element) => this.selectedValues.push(element));
  //   if (this.multiSelect) {
  //     this.onChange(this.selectedValues);
  //   } else {
  //     this.onChange(this.selectedValues[0]);
  //   }
  //
  //   //this.dialogVisible = false;
  // }

  /**
   * Respond to selection of a row in the popup dialog
   * @param event the row select event (contains the entire row object)
   */
  onRowSelect(event: any) {
    if (this.multiSelect) {
      let match = this.valueCache.find((val) => val.value === event.id);
      if (!match) {
        this.valueCache.push({
          value: event.id,
          name: event[this.displayPropertyName],
        });
      }
    } else {
      this.valueCache = [];
      this.valueCache.push({
        value: event.id,
        name: event[this.displayPropertyName],
      });
    }
  }

  clear() {
    this.selectedValues = [];
    this.onChange(this.selectedValues);
  }

  getSelectedValueString() {
    if (!this.selectedValues || this.selectedValues.length == 0) {
      return undefined;
    }
    return truncateDescriptions(this.selectedValues, 'name', this.translate);
  }

  writeValue(obj: any): void {
    if (obj) {
      if (this.multiSelect) {
        this.selectedValues = obj;
      } else {
        this.selectedValues = [];
        this.selectedValues.push(obj);
      }
    } else {
      this.selectedValues = [];
    }
  }

  registerOnChange(onChange: any) {
    this.onChange = onChange;
  }

  registerOnTouched(onTouched: any): void {
    this.onTouched = onTouched;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  isQuickAddButtonVisible() {
    return this.am?.quickAddAllowed && this.showQuickAddButton && this.isWriteAllowed();
  }

  isWriteAllowed(): boolean {
    if (!this.entityModel) {
      return false;
    }

    if (!this.entityModel.writeRoles || this.entityModel.writeRoles.length == 0) {
      return true;
    }
    return this.authService.hasRole(this.entityModel.writeRoles)
  }
}
