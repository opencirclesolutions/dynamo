import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BackendNotAvailableComponent } from './backend-not-available.component';

describe('BackendNotAvailableComponent', () => {
  let component: BackendNotAvailableComponent;
  let fixture: ComponentFixture<BackendNotAvailableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BackendNotAvailableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BackendNotAvailableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
