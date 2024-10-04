import { Pipe, PipeTransform } from '@angular/core';
import { Observable, map, of } from 'rxjs';
import { CRUDService } from 'dynamo/model';

/**
 * Pipe for translating an entity to its display property value
 */
@Pipe({
  name: 'lookupEntity',
})
export class LookupEntityPipe implements PipeTransform {
  constructor(private service: CRUDService) {}

  transform(
    obj: any,
    entityName: string,
    displayProperty: string
  ): Observable<string | undefined> {
    if (!obj) {
      return of('');
    }

    return this.service
      .get(entityName, obj['id'])
      .pipe(map((entity) => entity[displayProperty]));
  }
}
