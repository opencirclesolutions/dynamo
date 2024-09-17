import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe for translation a boolean value to its string representation
 */
@Pipe({
  name: 'translateBoolean',
})
export class TranslateBooleanPipe implements PipeTransform {
  constructor() {}

  transform(value: boolean, locale: string, trueRepresentations: {[key: string]: string},
      falseRepresentations: {[key: string]: string}): string | undefined {

    if (value === null || value === undefined) {
      return '';
    }

    if (value === true) {
      if (trueRepresentations[locale]) {
        return trueRepresentations[locale]
      }
      return 'true'
    }

    if (value === false) {
      if (falseRepresentations[locale]) {
        return falseRepresentations[locale]
      }
      return 'false'
    }
    return 'fallback';
  }

}
