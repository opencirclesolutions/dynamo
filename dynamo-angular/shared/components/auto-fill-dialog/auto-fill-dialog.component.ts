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
import { Component,  Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BaseComponent } from '../base-component';
import { TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { SelectOption } from '../../model/select-option';
import { AutoFillRequest, AutoFillService } from 'dynamo/model';

/**
 * A popup-dialog that can be used to automatically fill an input form based on
 * a large language model
 */
@Component({
  selector: 'app-auto-fill-dialog',
  templateUrl: './auto-fill-dialog.component.html',
  styleUrls: ['./auto-fill-dialog.component.scss'],
})
export class AutoFillDialogComponent extends BaseComponent implements OnInit {

  // the name of the entity to display
  @Input({ required: true }) entityName: string = '';
  // optional reference to further specify which entity model to use
  @Input() entityModelReference?: string = undefined;

  // method that is called after form fill action completes
  @Input() onFormFillCompleted?: (event: any) => void = (event) => {};

  dialogVisible: boolean = false;
  loading: boolean = false;
  options: SelectOption[] = [];
  mainForm?: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    translate: TranslateService,
    private autofillService: AutoFillService
  ) {
    super(translate);
  }

  ngOnInit(): void {
    this.mainForm = this.formBuilder.group([]);

    let type = this.formBuilder.control(
      {
        disabled: false,
        value: undefined,
      },
      { validators: [Validators.required] }
    );
    this.mainForm.addControl('type', type);

    let input = this.formBuilder.control(
      {
        disabled: false,
        value: undefined,
      },
      { validators: [Validators.required] }
    );
    this.mainForm.addControl('input', input);

    let additionalInstructions = this.formBuilder.control(
      {
        disabled: false,
        value: undefined,
      },
      { validators: [] }
    );
    this.mainForm.addControl('additionalInstructions', additionalInstructions);

    this.autofillService.getOptions().subscribe((res) => {
      this.options = [];
      let temp = res as any[];
      let defaultOption: SelectOption | undefined;

      temp.forEach((element) => {
        let obj: SelectOption = {
          value: element.type,
          name: element.description,
        };
        if (element.defaultValue) {
          defaultOption = obj;
        }
        this.options.push(obj);
      });

      if (this.options.length > 0) {
        this.mainForm?.get('type')?.setValue(defaultOption ? defaultOption : this.options[0]);
      }
      if (this.options.length == 1) {
        this.mainForm?.get('type')?.disable()
      }
    });
  }

  openDialog() {
    this.dialogVisible = true;
  }

  execute() {
    this.loading = true;
    let request: AutoFillRequest = {
      input: this.mainForm!.get('input')?.value,
      additionalInstructions: this.mainForm?.get('additionalInstructions')?.value,
      type: this.mainForm?.get('type')?.value.value
    };
    this.autofillService
      .autoFill(this.entityName, request, this.entityModelReference)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe((response) => {
        this.mainForm?.reset();
        this.dialogVisible = false;
        if (this.onFormFillCompleted) {
          this.onFormFillCompleted(response);
        }
      });
  }

  cancel() {
    this.dialogVisible = false;
  }
}
