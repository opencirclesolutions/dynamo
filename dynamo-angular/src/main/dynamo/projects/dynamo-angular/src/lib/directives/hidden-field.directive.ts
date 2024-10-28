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
import { Directive, Input, OnInit, inject } from "@angular/core";
import { HiddenFieldService } from "../services/hidden-field.service";

/**
 * A directive that can be used to provide a hidden field value to a form. Can be used to e.g.
 * create entities that depend on a master entity
 */
@Directive({
  selector: 'ng-template[dHiddenField]',
  exportAs: 'dHiddenField',
  standalone: true,
})
export class HiddenFieldDirective implements OnInit {
  private hiddenFieldService = inject(HiddenFieldService);

  @Input({ required: true }) attributeName!: string;
  @Input({ required: true }) value!: any;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
  }

  ngOnInit(): void {
    this.hiddenFieldService.setFieldValue(this.attributeName, this.value);
  }
}
