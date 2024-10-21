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
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';

export class DynamoValidators {
  constructor(private translateService: TranslateService) {}

  /**
   * A validator for checking that at least one value is fille in
   * either the control to which this validator is added, or the
   * other control that is provided
   * @param otherControl the other control
   * @returns null if validation passes, an error message if it fails
   */
  public atLeastOneRequiredValidator(
    otherControl: AbstractControl
  ): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (control.value || otherControl.value) {
        return null;
      }
      return {
        required: {
          value: this.translateService.instant('at_least_one_value_required'),
        },
      };
    };
  }

  /**
   * A validator for checking that the provided value is an array
   * @returns null if validation passes, an error message if it fails
   */
  public isArrayValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }
      if (!Array.isArray(control.value)) {
        return {
          required: { value: this.translateService.instant('invalid_value') },
        };
      }
      return null;
    };
  }

  /**
   * A validator for checking that a field contains a valid URL
   * @returns null if validation passes, an error message if it fails
   */
  public urlValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }

      let val = control.value as string;

      let urlRegex =
        /^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:/?#[\]@!\$&'\(\)\*\+,;=.]+$/;
      if (!val.match(urlRegex)) {
        return {
          url: { value: this.translateService.instant('not_valid_url') },
        };
      }
      return null;
    };
  }
}
