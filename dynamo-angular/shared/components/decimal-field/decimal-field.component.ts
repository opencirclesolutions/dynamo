import { Component, Input } from '@angular/core';
import { BaseComponent } from '../base-component';
import { AttributeModelResponse } from 'dynamo/model';
import { TranslateService } from '@ngx-translate/core';
import { getSimpleLocale } from '../../functions/functions';

@Component({
  selector: 'app-decimal-field',
  templateUrl: './decimal-field.component.html',
  styleUrls: ['./decimal-field.component.scss']
})
export class DecimalFieldComponent extends BaseComponent {

  getSimpleLocale = getSimpleLocale;

  @Input() searchMode: boolean = false;

  constructor(translate: TranslateService) {
    super(translate)
  }

  getDecimalMode(am: AttributeModelResponse): string {
    return am.currencyCode ? 'currency' : 'decimal';
  }

  getSuffix(am: AttributeModelResponse): string | undefined {
    if (am.percentage) {
      return '%';
    }
    return undefined;
  }
}
