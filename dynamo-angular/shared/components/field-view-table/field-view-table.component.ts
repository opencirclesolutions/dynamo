import { Component, Input, OnInit } from '@angular/core';
import { BaseCompositeComponent } from '../base-composite.component';
import {
  AttributeModelResponse,
  CRUDService,
  ModelService,
  EntityModelResponse,
} from 'dynamo/model';
import { NotificationService } from '../../service/notification-service';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../service/authentication-service';

/**
 * A table that displays a read-only table for displaying a collection of entities
 */
@Component({
  selector: 'app-field-view-table',
  templateUrl: './field-view-table.component.html',
  styleUrls: ['./field-view-table.component.scss'],
})
export class FieldViewTableComponent
  extends BaseCompositeComponent
  implements OnInit
{
  @Input() override entityModel: EntityModelResponse | undefined;
  @Input() rows: any[] | undefined = [];

  pageSize: number = 10;
  initDone: boolean = false;

  constructor(
    service: CRUDService,
    entityModelService: ModelService,
    messageService: NotificationService,
    router: Router,
    authService: AuthenticationService,
  ) {
    super(service, entityModelService, messageService, router, authService);
  }

  ngOnInit(): void {
    this.load();
  }

  load() {
    if (this.entityModel) {
      this.init(this.entityModel);
    } else {
      this.entityModelService
        .getEntityModel(this.entityName, this.entityModelReference)
        .subscribe((model) => {
          this.init(model);
          this.entityModel = model;
        });
    }
  }

  private init(model: EntityModelResponse) {
    this.setupEnums(model);
    this.setupLookups(model);
    this.entityModel!.attributeNamesOrderedForGrid.forEach((attributeName) => {
      let attrib = this.entityModel
        ?.attributeModels!.filter((am) => am.name !== 'id')
        .find((am) => am.name == attributeName);
      if (attrib && attrib.visibleInForm) {
        this.attributeModels.push(attrib);
      }
    });
  }

  getRows(): any[] {
    return this.rows ? this.rows.sort((a, b) => a.id - b.id) : [];
  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // do nothing
  }
}
