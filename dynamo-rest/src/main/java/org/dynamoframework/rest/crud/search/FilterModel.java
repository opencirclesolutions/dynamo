package org.dynamoframework.rest.crud.search;

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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "match")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EqualsFilterModel.class, name = "EQUALS"),
        @JsonSubTypes.Type(value = NumberRangeFilterModel.class, name = "NUMBER_RANGE"),
        @JsonSubTypes.Type(value = DateRangeFilterModel.class, name = "DATE_RANGE"),
        @JsonSubTypes.Type(value = InstantRangeFilterModel.class, name = "INSTANT_RANGE"),
        @JsonSubTypes.Type(value = NumberInFilterModel.class, name = "NUMBER_IN"),
        @JsonSubTypes.Type(value = TimeRangeFilterModel.class, name = "TIME_RANGE"),
        @JsonSubTypes.Type(value = LocalDateTimeRangeFilterModel.class, name = "LOCAL_DATE_TIME_RANGE"),
        @JsonSubTypes.Type(value = OrFilterModel.class, name = "OR"),
        @JsonSubTypes.Type(value = NotFilterModel.class, name = "NOT"),
        @JsonSubTypes.Type(value = ElementCollectionFilterModel.class, name = "ELEMENT_COLLECTION"),
        @JsonSubTypes.Type(value = NullFilterModel.class, name = "IS_NULL")
})
@Getter
@Setter
@SuperBuilder
public abstract class FilterModel {

    @NotNull
    private FilterType match;

    private String name;
}
