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
  Inject,
  Input,
} from '@angular/core';
import { AutoCompleteCompleteEvent, AutoCompleteModule } from 'primeng/autocomplete';
import { Observable, catchError, map, of } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { BaseEntityComponent } from '../../../base-entity/base-entity.component';
import { AuthenticationService } from '../../../../services/authentication.service';
import { NotificationService } from '../../../../services/notification.service';
import { AttributeModelResponse } from '../../../../interfaces/model/attributeModelResponse';
import { DynamoConfig } from '../../../../interfaces/dynamo-config';
import { CRUDServiceInterface } from '../../../../interfaces/service/crud.service';
import { FilterModel } from '../../../../interfaces/model/filterModel';
import { EqualsFilterModel } from '../../../../interfaces/model/equalsFilterModel';
import { SearchModel } from '../../../../interfaces/model/searchModel';
import { PagingModel } from '../../../../interfaces/model/pagingModel';
import { SortModel } from '../../../../interfaces/model/sortModel';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MessageModule } from 'primeng/message';
import { TooltipModule } from 'primeng/tooltip';
import { LookupFieldComponent } from '../lookup-field/lookup-field.component';
import { MultiSelectModule } from 'primeng/multiselect';
import { DropdownModule } from 'primeng/dropdown';

@Component({
  selector: 'd-select-entity-field',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, MessageModule, DropdownModule, MultiSelectModule, AutoCompleteModule, TooltipModule, LookupFieldComponent],
  templateUrl: './select-entity-field.component.html',
  styleUrl: './select-entity-field.component.css'
})
export class SelectEntityFieldComponent extends BaseEntityComponent {

  @Input() styleClass: string = 'main';
  @Input() nested: boolean = false;
  @Input() searchMode: boolean = false;

  // the options that are applicable in case of a lookup field
  filteredOptions$: Observable<any[]> = of([]);
  private crudService: CRUDServiceInterface;

  constructor(
    private messageService: NotificationService,
    translate: TranslateService,
    authService: AuthenticationService,
    @Inject("DYNAMO_CONFIG") configuration: DynamoConfig,
  ) {
    super(authService, translate);
    this.crudService = configuration.getCRUDService()
  }

  useLookupField(am: AttributeModelResponse) {
    if (this.searchMode) {
      return (
        am.searchSelectMode === AttributeModelResponse.SelectModeEnum.LOOKUP
      );
    }

    return am.selectMode === AttributeModelResponse.SelectModeEnum.LOOKUP;
  }

  useAutocompleteField(am: AttributeModelResponse) {
    if (this.searchMode) {
      return (
        am.searchSelectMode ===
        AttributeModelResponse.SelectModeEnum.AUTO_COMPLETE
      );
    }

    return (
      am.selectMode === AttributeModelResponse.SelectModeEnum.AUTO_COMPLETE
    );
  }

  useComboField(am: AttributeModelResponse) {
    if (this.searchMode) {
      return (
        am.searchSelectMode === AttributeModelResponse.SelectModeEnum.COMBO &&
        am.multipleSearch === false
      );
    }

    return am.selectMode === AttributeModelResponse.SelectModeEnum.COMBO;
  }

  useMultiSelect(am: AttributeModelResponse) {
    if (this.searchMode) {
      return (
        am.searchSelectMode !== AttributeModelResponse.SelectModeEnum.LOOKUP &&
        am.multipleSearch === true
      );
    }
    return false;
  }

  isMultiSelectLookup(am: AttributeModelResponse) {
    if (this.searchMode) {
      return am.multipleSearch === true;
    }
    return false;
  }

  /**
   * Carry out a filter in response to user input (for auto-suggest field)
   * @param event the event
   */
  filter(event: AutoCompleteCompleteEvent) {
    let query = event.query;

    let filters: FilterModel[] = [];
    let nameFilter: EqualsFilterModel = {
      match: 'EQUALS',
      name: this.am!.displayPropertyName,
      value: query as any,
    };

    filters.push(nameFilter);
    this.defaultFilters.forEach((defaultFilter) => filters.push(defaultFilter));

    let model: SearchModel = {
      paging: {
        pageNumber: 0,
        pageSize: 100,
        type: PagingModel.TypeEnum.PAGING,
      },
      sort: {
        sortField: this.am!.displayPropertyName!,
        sortDirection: SortModel.SortDirectionEnum.ASC,
      },
      filters: filters,
    };

    this.filteredOptions$ = this.crudService
      .search(this.am!.lookupEntityName!, model, this.am!.lookupEntityReference)
      .pipe(
        map((val) => val.results as any[]),
        map((input) => this.mapOptions(input, this.am!)),
        catchError((error) => {
          this.messageService.error(this.translate.instant('unknown_error'));
          return of([]);
        })
      );
  }

  mapOptions(input: any[], am: AttributeModelResponse) {
    if (!input) {
      return [];
    }
    let result: any[] = [];
    input.forEach((sel) => {
      let obj = {
        name: sel[am.displayPropertyName!],
        value: sel.id,
      };
      result.push(obj);
    });
    return result;
  }

  override doSetNewValue(newValue: any): void {
    this.formControl?.patchValue(newValue)
  }

  mapQueryType(input: AttributeModelResponse.LookupQueryTypeEnum) {
    if (input === AttributeModelResponse.LookupQueryTypeEnum.PAGING) {
      return PagingModel.TypeEnum.PAGING;
    }
    return PagingModel.TypeEnum.ID_BASED;
  }

}