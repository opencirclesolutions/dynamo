import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BackendNotAvailableComponent } from './backend-not-available.component';

describe('BackendNotAvailableComponent', () => {
  let component: BackendNotAvailableComponent;
  let fixture: ComponentFixture<BackendNotAvailableComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [BackendNotAvailableComponent]
    });
    fixture = TestBed.createComponent(BackendNotAvailableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
