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
export interface CascadeModel {
  cascadeTo: string;
  filterPath: string;
  cascadeMode: CascadeModel.CascadeModeEnum;
}

export namespace CascadeModel {
  export type CascadeModeEnum = 'SEARCH' | 'EDIT' | 'BOTH';
  export const CascadeModeEnum = {
    SEARCH: 'SEARCH' as CascadeModeEnum,
    EDIT: 'EDIT' as CascadeModeEnum,
    BOTH: 'BOTH' as CascadeModeEnum
  };
}


