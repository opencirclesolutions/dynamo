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
import { AdditionalActionMode } from "./additional-action-mode";

export interface AdditionalActionBase {
  icon?: string;
  messageKey: string;
  buttonClass?: string;
}

/**
 * An additional global action (does not operate on a specific entity)
 */
export interface AdditionalGlobalAction extends AdditionalActionBase {
  action: () => void;
  enabled?: () => boolean;
}

/**
 * An additional action that operates on a table row
 */
export interface AdditionalRowAction extends AdditionalActionBase {
  action: (obj: any) => void;
  enabled?: (obj: any) => boolean;
}

/**
 * An additional action that operates on a form
 */
export interface AdditionalFormAction extends AdditionalRowAction {
  mode: AdditionalActionMode;
}
