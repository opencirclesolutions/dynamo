import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DynamoAngularComponent } from './dynamo-angular.component';

describe('DynamoAngularComponent', () => {
  let component: DynamoAngularComponent;
  let fixture: ComponentFixture<DynamoAngularComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DynamoAngularComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DynamoAngularComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
