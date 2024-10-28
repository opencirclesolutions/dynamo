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
import { DateRangeFilterModel } from './dateRangeFilterModel';
import { ElementCollectionFilterModel } from './elementCollectionFilterModel';
import { NumberInFilterModel } from './numberInFilterModel';
import { OrFilterModel } from './orFilterModel';
import { InstantRangeFilterModel } from './instantRangeFilterModel';
import { NumberRangeFilterModel } from './numberRangeFilterModel';
import { TimeRangeFilterModel } from './timeRangeFilterModel';
import { LocalDateTimeRangeFilterModel } from './localDateTimeRangeFilterModel';
import { EqualsFilterModel } from './equalsFilterModel';
import { NotFilterModel } from './notFilterModel';
import { NullFilterModel } from './nullFilterModel';


/**
 * @type SearchModelFiltersInner
 * @export
 */
export type SearchModelFiltersInner = DateRangeFilterModel | ElementCollectionFilterModel | EqualsFilterModel | InstantRangeFilterModel | LocalDateTimeRangeFilterModel | NotFilterModel | NullFilterModel | NumberInFilterModel | NumberRangeFilterModel | OrFilterModel | TimeRangeFilterModel;

