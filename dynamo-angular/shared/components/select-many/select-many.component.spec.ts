import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectManyComponent } from './select-many.component';

describe('SelectManyComponent', () => {
  let component: SelectManyComponent;
  let fixture: ComponentFixture<SelectManyComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SelectManyComponent]
    });
    fixture = TestBed.createComponent(SelectManyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
