import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseSearchComponentComponent } from './base-search-component.component';

describe('BaseSearchComponentComponent', () => {
  let component: BaseSearchComponentComponent;
  let fixture: ComponentFixture<BaseSearchComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BaseSearchComponentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BaseSearchComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
