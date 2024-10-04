import {
  Component,
  Input,
  Output,
  EventEmitter,
  ViewChild,
} from '@angular/core';
import { BaseComponent } from '../base-component';
import { AttributeModelResponse, FileService } from 'dynamo/model';
import { FileUpload, FileUploadHandlerEvent } from 'primeng/fileupload';
import { setNestedValue } from '../../functions/entitymodel-functions';
import { FileUploadInfo } from '../../model/file-upload-info';
import { TranslateService } from '@ngx-translate/core';
import { FileClearInfo } from '../../model/file-clear-info';
import { getNestedValue } from '../../functions/functions';

/**
 * A composite component that encompasses the components
 * that are needed for file upload (with optional download)
 */
@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss'],
})
export class FileUploadComponent extends BaseComponent {
  @Input() editObject: any;
  @Input({ required: true }) entityName: string = '';
  @Input() entityId?: number = undefined;

  // event that fires after a file upload
  @Output() onFileUpload = new EventEmitter<FileUploadInfo>();
  // event that fires after clearing an uploaded file
  @Output() onFileClear = new EventEmitter<FileClearInfo>();

  @ViewChild('upload') upload: FileUpload | undefined;

  constructor(
    translate: TranslateService,
    private fileService: FileService
  ) {
    super(translate);
  }

  /**
   * @returns the extensions that an uploaded file is allowed to have
   */
  getAllowedExtensions(): string | undefined {
    if (!this.am.allowedExtensions) {
      return undefined;
    }
    return [...this.am.allowedExtensions!].map((ext) => '.' + ext).join(',');
  }

  /**
   * @returns the base64-encoded content of the image
   */
  getImageContent(): string {
    let val = this.getNestedValue(this.editObject, this.am) as string;
    if (!val) {
      return val;
    }

    if (!val.startsWith('data')) {
      val = 'data:image/jpg;base64,' + val;
    }
    return val;
  }

  /**
   * Returns a possibly nested property value
   * @param obj the object on which the property is set
   * @param am the attribute model
   * @returns the property value
   */
  getNestedValue(obj: any, am: AttributeModelResponse): any {
    return getNestedValue(obj, am.name);
  }

  /**
   * Event handler for dealing with a file upload
   * @param event the file upload event
   * @param am the attribute model
   */
  uploadHandler(event: FileUploadHandlerEvent) {
    // reads the file and show it in the preview
    var reader = new FileReader();
    let self = this;
    reader.onload = function () {
      setNestedValue(self.editObject, self.am, reader.result);
    };
    reader.readAsDataURL(event.files[0]);

    // set the file name after upload
    let fileName = event.files[0].name;

    // clear the upload component
    this.upload?.clear();

    let info: FileUploadInfo = {
      am: self.am,
      file: event.files[0],
      fileName: fileName,
    };
    this.onFileUpload.emit(info);
  }

  /**
   * Clears a file upload component
   */
  clearUpload() {
    setNestedValue(this.editObject, this.am, undefined);
    this.onFileClear.emit({
      am: this.am,
    });
  }

  /**
   * Downloads a file to the browser
   */
  downloadFile() {
    this.fileService
      .download(this.entityId!.toString(), this.entityName, this.am.name)
      .subscribe((data) => {
        let blob = new Blob([data], { type: data.type });
        let url = window.URL.createObjectURL(blob);
        let fileName = this.am.fileNameAttribute
          ? getNestedValue(this.editObject, this.am.fileNameAttribute)
          : 'unknown.txt';

        var anchor = document.createElement('a');
        anchor.download = fileName;
        anchor.href = url;
        anchor.click();
      });
  }
}
