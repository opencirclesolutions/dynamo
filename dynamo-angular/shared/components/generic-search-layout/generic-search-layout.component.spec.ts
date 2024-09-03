import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericSearchLayoutComponent } from './generic-search-layout.component';

describe('GenericSearchLayoutComponent', () => {
  let component: GenericSearchLayoutComponent;
  let fixture: ComponentFixture<GenericSearchLayoutComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [GenericSearchLayoutComponent]
    });
    fixture = TestBed.createComponent(GenericSearchLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
