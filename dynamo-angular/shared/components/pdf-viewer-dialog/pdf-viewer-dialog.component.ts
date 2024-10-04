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
import { Component, Input } from '@angular/core';
import { PdfViewerMode } from '../../model/pdf-viewer.mode';
import { NotificationService } from '../../service/notification-service';

@Component({
  selector: 'app-pdf-viewer-dialog',
  templateUrl: './pdf-viewer-dialog.component.html',
  styleUrls: ['./pdf-viewer-dialog.component.scss']
})
export class PdfViewerDialogComponent {

  @Input() mode: PdfViewerMode = PdfViewerMode.MODEL;
  @Input() fileName?: string;
  @Input() entityId?: number;
  @Input() entityName?: string;
  @Input() attributeName?: string;
  @Input() externalUrl?: string;

  dialogVisible: boolean = false;

  constructor(private notificationService: NotificationService){

  }

  showDialog(): void {
    if (!this.isValid()) {
      this.notificationService.error('Component is not properly configured')
      return
    }

    this.dialogVisible = true;
  }

  closeDialog(): void {
    this.dialogVisible = false;
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
