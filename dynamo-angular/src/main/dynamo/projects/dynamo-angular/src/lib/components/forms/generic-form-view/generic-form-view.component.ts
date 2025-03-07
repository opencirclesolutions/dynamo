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
import { Component, Input, OnInit, SimpleChanges, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { AttributeGroupMode } from '../../../interfaces/mode';
import { EntityModelResponse } from '../../../interfaces/model/entityModelResponse';
import { AttributeModelResponse } from '../../../interfaces/model/attributeModelResponse';
import { DynamoConfig } from '../../../interfaces/dynamo-config';
import { NotificationService } from '../../../services/notification.service';
import { AuthenticationService } from '../../../services/authentication.service';
import { AttributeGroupResponse } from '../../../interfaces/model/attributeGroupResponse';
import { FieldViewComponent } from '../field-view/field-view.component';
import { TooltipModule } from 'primeng/tooltip';
import { TabViewModule } from 'primeng/tabview';
import { PanelModule } from 'primeng/panel';
import { TranslateModule } from '@ngx-translate/core';
import { TranslateEntityPipe } from '../../../pipes/translate-entity.pipe';
import { BaseCompositeComponent } from '../base-composite/base-composite.component';
import {NgTemplateOutlet} from "@angular/common";

@Component({
  selector: 'd-generic-form-view',
  standalone: true,
  imports: [PanelModule, TabViewModule, TooltipModule, FieldViewComponent, TranslateModule, TranslateEntityPipe, NgTemplateOutlet],
  templateUrl: './generic-form-view.component.html',
  styleUrl: './generic-form-view.component.css'
})
export class GenericFormViewComponent
  extends BaseCompositeComponent
  implements OnInit {
  @Input() entity: any = undefined;
  @Input() attributeGroupMode: AttributeGroupMode = AttributeGroupMode.PANEL;
  @Input() numberOfColumns: number = 1;
  @Input({ required: true }) formGroup!: FormGroup;

  @Input() nestedEntityModelMap: Map<string, EntityModelResponse> = new Map<
    string,
    EntityModelResponse
  >();
  @Input() attributeVisible?: (
    am: AttributeModelResponse,
    editObject: any,
    formGroup: FormGroup
  ) => boolean;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    const messageService = inject(NotificationService);
    const router = inject(Router);
    const authService = inject(AuthenticationService);
    const configuration = inject<DynamoConfig>("DYNAMO_CONFIG" as any);

    super(
      messageService,
      router,
      authService,
      configuration
    );
  }

  ngOnInit(): void {
    this.setupEnums(this.entityModel!);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.attributeModels =
      this.entityModel?.attributeModels!.filter((am) => am.visibleInForm) || [];
    this.setupEnums(this.entityModel!);
  }

  getDigitsInfo(am: AttributeModelResponse) {
    return `1.${am.precision}-${am.precision}`;
  }

  getNestedEntityModel(
    am: AttributeModelResponse
  ): EntityModelResponse | undefined {
    return this.nestedEntityModelMap.get(am.name);
  }

  getAttributeModelGroups(): string[] {
    return (
      this.entityModel?.attributeGroups
        .sort((a, b) => a.index! - b.index!)
        .map((group) => group.groupName!) || []
    );
  }

  getAttributeModelsForGroup(group: string): AttributeModelResponse[] {
    let matches: string[] = this.findGroup(group)?.attributes || [];
    return this.attributeModels
      .filter((m) => matches.findIndex((match) => match === m.name) >= 0)
      .filter((am) => !this.isGroupedWithOther(this.attributeModels, am));
  }

  findGroup(groupName: string): AttributeGroupResponse {
    return this.entityModel!.attributeGroups.find(
      (group) => group.groupName === groupName
    )!;
  }

  getGroupDescription(groupName: string) {
    return this.findGroup(groupName).groupDescriptions[this.locale];
  }

  // the class that determines the width of each column
  getColumnsClass() {
    if (this.numberOfColumns === 2) {
      return 'col-lg-6 col-md-6 col-sm-12';
    } else if (this.numberOfColumns === 3) {
      return 'col-lg-4 col-md-6 col-sm-12';
    }
    return 'col-lg-12 col-md-12 col-sm-12';
  }

  protected override onLookupFilled(am: AttributeModelResponse): void { }

  useGroupTabs() {
    return this.attributeGroupMode === AttributeGroupMode.TAB;
  }

  useGroupPanels() {
    return this.attributeGroupMode === AttributeGroupMode.PANEL;
  }

  isAttributeVisible(am: AttributeModelResponse) {
    if (this.attributeVisible) {
      return this.attributeVisible(am, this.entity, this.formGroup);
    }
    return true;
  }
}
