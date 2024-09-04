import { Injectable } from '@angular/core';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';

/**
 * A service for displaying notification messages
 */
@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  constructor(
    private messageService: MessageService,
    private translate: TranslateService
  ) {}

  public info(message: string, sticky: boolean = false) {
    this.addMessage('info', sticky, message);
  }

  public warn(message: string, sticky: boolean = false) {
    this.addMessage('warn', sticky, message);
  }

  public error(message: string, sticky: boolean = false) {
    this.addMessage('error', sticky, message);
  }

  private addMessage(severity: Severity, sticky: boolean, messageKey: string) {
    this.messageService.add({
      severity: severity,
      sticky: sticky,
      summary: this.translate.instant(`severity.${severity}`),
      detail: this.translate.instant(messageKey),
    });
  }
}

type Severity = 'info' | 'warn' | 'error';
