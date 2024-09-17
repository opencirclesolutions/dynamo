import { Component, Input } from '@angular/core';
import {
  AttributeModelResponse,
} from 'dynamo/model';
import {
  isBoolean,
  isDate,
  isDecimal,
  isEnum,
  isFreeDetail,
  isIntegral,
  isLob,
  isMaster,
  isNestedDetail,
  isString,
  isTime,
  isInstant,
  isLocalDateTime,
  isUrl,
  isElementCollection,
  getErrorString
} from '../functions/entitymodel-functions';
import { getLocale, prependUrl } from '../functions/functions';
import { FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-base',
  template: ``,
  styles: [],
})
export abstract class BaseComponent {
  isDate = isDate;
  isBoolean = isBoolean;
  isDecimal = isDecimal;
  isEnum = isEnum;
  isIntegral = isIntegral;
  isMaster = isMaster;
  isString = isString;
  isInstant = isInstant;
  isLocalDateTime = isLocalDateTime;
  isFreeDetail = isFreeDetail;
  isTime = isTime;
  isNestedDetail = isNestedDetail;
  isLob = isLob;
  isUrl = isUrl;
  isElementCollection = isElementCollection;
  prependUrl = prependUrl;

  getLocale = getLocale();

  @Input() public locale: string = getLocale();
  @Input() am!: AttributeModelResponse;
  @Input() formGroup?: FormGroup;
  @Input() showValidationErrors: boolean = true;
  @Input() colClass: string = '';
  @Input() validationColClass: string = '';
  @Input() fullWidth: boolean = false;

  translate: TranslateService;

  constructor(translate: TranslateService) {
    this.translate = translate;
  }

  getDigitsInfo(am?: AttributeModelResponse) {
    return `1.${am!.precision}-${am!.precision}`
  }

  getErrorString(attribute: string): string {
    return getErrorString(attribute, this.formGroup!, this.translate);
  }

  getFromName(am: AttributeModelResponse): string {
    return am.name + '_from';
  }

  getToName(am: AttributeModelResponse): string {
    return am.name + '_to';
  }

  getPlaceholderFrom(am: AttributeModelResponse) {
    return this.translate.instant('placeholder_from', {
      placeholder: am.placeholders[this.locale],
    });
  }

  getPlaceholderTo(am: AttributeModelResponse) {
    return this.translate.instant('placeholder_to', {
      placeholder: am.placeholders[this.locale],
    });
  }


  getCalendarDateFormat(format: string) {
    //return getCalendarDateFormat(format);
    // PrimeNG calendar has wonky formatting that matches neither the JavaScript nor the java format
    return 'dd-mm-yy';
  }

}
