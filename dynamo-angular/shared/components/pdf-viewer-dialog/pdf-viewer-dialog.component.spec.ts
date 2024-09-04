import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PdfViewerDialogComponent } from './pdf-viewer-dialog.component';

describe('PdfViewerDialogComponent', () => {
  let component: PdfViewerDialogComponent;
  let fixture: ComponentFixture<PdfViewerDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PdfViewerDialogComponent]
    });
    fixture = TestBed.createComponent(PdfViewerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
