import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericEditTableLayoutComponent } from './generic-edit-table-layout.component';

describe('GenericEditTableLayoutComponent', () => {
  let component: GenericEditTableLayoutComponent;
  let fixture: ComponentFixture<GenericEditTableLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenericEditTableLayoutComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericEditTableLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
