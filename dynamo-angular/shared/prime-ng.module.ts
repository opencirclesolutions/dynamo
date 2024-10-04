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
import { NgModule } from '@angular/core';
import { DropdownModule } from 'primeng/dropdown';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { BlockUIModule } from 'primeng/blockui';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { TabMenuModule } from 'primeng/tabmenu';
import { TabViewModule } from 'primeng/tabview';
import { CalendarModule } from 'primeng/calendar';
import { TableModule } from 'primeng/table';
import { StepsModule } from 'primeng/steps';
import { CardModule } from 'primeng/card';
import { ScrollTopModule } from 'primeng/scrolltop';
import { MessagesModule } from 'primeng/messages';
import { PaginatorModule } from 'primeng/paginator';
import { SelectButtonModule } from 'primeng/selectbutton';
import { RippleModule } from 'primeng/ripple';
import { CheckboxModule } from 'primeng/checkbox';
import { TriStateCheckboxModule } from 'primeng/tristatecheckbox';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { KeyFilterModule } from 'primeng/keyfilter';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { FileUploadModule } from 'primeng/fileupload';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { MessageModule } from 'primeng/message';
import { DividerModule } from 'primeng/divider';
import { SplitterModule } from 'primeng/splitter';
import { PasswordModule } from 'primeng/password';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { PanelModule } from 'primeng/panel';
import { ChipsModule } from 'primeng/chips';
import { TooltipModule } from 'primeng/tooltip';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { InputSwitchModule } from 'primeng/inputswitch';
import { RadioButtonModule } from 'primeng/radiobutton';
import { ContextMenuModule } from 'primeng/contextmenu';


const modules = [
  BlockUIModule,
  BreadcrumbModule,
  CalendarModule,
  DropdownModule,
  PaginatorModule,
  ProgressSpinnerModule,
  ScrollTopModule,
  TabMenuModule,
  TabViewModule,
  TableModule,
  StepsModule,
  SelectButtonModule,
  RippleModule,
  CardModule,
  ToastModule,
  MessagesModule,
  MessageModule,
  CheckboxModule,
  TriStateCheckboxModule,
  InputTextModule,
  InputNumberModule,
  KeyFilterModule,
  MultiSelectModule,
  InputTextareaModule,
  FileUploadModule,
  ConfirmDialogModule,
  DialogModule,
  DividerModule,
  SplitterModule,
  PasswordModule,
  ToggleButtonModule,
  AutoCompleteModule,
  PanelModule,
  ChipsModule,
  TooltipModule,
  BrowserAnimationsModule,
  InputSwitchModule,
  RadioButtonModule,
  ContextMenuModule
];

/** Module for primeng modules, this way we can easily keep track of used primeng usages and keep other modules
 * clean of primeng imports **/
@NgModule({
  providers: [MessageService, ConfirmationService],
  exports: [...modules],
})
export class PrimeNgModule {}
