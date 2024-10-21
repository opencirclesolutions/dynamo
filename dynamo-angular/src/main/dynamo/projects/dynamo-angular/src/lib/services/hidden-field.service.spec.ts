import { TestBed } from '@angular/core/testing';

import { HiddenFieldService } from './hidden-field.service';

describe('HiddenFieldService', () => {
  let service: HiddenFieldService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HiddenFieldService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
