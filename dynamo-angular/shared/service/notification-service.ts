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
