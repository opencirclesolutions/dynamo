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
