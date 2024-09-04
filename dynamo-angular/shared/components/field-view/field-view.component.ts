import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { AttributeModelResponse, EntityModelResponse } from 'dynamo/model';
import { BaseComponent } from '../base-component';
import {
  decapitalize,
  getNestedValue,
  adjustTimestamp,
} from '../../functions/functions';
import { TranslateService } from '@ngx-translate/core';
import { FileService } from 'dynamo/model';
import { Router } from '@angular/router';

/**
 * A component for displaying the value of an attribute in read-only mode
 */
@Component({
  selector: 'app-field-view',
  templateUrl: './field-view.component.html',
  styleUrls: ['./field-view.component.scss'],
})
export class FieldViewComponent extends BaseComponent {
  // the entity being edited
  @Input({ required: true }) entity: any = {};
  // the entity to display
  @Input() entityName: string = '';
  // mapping from enum values to display names
  @Input() enumMap: Map<string, string> = new Map<string, string>();
  // nested entity model that is used in case of complex attributes
  @Input() nestedEntityModel: EntityModelResponse | undefined;
  // whether to display a label when no value is provided
  @Input() showEmptyValueLabel: boolean = false;

  adjustTimestamp = adjustTimestamp;

  constructor(
    private fileService: FileService,
    translate: TranslateService,
    private router: Router
  ) {
    super(translate);
  }

  hasValue(am: AttributeModelResponse): boolean {
    return this.entity[am.name];
  }

  getNestedValue(obj: any, am: AttributeModelResponse): any {
    return getNestedValue(obj, am.name!);
  }

  getInternalNavigateLink(am: AttributeModelResponse) {
    if (this.entity[am.name]) {
      return this.getNavigationPath(am) + '/' + this.entity[am.name]['id'];
    }
    return '';
  }

  getNavigationPath(am: AttributeModelResponse) {
    if (am.navigationLink && am.navigationLink.length > 0) {
      return am.navigationLink;
    }
    return decapitalize(am.lookupEntityName!);
  }

  navigate(am: AttributeModelResponse) {
    this.router.navigateByUrl(this.getInternalNavigateLink(am))
  }

  getDisplayFormat() {
    if (!this.am.displayFormats) {
      return 'dd-MM-yyyy';
    }
    return this.am.displayFormats[this.locale];
  }

  downloadFile(am: AttributeModelResponse) {
    this.fileService
      .download(this.entity.id!, this.entityName, am!.name)
      .subscribe((data) => {
        let blob = new Blob([data], { type: data.type });

        let url = window.URL.createObjectURL(blob);
        let fileName = am.fileNameAttribute
          ? getNestedValue(this.entity, am.fileNameAttribute)
          : 'unknown.txt';

        var anchor = document.createElement('a');
        anchor.download = fileName;
        anchor.href = url;
        anchor.click();
      });
  }

}
