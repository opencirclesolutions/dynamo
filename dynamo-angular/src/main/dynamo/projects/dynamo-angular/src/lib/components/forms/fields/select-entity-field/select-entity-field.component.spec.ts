import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectEntityFieldComponent } from './select-entity-field.component';

describe('SelectEntityFieldComponent', () => {
  let component: SelectEntityFieldComponent;
  let fixture: ComponentFixture<SelectEntityFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectEntityFieldComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SelectEntityFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
