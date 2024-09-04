import { Component, Input } from '@angular/core';
import { BaseComponent } from '../base-component';
import {  FormControl } from '@angular/forms';
import { AttributeModelResponse, EntityModelResponse, FilterModel } from 'dynamo/model';

@Component({
  selector: 'app-generic-field',
  templateUrl: './generic-field.component.html',
  styleUrls: ['./generic-field.component.scss'],
})
export class GenericFieldComponent extends BaseComponent {
  @Input() enumValues?: any[];
  @Input() options?: any[];
  // the form control corresponding to this component
  @Input() formControl?: FormControl<any>;
  @Input() entityModel?: EntityModelResponse;
  @Input() fieldFilters?: FilterModel[] | undefined = [];

  getFormControl(am: AttributeModelResponse): FormControl<any> {
    return this.formGroup?.get(am.name) as FormControl<any>;
  }

  // the class that determines the actual component with (minus the validation error)
  getColClass() {
    return 'col-lg-9 col-md-8 col-sm-12';
  }

  getValidationColClass() {
    return 'col-lg-3 col-md-4 col-sm-12';
  }
}
