import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimeFieldComponent } from './time-field.component';

describe('TimeFieldComponent', () => {
  let component: TimeFieldComponent;
  let fixture: ComponentFixture<TimeFieldComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TimeFieldComponent]
    });
    fixture = TestBed.createComponent(TimeFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
