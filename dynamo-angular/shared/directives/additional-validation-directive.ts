import { Directive, Input, OnInit } from "@angular/core";
import { FormGroup, ValidatorFn } from "@angular/forms";

@Directive({
	selector: 'ng-template[dAdditionalValidators]',
  exportAs: 'dAdditionalValidators'
})
export class AdditionalValidatorsDirective implements OnInit {
	@Input({ required: true }) attributeName: string = '';
	@Input({ required: true }) validators: ValidatorFn[] = [];
  @Input() formGroup?: FormGroup | undefined;

	ngOnInit(): void {
		let control = this.formGroup?.get(this.attributeName);
    control?.addValidators(this.validators);
	}
}
