import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElementCollectionFieldComponent } from './element-collection-field.component';

describe('ElementCollectionFieldComponent', () => {
  let component: ElementCollectionFieldComponent;
  let fixture: ComponentFixture<ElementCollectionFieldComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ElementCollectionFieldComponent]
    });
    fixture = TestBed.createComponent(ElementCollectionFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
