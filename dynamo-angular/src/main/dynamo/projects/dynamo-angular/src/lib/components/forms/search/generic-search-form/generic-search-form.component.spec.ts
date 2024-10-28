import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericSearchFormComponent } from './generic-search-form.component';

describe('GenericSearchFormComponent', () => {
  let component: GenericSearchFormComponent;
  let fixture: ComponentFixture<GenericSearchFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenericSearchFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericSearchFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
