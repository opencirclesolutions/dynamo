import { Component, Input } from '@angular/core';
import { BaseComponent } from '../base-component';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-date-field',
  templateUrl: './date-field.component.html',
  styleUrls: ['./date-field.component.scss']
})
export class DateFieldComponent extends BaseComponent {

  @Input() searchMode: boolean = false;

  constructor(translate: TranslateService) {
    super(translate)
  }

}
