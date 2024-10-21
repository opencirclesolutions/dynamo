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
import { CommonModule } from '@angular/common';
import { PrimeNgModule } from './prime-ng.module';
import { TranslateModule } from '@ngx-translate/core';
import { DataTableComponent } from './components/data-table/data-table.component';
import { TranslateEntityPipe } from './pipes/translate-entity-pipe';
import { TranslateEnumPipe } from './pipes/enum-pipe';
import { TruncatePipe } from './pipes/truncate-pipe';
import { LookupEntityPipe } from './pipes/lookup-entity-pipe';
import { GenericFormComponent } from './components/generic-form/generic-form.component';
import { GenericSearchLayoutComponent } from './components/generic-search-layout/generic-search-layout.component';
import { ReactiveFormsModule } from '@angular/forms';
import { GenericFormViewComponent } from './components/generic-form-view/generic-form-view.component';
import { TranslateBooleanPipe } from './pipes/translate-boolean-pipe';
import { DetailsGridComponent } from './components/details-grid/details-grid.component';
import { LabelComponent } from './components/label/label.component';
import { LookupFieldComponent } from './components/lookup-field/lookup-field.component';
import { SelectManyComponent } from './components/select-many/select-many.component';
import { SelectEntityComponent } from './components/select-entity/select-entity.component';
import { FieldViewComponent } from './components/field-view/field-view.component';
import { FieldViewTableComponent } from './components/field-view-table/field-view-table.component';
import { StringFieldComponent } from './components/string-field/string-field.component';
import { GenericSearchFormComponent } from './components/generic-search-form/generic-search-form.component';
import { GenericTableComponent } from './components/generic-table/generic-table.component';
import { GenericSplitLayoutComponent } from './components/generic-split-layout/generic-split-layout.component';
import { BooleanFieldComponent } from './components/boolean-field/boolean-field.component';
import { ElementCollectionDialogComponent } from './components/element-collection-dialog/element-collection-dialog.component';
import { ElementCollectionFieldComponent } from './components/element-collection-field/element-collection-field.component';
import { NumberFieldComponent } from './components/number-field/number-field.component';
import { DecimalFieldComponent } from './components/decimal-field/decimal-field.component';
import { EnumFieldComponent } from './components/enum-field/enum-field.component';
import { DateFieldComponent } from './components/date-field/date-field.component';
import { TimeFieldComponent } from './components/time-field/time-field.component';
import { FlexibleSearchFormComponent } from './components/flexible-search-form/flexible-search-form.component';
import { TimestampFieldComponent } from './components/timestamp-field/timestamp-field.component';
import { EntityPopupDialogComponent } from './components/entity-popup-dialog/entity-popup-dialog.component';
import { BackendNotAvailableComponent } from './components/backend-not-available/backend-not-available.component';
import { GenericEditTableLayoutComponent } from './components/generic-edit-table-layout/generic-edit-table-layout.component';
import { AutoFillDialogComponent } from './components/auto-fill-dialog/auto-fill-dialog.component';
import { FileUploadComponent } from './components/file-upload/file-upload.component';
import { AdditionalValidatorsDirective } from './directives/additional-validation-directive';
import { OverrideFieldDirective } from './directives/override-field-directive';
import { HiddenFieldDirective } from './directives/hidden-field-directive';
import { GenericFieldComponent } from './components/generic-field/generic-field.component';
import { PdfViewerComponent } from './components/pdf-viewer/pdf-viewer.component';
import { PdfViewerDialogComponent } from './components/pdf-viewer-dialog/pdf-viewer-dialog.component';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';

@NgModule({
  declarations: [
    DataTableComponent,
    TranslateEntityPipe,
    TranslateEnumPipe,
    LookupEntityPipe,
    TranslateBooleanPipe,
    GenericSearchLayoutComponent,
    GenericFormComponent,
    GenericFormViewComponent,
    DetailsGridComponent,
    LabelComponent,
    LookupFieldComponent,
    SelectManyComponent,
    SelectEntityComponent,
    FieldViewComponent,
    FieldViewTableComponent,
    StringFieldComponent,
    GenericSearchFormComponent,
    GenericTableComponent,
    GenericSplitLayoutComponent,
    BooleanFieldComponent,
    TruncatePipe,
    ElementCollectionDialogComponent,
    ElementCollectionFieldComponent,
    NumberFieldComponent,
    DecimalFieldComponent,
    EnumFieldComponent,
    DateFieldComponent,
    TimeFieldComponent,
    FlexibleSearchFormComponent,
    TimestampFieldComponent,
    EntityPopupDialogComponent,
    BackendNotAvailableComponent,
    GenericEditTableLayoutComponent,
    AutoFillDialogComponent,
    FileUploadComponent,
    AdditionalValidatorsDirective,
    OverrideFieldDirective,
    HiddenFieldDirective,
    GenericFieldComponent,
    PdfViewerComponent,
    PdfViewerDialogComponent
  ],
  imports: [TranslateModule, CommonModule, PrimeNgModule, ReactiveFormsModule, NgxExtendedPdfViewerModule],
  exports: [
    DataTableComponent,
    TranslateEntityPipe,
    TranslateEnumPipe,
    LookupEntityPipe,
    GenericFormComponent,
    GenericSearchLayoutComponent,
    TranslateBooleanPipe,
    DetailsGridComponent,
    LookupFieldComponent,
    SelectManyComponent,
    SelectEntityComponent,
    FieldViewComponent,
    StringFieldComponent,
    GenericSearchFormComponent,
    GenericTableComponent,
    GenericSplitLayoutComponent,
    TruncatePipe,
    ElementCollectionDialogComponent,
    ElementCollectionFieldComponent,
    NumberFieldComponent,
    DecimalFieldComponent,
    EnumFieldComponent,
    DateFieldComponent,
    TimeFieldComponent,
    FlexibleSearchFormComponent,
    TimestampFieldComponent,
    EntityPopupDialogComponent,
    BackendNotAvailableComponent,
    GenericEditTableLayoutComponent,
    AutoFillDialogComponent,
    FileUploadComponent,
    AdditionalValidatorsDirective,
    OverrideFieldDirective,
    HiddenFieldDirective,
    GenericFieldComponent,
    PdfViewerComponent,
    PdfViewerDialogComponent
  ],
})
export class SharedModule {}
