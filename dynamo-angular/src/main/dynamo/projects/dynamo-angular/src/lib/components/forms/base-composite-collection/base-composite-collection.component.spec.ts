import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseCompositeCollectionComponent } from './base-composite-collection.component';

describe('BaseCompositeCollectionComponent', () => {
  let component: BaseCompositeCollectionComponent;
  let fixture: ComponentFixture<BaseCompositeCollectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BaseCompositeCollectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BaseCompositeCollectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
