/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BackendNotAvailableComponent } from './backend-not-available.component';

describe('BackendNotAvailableComponent', () => {
  let component: BackendNotAvailableComponent;
  let fixture: ComponentFixture<BackendNotAvailableComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [BackendNotAvailableComponent]
    });
    fixture = TestBed.createComponent(BackendNotAvailableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});