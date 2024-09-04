import { Directive, Input, TemplateRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { AttributeModelResponse } from 'dynamo/model';


interface InputContext {
	$implicit: AttributeModelResponse;
  mainForm: FormGroup
}

/**
 * A directive that can be used to override a generic input field definition
 */
@Directive({
	selector: 'ng-template[dOverrideField]',
  exportAs:'dOverrideField'
})
export class OverrideFieldDirective {
	@Input({ required: true }) attributeName: string = '';

	constructor(public template: TemplateRef<any>) {
	}

	static ngTemplateContextGuard(
		dir: OverrideFieldDirective,
		ctx: unknown
	): ctx is InputContext {
		return true;
	}
}
