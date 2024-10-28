import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseCompositeComponent } from './base-composite.component';

describe('BaseCompositeComponent', () => {
  let component: BaseCompositeComponent;
  let fixture: ComponentFixture<BaseCompositeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BaseCompositeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BaseCompositeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
