import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlexibleSearchFormComponent } from './flexible-search-form.component';

describe('FlexibleSearchFormComponent', () => {
  let component: FlexibleSearchFormComponent;
  let fixture: ComponentFixture<FlexibleSearchFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FlexibleSearchFormComponent]
    });
    fixture = TestBed.createComponent(FlexibleSearchFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
