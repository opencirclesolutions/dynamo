package org.dynamoframework.rest.model;

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
 * The specific attribute types - these basically govern how the component is rendered
 */
public enum AttributeModelType {

    STRING, INTEGRAL, DECIMAL, ENUM, BOOL, DATE, MASTER, INSTANT, MANY_TO_MANY, ONE_TO_MANY, TIME,
    LOB, LOCAL_DATE_TIME, ELEMENT_COLLECTION
}
