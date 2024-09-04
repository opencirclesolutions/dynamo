import { Component } from '@angular/core';
import { BaseComponent } from '../base-component';
import { AttributeModelResponse } from 'dynamo/model';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-boolean-field',
  templateUrl: './boolean-field.component.html',
  styleUrls: ['./boolean-field.component.scss'],
})
export class BooleanFieldComponent extends BaseComponent {

  constructor(translate: TranslateService) {
    super(translate);
  }

  useCheckbox(am: AttributeModelResponse) {
    return (
      am.booleanFieldMode ==
      AttributeModelResponse.BooleanFieldModeEnum.CHECKBOX
    );
  }

  useToggle(am: AttributeModelResponse) {
    return (
      am.booleanFieldMode == AttributeModelResponse.BooleanFieldModeEnum.TOGGLE
    );
  }

  useSwitch(am: AttributeModelResponse) {
    return am.booleanFieldMode == AttributeModelResponse.BooleanFieldModeEnum.SWITCH
  }


}
