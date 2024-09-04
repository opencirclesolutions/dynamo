import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AutoFillDialogComponent } from './auto-fill-dialog.component';

describe('AutoFillDialogComponent', () => {
  let component: AutoFillDialogComponent;
  let fixture: ComponentFixture<AutoFillDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AutoFillDialogComponent]
    });
    fixture = TestBed.createComponent(AutoFillDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
