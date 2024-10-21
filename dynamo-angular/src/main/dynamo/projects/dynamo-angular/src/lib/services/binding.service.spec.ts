import { TestBed } from '@angular/core/testing';

import { BindingService } from './binding.service';

describe('BindingService', () => {
  let service: BindingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BindingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
