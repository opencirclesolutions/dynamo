import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { truncateDescriptions } from '../functions/entitymodel-functions';

const MAX_ITEMS = 3;

/**
 * Pipe for translating an entity to its display property value
 */
@Pipe({
  name: 'translateEntity',
})
export class TranslateEntityPipe implements PipeTransform {

  constructor(private translate: TranslateService) {

  }

  transform(obj: any, displayProperty: string): string {

    if (!obj) {
      return '';
    }

    if (Array.isArray(obj)) {
      return truncateDescriptions(obj,displayProperty, this.translate)
    }

    return obj[displayProperty] || this.translate.instant('display_unknown')
  }

}
