import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericSplitLayoutComponent } from './generic-split-layout.component';

describe('GenericSplitLayoutComponent', () => {
  let component: GenericSplitLayoutComponent;
  let fixture: ComponentFixture<GenericSplitLayoutComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [GenericSplitLayoutComponent]
    });
    fixture = TestBed.createComponent(GenericSplitLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
