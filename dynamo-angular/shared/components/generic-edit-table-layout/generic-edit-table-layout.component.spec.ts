import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericEditTableLayoutComponent } from './generic-edit-table-layout.component';

describe('GenericEditTableLayoutComponent', () => {
  let component: GenericEditTableLayoutComponent;
  let fixture: ComponentFixture<GenericEditTableLayoutComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [GenericEditTableLayoutComponent]
    });
    fixture = TestBed.createComponent(GenericEditTableLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
