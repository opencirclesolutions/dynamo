import {
  Component,
  Input,
} from '@angular/core';
import {
  AttributeModelResponse,
  CRUDService,
  EqualsFilterModel,
  FilterModel,
  PagingModel,
  SearchModel,
  SortModel,
} from 'dynamo/model';
import { AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Observable, catchError, map, of } from 'rxjs';
import { NotificationService } from '../../service/notification-service';
import { TranslateService } from '@ngx-translate/core';
import { BaseEntityComponent } from '../base-entity/base-entity.component';
import { AuthenticationService } from '../../service/authentication-service';

/**
 * A component for selecting an entity from a list. Delegates to either
 * a combo box, auto-complete field, or lookup field
 */
@Component({
  selector: 'app-select-entity',
  templateUrl: './select-entity.component.html',
  styleUrls: ['./select-entity.component.scss'],
})
export class SelectEntityComponent extends BaseEntityComponent {

  @Input() styleClass: string = 'main';
  @Input() nested: boolean = false;
  @Input() searchMode: boolean = false;

  // the options that are applicable in case of a lookup field
  filteredOptions$: Observable<any[]> = of([]);

  constructor(
    private crudService: CRUDService,
    private messageService: NotificationService,
    translate: TranslateService,
    authService: AuthenticationService,
  ) {
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
