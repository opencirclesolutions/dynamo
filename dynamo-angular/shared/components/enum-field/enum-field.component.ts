import { Component, Input } from '@angular/core';
import { BaseComponent } from '../base-component';
import { TranslateService } from '@ngx-translate/core';
import { AttributeModelResponse } from 'dynamo/model';

/**
 * A component for managing an enumeration
 */
@Component({
  selector: 'app-enum-field',
  templateUrl: './enum-field.component.html',
  styleUrls: ['./enum-field.component.scss']
})
export class EnumFieldComponent extends BaseComponent {

  @Input({ required: true }) options!: any[];
  @Input() searchMode: boolean = false;

  constructor(translate: TranslateService) {
    super(translate)
  }

  useRadioButton() {
    if (this.searchMode) {
      return false;
    }
    return this.am.enumFieldMode === AttributeModelResponse.EnumFieldModeEnum.RADIO;
  }
}
