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

export interface FilterModel { 
    match: FilterModel.MatchEnum;
    name?: string;
}
export namespace FilterModel {
    export type MatchEnum = 'EQUALS' | 'DATE_RANGE' | 'NUMBER_RANGE' | 'INSTANT_RANGE' | 'LOCAL_DATE_TIME_RANGE' | 'NUMBER_IN' | 'TIME_RANGE' | 'OR' | 'NOT' | 'ELEMENT_COLLECTION';
    export const MatchEnum = {
        EQUALS: 'EQUALS' as MatchEnum,
        DATE_RANGE: 'DATE_RANGE' as MatchEnum,
        NUMBER_RANGE: 'NUMBER_RANGE' as MatchEnum,
        INSTANT_RANGE: 'INSTANT_RANGE' as MatchEnum,
        LOCAL_DATE_TIME_RANGE: 'LOCAL_DATE_TIME_RANGE' as MatchEnum,
        NUMBER_IN: 'NUMBER_IN' as MatchEnum,
        TIME_RANGE: 'TIME_RANGE' as MatchEnum,
        OR: 'OR' as MatchEnum,
        NOT: 'NOT' as MatchEnum,
        ELEMENT_COLLECTION: 'ELEMENT_COLLECTION' as MatchEnum
    };
}


