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
import { Component, Input, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '../../../services/notification.service';
import { AuthenticationService } from '../../../services/authentication.service';
import { DynamoConfig } from '../../../interfaces/dynamo-config';
import { AttributeModelResponse } from '../../../interfaces/model/attributeModelResponse';
import { EntityModelResponse } from '../../../interfaces/model/entityModelResponse';
import { FieldViewComponent } from '../field-view/field-view.component';
import { TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { BaseCompositeComponent } from '../base-composite/base-composite.component';

@Component({
  selector: 'd-field-view-table',
  standalone: true,
  imports: [CommonModule, TableModule, FieldViewComponent],
  templateUrl: './field-view-table.component.html',
  styleUrl: './field-view-table.component.css'
})
export class FieldViewTableComponent
  extends BaseCompositeComponent
  implements OnInit {
  @Input({ required: true }) am!: AttributeModelResponse;
  @Input() rows: any[] | undefined = [];

  pageSize: number = 10;
  initDone: boolean = false;

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

  showPaginator() {

  }

  protected override onLookupFilled(am: AttributeModelResponse): void {
    // do nothing
  }
}
