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
import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  TemplateRef
} from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { MenuItem } from 'primeng/api';
import { Router } from '@angular/router';
import { adjustTimestamp, prependUrl, getNestedValue } from '../../functions/functions';
import { ConfirmService } from '../../services/confirm.service';
import { TranslateEntityPipe } from '../../pipes/translate-entity.pipe';
import { TruncatePipe } from '../../pipes/truncate.pipe';
import { TranslateEnumPipe } from '../../pipes/translate-enum.pipe';
import { TranslateBooleanPipe } from '../../pipes/translate-boolean.pipe';
import { CommonModule } from '@angular/common';
import { TooltipModule } from 'primeng/tooltip';
import { ContextMenuModule } from 'primeng/contextmenu';
import { MultiSelectModule } from 'primeng/multiselect';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'd-data-table',
  standalone: true,
  imports: [TranslateModule, FormsModule, CommonModule, TooltipModule, ContextMenuModule, MultiSelectModule, TableModule, TranslateEntityPipe, TruncatePipe, TranslateEnumPipe, TranslateBooleanPipe],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.css'
})
export class DataTableComponent implements OnInit {
  prependUrl = prependUrl;
  adjustTimestamp = adjustTimestamp;

  // the columns to display in the table
  @Input({ required: true }) columns: TableColumn[] = [];
  // the locale to use
  @Input({ required: true }) locale: string = '';
  // whether the selection of multiple rows is allowed
  @Input() multiSelect: boolean = false;
  // the rows to display
  @Input() rows: any[] | undefined = [];
  // the page size
  @Input() pageSize: number = 10;
  // the total number of records in the result set
  @Input() totalRecords: number = 0;
  // whether data is being loaded
  @Input() loading: boolean = false;
  @Input() initialSortField: string = 'id';
  @Input() initialSortOrder: 1 | 0 | -1 = 0;
  @Input() enablePaginator: boolean = true;
  @Input() deleteEnabled: boolean = true;
  // whether navigating to details screen is enabled
  @Input() detailsModeEnabled: boolean = true;
  // whether to display a detail button
  @Input() showDetailButton: boolean = false;
  // IDs of currently selected rows
  @Input() selectedIds: any[] = [];
  // template for inserting custom buttons
  @Input() customButtonTemplate?: TemplateRef<any>;
  // context menu items for download
  @Input() contextMenuItems: MenuItem[] = [];
  // event handler for dealing with a row click
  @Output() rowClick: EventEmitter<Number> = new EventEmitter<Number>();
  // event handler for responding to a delete button click
  @Output() deleteButtonClick: EventEmitter<Number> =
    new EventEmitter<Number>();
  // respond to a lazy load event
  @Output() lazyLoad: EventEmitter<TableLazyLoadEvent> =
    new EventEmitter<TableLazyLoadEvent>();

  first: number = 0;
  last: number = this.first + this.pageSize;
  selectedColumns: TableColumn[] = [];

  constructor(
    private confirmService: ConfirmService,
    private translate: TranslateService,
    private router: Router,
  ) { }

  /**
   * Respond to a lazy load event, update first and last records
   * @param event
   */
  onLazyLoad(event: TableLazyLoadEvent) {

    this.first = event.first || 0;
    this.last =
      this.first + this.pageSize < (this.totalRecords || 0)
        ? this.first + this.pageSize
        : this.totalRecords || 0;


    this.lazyLoad.emit(event);
  }

  ngOnInit(): void {
    this.selectedColumns = this.columns;
  }

  /**
   * Listener that fires when a row in the table is selected
   * @param event the event (a.k.a. the entire row object)
   */
  onRowSelect(event: any) {
    let index = this.selectedIds.indexOf(event.id);
    if (index < 0) {
      if (!this.multiSelect) {
        this.selectedIds = [];
      }
      this.selectedIds.push(event.id);
    } else {
      this.selectedIds = this.selectedIds.slice(index + 1, index + 2);
    }
    if (this.detailsModeEnabled && !this.showDetailButton) {
      this.rowClick.emit(event);
    }
  }

  /**
   * Listener that fires when a delete button is clicked
   * @param event
   */
  onDeleteClick(event: any) {
    var callback = (e: any): void => {
      this.onDelete(event);
    };
    this.confirmService.confirm('delete_confirmation', callback);
  }

  private onDelete(event: any) {
    this.deleteButtonClick.emit(event.id);
  }

  onDetailButtonClick(event: any) {
    if (this.detailsModeEnabled && this.rowClick) {
      this.rowClick.emit(event);
    }
  }

  getRows(): any[] {
    return this.rows ? this.rows : [];
  }

  getTotalRecords(): number {
    return this.totalRecords ? this.totalRecords : 0;
  }

  getRowClass(row: any) {
    return this.selectedIds.indexOf(row.id) >= 0 ? 'selected' : 'not_selected';
  }

  getSelectedItemsLabel() {
    return this.translate.instant('selected_columns');
  }

  getCurrentPageReport() {
    return this.translate.instant('current_page_report', {
      first: this.first,
      last: this.last,
      totalRecords: this.totalRecords,
    });
  }

  navigate(row: any, col: TableColumn) {
    let link = `${col.navigateLink}/${row[col.field]['id']} `
    this.router.navigateByUrl(link)
  }

  getNestedValue(row: any, name: string) {
    return getNestedValue(row, name);
  }
}

export interface TableColumn {
  /**
   * Text field, if set, field is not used and instead this text is shown in column
   */
  text?: string;

  /**
   * Field name in row, used for dynamic data
   */
  field: string;

  /**
   * Header text
   */
  header: string;

  /**
   * Style object, e.g. {font-weight: '300'}
   */
  style?: object;

  /**
   * Optional date pipe, if set, will use a date pipe with the given format
   */
  datePipe?: {
    formats?: { [key: string]: string };
    instant: boolean;
  };

  /**
   * Use a decimal pipe for this field
   * digitsInfo: {min digits before decimal point}.{min digits after point}-{max digits after point}
   * locale: a locale/culture string, e.g. 'nl-NL'
   */
  numberFormat?: {
    digitsInfo?: string;
    locale?: string;
  };

  /**
   * Enumeration value translations
   */
  translateEnum?: Map<string, string>;

  sortable?: boolean;

  translateBoolean?: {
    trueRepresentations?: { [key: string]: string };
    falseRepresentations?: { [key: string]: string };
  };

  /**
   * Entity translation (the name of the property to use)
   */
  translateEntity?: {
    property: string;
  };

  percentage?: boolean;
  currencyCode?: string;
  url?: boolean;
  navigable?: boolean;
  navigateLink?: string;
  maxLength?: number;
}
