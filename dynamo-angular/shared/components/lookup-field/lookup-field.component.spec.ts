import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LookupFieldComponent } from './lookup-field.component';

describe('LookupFieldComponent', () => {
  let component: LookupFieldComponent;
  let fixture: ComponentFixture<LookupFieldComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LookupFieldComponent]
    });
    fixture = TestBed.createComponent(LookupFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
