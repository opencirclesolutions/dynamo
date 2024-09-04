import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericFieldComponent } from './generic-field.component';

describe('GenericFieldComponent', () => {
  let component: GenericFieldComponent;
  let fixture: ComponentFixture<GenericFieldComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [GenericFieldComponent]
    });
    fixture = TestBed.createComponent(GenericFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
