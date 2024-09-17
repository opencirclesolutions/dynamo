import { Component, Input } from '@angular/core';
import { BaseComponent } from '../base-component';
import { AttributeModelResponse } from 'dynamo/model';
import { TranslateService } from '@ngx-translate/core';

/**
 * A component for displaying a number field (with input mask, optional currency and percentage signs, and
 * spinner buttons)
 */
@Component({
  selector: 'app-number-field',
  templateUrl: './number-field.component.html',
  styleUrls: ['./number-field.component.scss']
})
export class NumberFieldComponent extends BaseComponent {

  @Input() searchMode: boolean = false;

  constructor(translate: TranslateService) {
    super(translate)
  }

  isShowNumberSpinnerButtons(am: AttributeModelResponse) {
    return am.includeNumberSpinnerButton === true;
  }

}
