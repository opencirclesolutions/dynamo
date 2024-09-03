import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericSearchFormComponent } from './generic-search-form.component';

describe('GenericSearchFormComponent', () => {
  let component: GenericSearchFormComponent;
  let fixture: ComponentFixture<GenericSearchFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [GenericSearchFormComponent]
    });
    fixture = TestBed.createComponent(GenericSearchFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
