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
import { Component, Inject, Input, OnInit } from '@angular/core';
import { PdfViewerMode } from '../../../interfaces/mode';
import { NotificationService } from '../../../services/notification.service';
import { DynamoConfig } from '../../../interfaces/dynamo-config';
import { FileServiceInterface } from '../../../interfaces/service/file.service';
import { Observable } from 'rxjs';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

@Component({
  selector: 'd-pdf-viewer',
  standalone: true,
  imports: [NgxExtendedPdfViewerModule],
  templateUrl: './pdf-viewer.component.html',
  styleUrl: './pdf-viewer.component.css'
})
export class PdfViewerComponent implements OnInit {
  @Input() mode: PdfViewerMode = PdfViewerMode.MODEL;
  @Input() fileName?: string;
  @Input() entityId?: number;
  @Input() entityName?: string;
  @Input() attributeName?: string;
  @Input() externalUrl?: string;

  file?: Observable<string>
  fileService: FileServiceInterface;

  constructor(
    @Inject("DYNAMO_CONFIG") configuration: DynamoConfig,
    private notificationService: NotificationService
  ) {
    this.fileService = configuration.getFileService()
  }

  ngOnInit(): void {
    if (this.isModelMode()) {
      if (!this.entityId || !this.entityName || !this.attributeName) {
        this.notificationService.error(
          'entity ID, entity name, and/or attribute name not provided'
        );
        return;
      }

      this.file =
        this.fileService
          .downloadBase64(
            this.entityId.toLocaleString(),
            this.entityName,
            this.attributeName
          )
    }
  }

  isModelMode() {
    return this.mode === PdfViewerMode.MODEL;
  }

  isExternalMode() {
    return this.mode === PdfViewerMode.EXTERNAL_URL;
  }
}
