import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseEntityComponent } from './base-entity.component';

describe('BaseEntityComponent', () => {
  let component: BaseEntityComponent;
  let fixture: ComponentFixture<BaseEntityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BaseEntityComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BaseEntityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
