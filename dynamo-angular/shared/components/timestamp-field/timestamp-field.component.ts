import { Component, Input } from '@angular/core';
import { BaseComponent } from '../base-component';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-timestamp-field',
  templateUrl: './timestamp-field.component.html',
  styleUrls: ['./timestamp-field.component.scss']
})
export class TimestampFieldComponent extends BaseComponent {

  @Input() searchMode: boolean = false;

  constructor(translate: TranslateService) {
    super(translate)
  }

  override getCalendarDateFormat(format: string) {
    //return getCalendarDateFormat(format);
    // PrimeNG calendar has wonky formatting that matches neither the JavaScript nor the java format
    return 'dd-mm-yy';
  }
}
