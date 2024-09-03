import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService } from 'primeng/api';

@Injectable({
  providedIn: 'root'
})
export class ConfirmService {

  constructor(private confirmationService: ConfirmationService, private translate: TranslateService) { }

  confirm(messageKey: string, func: Function) {
    this.confirmationService.confirm({
      message: this.translate.instant(messageKey),
      header: this.translate.instant('delete_confirm_header'),
      icon: 'pi pi-exclamation-triangle',
      rejectButtonStyleClass: 'p-button-danger',
      rejectLabel: this.translate.instant('no'),
      acceptButtonStyleClass: 'p-button-success',
      acceptLabel: this.translate.instant('yes'),
      accept: () => func(),
      reject: () => {},
    });
  }
}
