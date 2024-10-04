import { Component, Input } from '@angular/core';
import { AttributeModelResponse, PagingModel } from 'dynamo/model';
import { TranslateService } from '@ngx-translate/core';
import { BaseEntityComponent } from '../base-entity/base-entity.component';
import { SelectOption } from '../../model/select-option';
import { AuthenticationService } from '../../service/authentication-service';

@Component({
  selector: 'app-select-many',
  templateUrl: './select-many.component.html',
  styleUrls: ['./select-many.component.scss'],
})
export class SelectManyComponent extends BaseEntityComponent {
  @Input() searchMode: boolean = false;

  cache: SelectOption[] = [];

  constructor(authService: AuthenticationService, translate: TranslateService) {
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
}
