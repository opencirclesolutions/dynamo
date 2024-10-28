import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FieldViewTableComponent } from './field-view-table.component';

describe('FieldViewTableComponent', () => {
  let component: FieldViewTableComponent;
  let fixture: ComponentFixture<FieldViewTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FieldViewTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FieldViewTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
