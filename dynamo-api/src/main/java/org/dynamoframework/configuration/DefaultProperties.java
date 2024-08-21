package org.dynamoframework.configuration;

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

