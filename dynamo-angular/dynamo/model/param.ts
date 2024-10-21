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
/**
 * Standard parameter styles defined by OpenAPI spec
 */
export type StandardParamStyle =
  | 'matrix'
  | 'label'
  | 'form'
  | 'simple'
  | 'spaceDelimited'
  | 'pipeDelimited'
  | 'deepObject'
  ;

/**
 * The OpenAPI standard {@link StandardParamStyle}s may be extended by custom styles by the user.
 */
export type ParamStyle = StandardParamStyle | string;

/**
 * Standard parameter locations defined by OpenAPI spec
 */
export type ParamLocation = 'query' | 'header' | 'path' | 'cookie';

/**
 * Standard types as defined in <a href="https://swagger.io/specification/#data-types">OpenAPI Specification: Data Types</a>
 */
export type StandardDataType =
  | "integer"
  | "number"
  | "boolean"
  | "string"
  | "object"
  | "array"
  ;

/**
 * Standard {@link DataType}s plus your own types/classes.
 */
export type DataType = StandardDataType | string;

/**
 * Standard formats as defined in <a href="https://swagger.io/specification/#data-types">OpenAPI Specification: Data Types</a>
 */
export type StandardDataFormat =
  | "int32"
  | "int64"
  | "float"
  | "double"
  | "byte"
  | "binary"
  | "date"
  | "date-time"
  | "password"
  ;

export type DataFormat = StandardDataFormat | string;

/**
 * The parameter to encode.
 */
export interface Param {
  name: string;
  value: unknown;
  in: ParamLocation;
  style: ParamStyle,
  explode: boolean;
  dataType: DataType;
  dataFormat: DataFormat | undefined;
}
