import { Directive, Input, OnInit } from "@angular/core";
import { HiddenFieldService } from "../service/hidden-field-service";

/**
 * A directive that can be used to provide a hidden field value to a form. Can be used to e.g.
 * create entities that depend on a master entity
 */
@Directive({
	selector: 'ng-template[dHiddenField]',
  exportAs: 'dHiddenField',
})
export class HiddenFieldDirective implements OnInit {
	@Input({ required: true }) attributeName!: string;
	@Input({ required: true }) value!: any;

  constructor(private hiddenFieldService: HiddenFieldService) {
  }

	ngOnInit(): void {
      this.hiddenFieldService.setFieldValue(this.attributeName, this.value);
	}
}
