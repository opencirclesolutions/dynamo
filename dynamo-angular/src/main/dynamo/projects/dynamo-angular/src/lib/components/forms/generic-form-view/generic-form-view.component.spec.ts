import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericFormViewComponent } from './generic-form-view.component';

describe('GenericFormViewComponent', () => {
  let component: GenericFormViewComponent;
  let fixture: ComponentFixture<GenericFormViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenericFormViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericFormViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
