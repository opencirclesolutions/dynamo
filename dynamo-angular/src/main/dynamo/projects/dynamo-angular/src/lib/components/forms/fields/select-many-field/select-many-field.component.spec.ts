import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectManyFieldComponent } from './select-many-field.component';

describe('SelectManyFieldComponent', () => {
  let component: SelectManyFieldComponent;
  let fixture: ComponentFixture<SelectManyFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectManyFieldComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SelectManyFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
