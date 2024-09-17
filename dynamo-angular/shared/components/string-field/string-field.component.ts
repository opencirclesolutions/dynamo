import { Component, Input } from '@angular/core';
import { AttributeModelResponse } from 'dynamo/model';
import { BaseComponent } from '../base-component';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-string-field',
  templateUrl: './string-field.component.html',
  styleUrls: ['./string-field.component.scss'],
})
export class StringFieldComponent extends BaseComponent {

  @Input() allowTextAreas: boolean = true;
  @Input() classes: string = '';

  constructor(translate: TranslateService) {
    super(translate)
  }

  isTextArea(am: AttributeModelResponse): boolean {
    return this.allowTextAreas === true && am.textFieldMode == AttributeModelResponse.TextFieldModeEnum.TEXTAREA;
  }

  isPassword(am: AttributeModelResponse): boolean {
    return  am.textFieldMode == AttributeModelResponse.TextFieldModeEnum.PASSWORD;
  }

  isTextField(am: AttributeModelResponse): boolean {
    return !this.isTextArea(am) && !this.isPassword(am)
  }
}
