import { TestBed } from '@angular/core/testing';

import { SearchFormStateService } from './search-form-state.service';

describe('SearchFormStateService', () => {
  let service: SearchFormStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SearchFormStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
