import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElementCollectionDialogComponent } from './element-collection-dialog.component';

describe('ElementCollectionDialogComponent', () => {
  let component: ElementCollectionDialogComponent;
  let fixture: ComponentFixture<ElementCollectionDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ElementCollectionDialogComponent]
    });
    fixture = TestBed.createComponent(ElementCollectionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
