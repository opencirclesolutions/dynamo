import { AbstractControl,  ValidationErrors, ValidatorFn } from "@angular/forms";

export function atLeastOneRequiredValidator(otherControl: AbstractControl): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (control.value || otherControl.value) {
      return null;
    }
    return {required: { value: 'At least one value must be filled' }}
  };
}

export function urlValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {

    if (!control.value) {
      return null;
    }

    let val = control.value as string

    let urlRegex = /^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:/?#[\]@!\$&'\(\)\*\+,;=.]+$/;
    if (!val.match(urlRegex)) {
      return {url: { value: 'Not a valid URL' }}
    }
    return null;
  };
}

