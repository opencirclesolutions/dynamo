package org.dynamoframework.configuration;

public interface DefaultProperties {
    org.dynamoframework.domain.model.AttributeBooleanFieldMode getBooleanFieldMode();

    String getDateFormat();

    String getDateTimeFormat();

    Integer getDecimalPrecision();

    org.dynamoframework.domain.model.ElementCollectionMode getElementCollectionMode();

    org.dynamoframework.domain.model.AttributeEnumFieldMode getEnumFieldMode();

    String getFalseRepresentation();

    java.util.Map<String, String> getFalseRepresentations();

    java.util.Map<String, String> getTrueRepresentations();

    String getTrueRepresentation();

    org.dynamoframework.domain.model.GroupTogetherMode getGroupTogetherMode();

    Integer getGroupTogetherWidth();

    java.util.Locale getLocale();

    Integer getNestingDepth();

    org.dynamoframework.domain.model.NumberFieldMode getNumberFieldMode();

    boolean isSearchCaseSensitive();

    boolean isSearchPrefixOnly();

    String getTimeFormat();

    boolean isPromptValue();

    boolean isTrimSpaces();
}

