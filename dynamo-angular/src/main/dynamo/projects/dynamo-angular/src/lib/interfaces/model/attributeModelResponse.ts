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
import { CascadeModel } from './cascadeModel';

export interface AttributeModelResponse {
  allowedExtensions?: Set<string>;
  currencyCode?: string;
  dateType?: AttributeModelResponse.DateTypeEnum;
  defaultValue?: object;
  attributeModelDataType: AttributeModelResponse.AttributeModelDataTypeEnum;
  descriptions: { [key: string]: string; };
  displayNames: { [key: string]: string; };
  displayFormats: { [key: string]: string; };
  editableType: AttributeModelResponse.EditableTypeEnum;
  groupTogetherWith?: Array<string>;
  name: string;
  neededInData: boolean;
  percentage?: boolean;
  precision?: number;
  required: boolean;
  requiredForSearching?: boolean;
  searchDateOnly?: boolean;
  searchForExactValue?: boolean;
  searchMode: AttributeModelResponse.SearchModeEnum;
  searchSelectMode?: AttributeModelResponse.SearchSelectModeEnum;
  selectMode?: AttributeModelResponse.SelectModeEnum;
  sortable: boolean;
  textFieldMode?: AttributeModelResponse.TextFieldModeEnum;
  visibleInForm: boolean;
  visibleInGrid: boolean;
  displayPropertyName?: string;
  enumDescriptions?: { [key: string]: { [key: string]: string; }; };
  lookupEntityName?: string;
  lookupEntityReference?: string;
  multipleSearch?: boolean;
  maxLength?: number;
  minLength?: number;
  minValue?: number;
  maxValue?: number;
  trueRepresentations: { [key: string]: string; };
  falseRepresentations: { [key: string]: string; };
  email?: boolean;
  includeNumberSpinnerButton?: boolean;
  numberFieldStep?: number;
  image?: boolean;
  downloadAllowed?: boolean;
  fileNameAttribute?: string;
  placeholders: { [key: string]: string; };
  booleanFieldMode?: AttributeModelResponse.BooleanFieldModeEnum;
  url?: boolean;
  navigable?: boolean;
  maxLengthInGrid?: number;
  elementCollectionType?: AttributeModelResponse.ElementCollectionTypeEnum;
  elementCollectionMode?: AttributeModelResponse.ElementCollectionModeEnum;
  minCollectionSize?: number;
  maxCollectionSize?: number;
  navigationLink?: string;
  quickAddAllowed?: boolean;
  enumFieldMode?: AttributeModelResponse.EnumFieldModeEnum;
  defaultSearchValue?: object;
  defaultSearchValueFrom?: object;
  defaultSearchValueTo?: object;
  cascades?: Array<CascadeModel>;
  ignoreInSearchFilter?: boolean;
  trimSpaces?: boolean;
  lookupQueryType?: AttributeModelResponse.LookupQueryTypeEnum;
  showDetailsPaginator?: boolean;
}
export namespace AttributeModelResponse {
  export type DateTypeEnum = 'INHERIT' | 'LOCAL_DATE_TIME' | 'INSTANT' | 'DATE' | 'TIME';
  export const DateTypeEnum = {
    INHERIT: 'INHERIT' as DateTypeEnum,
    LOCAL_DATE_TIME: 'LOCAL_DATE_TIME' as DateTypeEnum,
    INSTANT: 'INSTANT' as DateTypeEnum,
    DATE: 'DATE' as DateTypeEnum,
    TIME: 'TIME' as DateTypeEnum
  };
  export type AttributeModelDataTypeEnum = 'STRING' | 'INTEGRAL' | 'DECIMAL' | 'ENUM' | 'BOOL' | 'DATE' | 'MASTER' | 'INSTANT' | 'MANY_TO_MANY' | 'ONE_TO_MANY' | 'TIME' | 'LOB' | 'LOCAL_DATE_TIME' | 'ELEMENT_COLLECTION';
  export const AttributeModelDataTypeEnum = {
    STRING: 'STRING' as AttributeModelDataTypeEnum,
    INTEGRAL: 'INTEGRAL' as AttributeModelDataTypeEnum,
    DECIMAL: 'DECIMAL' as AttributeModelDataTypeEnum,
    ENUM: 'ENUM' as AttributeModelDataTypeEnum,
    BOOL: 'BOOL' as AttributeModelDataTypeEnum,
    DATE: 'DATE' as AttributeModelDataTypeEnum,
    MASTER: 'MASTER' as AttributeModelDataTypeEnum,
    INSTANT: 'INSTANT' as AttributeModelDataTypeEnum,
    MANY_TO_MANY: 'MANY_TO_MANY' as AttributeModelDataTypeEnum,
    ONE_TO_MANY: 'ONE_TO_MANY' as AttributeModelDataTypeEnum,
    TIME: 'TIME' as AttributeModelDataTypeEnum,
    LOB: 'LOB' as AttributeModelDataTypeEnum,
    LOCAL_DATE_TIME: 'LOCAL_DATE_TIME' as AttributeModelDataTypeEnum,
    ELEMENT_COLLECTION: 'ELEMENT_COLLECTION' as AttributeModelDataTypeEnum
  };
  export type EditableTypeEnum = 'READ_ONLY' | 'CREATE_ONLY' | 'EDITABLE' | 'HIDDEN';
  export const EditableTypeEnum = {
    READ_ONLY: 'READ_ONLY' as EditableTypeEnum,
    CREATE_ONLY: 'CREATE_ONLY' as EditableTypeEnum,
    EDITABLE: 'EDITABLE' as EditableTypeEnum,
    HIDDEN: 'HIDDEN' as EditableTypeEnum
  };
  export type SearchModeEnum = 'NONE' | 'ALWAYS' | 'ADVANCED';
  export const SearchModeEnum = {
    NONE: 'NONE' as SearchModeEnum,
    ALWAYS: 'ALWAYS' as SearchModeEnum,
    ADVANCED: 'ADVANCED' as SearchModeEnum
  };
  export type SearchSelectModeEnum = 'COMBO' | 'LOOKUP' | 'MULTI_SELECT' | 'AUTO_COMPLETE';
  export const SearchSelectModeEnum = {
    COMBO: 'COMBO' as SearchSelectModeEnum,
    LOOKUP: 'LOOKUP' as SearchSelectModeEnum,
    MULTI_SELECT: 'MULTI_SELECT' as SearchSelectModeEnum,
    AUTO_COMPLETE: 'AUTO_COMPLETE' as SearchSelectModeEnum
  };
  export type SelectModeEnum = 'COMBO' | 'LOOKUP' | 'MULTI_SELECT' | 'AUTO_COMPLETE';
  export const SelectModeEnum = {
    COMBO: 'COMBO' as SelectModeEnum,
    LOOKUP: 'LOOKUP' as SelectModeEnum,
    MULTI_SELECT: 'MULTI_SELECT' as SelectModeEnum,
    AUTO_COMPLETE: 'AUTO_COMPLETE' as SelectModeEnum
  };
  export type TextFieldModeEnum = 'TEXTFIELD' | 'TEXTAREA' | 'PASSWORD';
  export const TextFieldModeEnum = {
    TEXTFIELD: 'TEXTFIELD' as TextFieldModeEnum,
    TEXTAREA: 'TEXTAREA' as TextFieldModeEnum,
    PASSWORD: 'PASSWORD' as TextFieldModeEnum
  };
  export type BooleanFieldModeEnum = 'CHECKBOX' | 'TOGGLE' | 'SWITCH';
  export const BooleanFieldModeEnum = {
    CHECKBOX: 'CHECKBOX' as BooleanFieldModeEnum,
    TOGGLE: 'TOGGLE' as BooleanFieldModeEnum,
    SWITCH: 'SWITCH' as BooleanFieldModeEnum
  };
  export type ElementCollectionTypeEnum = 'STRING' | 'INTEGRAL' | 'DECIMAL' | 'ENUM' | 'BOOL' | 'DATE' | 'MASTER' | 'INSTANT' | 'MANY_TO_MANY' | 'ONE_TO_MANY' | 'TIME' | 'LOB' | 'LOCAL_DATE_TIME' | 'ELEMENT_COLLECTION';
  export const ElementCollectionTypeEnum = {
    STRING: 'STRING' as ElementCollectionTypeEnum,
    INTEGRAL: 'INTEGRAL' as ElementCollectionTypeEnum,
    DECIMAL: 'DECIMAL' as ElementCollectionTypeEnum,
    ENUM: 'ENUM' as ElementCollectionTypeEnum,
    BOOL: 'BOOL' as ElementCollectionTypeEnum,
    DATE: 'DATE' as ElementCollectionTypeEnum,
    MASTER: 'MASTER' as ElementCollectionTypeEnum,
    INSTANT: 'INSTANT' as ElementCollectionTypeEnum,
    MANY_TO_MANY: 'MANY_TO_MANY' as ElementCollectionTypeEnum,
    ONE_TO_MANY: 'ONE_TO_MANY' as ElementCollectionTypeEnum,
    TIME: 'TIME' as ElementCollectionTypeEnum,
    LOB: 'LOB' as ElementCollectionTypeEnum,
    LOCAL_DATE_TIME: 'LOCAL_DATE_TIME' as ElementCollectionTypeEnum,
    ELEMENT_COLLECTION: 'ELEMENT_COLLECTION' as ElementCollectionTypeEnum
  };
  export type ElementCollectionModeEnum = 'CHIPS' | 'DIALOG';
  export const ElementCollectionModeEnum = {
    CHIPS: 'CHIPS' as ElementCollectionModeEnum,
    DIALOG: 'DIALOG' as ElementCollectionModeEnum
  };
  export type EnumFieldModeEnum = 'DROPDOWN' | 'RADIO';
  export const EnumFieldModeEnum = {
    DROPDOWN: 'DROPDOWN' as EnumFieldModeEnum,
    RADIO: 'RADIO' as EnumFieldModeEnum
  };
  export type LookupQueryTypeEnum = 'PAGING' | 'ID_BASED';
  export const LookupQueryTypeEnum = {
    PAGING: 'PAGING' as LookupQueryTypeEnum,
    ID_BASED: 'ID_BASED' as LookupQueryTypeEnum
  };
}


