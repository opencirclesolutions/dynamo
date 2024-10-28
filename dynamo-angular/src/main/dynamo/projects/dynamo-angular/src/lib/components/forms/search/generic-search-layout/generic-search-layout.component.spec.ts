import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericSearchLayoutComponent } from './generic-search-layout.component';

describe('GenericSearchLayoutComponent', () => {
  let component: GenericSearchLayoutComponent;
  let fixture: ComponentFixture<GenericSearchLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GenericSearchLayoutComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericSearchLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
