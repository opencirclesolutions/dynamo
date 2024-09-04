import { TestBed } from '@angular/core/testing';

import { CreateFilterService } from './create-filter.service';

describe('CreateFilterService', () => {
  let service: CreateFilterService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CreateFilterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
