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
export * from './autoFill.service';
import { AutoFillService } from './autoFill.service';
export * from './cRUD.service';
import { CRUDService } from './cRUD.service';
export * from './export.service';
import { ExportService } from './export.service';
export * from './file.service';
import { FileService } from './file.service';
export * from './model.service';
import { ModelService } from './model.service';
export * from './status.service';
import { StatusService } from './status.service';
export const APIS = [AutoFillService, CRUDService, ExportService, FileService, ModelService, StatusService];
