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
import { Component, Input, OnInit } from '@angular/core';
import { FileService } from 'dynamo/model';
import { PdfViewerMode } from '../../model/pdf-viewer.mode';
import { environment } from 'src/environments/environment';
import { NotificationService } from '../../service/notification-service';

@Component({
  selector: 'app-pdf-viewer',
  templateUrl: './pdf-viewer.component.html',
  styleUrls: ['./pdf-viewer.component.scss'],
})
export class PdfViewerComponent implements OnInit {
  @Input() mode: PdfViewerMode = PdfViewerMode.MODEL;
  @Input() fileName?: string;
  @Input() entityId?: number;
  @Input() entityName?: string;
  @Input() attributeName?: string;
  @Input() externalUrl?: string;

  val?: string;

  constructor(
    private fileService: FileService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    if (this.isModelMode()) {
      if (!this.entityId || !this.entityName || !this.attributeName) {
        this.notificationService.error(
          'entity ID, entity name, and/or attribute name not provided'
        );
        return;
      }

      this.fileService
        .downloadBase64(
          this.entityId!.toLocaleString(),
          this.entityName!,
          this.attributeName!
        )
        .subscribe((data) => {
          this.val = data as string;
        });
    }
  }

  getUrl() {
    return environment.apiUrl + '/pdf/' + this.fileName;
  }

  getContent(): string {
    if (!this.val) {
      return '';
    }
    return this.val;
  }

  isValid() {
    return this.isModelMode() || this.isInternalMode() || this.isExternalMode();
  }

  isModelMode() {
    return this.mode === PdfViewerMode.MODEL;
  }

  isInternalMode() {
    return this.fileName && this.mode === PdfViewerMode.INTERNAL_URL;
  }

  isExternalMode() {
    return this.mode === PdfViewerMode.EXTERNAL_URL;
  }
}
