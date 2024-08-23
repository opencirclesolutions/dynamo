package org.dynamoframework.configuration;

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

import org.dynamoframework.domain.model.*;

public interface DefaultProperties {
    AttributeBooleanFieldMode getBooleanFieldMode();

    String getDateFormat();

    String getDateTimeFormat();

    Integer getDecimalPrecision();

    ElementCollectionMode getElementCollectionMode();

    AttributeEnumFieldMode getEnumFieldMode();

    String getFalseRepresentation();

    java.util.Map<String, String> getFalseRepresentations();

    java.util.Map<String, String> getTrueRepresentations();

    String getTrueRepresentation();

    GroupTogetherMode getGroupTogetherMode();

    Integer getGroupTogetherWidth();

    java.util.Locale getLocale();

    Integer getNestingDepth();

    NumberFieldMode getNumberFieldMode();

    boolean isSearchCaseSensitive();

    boolean isSearchPrefixOnly();

    String getTimeFormat();

    boolean isUsePromptValue();

    boolean isTrimSpaces();

    String getAiService();
}

