import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityPopupDialogComponent } from './entity-popup-dialog.component';

describe('EntityPopupDialogComponent', () => {
  let component: EntityPopupDialogComponent;
  let fixture: ComponentFixture<EntityPopupDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EntityPopupDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EntityPopupDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
