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
  Output,
  OnInit,
  ViewChild,
  ViewContainerRef,
  QueryList,
  Inject,
} from '@angular/core';
import { Router } from '@angular/router';
import { TableLazyLoadEvent } from 'primeng/table';
import { decapitalize } from '../../functions/functions';
import { finalize } from 'rxjs';
import { FormGroup } from '@angular/forms';
import { HttpResponse } from '@angular/common/http';
import { MenuItem } from 'primeng/api';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { EntityModelResponse } from '../../interfaces/model/entityModelResponse';
import { PopupButtonMode } from '../../interfaces/mode';
import { OverrideFieldDirective } from '../../directives/override-field.directive';
import { AttributeModelResponse } from '../../interfaces/model/attributeModelResponse';
import { NotificationService } from '../../services/notification.service';
import { AuthenticationService } from '../../services/authentication.service';
import { DynamoConfig } from '../../interfaces/dynamo-config';
import { CreateFilterService } from '../../services/create-filter.service';
import { ExportServiceInterface } from '../../interfaces/service/export.service';
import { EntityPopupDialogComponent } from '../dialogs/entity-popup-dialog/entity-popup-dialog.component';
import { SearchModel } from '../../interfaces/model/searchModel';
import { FilterModel } from '../../interfaces/model/filterModel';
import { EntityModelActionResponse } from '../../interfaces/model/entityModelActionResponse';
import { AdditionalRowAction } from '../../interfaces/action';
import { DataTableComponent, TableColumn } from '../data-table/data-table.component';
import { BaseCompositeCollectionComponent } from '../forms/base-composite-collection/base-composite-collection.component';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'd-generic-table',
  standalone: true,
  imports: [TranslateModule, TooltipModule, DataTableComponent],
  templateUrl: './generic-table.component.html',
  styleUrl: './generic-table.component.css'
})
export class GenericTableComponent
  extends BaseCompositeCollectionComponent
  implements OnInit {
  defaultPageSize = 10;

  // the search object
  @Input({ required: true }) searchObject: any = {};
  // whether advanced search mode is enabled
  @Input() advancedMode: boolean = false;
  // whether to immediately perform a search request
  @Input() searchImmediately: boolean = true;
  // the selected IDs
  @Input() selectedIds: any[] = [];
  // whether multiple rows can be selected at once
  @Input() multiSelect: boolean = false;
  // whether it is possible to navigate to a detail screen
  @Input() detailsModeEnabled: boolean = true;
  // whether the component is nested inside a dialog
  @Input() nested: boolean = false;
  // whether to show a popup button to bring up details of a row
  @Input() popupButtonMode: PopupButtonMode = PopupButtonMode.NONE;
  // whether to display a button for navigating to a detail screen
  @Input() showDetailButton: boolean = false;
  // additional validation function to carry out before submitting the form
  @Input() additionalValidation?: (formGroup: FormGroup) => string | undefined;
  // event handler that is called when a user select a row
  @Output() onRowSelect: EventEmitter<any> = new EventEmitter();
  // event handler that is called after a search completes
  @Output() onSearchComplete: EventEmitter<any> = new EventEmitter();

  // container for holding optional popup dialog
  @ViewChild('popupDialogContainerRef', { read: ViewContainerRef })
  vcr!: ViewContainerRef;
  @Input() injectedCustomInputs?: QueryList<OverrideFieldDirective>;

  columns: TableColumn[] = [];
  rows: any[] = [];
  totalRecords: number = 0;
  searched: boolean = false;
  tableCaptionPlural: string = '';
  searchAttributeModels: AttributeModelResponse[] = [];
  loading: boolean = true;
  items!: MenuItem[];
  storedSortField: string = 'id';
  storedSortOrder: number = 1;
  private exportService: ExportServiceInterface

  constructor(
    messageService: NotificationService,
    router: Router,
    private translate: TranslateService,
    authService: AuthenticationService,
    private createFilterService: CreateFilterService,
    @Inject("DYNAMO_CONFIG") configuration: DynamoConfig,
  ) {
    super(messageService, router, authService, configuration,);
    this.exportService = configuration.getExportService()
  }

  ngOnInit(): void {
    if (this.entityModel) {
      this.init(this.entityModel);
      if (this.searchImmediately) {
        this.search();
      }
    } else {
      this.entityModelService
        .getEntityModel(this.entityName, this.entityModelReference)
        .subscribe({
          next: (model) => {
            this.entityModel = model;
            this.init(model);
            if (this.searchImmediately) {
              this.search();
            }
          },
          error: (error) => this.messageService.error(error.error?.message),
        });
    }

    this.items = [
      {
        label: this.translate.instant('export_excel'),
        icon: 'pi pi-download',
        command: () => this.exportToExcel(),
      },
      {
        label: this.translate.instant('export_csv'),
        icon: 'pi pi-download',
        command: () => this.exportToCsv(),
      },
    ];
  }

  private init(model: EntityModelResponse): void {
    this.setupEnums(model);
    this.tableCaptionPlural = model.displayNamesPlural[this.locale];
    this.filterSearchModels(model);
    this.setupGridColumns(model);
  }

  /**
   * Displays a popup dialog containing the selected entity
   * @param row the selected entity
   */
  openEntityDialog(row: any): void {
    let componentRef = this.vcr!.createComponent(EntityPopupDialogComponent);
    componentRef.instance.entityId = row.id;
    componentRef.instance.entityModel = this.entityModel;
    componentRef.instance.entityModelReference = this.entityModelReference;
    componentRef.instance.entityName = this.entityName;
    componentRef.instance.fieldFilters = this.fieldFilters;
    componentRef.instance.additionalActions = this.additionalFormActions;
    componentRef.instance.postProcessInputForm = this.postProcessInputForm;
    componentRef.instance.additionalValidation = this.additionalValidation;
    componentRef.instance.customValidatorTemplate =
      this.customValidatorTemplate;
    componentRef.instance.injectedCustomInputs = this.injectedCustomInputs;
    componentRef.instance.injectedHiddenFieldService = this.hiddenFieldService;
    componentRef.instance.readOnly =
      this.popupButtonMode === PopupButtonMode.READ_ONLY ||
      !this.isWriteAllowed();

    var callback = (obj: any): void => {
      this.search();
    };
    componentRef.instance.onDialogClosed = callback;
    componentRef.instance.showDialog();
  }

  /**
   * Filters the attribute model, returning only those that are searchable
   * @param model the entity model
   */
  private filterSearchModels(model: EntityModelResponse): void {
    this.searchAttributeModels = [];
    model.attributeNamesOrderedForSearch.forEach((attributeName) => {
      let am = model.attributeModels!.find((am) => am.name == attributeName);
      if (am && this.isSearchable(am)) {
        this.searchAttributeModels.push(am);
      }
    });
  }

  /**
   * Sets up grid columns
   * @param model the entity model to base the columns on
   */
  private setupGridColumns(model: EntityModelResponse): void {
    this.columns = [];
    model.attributeNamesOrderedForGrid?.forEach((attributeName) => {
      let am = model.attributeModels!.find((am) => am.name === attributeName)!;

      if (am.visibleInGrid) {
        let displayName = am?.displayNames![this.locale];
        let datePipe =
          am.dateType == AttributeModelResponse.DateTypeEnum.DATE ||
            am.dateType == AttributeModelResponse.DateTypeEnum.INSTANT ||
            am.dateType == AttributeModelResponse.DateTypeEnum.LOCAL_DATE_TIME
            ? {
              formats: am.displayFormats!,
              instant: this.isInstant(am),
            }
            : null;

        let translateEntity = this.createTranslateEntityInfo(am);
        let numberFormat = undefined;
        let percentage = undefined;
        let currencyCode = undefined;
        let maxLengthInGrid = am.maxLengthInGrid;

        if (this.isDecimal(am)) {
          if (!am.currencyCode) {
            numberFormat = {
              digitsInfo: `1.${am.precision}-${am.precision}`,
              locale: this.locale,
            };
            percentage = am.percentage;
          } else {
            currencyCode = am.currencyCode;
          }
        }

        let translateEnum = undefined;
        if (this.isEnum(am)) {
          translateEnum = this.simpleEnumMap.get(am.name);
        }

        let translateBoolean;
        if (this.isBoolean(am)) {
          translateBoolean = {
            trueRepresentations: am.trueRepresentations,
            falseRepresentations: am.falseRepresentations,
          };
        }

        let column: TableColumn = {
          field: am.name!,
          header: displayName || am.name!,
          sortable: am.sortable,
          datePipe: datePipe || undefined,
          translateEntity: translateEntity,
          numberFormat: numberFormat,
          translateEnum: translateEnum,
          percentage: percentage!,
          currencyCode: currencyCode!,
          translateBoolean: translateBoolean,
          url: am.url,
          navigable: am.navigable,
          navigateLink: this.getNavigationLink(am),
          maxLength: maxLengthInGrid,
        };
        this.columns.push(column);
      }
    });
  }

  private createTranslateEntityInfo(am: AttributeModelResponse) {
    let translateEntity;
    if (this.isComplexAttribute(am)) {
      translateEntity = {
        property: am.displayPropertyName!,
      };
    }
    return translateEntity;
  }

  search(): void {
    this.searched = true;
    this.loadPage(this.createSearchRequest());
  }

  reset(): void {
    this.searched = false;
  }

  isComplexAttribute(am: AttributeModelResponse): boolean {
    return (
      am.attributeModelDataType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.MASTER ||
      am.attributeModelDataType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.ONE_TO_MANY ||
      am.attributeModelDataType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.MANY_TO_MANY
    );
  }

  createSearchRequest(): TableLazyLoadEvent {
    return {
      first: 0,
      rows: this.defaultPageSize,
      sortField: this.entityModel!.sortProperty
        ? this.entityModel!.sortProperty
        : 'id',
      sortOrder: this.entityModel!.sortAscending ? 1 : -1,
    };
  }

  getInitialSortField(): string {
    return this.entityModel!.sortProperty || 'id';
  }

  getInitialSortOrder(): 0 | 1 | -1 {
    return this.entityModel!.sortAscending ? 1 : -1;
  }

  loadPage(event: TableLazyLoadEvent) {
    let offset = event.first! / event.rows!;

    if (Array.isArray(event.sortField)) {
      this.storedSortField = event.sortField[0];
    } else {
      this.storedSortField = event.sortField
        ? event.sortField
        : this.entityModel!.sortProperty;
    }

    this.storedSortOrder = event.sortOrder!;
    console.log(JSON.stringify(this.searchObject));

    let model: SearchModel = this.createSearchModel(
      offset,
      event.rows!,
      this.storedSortField,
      event.sortOrder!
    );
    this.loading = true;
    this.service
      .search(this.entityName, model, this.entityModelReference)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.onSearchComplete.emit();
        })
      )
      .subscribe({
        next: (data) => {
          this.rows = data.results!;
          this.totalRecords = data.stats!.totalResults!;
          if (data.stats?.tooManyResults) {
            this.messageService.warn(
              'Warning: search resulted in too many results'
            );
          }
        },
        error: (error) => this.messageService.error(error.error?.message),
      });
  }

  private createSearchModel(
    offset: number,
    rows: number,
    sortField: string,
    sortOrder: number
  ) {
    let filters: FilterModel[] = this.createFilterService.createFilters(
      this.searchObject,
      this.searchAttributeModels,
      this.defaultFilters
    );

    let model: SearchModel = {
      paging: {
        pageNumber: offset,
        pageSize: rows,
        type: this.queryType,
      },
      sort: {
        sortField: sortField,
        sortDirection: sortOrder == 1 ? 'ASC' : 'DESC',
      },
      filters: filters,
    };
    return model;
  }

  rowClick(event: any): void {
    console.log('Generic search row click: ' + event.id);
    this.onRowSelect.emit(event);
  }

  deleteButtonClick(event: any): void {
    this.service._delete(this.entityName, event.toString()).subscribe({
      next: () => this.search(),
      error: (error) => this.messageService.error(error.error.message),
    });
  }

  private isSearchable(am: AttributeModelResponse): boolean {
    return (
      am.searchMode == AttributeModelResponse.SearchModeEnum.ALWAYS ||
      (this.advancedMode &&
        am.searchMode == AttributeModelResponse.SearchModeEnum.ADVANCED)
    );
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // no action necessary
  }

  override filterEntityModelActions() {
    return this.entityModel?.actions
      ?.filter(
        (action) => action.type == EntityModelActionResponse.TypeEnum.UPDATE
      )
      .filter((action) => this.isActionAllowed(action));
  }

  callAdditionalRowAction(action: AdditionalRowAction, row: any) {
    action.action(row);
  }

  isRowActionDisabled(action: AdditionalRowAction, row: any) {
    if (!action.enabled) {
      return false;
    }
    return !action.enabled(row);
  }

  getNavigationLink(am: AttributeModelResponse) {
    if (am.navigationLink && am.navigationLink.length > 0) {
      return am.navigationLink;
    }
    return decapitalize(am.lookupEntityName!);
  }

  showPopupButton() {
    return this.popupButtonMode !== PopupButtonMode.NONE;
  }

  exportToExcel() {
    this.loading = true;
    let searchModel: SearchModel = this.createSearchModel(
      0,
      10,
      this.storedSortField,
      this.storedSortOrder
    );
    this.exportService
      .exportExcel(
        this.entityName,
        this.exportMode,
        searchModel,
        this.exportModeReference
          ? this.exportModeReference
          : this.entityModelReference,
        this.locale,
        'response'
      )
      .subscribe((response: HttpResponse<Blob>) => {
        this.handleExport(response);
      });
  }

  exportToCsv() {
    this.loading = true;
    let searchModel: SearchModel = this.createSearchModel(
      0,
      10,
      this.storedSortField,
      this.storedSortOrder
    );
    this.exportService
      .exportCsv(
        this.entityName,
        this.exportMode,
        searchModel,
        this.exportModeReference
          ? this.exportModeReference
          : this.entityModelReference,
        this.locale,
        'response'
      )
      .subscribe((response: HttpResponse<Blob>) => {
        this.handleExport(response);
      });
  }

  private handleExport(response: HttpResponse<Blob>) {
    let contentDispositionHeader = response.headers.get('content-disposition')!;
    let fileName: string = contentDispositionHeader
      .split(';')[1]
      .trim()
      .split('=')[1];

    let data = response.body as Blob;
    let blob = new Blob([data], { type: data.type });

    let url = window.URL.createObjectURL(blob);
    var anchor = document.createElement('a');
    anchor.download = fileName;
    anchor.href = url;
    anchor.click();

    this.loading = false;
  }

  getContextMenuItems() {
    return this.entityModel?.exportAllowed ? this.items : [];
  }

  override afterActionDialogClosed(): void {
    this.search();
  }
}
