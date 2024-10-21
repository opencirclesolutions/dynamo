import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { backendAvailableGuard } from './backend-available.guard';

describe('backendAvailableGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => backendAvailableGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
