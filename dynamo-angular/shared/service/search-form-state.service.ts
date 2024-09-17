import { Injectable } from '@angular/core';
import { EntityModelResponse } from 'dynamo/model';

@Injectable({
  providedIn: 'root'
})
export class SearchFormStateService {

  // map for keeping track of uploaded files
  searchFormStateMap: Map<string, any> = new Map<
  string,
    any
  >();

  constructor() { }

  public storeState(entityModel: string, searchObject: any): void {
    this.searchFormStateMap.set(entityModel, searchObject)
  }

  public retrieveState(entityModel: string): any {
    return this.searchFormStateMap.get(entityModel)
  }
}
