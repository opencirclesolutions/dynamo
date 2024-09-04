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

  @Input() nestedEntityModelMap: Map<string, EntityModelResponse> = new Map<
    string,
    EntityModelResponse
  >();
  @Input() attributeVisible?: (
    am: AttributeModelResponse,
    viewObject: any
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
}
