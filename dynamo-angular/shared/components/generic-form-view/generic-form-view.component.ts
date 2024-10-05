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
import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { AttributeGroupMode } from '../../model/attribute-group-mode';
import {
  AttributeModelResponse,
  EntityModelResponse,
  AttributeGroupResponse,
  CRUDService,
  ModelService,
} from 'dynamo/model';
import { NotificationService } from '../../service/notification-service';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../service/authentication-service';
import { BaseCompositeComponent } from '../base-composite.component';
import { FormGroup } from '@angular/forms';

/**
 * A component for displaying a form in view mode
 */
@Component({
  selector: 'app-generic-form-view',
  templateUrl: './generic-form-view.component.html',
  styleUrls: ['./generic-form-view.component.scss'],
})
export class GenericFormViewComponent
  extends BaseCompositeComponent
  implements OnInit
{
  @Input() entity: any = undefined;
  @Input() attributeGroupMode: AttributeGroupMode = AttributeGroupMode.PANEL;
  @Input() numberOfColumns: number = 1;
  @Input({required: true}) formGroup!: FormGroup;

  @Input() nestedEntityModelMap: Map<string, EntityModelResponse> = new Map<
    string,
    EntityModelResponse
  >();
  @Input() attributeVisible?: (
    am: AttributeModelResponse,
    editObject: any,
    formGroup: FormGroup
  ) => boolean;

  constructor(
    service: CRUDService,
    entityModelService: ModelService,
    notificationService: NotificationService,
    router: Router,
    authService: AuthenticationService
  ) {
    super(
      service,
      entityModelService,
      notificationService,
      router,
      authService
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

  protected override onLookupFilled(am: AttributeModelResponse): void {}

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
