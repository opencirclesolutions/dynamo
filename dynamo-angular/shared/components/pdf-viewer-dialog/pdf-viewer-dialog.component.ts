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
