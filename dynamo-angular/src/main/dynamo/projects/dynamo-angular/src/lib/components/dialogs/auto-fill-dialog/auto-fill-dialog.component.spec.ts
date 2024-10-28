import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AutoFillDialogComponent } from './auto-fill-dialog.component';

describe('AutoFillDialogComponent', () => {
  let component: AutoFillDialogComponent;
  let fixture: ComponentFixture<AutoFillDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AutoFillDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AutoFillDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
