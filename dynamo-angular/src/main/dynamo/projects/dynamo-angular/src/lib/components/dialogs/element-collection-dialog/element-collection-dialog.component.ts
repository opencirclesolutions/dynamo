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
import { Component, Input, OnInit, forwardRef, inject } from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BaseComponent } from '../../base/base.component';
import { createValidators, getErrorString } from '../../../functions/entitymodel-functions';
import { AttributeModelResponse } from '../../../interfaces/model/attributeModelResponse';
import { MessageModule } from 'primeng/message';
import { StringFieldComponent } from '../../forms/fields/string-field/string-field.component';
import { InputNumberModule } from 'primeng/inputnumber';
import { DialogModule } from 'primeng/dialog';

@Component({
  selector: 'd-element-collection-dialog',
  standalone: true,
  imports: [TranslateModule, ReactiveFormsModule, InputNumberModule, DialogModule, MessageModule, StringFieldComponent],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ElementCollectionDialogComponent),
      multi: true,
    },
  ],
  templateUrl: './element-collection-dialog.component.html',
  styleUrl: './element-collection-dialog.component.css'
})
export class ElementCollectionDialogComponent
  extends BaseComponent
  implements ControlValueAccessor, OnInit {
  private formBuilder = inject(FormBuilder);


  @Input() searchMode: boolean = false;
  @Input() rowClass: string = 'row';
  @Input() disabled: boolean = false;

  dialogVisible: boolean = false;
  dialogForm: FormGroup;
  selectedValues: any[] = [];

  onChange: any = () => { };
  onTouched: any = () => { };

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const translate = inject(TranslateService);

    super(translate);
    this.dialogForm = this.formBuilder.group([]);
  }

  ngOnInit(): void {
    let validators = createValidators(this.translate, this.am, true, this.searchMode);
    validators.push(Validators.required);

    let control = this.formBuilder.control(
      {
        disabled: false,
        value: undefined,
      },
      { validators: validators }
    );
    this.dialogForm.addControl(this.am.name, control);
  }

  override getErrorString(attribute: string): string {
    if (!this.dialogForm) {
      return '';
    }

    return getErrorString(attribute, this.dialogForm!, this.translate);
  }

  getSelectedValueString(): string | undefined {
    if (!this.selectedValues || this.selectedValues.length == 0) {
      return undefined;
    }

    return this.selectedValues.join(', ');
  }

  writeValue(obj: any): void {
    this.selectedValues = obj || [];
  }

  clear(): void {
    this.selectedValues = [];
    this.onChange(this.selectedValues);
  }

  registerOnChange(onChange: any): void {
    this.onChange = onChange;
  }

  registerOnTouched(onTouched: any): void {
    this.onTouched = onTouched;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  showDialog(): void {
    this.dialogForm?.get(this.am.name)?.reset();
    this.dialogVisible = true;
  }

  cancel(): void {
    this.dialogVisible = false;
  }

  elementsNumeric(): boolean {
    return (
      this.am.elementCollectionType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.INTEGRAL
    );
  }

  elementsString(): boolean {
    return (
      this.am.elementCollectionType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.STRING
    );
  }

  selectAndClose(): void {
    this.dialogVisible = false;

    let value = this.dialogForm!.get(this.am.name)?.value;
    if (value && !this.selectedValues.find((val) => val === value)) {
      this.selectedValues.push(value);
      this.onChange(this.selectedValues);
    }
  }

}
