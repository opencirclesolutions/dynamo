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
 * GTS
 * Gift tracking
 *
 * The version of the OpenAPI document: 1
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { DateRangeFilterModel } from './dateRangeFilterModel';
import { ElementCollectionFilterModel } from './elementCollectionFilterModel';
import { LocalTime } from './localTime';
import { NullFilterModel } from './nullFilterModel';
import { NumberInFilterModel } from './numberInFilterModel';
import { OrFilterModel } from './orFilterModel';
import { InstantRangeFilterModel } from './instantRangeFilterModel';
import { NumberRangeFilterModel } from './numberRangeFilterModel';
import { FilterModel } from './filterModel';
import { TimeRangeFilterModel } from './timeRangeFilterModel';
import { LocalDateTimeRangeFilterModel } from './localDateTimeRangeFilterModel';
import { EqualsFilterModel } from './equalsFilterModel';
import { NotFilterModel } from './notFilterModel';


/**
 * @type SearchModelFiltersInner
 * @export
 */
export type SearchModelFiltersInner = DateRangeFilterModel | ElementCollectionFilterModel | EqualsFilterModel | InstantRangeFilterModel | LocalDateTimeRangeFilterModel | NotFilterModel | NullFilterModel | NumberInFilterModel | NumberRangeFilterModel | OrFilterModel | TimeRangeFilterModel;

