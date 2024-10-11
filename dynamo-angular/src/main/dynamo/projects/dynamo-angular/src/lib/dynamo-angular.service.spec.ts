import { TestBed } from '@angular/core/testing';

import { DynamoAngularService } from './dynamo-angular.service';

describe('DynamoAngularService', () => {
  let service: DynamoAngularService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DynamoAngularService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
