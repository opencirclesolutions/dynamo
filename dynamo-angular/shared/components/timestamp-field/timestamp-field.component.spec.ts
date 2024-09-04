import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimestampFieldComponent } from './timestamp-field.component';

describe('TimestampFieldComponent', () => {
  let component: TimestampFieldComponent;
  let fixture: ComponentFixture<TimestampFieldComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TimestampFieldComponent]
    });
    fixture = TestBed.createComponent(TimestampFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
