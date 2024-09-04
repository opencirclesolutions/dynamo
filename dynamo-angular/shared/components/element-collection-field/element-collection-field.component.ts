import { Component } from '@angular/core';
import { BaseComponent } from '../base-component';
import { AttributeModelResponse } from 'dynamo/model';
import { NotificationService } from '../../service/notification-service';
import { TranslateService } from '@ngx-translate/core';

/**
 * A component for managing an element collection. Will display either
 * a chips component or a dialog
 */
@Component({
  selector: 'app-element-collection-field',
  templateUrl: './element-collection-field.component.html',
  styleUrls: ['./element-collection-field.component.scss'],
})
export class ElementCollectionFieldComponent extends BaseComponent {

  constructor(
    private messageService: NotificationService,
    translate: TranslateService
  ) {
    super(translate);
  }

  useChips(): boolean {
    return (
      this.am?.elementCollectionMode ===
      AttributeModelResponse.ElementCollectionModeEnum.CHIPS
    );
  }

  useDialog(): boolean {
    return (
      this.am?.elementCollectionMode ===
      AttributeModelResponse.ElementCollectionModeEnum.DIALOG
    );
  }

  /**
   * Validates the input after the user adds a new value
   * @param event the new value event
   */
  validateInput(event: any) {
    let am = this.am;
    if (
      am.elementCollectionType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.STRING
    ) {
      this.manageStringField(am, event);
    }

    if (
      am.elementCollectionType ===
      AttributeModelResponse.AttributeModelDataTypeEnum.INTEGRAL
    ) {
      this.manageNumberField(am, event);
    }
  }

  /**
   * Manages the addition of a a string value
   * @param am the attribute model
   * @param event the event that fires after the user adds the value
   */
  private manageStringField(am: AttributeModelResponse, event: any) {
    let values = this.formGroup!.get(am.name)!.value as any[];
    let val = event.value as string;

    // prevent duplicates
    let index: number = values.findIndex((a) => a === val);
    if (index >= 0 && index !== values.length - 1) {
      values.pop();
    }

    if (val && am.maxLength && val.length > am.maxLength) {
      values.pop();
      this.messageService.error(
        this.translate.instant('maximum_length', {
          maxLength: am.maxLength,
        })
      );
    }
    if (val && am.minLength && val.length < am.minLength) {
      values.pop();
      this.messageService.error(
        this.translate.instant('minimum_length', {
          minLength: am.minLength,
        })
      );
    }
  }

  /**
   * Manages the addition of a number field
   * @param am the attribute model
   * @param event the event that fires after the user adds the value
   */
  private manageNumberField(am: AttributeModelResponse, event: any) {
    let values = this.formGroup!.get(am.name)!.value as any[];

    if (isNaN(Number(event.value))) {
      values.pop();
      this.messageService.error(this.translate.instant('not_a_number'));
    }

    let number = parseInt(event.value);

    // prevent duplicates
    let index: number = values.findIndex((a) => a == number);
    if (index >= 0 && index !== values.length - 1) {
      values.pop();
    }

    if (am.maxValue && number > am.maxValue) {
      values.pop();
      this.messageService.error(
        this.translate.instant('maximum_value', {
          maxValue: am.maxValue,
        })
      );
    }

    if (am.minValue && number < am.minValue) {
      values.pop();
      this.messageService.error(
        this.translate.instant('minimum_value', {
          minValue: am.minValue,
        })
      );
    }
  }

}
