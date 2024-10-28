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
  Inject,
  Input,
  Output,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { Router } from '@angular/router';
import { PopupButtonMode, SearchFormMode } from '../../../../interfaces/mode';
import { FormInfo } from '../../../../interfaces/info';
import { AuthenticationService } from '../../../../services/authentication.service';
import { NotificationService } from '../../../../services/notification.service';
import { DynamoConfig } from '../../../../interfaces/dynamo-config';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { TranslateModule } from '@ngx-translate/core';
import { GenericTableComponent } from '../../../generic-table/generic-table.component';
import { DividerModule } from 'primeng/divider';
import { FlexibleSearchFormComponent } from '../flexible-search-form/flexible-search-form.component';
import { BaseCompositeCollectionComponent } from '../../base-composite-collection/base-composite-collection.component';
import { GenericSearchFormComponent } from '../generic-search-form/generic-search-form.component';

@Component({
  selector: 'd-generic-search-layout',
  standalone: true,
  imports: [TranslateModule, DividerModule, GenericTableComponent, FlexibleSearchFormComponent, GenericSearchFormComponent],
  templateUrl: './generic-search-layout.component.html',
  styleUrl: './generic-search-layout.component.css'
})
export class GenericSearchLayoutComponent extends BaseCompositeCollectionComponent {
  // the key under which to store data in the state store key
  @Input() stateStoreKey?: string = undefined;
  // the IDs of the selected rows
  @Input() selectedIds: any[] = [];
  // whether multiple select is allowed. this is used when this layout is used inside a search dialog
  @Input() multiSelect: boolean = false;
  // whether this component is nested inside another component (notable a search dialog)
  @Input() nested: boolean = false;
  // whether to ask for confirmation before clearing the search form
  @Input() confirmClear: boolean = false;
  // whether to preserve the search terms between page visits
  @Input() preserveSearchTerms: boolean = true;
  // whether advance search mode is enabled
  @Input() advancedModeEnabled: boolean = false;
  // whether to search immediately after opening the screen
  @Input() searchImmediately: boolean = true;
  // whether it is possible to navigate to the detail screen
  @Input() detailsModeEnabled: boolean = true;
  // whether to display the button to navigate to the detail screen in the grid
  @Input() showDetailButton: boolean = false;
  // the search form mode
  @Input() searchFormMode: SearchFormMode = SearchFormMode.STANDARD;
  // popup button mode
  @Input() popupButtonMode: PopupButtonMode = PopupButtonMode.NONE;
  // detail navigation path
  @Input() detailNavigationPath: string = '';

  // event handler that is called when the a row is selected in the table
  @Output() onRowSelect: EventEmitter<any> = new EventEmitter();
  // event handler that is called when the add button is clicked
  @Output() onAddButtonClick: EventEmitter<any> = new EventEmitter();
  // event handler that is called after a search is performed
  @Output() afterSearchPerformed: EventEmitter<any> = new EventEmitter();
  // event handler that is called after the search form has been built. Use to set defaults
  @Output() afterSearchFormBuilt: EventEmitter<any> =
    new EventEmitter<FormInfo>();

  // nested table component
  @ViewChild(GenericTableComponent) table?: GenericTableComponent;
  //container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  vcr!: ViewContainerRef;

  defaultPageSize = 10;
  searchObject: any = {};
  tableCaptionPlural: string = '';
  loading: boolean = !this.searchImmediately;

  constructor(
    messageService: NotificationService,
    router: Router,
    authService: AuthenticationService,
    @Inject("DYNAMO_CONFIG") configuration: DynamoConfig,
  ) {
    super(messageService, router, authService, configuration)
  }

  ngOnInit(): void {
    this.entityModelService
      .getEntityModel(this.entityName, this.entityModelReference)
      .subscribe({
        next: (model) => {
          this.entityModel = model;
          this.tableCaptionPlural =
            this.entityModel.displayNamesPlural[this.locale];
        },
        error: (error) =>
          error?.error
            ? this.messageService.error(error.error?.message)
            : this.messageService.error('Unknown error'),
      });
  }

  onSearch(searchObject: any) {
    this.searchObject = searchObject;
    this.loading = true;
    if (this.table) {
      this.table.searchObject = searchObject;
      this.table.search();
    }
    this.afterSearchPerformed.emit(searchObject);
  }

  onReset(event: any) {
    if (this.table) {
      this.table.reset();
    }
  }

  search() {
    this.table!.search();
  }

  /**
   * Respond to a click on the "add"-button. Will by default
   * navigate to a detail page
   */
  add() {
    if (this.detailNavigationPath) {
      this.router.navigateByUrl(this.detailNavigationPath);
    } else {
      this.onAddButtonClick.emit();
    }
  }

  /**
   * Respond to a click on a row in the table. Will by default
   * navigate to a detail page
   */
  rowClick(event: any) {
    if (event.id && this.detailNavigationPath) {
      this.router.navigateByUrl(this.detailNavigationPath + '/' + event.id);
    } else {
      this.onRowSelect.emit(event);
    }
  }

  searchComplete() {
    this.loading = false;
  }

  isStandard() {
    return this.searchFormMode == SearchFormMode.STANDARD;
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // no action necessary
  }

  doAfterSearchFormBuilt(info: FormInfo) {
    this.afterSearchFormBuilt.emit(info);
  }

  override afterActionDialogClosed(): void {
    this.search();
  }
}
