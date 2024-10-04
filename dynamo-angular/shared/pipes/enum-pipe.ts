import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe for translating an enum constant to its string representation
 */
@Pipe({
  name: 'translateEnum',
})
export class TranslateEnumPipe implements PipeTransform {
  constructor() {}

  transform(key: string, map: Map<string,string>): string | undefined {
    if (key === null) {
      return '';
    }

    return map.get(key);
  }

}
